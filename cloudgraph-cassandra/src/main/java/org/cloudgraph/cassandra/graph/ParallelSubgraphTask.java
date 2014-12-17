package org.cloudgraph.cassandra.graph;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.concurrent.SubgraphTask;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.provider.common.PropertyPair;

import com.datastax.driver.core.Session;

import commonj.sdo.Property;

//package protection
class ParallelSubgraphTask extends AssemblerSupport implements SubgraphTask {
    private static Log log = LogFactory.getLog(ParallelSubgraphTask.class);
	protected PlasmaType subrootType;
	protected PlasmaDataObject source;
	protected PlasmaProperty sourceProperty;
	protected List<PropertyPair> subrootChildKeyPairs;
	protected int level;
	protected int sequence;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 20,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadPoolExecutor.CallerRunsPolicy());
            //(ThreadPoolExecutor)Executors.newFixedThreadPool(    		
    		//1/*Runtime.getRuntime().availableProcessors()*/);	
	
	private ParallelGraphAssembler sharedAssembler;
    
	public ParallelSubgraphTask(PlasmaType subrootType,
			PlasmaDataObject source,
			SelectionCollector selection,
			Session con,
			PlasmaProperty sourceProperty, List<PropertyPair> childKeyPairs,
			int level, int sequence,
			ParallelGraphAssembler assembler) {
		super(selection, con);
		this.subrootType = subrootType;
		this.source = source;
		this.sourceProperty = sourceProperty;
		this.subrootChildKeyPairs = childKeyPairs;
		this.level = level;
		this.sequence = sequence;
		this.sharedAssembler = assembler;
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":process: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + subrootType.getName() + ": "
					+ childKeyPairs);
	}

    @Override
    public void start() {
    	if (log.isDebugEnabled())
    		log.debug("start-" + level + "." + sequence);
    	try {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
            	//  begin a breadth first traversal from the given node
        		assemble();
        		shutdown();
            }
        });
    	}
    	catch (RejectedExecutionException e) {
    	    log.error(e.getMessage(), e);	
    	}
    }
    
    protected void shutdown() {
        this.shutdownLatch.countDown();
    }

    @Override
    public void join() {
    	if (log.isDebugEnabled())
    		log.debug("join-" + level + "." + sequence);
       //Uninterruptibles.joinUninterruptibly(this.runner);
       awaitUninterruptibly(shutdownLatch);
    }
    
    public void assemble() {
		assemble(this.subrootType, this.source, this.sourceProperty, this.subrootChildKeyPairs, this.level);
    }
	
	/**
	 * Assembles a data object of the given target type by first forming a query using the
	 * given key/property pairs. If an existing data object is mapped for the given
	 * key pairs, the existing data object is linked. 
	 * @param targetType the type for the data object to be assembled
	 * @param source the source data object
	 * @param sourceProperty the source property
	 * @param childKeyPairs the key pairs for the data object to be assembled
	 */
	private void assemble(PlasmaType targetType, PlasmaDataObject source,
			PlasmaProperty sourceProperty, List<PropertyPair> childKeyPairs, int level) 
	{
		Set<Property> props = this.collector.getProperties(targetType, level);
		if (props == null) {
			props = DefaultAssembler.EMPTY_PROPERTY_SET;
		}
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":assemble: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + targetType.getName() + ": "
					+ props);
		
		List<List<PropertyPair>> result = this.getPredicateResult(targetType, sourceProperty, 
				props, childKeyPairs);
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":results: "  + result.size());
	    
		Map<PlasmaDataObject, List<PropertyPair>> resultMap = this.sharedAssembler.collectResults(
				targetType, source, sourceProperty, result);
		
		// now traverse
		Iterator<PlasmaDataObject> iter = resultMap.keySet().iterator();
		while (iter.hasNext()) {
			PlasmaDataObject target = iter.next();
			List<PropertyPair> row = resultMap.get(target);
	        List<Traversal> traversals = new ArrayList<Traversal>();

			// traverse singular results props
			for (PropertyPair pair : row) {
				if (pair.getProp().isMany() || pair.getProp().getType().isDataType()) 
				    continue; // only singular reference props
				if (!pair.isQueryProperty())
					continue; // property is a key or other property not explicitly cited in the source query, don't traverse it
				List<PropertyPair> nextKeyPairs = this.getNextKeyPairs(target, pair, level);
								    
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + pair.getProp().isMany() 
							+ ") " + pair.getProp().toString() + ":" + String.valueOf(pair.getValue()));
				
				Traversal trav = new Traversal((PlasmaType)pair.getProp().getType(), target,
						pair.getProp(), nextKeyPairs,
						level+1);
				traversals.add(trav);					
			}
			
			// traverse multi props based, not on the results
			// row, but on keys within this data object
			for (Property p : props) {
				PlasmaProperty prop = (PlasmaProperty)p;
				if (!prop.isMany() || prop.getType().isDataType())
				    continue; // only many reference props
				
				List<PropertyPair> childKeyProps = this.getChildKeyProps(target, targetType, prop);
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + prop.isMany() 
							+ ") " + prop.toString() + " - " + childKeyProps.toArray().toString());
				Traversal trav = new Traversal((PlasmaType)prop.getType(), target,
						prop, childKeyProps,
						level+1);
				traversals.add(trav);					
			}
			
			// create concurrent tasks based on pool availability
			logPoolStatistics();
			int available = numThreadsAvailable();
			if (available > traversals.size())
				available = traversals.size();
			List<SubgraphTask> concurrentTasks = new ArrayList<SubgraphTask>();
			for (int i = 0; i < available; i++) {
				Traversal trav = traversals.get(i);
				SubgraphTask task = new ParallelSubgraphTask(
						trav.getSubrootType(),
						trav.getSource(),
						this.collector,
						this.con,
						trav.getSourceProperty(), trav.getChildKeyPairs(),
						trav.getLevel(), i, this.sharedAssembler);
				concurrentTasks.add(task);
			}
			
			// start any asynchronous assemblers
			for (SubgraphTask task : concurrentTasks)
				task.start();
			for (SubgraphTask task : concurrentTasks)
				task.join();

			// add remainder 
			// continue with traversals for this thread
			for (int i = available; i < traversals.size(); i++) {
				Traversal trav = traversals.get(i);
				assemble(trav.getSubrootType(), 
						trav.getSource(), trav.getSourceProperty(),
						trav.getChildKeyPairs(), trav.getLevel());				
			}
		}
	}
	
	public static void logPoolStatistics() {
		if (log.isDebugEnabled())
			log.debug("active: " + executorService.getActiveCount() + ", size: " + executorService.getPoolSize());		
	}
	
	public static boolean threadsAvailable() {
		return executorService.getActiveCount() < executorService.getMaximumPoolSize();		
	}
	
	public static int numThreadsAvailable() {
		int result = executorService.getMaximumPoolSize() - executorService.getActiveCount();
		if (result < 0)
			result = 0;
		return result;		
	}

}
