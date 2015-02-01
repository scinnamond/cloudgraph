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
package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.concurrent.ConfigProps;
import org.cloudgraph.common.concurrent.SubgraphTask;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.Selection;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.Property;

/**
 * A concurrent assembly task which assembles a sub-graph "sliced" 
 * using any number of path predicates 
 * starting with a given "sub root" based on the
 * given <a target="#"
 * href="http://plasma-sdo.org/org/plasma/query/collector/Selection.html">"selection graph"</a>.
 * Processing proceeds as a breadth-first
 * traversal and additional tasks are dynamically spawned based on thread availability
 * within a shared <a href=
 * "https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html"
 * >thread pool</a>. If thread availability is exhausted, processing proceeds
 * within the current thread. 
 *  
 * @see GraphSliceSupport 
 * @see DistributedReader
 * @see RowReader
 * 
 * @author Scott Cinnamond
 * @since 0.6.2
 */
//package protection
class ParallelSliceSubgraphTask extends DefaultSubgraphTask implements SubgraphTask {
    private static Log log = LogFactory.getLog(ParallelSliceSubgraphTask.class);
    private GraphSliceSupport sliceSupport;
    
	public ParallelSliceSubgraphTask(PlasmaDataObject subroot,
			Selection selection,
			Timestamp snapshotDate,
			DistributedReader distributedReader,
			PlasmaDataObject source,
			PlasmaProperty sourceProperty,
			RowReader rowReader,
			int level, int sequence,
			ThreadPoolExecutor executorService,
			ConfigProps config) {
		super(subroot,selection,snapshotDate,distributedReader,source,sourceProperty,rowReader,
			level,sequence, executorService, config);
		this.sliceSupport = new GraphSliceSupport(selection,snapshotDate);
	}
	
	@Override
	protected SubgraphTask newTask(PlasmaDataObject subroot,
			Selection selection, Timestamp snapshotDate,
			DistributedReader distributedReader, PlasmaDataObject source,
			PlasmaProperty sourceProperty, RowReader rowReader, int level,
			int sequence, ThreadPoolExecutor executorService, ConfigProps config) {
		return new ParallelSliceSubgraphTask(subroot,selection,snapshotDate,distributedReader,source,sourceProperty,rowReader,
				level,sequence, executorService, config);
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
			
			PlasmaType childType = (PlasmaType)prop.getType();
			
			// NOTE: can we have predicates on singular props? 
			Where where = this.selection.getPredicate(prop);
			
			boolean external = isExternal(edges, rowReader);			
			if (!external) { 								
				Map<Integer, Integer> sequences = null;
				if (prop.isMany() && where != null) {
			    	sequences = this.sliceSupport.fetchSequences((PlasmaType)prop.getType(), 
			    			where, rowReader);
			    	// preload properties for the NEXT level into the current row so we have something to assemble
					Set<Property> childProperies = this.selection.getInheritedProperties(prop.getType(), level+1); 
					this.sliceSupport.loadBySequenceList(sequences.values(), 
							childProperies,
			    		childType, rowReader);
				}
				else {  
			    	// preload properties for the NEXT level into the current row so we have something to assemble
					Set<Property> childProperies = this.selection.getInheritedProperties(prop.getType(), level+1);
				    this.sliceSupport.load(childProperies,
			    			childType, rowReader);
				}			
				
	        	assembleEdges(target, prop, edges, sequences, rowReader, 
	        			rowReader.getTableReader(), 
	        			rowReader, level);			
	        }
			else 
			{
				String childTable = rowReader.getGraphState().getRowKeyTable(edges[0].getUuid());
				TableReader externalTableReader = distributedReader.getTableReader(childTable);
				
				if (log.isDebugEnabled())
					if (!tableConfig.getName().equals(externalTableReader.getTable().getName()))
					    log.debug("switching row context from table: '"
						    + tableConfig.getName() + "' to table: '"
						    + externalTableReader.getTable().getName() + "'");
				Map<String, Result> resultRows = null;
				if (prop.isMany() && where != null) {
					 resultRows = this.sliceSupport.filter(childType, edges, 
						where, rowReader, externalTableReader);					
				}
				assembleExternalEdges(target, prop, edges, rowReader,	
					resultRows, externalTableReader, level);
			}			
		}		
		
		traverse(level);		
	}
	
	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, Map<Integer, Integer> sequences, RowReader rowReader, 
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
        		        continue; 
    	            }
        	    }
        	}
        	if (sequences != null && sequences.get(edge.getId()) == null)
				continue; // screen out edges
			
			if (log.isDebugEnabled())
				log.debug("local edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + edge.getUuid() + ")");
         	// create a child object
			PlasmaDataObject child = null;
	    	synchronized (target) {
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
			Edge[] edges, RowReader rowReader, Map<String, Result> resultRows,
			TableReader childTableReader, int level) throws IOException 
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
			if (resultRows != null && resultRows.get(childRowKeyStr) == null)
				continue; //not found in predicate
			
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
			uuid = reconstituteUUID(childResult, childTableReader);;	    	
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
}
