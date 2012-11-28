package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelection;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;

/**
 * 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class SimpleGraphSliceAssembler extends DefaultAssembler
    implements HBaseGraphAssembler {

	protected TableConfig tableConfig;
	protected RowReader rowReader;
	private SliceSupport2 sliceSupport = new SliceSupport2();
	
    public SimpleGraphSliceAssembler(PlasmaType rootType,
    		PropertySelection collector, 
			TableReader tableReader,
			Timestamp snapshotDate) {
		super(rootType, collector, tableReader, snapshotDate);
		this.tableConfig = this.rootTableReader.getTable();
	}

	private static Log log = LogFactory.getLog(SimpleGraphSliceAssembler.class);

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
		
		List<String> names = this.selection.getProperties(target.getType());
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

			PlasmaType childType = (PlasmaType)prop.getType();
			
			// NOTE: can we have predicates on singular props? 
			Where where = this.selection.getPredicate(prop);
			Map<Integer, Integer> sequences = null;
			if (prop.isMany() && where != null) {
		    	sequences = this.sliceSupport.fetchSequences(childType, 
		    		where, this.rowReader);
				List<String> childPropertyNames = this.selection.getProperties(prop.getType());
				this.sliceSupport.loadBySequenceList(
					sequences.values(), 
					childPropertyNames, 
					childType, rowReader);
			}
			else {
			    List<String> childPropertyNames = this.selection.getProperties(prop.getType());
			    this.sliceSupport.load(childPropertyNames,
		    			childType, rowReader);
			}						
			
			Edge[] edges = this.rowReader.getGraphState().unmarshalEdges( 
				keyValue);
			
			assembleEdges(target, prop, edges, sequences, this.rowReader, level);
		}		
	}

	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
		Edge[] edges, Map<Integer, Integer> sequences, RowReader rowReader,
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
			if (sequences != null && sequences.get(edge.getId()) == null)
				continue;
			
        	// create a child object
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
			rowReader.addDataObject(child);			
            
	        assemble(child, target, prop, level+1);		
		}
	}
	
	@Override
	public void clear() {
		this.root = null;		 
		this.rootTableReader.clear();
	}
}
