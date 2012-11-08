package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
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
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class FederatedGraphAssembler extends FederatedAssembler
{
    private static Log log = LogFactory.getLog(FederatedGraphAssembler.class);
		
	/**
	 * Constructor.
	 * @param rootType the SDO root type for the result data graph
	 * @param collector selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
	 * @param snapshotDate the query snapshot date which is populated
	 * into every data object in the result data graph. 
	 */
	public FederatedGraphAssembler(PlasmaType rootType,
			PropertySelectionCollector collector, 
			FederatedReader federatedReader,			
			Timestamp snapshotDate) 
	{
		super(rootType, collector, federatedReader, snapshotDate);
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
		
		List<String> names = this.propertyMap.get(target.getType());
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
			if (keyValue == null)
				continue;
			
			Edge[] edges = rowReader.getGraphState().parseEdges(prop.getType(), 
				keyValue);
			
			PlasmaType childType = (PlasmaType)prop.getType();
			
			// if target type is not bound to a specific table/root,
			// derive a child row reader context from its level
			TableReader externalTableReader = this.federatedReader.getTableReader(childType.getQualifiedName());
			if (externalTableReader == null) { 
				RowReader childRowReader = getRowReader(level);
	        	assembleEdges(target, prop, edges, rowReader, 
	        		childRowReader.getTableReader(), 
	        		childRowReader, level);			
	        }
			else 
			{
				if (log.isDebugEnabled())
					if (!tableConfig.getName().equals(externalTableReader.getTable().getName()))
					    log.debug("switching row context from table: '"
						    + tableConfig.getName() + "' to table: '"
						    + externalTableReader.getTable().getName() + "'");
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
			RowReader existingChildRowReader = childTableReader.getRowReader(edge.getUuid());
        	if (existingChildRowReader != null)
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
			
        	// create a child object
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
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
			
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
			// create a row reader for every external edge
			byte[] childRowKey = rowReader.getGraphState().getRowKey(child);
			Result childResult = fetchGraph(childRowKey, childTableReader, child);
			childRowReader = childTableReader.createRowReader(
				child, childResult);
			
			assembleEdge(target, prop, edge, 
		        child, childRowReader, level);
		}
	}
	
}
