/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.cassandra.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.filter.FilterAssembler;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphAssembler;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class GraphAssembler extends CQLSupport
    implements DataGraphAssembler {

    private static Log log = LogFactory.getLog(GraphAssembler.class);
	private static Set<Property> EMPTY_PROPERTY_SET = new HashSet<Property>();
	private static List<DataObject> EMPTY_DATA_OBJECT_LIST = new ArrayList<DataObject>();
	private PlasmaType rootType;
	private PlasmaDataObject root;
    private SelectionCollector collector;
	private Timestamp snapshotDate;
	private Session con;
	private Map<Integer, PlasmaDataObject> dataObjectMap = new HashMap<Integer, PlasmaDataObject>();
	private Comparator<PropertyPair> nameComparator;
	
	@SuppressWarnings("unused")
	private GraphAssembler() {}
	
	public GraphAssembler(PlasmaType rootType,
			SelectionCollector collector, Timestamp snapshotDate,
			Session con) {
		this.rootType = rootType;
		this.collector = collector;
		this.snapshotDate = snapshotDate;
		this.con = con;
		this.converter = CQLDataConverter.INSTANCE;
		
		this.nameComparator = new Comparator<PropertyPair>() {
			@Override
			public int compare(PropertyPair o1, PropertyPair o2) {				
				return o1.getProp().getName().compareTo(
						o2.getProp().getName());
			}
		};		
	}
	
	/**
	 * Initiates the assembly of a data graph based on the 
	 * given results list. 
	 * @param results the results list
	 * @throws SQLException 
	 * 
	 * @see DataGraphAssembler.getDataGraph()
	 */
	public void assemble(List<PropertyPair> results)  {
		
    	long before = System.currentTimeMillis();

    	DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	this.root = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);		
		if (log.isDebugEnabled())
			log.debug("assembling root: " 
		        + this.root.getType().getName());
		
		CoreNode rootNode = (CoreNode)this.root;
        // add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);
		// set data properties
		for (PropertyPair pair : results) {
			if (pair.getProp().getType().isDataType()) {
				rootNode.setValue(pair.getProp().getName(), 
						pair.getValue());
			}
		}
		
        // map it
        int key = createHashKey(
        	(PlasmaType)this.root.getType(), results);
        if (log.isDebugEnabled())
        	log.debug("mapping root " + key + "->" + this.root);
        this.dataObjectMap.put(key, this.root);  
		
		// singular reference props
		for (PropertyPair pair : results) {
			if (pair.getProp().isMany() || pair.getProp().getType().isDataType())
			    continue;
			List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
			PlasmaProperty supplier = pair.getProp().getKeySupplier();
			if (supplier != null) {
				PropertyPair childPair = new PropertyPair(supplier,
		    			pair.getValue());
				childPair.setValueProp(supplier);
		    	childKeyProps.add(childPair);
			}
			else {
				List<Property> childPkProps = ((PlasmaType)pair.getProp().getType()).findProperties(KeyType.primary);
			    if (childPkProps.size() == 1) {
			    	childKeyProps.add(
			    		new PropertyPair((PlasmaProperty)childPkProps.get(0),
			    			pair.getValue()));
			    }
			    else
				    throwPriKeyError(childPkProps, 
				    		pair.getProp().getType(), pair.getProp());
			}
		    
		    assemble((PlasmaType)pair.getProp().getType(), 
				(PlasmaDataObject)this.root, pair.getProp(),
				childKeyProps, 1);
		    
		}
		
		// multi reference props (not found in results)
		Set<Property> props = this.collector.getProperties(this.rootType);
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType()) {
		    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
		    	if (opposite == null)
			    	throw new DataAccessException("no opposite property found"
				        + " - cannot map from many property, " + prop.toString());			    				    	
				List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
				List<Property> rootPkProps = ((PlasmaType)root.getType()).findProperties(KeyType.primary);
			    if (rootPkProps.size() == 1) {
			    	PlasmaProperty rootProp = (PlasmaProperty)rootPkProps.get(0);
			    	Object value = root.get(rootProp);
			    	if (value != null) {
			    		PropertyPair pair = new PropertyPair(opposite, value);
			    		pair.setValueProp(rootProp);
			    	    childKeyProps.add(pair);
			    	}
			    	else
			    		throw new GraphServiceException("no value found for key property, "  
			    				+ rootProp.toString());
			    }
			    else
				    throwPriKeyError(rootPkProps, 
				    		root.getType(), prop);
			    
			    assemble((PlasmaType)prop.getType(), 
						(PlasmaDataObject)this.root, prop,
						childKeyProps, 1);
			}
		}
		
    	long after = System.currentTimeMillis();
    	
    	rootNode.getValueObject().put(
    		CloudGraphConstants.GRAPH_ASSEMBLY_TIME,
    		Long.valueOf(after - before));    	
    	
    	GraphMetricVisitor visitor = new GraphMetricVisitor();
    	this.root.accept(visitor);
    	
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_COUNT,
        		Long.valueOf(visitor.getCount()));
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_DEPTH,
        		Long.valueOf(visitor.getDepth()));
	}
	
	/**
	 * Assembles a data object of the given target type by first forming a query using the
	 * given key/property pairs. If an existing data object is mapped for the given
	 * key pairs, the existing data object is linked. 
	 * @param targetType the type for the data object to be assembled
	 * @param source the source data object
	 * @param sourceProperty the source property
	 * @param childKeyPairs the key pairs for the data object to be assembled
	 * @throws SQLException 
	 */
	private void assemble(PlasmaType targetType, PlasmaDataObject source, PlasmaProperty sourceProperty, 
			List<PropertyPair> childKeyPairs, int level)  {
		Set<Property> props = this.collector.getProperties(targetType, level);
		if (props == null)
			props = EMPTY_PROPERTY_SET;
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":assemble: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + targetType.getName() + ": "
					+ props);
		
		List<List<PropertyPair>> result = null;
		Where where = this.collector.getPredicate(sourceProperty);
		if (where == null) {        
			List<Object> params = new ArrayList<Object>();
			StringBuilder query = createSelect(targetType, props, childKeyPairs, params);
			//The partition key is the first feild in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			result = fetch(targetType, query, props, paramArray, this.con);
		}
		else {
			FilterAssembler filterAssembler = new FilterAssembler(where, 
					targetType);
			List<Object> params = new ArrayList<Object>();			
			StringBuilder query = createSelect(targetType,
		    	props, childKeyPairs, filterAssembler, params);
			//The partition key is the first feild in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			
			result = fetch(targetType, query, props, paramArray,
				this.con);
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
			
			PlasmaDataObject target = findDataObject(targetType, row);
			// if no existing data-object in graph
			if (target == null) {
			    target = createDataObject(row, source, 
					sourceProperty);
			    resultMap.put(target, row); // add only new object for later traversal
			}
			else { 
				link(target, source, sourceProperty);
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
				    	PropertyPair nextPair = findNextKeyValue(target, 
				    			nextProp, opposite);
				    	nextKeyPairs.add(nextPair);
			    	}
				    else {
					    if (log.isDebugEnabled())
						    log.debug(String.valueOf(level) + ":found multiple PK's - throwing PK error");
					    throwPriKeyError(nextKeyProps, 
							pair.getProp().getType(), pair.getProp());
				    }
				}
								    
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + pair.getProp().isMany() 
							+ ") " + pair.getProp().toString() + ":" + String.valueOf(pair.getValue()));
				assemble((PlasmaType)pair.getProp().getType(), 
						target, pair.getProp(), nextKeyPairs, level+1);				
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
			    	PropertyPair pair = findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
		    	}			    
				else if (nextKeyProps.size() == 1) {
			    	PlasmaProperty nextProp = (PlasmaProperty)nextKeyProps.get(0);	
			    	PropertyPair pair = findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
			    }
			    else {  
				     throwPriKeyError(nextKeyProps, 
					    		targetType, prop);
			    }
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + prop.isMany() 
							+ ") " + prop.toString() + " - " + childKeyProps.toArray().toString());
			    assemble((PlasmaType)prop.getType(), 
			    		target, prop,
						childKeyProps, level+1);
			}				
		}
	}
	
	/**
	 * If the given property is a datatype property, returns a property pair with the
	 * given property set as the pair value property, otherwise traverses the data object graph 
	 * via opposite property links until a datatype
	 * property is found, then returns the property value pair with the traversal endpint
	 * property set as the pair value property.
	 * @param dataObject the data object
	 * @param prop the property
	 * @param opposite the opposite property
	 * @return the property value pair
	 */
	private PropertyPair findNextKeyValue(PlasmaDataObject dataObject, PlasmaProperty prop, PlasmaProperty opposite)
	{
		PlasmaDataObject valueTarget = dataObject;
		PlasmaProperty valueProp = prop;
		
    	Object value = valueTarget.get(valueProp.getName());
    	while (!valueProp.getType().isDataType()) {
    		valueTarget = (PlasmaDataObject)value;
    		valueProp = getOppositePriKeyProperty(valueProp);
    		value = valueTarget.get(valueProp.getName()); // FIXME use prop API
    	}
    	if (value != null) {
    		PropertyPair pair = new PropertyPair(opposite, value);
    		pair.setValueProp(valueProp);
    		return pair;
    	}
    	else 
    		throw new GraphServiceException("no value found for key property, " 
    	       + valueProp.toString());
	}
	
	/**
	 * Creates a new data object contained by the given source
	 * data object and source property.
	 * @param row the results row
	 * @param source the source data object
	 * @param sourceProperty the source containment property
	 * @return the new data object
	 */
	private PlasmaDataObject createDataObject(List<PropertyPair> row,
			PlasmaDataObject source, PlasmaProperty sourceProperty) {
		
		PlasmaDataObject target = (PlasmaDataObject)source.createDataObject(sourceProperty);		 
		CoreNode node = (CoreNode)target;
	    if (log.isDebugEnabled())
		    log.debug("create: " + source.getType().getName() 
				+ "." + sourceProperty.getName()
				+ "->" + target.getType().getName());
        
		// add concurrency fields
        if (snapshotDate != null)
        	node.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);
	    
        // set data properties bypassing SDO "setter" API
        // to avoid triggering read-only property error 
        for (PropertyPair pair : row) {
			if (pair.getProp().getType().isDataType()) {
				if (log.isDebugEnabled())
					log.debug("set: (" + pair.getValue() 
							+ ") " + pair.getProp().getContainingType().getName() 
							+ "." + pair.getProp().getName());
				node.setValue(pair.getProp().getName(), pair.getValue());
			}
		}
        
        // map it
        int key = createHashKey(
        	(PlasmaType)target.getType(), row);
        if (log.isDebugEnabled())
        	log.debug("mapping " + key + "->" + target);
        this.dataObjectMap.put(key, target);  
        
        return target;
	}

	/**
	 * Finds and returns an existing data object based on hte
	 * given results row which is part
	 * if this assembly unit, or returns null if not exists
	 * @param type the target type
	 * @param row the results row
	 * @return the data object
	 */
	private PlasmaDataObject findDataObject(PlasmaType type, List<PropertyPair> row) {
        int key = createHashKey(type, row);
        PlasmaDataObject result = this.dataObjectMap.get(key);
        if (log.isDebugEnabled()) {
            if (result != null)
            	log.debug("found existing mapping " + key + "->" + result);
            else
            	log.debug("found no existing mapping for hash key: " + key);
        }	
        return result;        
	}
	
	/**
	 * Creates a unique mappable key using the qualified type name
	 * and all key property values from the given row.
	 * @param type the type
	 * @param row the data values
	 * @return the key
	 */
	private int createHashKey(PlasmaType type, List<PropertyPair> row) {
		PropertyPair[] pairs = new PropertyPair[row.size()];
		row.toArray(pairs);
		Arrays.sort(pairs, this.nameComparator);
		int pkHash = type.getQualifiedName().hashCode();
		int fallBackHash = type.getQualifiedName().hashCode();
		
		int pks = 0;
		for (int i = 0; i < pairs.length; i++) {
			Object value = pairs[i].getValue();
			if (value == null) {
				log.warn("null voue for property, " + pairs[i].getProp().toString());
				continue;
			}
			if (pairs[i].getProp().isKey(KeyType.primary)) {
				pkHash = pkHash ^ value.hashCode();
				fallBackHash = fallBackHash ^ value.hashCode();
				pks++;
			}
			else {
				fallBackHash = fallBackHash ^ value.hashCode();
			}
		}
		if (pks > 0) {
		    List<Property> pkProps = type.findProperties(KeyType.primary);
		    if (pkProps.size() == pks)
		    	return pkHash;
		}
		
		return fallBackHash;
	}
	
	/**
	 * Creates a directed (link) between the given
	 * source and target data objects. The reference is
	 * created as a containment reference only if the given target
	 * has no container.
	 * @param target the data object which is the target 
	 * @param source the source data object
	 * @param sourceProperty the source property
	 * 
	 * @see TraversalDirection
	 */
    private void link(PlasmaDataObject target, PlasmaDataObject source, PlasmaProperty sourceProperty)
    {      
        if (log.isDebugEnabled())
            log.debug("linking source (" + source.getUUIDAsString() + ") "
                    + source.getType().getURI() + "#" + source.getType().getName() 
                    + "." + sourceProperty.getName() + "->("
                    + target.getUUIDAsString() + ") "
                    + target.getType().getURI() + "#" + target.getType().getName());
        
        if (sourceProperty.isMany()) {
        	
        	PlasmaProperty opposite = (PlasmaProperty)sourceProperty.getOpposite();
        	if (opposite != null && !opposite.isMany() && target.isSet(opposite)) {
                PlasmaDataObject existingOpposite = (PlasmaDataObject)target.get(opposite);
                if (existingOpposite != null) {
                    log.warn("encountered existing opposite (" + existingOpposite.getType().getName()
                            + ") value found while creating link " + source.toString()  
                            + "." + sourceProperty.getName() + "->"
                            + target.toString() + " - no link created");
        		    return;
                }
        	}
            @SuppressWarnings("unchecked")
			List<DataObject> list = source.getList(sourceProperty);
            if (list == null) 
                list = EMPTY_DATA_OBJECT_LIST;
             
            if (!list.contains(target)) {
            	// check if any existing list members already have the opposite property set
            	for (DataObject existing : list) {
                	if (opposite != null && !opposite.isMany() && existing.isSet(opposite)) {
                        PlasmaDataObject existingOpposite = (PlasmaDataObject)existing.get(opposite);
                        if (existingOpposite != null) {
                        	log.warn("encountered existing opposite (" + existingOpposite.getType().getName()
                                    + ") value found while creating link " + source.toString()  
                                    + "." + sourceProperty.getName() + "->"
                                    + target.toString() + " - no link created");
                		    return;
                        }
                	}
            	}
            	
                if (log.isDebugEnabled())
                    log.debug("adding target " + source.toString()  
                        + "." + sourceProperty.getName() + "->" + target.toString());
                if (target.getContainer() == null) {
                    target.setContainer(source);
                    target.setContainmentProperty(sourceProperty);
                }
                list.add(target);                
                source.setList(sourceProperty, list); 
                // FIXME: replaces existing list according to SDO spec (memory churn)
                // store some temp instance-property list on DO and only set using SDO
                // API on completion of graph. 
            }
        }
        else {
            // Selection map keys are paths from the root entity and
            // elements in the path are often repeated. Expect repeated 
        	// events for repeated path elements, which
            // may be useful for some implementations, but not this one. So
            // we screen these out here. 
            PlasmaDataObject existing = (PlasmaDataObject)source.get(sourceProperty);
            if (existing == null) {
                source.set(sourceProperty, target); 
                // While the SDO spec seems to indicate (see 3.1.6 Containment) that
                // a Type may have only 1 reference property which a containment 
                // property, this seems too inflexible given the almost infinite
                // number of ways a graph could be constructed. We therefore allow any reference
                // property to be a containment property, and let the graph assembly
                // order determine which properties are containment properties for a particular
                // graph result. The SDO spec is crystal clear that every Data Object
                // other than the root, must have one-and-only-one container. We set the container
                // here as well as the specific reference property that currently is
                // the containment property, based on graph traversal order. Note it would be
                // possible to specify exactly which property is containment in a
                // query specification. We set no indication of containment on the 
                // (source) container object because all reference properties are 
                // potentially containment properties.  
                if (target.getContainer() == null) {
                    target.setContainer(source);
                    target.setContainmentProperty(sourceProperty);
                }
            }
            else
                if (!existing.equals(target))
                	if (log.isDebugEnabled())
                        log.debug("encountered existing (" + existing.getType().getName()
	                        + ") value found while creating link " + source.toString() 
	                        + "." + sourceProperty.getName() + "->"
	                        + target.toString());
        }
    }
    	
	private void throwPriKeyError(List<Property> rootPkProps, 
			Type type, Property prop) {
		if (prop.isMany())
		    if (rootPkProps.size() == 0)
		    	throw new DataAccessException("no pri-keys found for "
			        + type.getURI() + "#" + type.getName()
			        + " - cannot map from many property, "
			        + prop.getType() + "." + prop.getName());			    	
		    else	
		    	throw new DataAccessException("multiple pri-keys found for "
		    		+ type.getURI() + "#" + type.getName()
		            + " - cannot map from many property, "
		            + prop.getType() + "." + prop.getName());
		else
		    if (rootPkProps.size() == 0)
		    	throw new DataAccessException("no pri-keys found for "
			    	+ type.getURI() + "#" + type.getName()
			        + " - cannot map from singular property, "
			        + prop.getType() + "." + prop.getName());			    	
		    else	
		    	throw new DataAccessException("multiple pri-keys found for "
		    		+ type.getURI() + "#" + type.getName()
		            + " - cannot map from singular property, "
		            + prop.getType() + "." + prop.getName());			    					
	}
	 
	public PlasmaDataGraph getDataGraph() {
		return (PlasmaDataGraph)this.root.getDataGraph();
	}
	 
	public void clear() {
		this.root = null;
		this.dataObjectMap.clear();
	}
	
	private class GraphMetricVisitor implements PlasmaDataGraphVisitor {
		
		private long count = 0;
		private long depth = 0;
		@Override
		public void visit(DataObject target, DataObject source,
				String sourcePropertyName, int level) {
			count++;
			if (level > depth)
				depth = level;
			
		}
		public long getCount() {
			return count;
		}
		public long getDepth() {
			return depth;
		}		
	}
}
