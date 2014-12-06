package org.cloudgraph.cassandra.graph;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.cql.CQLStatementExecutor;
import org.cloudgraph.cassandra.cql.CQLStatementFactory;
import org.cloudgraph.cassandra.cql.FilterAssembler;
import org.cloudgraph.cassandra.cql.StatementExecutor;
import org.cloudgraph.cassandra.cql.StatementFactory;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

//package protection
class SubgraphTask
    implements SubgraphAssembler {
    private static Log log = LogFactory.getLog(SubgraphTask.class);
	protected static Set<Property> EMPTY_PROPERTY_SET = new HashSet<Property>();
	protected static List<DataObject> EMPTY_DATA_OBJECT_LIST = new ArrayList<DataObject>();
	protected PlasmaType subrootType;
	protected PlasmaDataObject source;
	protected SelectionCollector collector;
	protected Timestamp snapshotDate;
	protected Session con;
	protected Comparator<PropertyPair> nameComparator;
	protected StatementFactory statementFactory;
	protected StatementExecutor statementExecutor;
	protected PlasmaProperty sourceProperty;
	protected List<PropertyPair> childKeyPairs;
	protected int level;
	protected int sequence;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final ThreadPoolExecutor executorService = (ThreadPoolExecutor)Executors.newFixedThreadPool(
    		20/*Runtime.getRuntime().availableProcessors()*/);	
	
	private ThreadPoolGraphAssembler parent;

	public SubgraphTask(PlasmaType subrootType,
			PlasmaDataObject source,
			SelectionCollector collector,
			Timestamp snapshotDate, Session con,
			Comparator<PropertyPair> nameComparator,
			PlasmaProperty sourceProperty, List<PropertyPair> childKeyPairs,
			int level, int sequence,
			ThreadPoolGraphAssembler parent) {
		super();
		this.subrootType = subrootType;
		this.source = source;
		this.collector = collector;
		this.snapshotDate = snapshotDate;
		this.con = con;
		this.nameComparator = nameComparator;
		this.sourceProperty = sourceProperty;
		this.childKeyPairs = childKeyPairs;
		this.level = level;
		this.sequence = sequence;
		this.parent = parent;
		this.statementFactory = new CQLStatementFactory();
		this.statementExecutor = new CQLStatementExecutor(con);
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":process: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + subrootType.getName() + ": "
					+ childKeyPairs);
	}

    @Override
    public void start() {
    	if (log.isDebugEnabled())
    		log.debug("start-" + level + "." + sequence);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
            	//  begin a breadth first traversal from the given node
        		assemble(subrootType, source, sourceProperty, childKeyPairs, level);
        		shutdown();
            }
        });
    }

    @Override
    public void join() {
    	if (log.isDebugEnabled())
    		log.debug("join-" + level + "." + sequence);
       //Uninterruptibles.joinUninterruptibly(this.runner);
       awaitUninterruptibly(shutdownLatch);
    }
	
	private void assemble(PlasmaType targetType, PlasmaDataObject source,
			PlasmaProperty sourceProperty, List<PropertyPair> childKeyPairs, int level) 
	{
		Set<Property> props = this.collector.getProperties(targetType, level);
		if (props == null) {
			props = EMPTY_PROPERTY_SET;
		}
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":assemble: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + targetType.getName() + ": "
					+ props);
		
		List<List<PropertyPair>> result = null;
		Where where = this.collector.getPredicate(sourceProperty);
		if (where == null) {        
			List<Object> params = new ArrayList<Object>();
			StringBuilder query = this.statementFactory.createSelect(targetType, props, childKeyPairs, params);
			//The partition key is the first field in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			result = this.statementExecutor.fetch(targetType, query, props, paramArray);
		}
		else {
			FilterAssembler filterAssembler = new FilterAssembler(where, 
					targetType);
			List<Object> params = new ArrayList<Object>();			
			StringBuilder query = this.statementFactory.createSelect(targetType,
		    	props, childKeyPairs, filterAssembler, params);
			
			//The partition key is the first field in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			
			result = this.statementExecutor.fetch(targetType, query, props, paramArray);
		}		
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":results: "  + result.size());
	    
		// first create (or link existing) data objects 
		// "filling out" the containment hierarchy at this traversal level
		// BEFORE recursing, as we may "cancel" out an object
		// at the current level if it is first encountered
		// within the recursion.
		Map<PlasmaDataObject, List<PropertyPair>> resultMap = new HashMap<PlasmaDataObject, List<PropertyPair>>();
		for (List<PropertyPair> row : result) {
			
			PlasmaDataObject target = this.parent.findDataObject(targetType, row);
			// if no existing data-object in graph
			if (target == null) {
			    target = this.parent.createDataObject(row, source, 
					sourceProperty);
			    resultMap.put(target, row); // add only new object for later traversal
			}
			else { 
				this.parent.link(target, source, sourceProperty);
				continue; // don't map it for later traversal
				// Assume we traverse no farther given no traversal
				// direction or containment info. We only know that we 
				// encountered an existing node. Need more path specific 
				// info including containment and traversal direction to construct
				// a directed graph here. 
				// Since the current selection collector maps any and all
				// properties selected to a type, for each type/data-object
				// we will, at this point, have gotten all the properties we expect anyway.
				// So we create a link from the source to the existing DO, but
				// traverse no further. 
			}   
		}	
		
		// now traverse
		Iterator<PlasmaDataObject> iter = resultMap.keySet().iterator();
		while (iter.hasNext()) {
			PlasmaDataObject target = iter.next();
			List<PropertyPair> row = resultMap.get(target);
	        List<SubgraphAssembler> subAssemblers = new ArrayList<SubgraphAssembler>();
	        List<Traversal> traversals = new ArrayList<Traversal>();

			// traverse singular results props
			for (PropertyPair pair : row) {
				if (pair.getProp().isMany() || pair.getProp().getType().isDataType()) 
				    continue; // only singular reference props
				if (!pair.isQueryProperty())
					continue; // property is a key or other property not explicitly cited in the source query, don't traverse it
				List<PropertyPair> nextKeyPairs = new ArrayList<PropertyPair>();
				List<Property> nextKeyProps = ((PlasmaType)pair.getProp().getType()).findProperties(KeyType.primary);
			    			
		    	if (nextKeyProps.size() == 1) {
					if (log.isDebugEnabled())
						log.debug(String.valueOf(level) + ":found single PK for type, " + pair.getProp().getType());
					PlasmaProperty next = (PlasmaProperty)nextKeyProps.get(0);
					
					PropertyPair nextPair = new PropertyPair(next, pair.getValue());					
					nextKeyPairs.add(nextPair);
					if (log.isDebugEnabled())
						log.debug(String.valueOf(level) + ":added single PK, " + next.toString());
			    }
				else {
			    	PlasmaProperty opposite = (PlasmaProperty)pair.getProp().getOpposite();
			    	if (opposite == null)
				    	throw new DataAccessException("no opposite property found"
				        + " - cannot map from singular property, "
				        + pair.getProp().toString());	
			    	PlasmaProperty supplier = opposite.getKeySupplier();
			    	if (supplier != null) {
			    		PlasmaProperty nextProp = supplier;
				    	PropertyPair nextPair = this.parent.findNextKeyValue(target, 
				    			nextProp, opposite);
				    	nextKeyPairs.add(nextPair);
			    	}
				    else {
					    if (log.isDebugEnabled())
						    log.debug(String.valueOf(level) + ":found multiple PK's - throwing PK error");
					    this.parent.throwPriKeyError(nextKeyProps, 
							pair.getProp().getType(), pair.getProp());
				    }
				}
								    
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + pair.getProp().isMany() 
							+ ") " + pair.getProp().toString() + ":" + String.valueOf(pair.getValue()));
				
				if (executorService.getActiveCount() < executorService.getPoolSize()) {
					SubgraphAssembler assem = new SubgraphTask(
							(PlasmaType)pair.getProp().getType(),
							target,
							this.collector,
							this.snapshotDate, this.con,
							this.nameComparator,
							pair.getProp(), nextKeyPairs,
							level+1, subAssemblers.size(), this.parent);
					subAssemblers.add(assem);
				}
				else {
					Traversal trav = new Traversal((PlasmaType)pair.getProp().getType(), target,
							pair.getProp(), nextKeyPairs,
							level+1);
					traversals.add(trav);					
				}
				//assemble((PlasmaType)pair.getProp().getType(), 
				//		target, pair.getProp(), nextKeyPairs, level+1);	
			}
			
			// traverse multi props based, not on the results
			// row, but on keys within this data object
			for (Property p : props) {
				PlasmaProperty prop = (PlasmaProperty)p;
				if (!prop.isMany() || prop.getType().isDataType())
				    continue; // only many reference props
				
		    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
		    	if (opposite == null)
			    	throw new DataAccessException("no opposite property found"
			        + " - cannot map from many property, "
			        + prop.getType() + "." + prop.getName());	
				List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
				List<Property> nextKeyProps = ((PlasmaType)targetType).findProperties(KeyType.primary);
				PlasmaProperty supplier = opposite.getKeySupplier();
				if (supplier != null) {
		    		PlasmaProperty nextProp = supplier;
			    	PropertyPair pair = this.parent.findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
		    	}			    
				else if (nextKeyProps.size() == 1) {
			    	PlasmaProperty nextProp = (PlasmaProperty)nextKeyProps.get(0);	
			    	PropertyPair pair = this.parent.findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
			    }
			    else {  
			    	this.parent.throwPriKeyError(nextKeyProps, 
					    		targetType, prop);
			    }
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + prop.isMany() 
							+ ") " + prop.toString() + " - " + childKeyProps.toArray().toString());
			    //assemble((PlasmaType)prop.getType(), 
			    //		target, prop,
				//		childKeyProps, level+1);
				if (executorService.getActiveCount() < executorService.getPoolSize()) {
					SubgraphAssembler assem = new SubgraphTask(
							(PlasmaType)prop.getType(),
							target,
							this.collector,
							this.snapshotDate, this.con,
							this.nameComparator,
							prop, childKeyProps,
							level+1, subAssemblers.size(), this.parent);
					subAssemblers.add(assem);
				}
				else {
					Traversal trav = new Traversal((PlasmaType)prop.getType(), target,
							prop, childKeyProps,
							level+1);
					traversals.add(trav);					
				}
			}
			
			// start any asynchronous assemblers
			// FIXME: what if the active threads fill up by the time we get here?
			for (SubgraphAssembler asem : subAssemblers)
				asem.start();
			for (SubgraphAssembler asem : subAssemblers)
				asem.join();
			
			// continue with traversals for this thread
			for (Traversal trav : traversals) {
				assemble(trav.getSubrootType(), 
						trav.getSource(), trav.getSourceProperty(),
						trav.getChildKeyPairs(), trav.getLevel());				
			}
			
			
		}
	}

    protected void shutdown() {
        shutdownLatch.countDown();
    }

    private class Traversal {
    	private PlasmaType subrootType;
    	private PlasmaDataObject source;
    	private PlasmaProperty sourceProperty;
    	private List<PropertyPair> childKeyPairs;
    	private int level;
    	@SuppressWarnings("unused")
		private Traversal() {}
		 
		public Traversal(PlasmaType subrootType, PlasmaDataObject source,
				PlasmaProperty sourceProperty,
				List<PropertyPair> childKeyPairs, int level) {
			super();
			this.subrootType = subrootType;
			this.source = source;
			this.sourceProperty = sourceProperty;
			this.childKeyPairs = childKeyPairs;
			this.level = level;
		}
		public PlasmaType getSubrootType() {
			return subrootType;
		}
		public PlasmaDataObject getSource() {
			return source;
		}
		public PlasmaProperty getSourceProperty() {
			return sourceProperty;
		}
		public List<PropertyPair> getChildKeyPairs() {
			return childKeyPairs;
		}
		public int getLevel() {
			return level;
		}
    	
    }
}
