package org.cloudgraph.hbase.graph;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.concurrent.SubgraphTask;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.OperationException;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.Selection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.Property;

//package protection
class ParallelSubgraphTask extends DistributedAssembler implements SubgraphTask {
    private static Log log = LogFactory.getLog(ParallelSubgraphTask.class);
	protected PlasmaDataObject subroot;
	protected DistributedReader distributedReader;
	protected PlasmaDataObject source;
	protected PlasmaProperty sourceProperty;
	protected RowReader rowReader;
	protected int level;
	protected int sequence;
	/** 
	 * Maps row key strings to lock objects. Where multiple threads arrive at the same 
	 * node and the first thread is currently processing a fetch
	 * the first thread registers a lock for the row, then removes it when
	 * the fetch and any subsequent shared processing is complete. 
	 */
	private static Map<String, Object> fetchLocks = new ConcurrentHashMap<String, Object>();
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private ThreadPoolExecutor executorService;	
	
	private List<Traversal> traversals = new ArrayList<Traversal>();
    
	public ParallelSubgraphTask(PlasmaDataObject subroot,
			Selection selection,
			Timestamp snapshotDate,
			DistributedReader distributedReader,
			PlasmaDataObject source,
			PlasmaProperty sourceProperty,
			RowReader rowReader,
			int level, int sequence,
			ThreadPoolExecutor executorService) {
		super((PlasmaType)subroot.getType(), selection, distributedReader, snapshotDate); 
		this.subroot = subroot;
		this.selection = selection;
		this.snapshotDate = snapshotDate;
		this.distributedReader = distributedReader;
		this.source = source;
		this.sourceProperty = sourceProperty;
		this.rowReader = rowReader;
		this.level = level;
		this.sequence = sequence;
		this.executorService = executorService;
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
        		try {
					assemble(subroot, source, sourceProperty, rowReader, level);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
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
    
    public void assemble() throws IOException {
		assemble(subroot, source, sourceProperty, rowReader, level);
    }
    
	@Override
	protected void assemble(PlasmaDataObject target, PlasmaDataObject source,
			PlasmaProperty sourceProperty, RowReader rowReader, int level)
			throws IOException {
		
		Set<Property> props = this.getProperties(target, source, sourceProperty, level);
		if (props.size() == 0) 
			return;
        if (log.isDebugEnabled())
			log.debug("assembling("+level+"): " + target.toString() + ": " + props.toString());
        
        // synchronize on row-reader here rather than target because row-reader
        // uses shared column key factory
        synchronized (rowReader) {
		    assembleData(target, props, rowReader);	
        }
		
		TableReader tableReader = rowReader.getTableReader();
		TableConfig tableConfig = tableReader.getTable();
		
		traversals.clear();
	 
		// reference props
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.getType().isDataType())
				continue;
			
			byte[] keyValue = getColumnValue(target, prop, 
				tableConfig, rowReader);
			if (keyValue == null || keyValue.length == 0 ) {
				continue; // zero length can happen on modification or delete as we keep cell history
			}
			if (log.isDebugEnabled())
				log.debug(prop.getName() + ": " + Bytes.toString(keyValue));
			
			Edge[] edges = rowReader.getGraphState().unmarshalEdges( 
				keyValue);
			if (edges.length == 0) {
				continue; // zero length can happen on modification or delete as we keep cell history
			}
			
			boolean external = isExternal(edges, rowReader);			
			if (!external) {
				assembleEdges(target, prop, edges, rowReader, 
						tableReader, rowReader, level);
			}
			else {
				String childTable = rowReader.getGraphState().getRowKeyTable(edges[0].getUuid());
				if (childTable == null)
					throw new OperationException("no table found for type, " + 
							edges[0].getType());
				TableReader externalTableReader = distributedReader.getTableReader(childTable);
				if (externalTableReader == null)
					throw new OperationException("no table reader found for type, " + 
							edges[0].getType());
				assembleExternalEdges(target, prop, edges, rowReader,
						externalTableReader, level);			
			}
		}		
		
		List<Traversal> sync = new ArrayList<Traversal>();
		List<Traversal> async = new ArrayList<Traversal>();
		for (Traversal trav: traversals)
			if (trav.isConcurrent())
				async.add(trav);
			else
				sync.add(trav);
		traversals.clear();
		
		// create concurrent tasks based on pool availability
		int available = numThreadsAvailable();
		if (available > async.size())
			available = async.size();
		List<SubgraphTask> concurrentTasks = new ArrayList<SubgraphTask>();
		for (int i = 0; i < available; i++) {
			Traversal trav = async.get(i);
			SubgraphTask task = new ParallelSubgraphTask(trav.getSubroot(),
					this.selection,
					this.snapshotDate,
					this.distributedReader,
					trav.getSource(), trav.getSourceProperty(), trav.getRowReader(),
					trav.getLevel(), concurrentTasks.size(),
					this.executorService);
			concurrentTasks.add(task);
		}
		// add remainder 
		for (int i = available; i < async.size(); i++) {
			Traversal trav = async.get(i);
			sync.add(trav);
		}
		
		// start any asynchronous assemblers
		for (SubgraphTask task : concurrentTasks)
			task.start();
		for (SubgraphTask task : concurrentTasks)
			task.join();
		
		// continue with sync tasks/traversals in this/current thread
		for (Traversal trav : sync) {
			assemble(trav.getSubroot(), 
					trav.getSource(), trav.getSourceProperty(),
					trav.getRowReader(), trav.getLevel());				
		}		
	}
	
	protected void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
		Edge[] edges, RowReader rowReader, 
		TableReader childTableReader, RowReader childRowReader,
		int level) throws IOException 
	{
		for (Edge edge : edges) {	
			
			UUID uuid = UUID.fromString(edge.getUuid());
        	if (childRowReader.contains(uuid))
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)childRowReader.getDataObject(uuid);
    		    synchronized (existingChild) {
    		        synchronized (target) {
        		        link(existingChild, target, prop);
    	            }
        	    }
        		continue; 
        	}
        	
			if (log.isDebugEnabled())
				log.debug("local edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + edge.getUuid() + ")");
			
			PlasmaDataObject child = null;
	    	synchronized (target) {
        	    // create a child object
			    child = createChild(target, prop, edge);
	    	}
			synchronized (childRowReader) {
                childRowReader.addDataObject(child);
			}
		    synchronized (this.distributedReader) {
		        this.distributedReader.mapRowReader(child, 
					childRowReader);	
		    }
		    // indicate a non-concurrent traversal given this 
		    // is not a slice assembler no fetch occurring for internal edges
			traversals.add(new Traversal(child,
					target, prop, childRowReader,
					false, // indicate a non-concurrent traversal  
					level+1));					
		}
	}
	
	/**
	 * Assembles a given set of edges where the target is a different row, within this table or another.
	 * Since we are assembling a graph, each edge requires
	 * a new row reader. Each edge is a new root in the target table
	 * so need a new row reader for each. 
	 * @param target the object source to which we link edges
	 * @param prop the edge property
	 * @param edges the edges
	 * @param rowReader the row reader
	 * @param childTableReader the table reader for the child objects
	 * @param level the assembly level
	 * @throws IOException
	 */
	protected void assembleExternalEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, RowReader rowReader, TableReader childTableReader, int level) throws IOException 
	{
		for (Edge edge : edges) {
			byte[] childRowKey = null;
			UUID uuid = null;
			Result childResult = null;
			
			// need to look up an existing row reader based on the root UUID of the external graph
			// or the row key, and the row key is all we have in the local graph state. The edge UUID
			// is a local graph UUID. 
			childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid()); // use local edge UUID
			String childRowKeyStr = Bytes.toString(childRowKey);
			
			// see if this row is locked during fetch, and wait for it
			Object rowLock = fetchLocks.get(childRowKeyStr);
			if (rowLock != null) {
				synchronized (rowLock) {
					try {
						rowLock.wait();
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			
			RowReader existingChildRowReader = childTableReader.getRowReader(childRowKey);
        	if (existingChildRowReader != null)
        	{      
        		// If assembled this row root before, 
        		// just link it. The data is already complete.
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getRootDataObject();
    		    synchronized (existingChild) {
    		        synchronized (target) {
        		        link(existingChild, target, prop);
    	            }
        	    }
    		    continue; 
        	}   
 			
        	// While fetching this node, another thread can fail to find an existing row reader registered
        	// above and fall through to this fetch, and therefore fetch the same row, in addition
        	// to attempting to create the same row reader below, causing an error or warning
        	// The second thread may be arriving at this node from another property/edge and
        	// therefore need to link from another edge above. 
           	fetchLocks.put(childRowKeyStr, new Object());

           	if (log.isDebugEnabled())
				log.debug("fetch external row: " 
			        + prop.toString() + " (" + Bytes.toString(childRowKey) + ")");
			
			childResult = fetchGraph(childRowKey, childTableReader, edge.getType());
			
	    	if (childResult.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
	    			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
	    		log.warn("ignoring toubstone result row '" + 
	    				childRowKeyStr + "'");
				continue; // ignore toumbstone edge
	    	}
	    	
	        // need to reconstruct the original graph, so need original UUID
			byte[] rootUuid = childResult.getValue(Bytes.toBytes(
					childTableReader.getTable().getDataColumnFamilyName()), 
	                Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
			if (rootUuid == null)
				throw new GraphServiceException("expected column: "
					+ childTableReader.getTable().getDataColumnFamilyName() + ":"
					+ GraphState.ROOT_UUID_COLUMN_NAME);
			String uuidStr = null;
			uuidStr = new String(rootUuid, 
					childTableReader.getTable().getCharset());
			uuid = UUID.fromString(uuidStr);	    	
			if (log.isDebugEnabled())
				log.debug("external edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + uuid.toString() + ")");
      	
			PlasmaDataObject child = null;
	    	synchronized (target) {
        	    // create a child object using UUID from external row root
			    child = createChild(target, prop, edge, uuid);
	    	}
	    	
			RowReader childRowReader = null;
		    synchronized (childTableReader) {
		        childRowReader = childTableReader.createRowReader(
				        child, childResult);
		    }
		    synchronized (this.distributedReader) {
		        this.distributedReader.mapRowReader(child, 
					childRowReader);	
		    }
			traversals.add(new Traversal(child,
					target, prop, childRowReader,
					true,
					level+1));					
	     
		    rowLock = fetchLocks.remove(childRowKeyStr);
		    synchronized (rowLock) {
		    	rowLock.notifyAll();
		    }
		}
	}
			
	public void logPoolStatistics() {
		if (log.isDebugEnabled())
			log.debug("active: " + this.executorService.getActiveCount() + ", size: " 
		        + this.executorService.getPoolSize());		
	}
	
	public boolean threadsAvailable() {
		return this.executorService.getActiveCount() < this.executorService.getMaximumPoolSize();		
	}

	public int numThreadsAvailable() {
		int result = this.executorService.getMaximumPoolSize() - this.executorService.getActiveCount();
		if (result < 0)
			result = 0;
		return result;		
	}

}
