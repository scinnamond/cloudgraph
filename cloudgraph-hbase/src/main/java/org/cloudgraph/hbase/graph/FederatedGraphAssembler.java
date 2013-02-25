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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.ToumbstoneRowException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreNode;

/**
 * Constructs a data graph starting with a given root SDO type based on
 * a map of selected SDO properties, where properties are mapped by 
 * selected types required in the result graph.
 * <p>
 * The assembly is triggered by calling the 
 * {@link FederatedGraphAssembler#assemble(Result resultRow)} method which
 * recursively reads HBase keys and values re-constituting the
 * data graph. The assembly traversal is driven by HBase column 
 * values representing the original edges or containment structure 
 * of the graph. 
 * </p>
 * <p>
 * Since every column key in HBase must be unique, and a data graph
 * may contain any number of nodes, a column key factory is used both 
 * to persist as well as re-constitute a graph. A minimal amount of
 * "state" information is therefore stored with each graph which maps
 * user readable sequence numbers (which are used in column keys) to
 * UUID values. The nodes of the resulting data graph are re-created with
 * the original UUID values.       
 * </p>
 * 
 * @see org.cloudgraph.hbase.key.StatefullColumnKeyFactory
 * 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class FederatedGraphAssembler extends FederatedAssembler
{
    private static Log log = LogFactory.getLog(FederatedGraphAssembler.class);
		
	/**
	 * Constructor.
	 * @param rootType the SDO root type for the result data graph
	 * @param selection selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
	 * @param snapshotDate the query snapshot date which is populated
	 * into every data object in the result data graph. 
	 */
	public FederatedGraphAssembler(PlasmaType rootType,
			PropertySelection selection, 
			FederatedReader federatedReader,			
			Timestamp snapshotDate) 
	{
		super(rootType, selection, federatedReader, snapshotDate);
	}	
	
	protected void assemble(PlasmaDataObject target, 
			PlasmaDataObject source, PlasmaProperty sourceProperty, 
			RowReader rowReader, int level) throws IOException
    {		 
		CoreNode targetDataNode = (CoreNode)target;
        
        if (log.isDebugEnabled())
			log.debug("assembling: ("
				+ targetDataNode.getUUIDAsString() + ") "
					+ target.getType().getURI() + "#" 
					+ target.getType().getName());
		
		List<String> names = this.selection.getInheritedProperties(target.getType());
		if (names == null)
			throw new GraphServiceException("expected selection property names for type, "
					+ target.getType().getURI() + "#" + target.getType().getName());
		if (log.isDebugEnabled())
			log.debug(target.getType().getName() + " names: " + names.toString());
		
		assembleData(target, names, rowReader);		
		
		TableReader tableReader = rowReader.getTableReader();
		TableConfig tableConfig = tableReader.getTable();

		// reference props
		for (String name : names) {
			PlasmaProperty prop = (PlasmaProperty)target.getType().getProperty(name);
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
				TableReader externalTableReader = federatedReader.getTableReader(childTable);
				assembleExternalEdges(target, prop, edges, rowReader,
						externalTableReader, level);			
			}
		}
    }
	
	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
		Edge[] edges, RowReader rowReader, 
		TableReader childTableReader, RowReader childRowReader,
		int level) throws IOException 
	{
		for (Edge edge : edges) {	
        	if (childRowReader.contains(edge.getUuid()))
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)childRowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
        	
			if (log.isDebugEnabled())
				log.debug("local edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + edge.getUuid() + ")");
			
        	// create a child object
			PlasmaDataObject child = createChild(target, prop, edge, rowReader);
            childRowReader.addDataObject(child);
            
			assembleEdge(target, prop, edge, 
			        child, childRowReader, level);
		}
	}
	
	
	// Target is a different row, within this table or another.
	// Since we are assembling a graph, each edge requires
	// a new row reader. 
	// each edge is a new root in the target table
	// so need a new row reader for each
	private void assembleExternalEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, RowReader rowReader, TableReader childTableReader, int level) throws IOException 
	{
		RowReader childRowReader = null;
		for (Edge edge : edges) {	
			RowReader existingChildRowReader = childTableReader.getRowReader(edge.getUuid());
        	if (existingChildRowReader != null)
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
			if (log.isDebugEnabled())
				log.debug("external edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + edge.getUuid() + ")");
        	
			byte[] childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid());
			
			Result childResult = fetchGraph(childRowKey, childTableReader, edge.getType());
	    	if (childResult.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
	    			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
	    		log.warn("ignoring toubstone result row '" + 
		    			Bytes.toString(childRowKey) + "'");
				continue; // ignore toumbstone edge
	    	}
        	// create a child object
			PlasmaDataObject child = createChild(target, prop, edge, rowReader);
			
			// create a row reader for every external edge
			childRowReader = childTableReader.createRowReader(
				child, childResult);
			
			assembleEdge(target, prop, edge, 
		        child, childRowReader, level);
		}
	}
	
	
}
