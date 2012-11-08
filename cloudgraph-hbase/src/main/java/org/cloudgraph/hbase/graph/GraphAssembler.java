package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.core.TraversalDirection;

/**
 * Constructs a data graph starting with a given root SDO type based on
 * a map of selected SDO properties, where properties are mapped by 
 * selected types required in the result graph.
 * <p>
 * The assembly is triggered by calling the 
 * {@link GraphAssembler#assemble(Result resultRow)} method which
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
public class GraphAssembler extends DefaultAssembler
    implements HBaseGraphAssembler {

	protected TableConfig tableConfig;
	protected RowReader rowReader;
    public GraphAssembler(PlasmaType rootType,
			PropertySelectionCollector collector, 
			TableReader tableReader,
			Timestamp snapshotDate) {
		super(rootType, collector, tableReader, snapshotDate);
		this.tableConfig = this.rootTableReader.getTable();
	}

	private static Log log = LogFactory.getLog(GraphAssembler.class);

	@Override
	public void assemble(Result resultRow) {
	    this.root = createRoot(resultRow);
	    this.rowReader = this.rootTableReader.createRowReader(
				this.root, resultRow);
		try {
			assemble(this.root, null, null, 0);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}		
	}
	
	protected void assemble(PlasmaDataObject target, 
		PlasmaDataObject source, PlasmaProperty sourceProperty, 
		int level) throws IOException 
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

		// reference props
		for (String name : names) {
			PlasmaProperty prop = (PlasmaProperty)target.getType().getProperty(name);
			if (prop.getType().isDataType())
				continue;
			
			byte[] keyValue = getColumnValue(target, prop, 
				this.tableConfig, rowReader);
			if (keyValue == null)
				continue;
			
			Edge[] edges = this.rowReader.getGraphState().parseEdges(prop.getType(), 
				keyValue);
			
			assembleEdges(target, prop, edges, this.rowReader, level);
		}		
	}

	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
		Edge[] edges, RowReader rowReader,
		int level) throws IOException 
	{
		for (Edge edge : edges) {	
        	if (rowReader.contains(edge.getUuid()))
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)rowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
			
        	// create a child object
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
			rowReader.addDataObject(child);			
            
			// FIXME: if left, then create a link only??
			if (edge.getDirection().ordinal() == TraversalDirection.RIGHT.ordinal())
		        assemble(child, target, prop, level+1);		
		}
	}
	
	@Override
	public void clear() {
		this.root = null;		 
		this.rootTableReader.clear();
	}
}
