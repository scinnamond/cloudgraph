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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.concurrent.GraphMetricVisitor;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.query.collector.Selection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.core.CoreNode;

import commonj.sdo.Property;

/**
 * Supports the assembly of a directed data graph which may span multiple
 * HBase tables and/or rows by maintaining a stack of
 * row readers annotated with graph "level" and other information. 
 * This allows a specific row reader to be determined for any data object within a graph
 * based entirely or in part on its level within the graph. 
 * This is essential for data object types which are not 
 * configured as root types within any table, and yet a specific
 * row reader must be determined. 
 * 
 * @see org.cloudgraph.hbase.io.DistributedReader
 * @see org.cloudgraph.hbase.io.TableReader
 * @see org.cloudgraph.hbase.io.RowReader
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class DistributedAssembler extends DefaultAssembler 
    implements HBaseGraphAssembler
{
    private static Log log = LogFactory.getLog(DistributedAssembler.class);

	protected DistributedReader distributedReader;

	/**
	 * Constructor. 
	 * @param rootType the distributed graph root type
	 * @param selection the selection collector
	 * @param distributedReader the distributed reader
	 * @param snapshotDate the query snapshot date
	 */
	public DistributedAssembler(PlasmaType rootType,
			Selection selection,
			DistributedReader distributedReader,
			Timestamp snapshotDate) {
		super(rootType, selection, 
			distributedReader.getRootTableReader(),
			snapshotDate);
		this.distributedReader = distributedReader;
	}

	/**
     * Recursively re-constitutes a data graph distributed across multiple
     * HBase tables and/or rows, starting with the given HBase client result row. 
     * <p>
     * To retrieve the graph use {@link GraphAssembler#getDataGraph()}.
     * a map of selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
     * </p>
	 * @param resultRow the HBase client
     * result (row).
	 */
	@Override
	public void assemble(Result resultRow) {
		
	    this.root = createRoot(resultRow);
		
		RowReader rowReader = this.rootTableReader.createRowReader(
			this.root, resultRow);
		this.distributedReader.mapRowReader(this.root, 
				rowReader);					
    	// FIXME: are there not supposed to be instance
    	// properties on data object? Why must we
    	// go into core object. 
    	CoreDataObject root = (CoreDataObject)this.root;
    	root.getValueObject().put(
        	CloudGraphConstants.ROW_KEY, rowReader.getRowKey());
		
    	long before = System.currentTimeMillis();
		try {
			assemble(this.root, null, null, rowReader, 0);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}
    	long after = System.currentTimeMillis();
    	
        root.getValueObject().put(
    		CloudGraphConstants.GRAPH_ASSEMBLY_TIME,
    		Long.valueOf(after - before));    	
    	
    	GraphMetricVisitor visitor = new GraphMetricVisitor();
    	this.root.accept(visitor);
    	
    	root.getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_COUNT,
        		Long.valueOf(visitor.getCount()));
    	root.getValueObject().put(
        		CloudGraphConstants.GRAPH_DEPTH,
        		Long.valueOf(visitor.getDepth()));
    	root.getValueObject().put(
        		CloudGraphConstants.GRAPH_THREAD_COUNT,
        		Long.valueOf(visitor.getThreadCount()));
    	
    	List<String> tables = new ArrayList<String>();
    	for (TableReader tableReader : this.distributedReader.getTableReaders()) {
    		tables.add(tableReader.getTableName());
    	}
    	root.getValueObject().put(
        		CloudGraphConstants.GRAPH_TABLE_NAMES,
        		tables);    	
	}
		
	/**
	 * Populates the given data object target, recursively fetching
	 * data for and linking related data objects which make up the 
	 * resulting directed graph. 
	 * @param target the current data object target
	 * @param source the source or parent data object
	 * @param sourceProperty the source (reference) property
	 * @param rowReader the current row reader
	 * @param level the current graph level
     * @throws IOException if a remote or network exception occurs.
	 */
	protected abstract void assemble(PlasmaDataObject target, 
		PlasmaDataObject source, PlasmaProperty sourceProperty, 
		RowReader rowReader, int level) throws IOException;
	
	protected void assembleEdge(PlasmaDataObject target, PlasmaProperty prop,
		Edge edge, PlasmaDataObject child, RowReader childRowReader, int level) throws IOException {
    	this.distributedReader.mapRowReader(child, 
				childRowReader);					
        
		if (log.isDebugEnabled())
			log.debug("traverse: (" + prop.getName() + ") " + String.valueOf(edge.getId()));					
		
        assemble(child, target, prop, childRowReader, level+1);		
	}	
	
	protected UUID reconstituteUUID(Result result, TableReader tableReader) {
        // need to reconstruct the original graph, so need original UUID
		byte[] rootUuid = result.getValue(Bytes.toBytes(
				tableReader.getTable().getDataColumnFamilyName()), 
                Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
		if (rootUuid == null)
			throw new GraphServiceException("expected column: "
				+ tableReader.getTable().getDataColumnFamilyName() + ":"
				+ GraphState.ROOT_UUID_COLUMN_NAME);
		String uuidStr = null;
		uuidStr = new String(rootUuid, 
				tableReader.getTable().getCharset());
		return UUID.fromString(uuidStr);	    			
	}
	
	/**
	 * Creates contained child data object with the same type
	 * as the given containment property or of a specific sub-type
	 * as determined by querying the graph state, as represented in the
	 * given state Edge.
	 * 
	 * @param target the container data object
	 * @param prop the containment property
	 * @param edge the state edge
	 * @return the new child data object
	 * @throws IOException 
	 */
	protected PlasmaDataObject createChild(PlasmaDataObject target, PlasmaProperty prop,
			Edge edge) throws IOException {
		PlasmaType edgeType = edge.getType();
		if (log.isDebugEnabled())
			log.debug("creating data object ("
		        + edge.getUuid()+ ") type:  "
		        + edgeType.toString());
		PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop, edge.getType());								
		child.resetUUID(UUID.fromString(edge.getUuid()));		
		((CoreNode)child).getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_THREAD_NAME,
        		Thread.currentThread().getName());
		return child;		
	}
	
	/**
	 * Creates contained child data object with the same type
	 * as the given containment property or of a specific sub-type
	 * as determined by querying the graph state, as represented in the
	 * given state Edge, but using the given external row root UUID
	 * @param target the container data object
	 * @param prop the containment property
	 * @param edge the state edge
	 * @param rootUuid the external row root UUID
	 * @return the new child data object
	 * @throws IOException
	 */
	protected PlasmaDataObject createChild(PlasmaDataObject target, PlasmaProperty prop,
			Edge edge, UUID rootUuid) throws IOException {
		PlasmaType edgeType = edge.getType();
		if (log.isDebugEnabled())
			log.debug("creating data object ("
		        + rootUuid.toString() + ") type:  "
		        + edgeType.toString());
		PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop, edge.getType());
		child.resetUUID(rootUuid);		
		((CoreNode)child).getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_THREAD_NAME,
        		Thread.currentThread().getName());
		return child;		
	}
	
	/**
	 * Peeks at the first edge and determines whether 
	 * an external edge collection
	 * @param edges the state edges
	 * @param rowReader the reader
	 * @return whether 
	 * an external edge collection
	 * @throws IOException
	 */
	protected boolean isExternal(Edge[] edges, RowReader rowReader) throws IOException {
		if (edges.length > 0) {
		    return rowReader.getGraphState().findRowKey(
				edges[0].getUuid()) != null;
		}
		else {
			return false;
		}
	}
	
	protected Set<Property> getProperties(PlasmaDataObject target, PlasmaDataObject source,
			PlasmaProperty sourceProperty, int level)
	{
		Set<Property> props;
		if (sourceProperty != null) {
			//props = this.selection.getInheritedProperties(target.getType(), sourceProperty, level);
			props = this.selection.getInheritedProperties(target.getType(), level);
			if (props.size() == 0) {
		        if (log.isDebugEnabled())
		        	log.debug("no properties for " + target.toString() + " at level: " + level 
		        		+ " for source edge, " + sourceProperty.toString() + " - aborting traversal");
			}
		}
		else {
			props = this.selection.getInheritedProperties(target.getType(), level);
			if (props.size() == 0) {
		        if (log.isDebugEnabled())
		        	log.debug("no properties for " + target.toString() + " at level: " + level 
		        		+ " - aborting traversal");
			}
		}
		return props;
	}
	
	/**
	 * Resets the assembler.
	 */
	@Override
	public void clear() {
		this.root = null;
		this.distributedReader.clear();
	}	
}
