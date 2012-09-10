package org.cloudgraph.hbase.service;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.GraphState;
import org.cloudgraph.common.service.GraphState.Edge;
import org.cloudgraph.common.service.DispatcherSupport;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.key.HBaseStatefullColumnKeyFactory;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.Type;

/**
 * Constructs a data graph starting with a given root SDO type based on
 * a map of selected SDO properties, where properties are mapped by 
 * selected types required in the result graph.
 * <p>
 * The assembly is triggered by calling the 
 * {@link HBaseMemoryGraphAssembler#assemble(Result resultRow)} method which
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
 * @see org.cloudgraph.hbase.key.HBaseStatefullColumnKeyFactory
 */
public class HBaseMemoryGraphAssembler
    implements HBaseGraphAssembler {

    private static Log log = LogFactory.getLog(HBaseMemoryGraphAssembler.class);
	private PlasmaType rootType;
	private PlasmaDataObject root;
	private Map<Type, List<String>> propertyMap;
	private Timestamp snapshotDate;
	private GraphState graphState;		
	private GraphStatefullColumnKeyFactory columnKeyGen;
	private Map<UUID, PlasmaDataObject> dataObjects = new HashMap<UUID, PlasmaDataObject>();
	private TableConfig tableConfig;
	
	@SuppressWarnings("unused")
	private HBaseMemoryGraphAssembler() {}
	
	/**
	 * Constructor.
	 * @param rootType the SDO root type for the result data graph
	 * @param propertyMap a map of selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
	 * @param snapshotDate the query snapshot date which is populated
	 * into every data object in the result data graph. 
	 */
	public HBaseMemoryGraphAssembler(PlasmaType rootType,
			Map<Type, List<String>> propertyMap, 
			Timestamp snapshotDate,
			TableConfig tableConfig) {
		this.rootType = rootType;
		this.propertyMap = propertyMap;
		this.snapshotDate = snapshotDate;
		this.tableConfig = tableConfig;
	}
	
	/**
     * Re-constitutes a data graph from the given HBase client
     * result (row). To retrieve the graph use {@link HBaseMemoryGraphAssembler#getDataGraph()}.
     * a map of selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
	 * @param resultRow the HBase client
     * result (row).
	 */
	public void assemble(Result resultRow) {
		byte[] state = resultRow.getValue(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME));
        if (state != null) {
        	if (log.isDebugEnabled())
        		log.debug(GraphState.STATE_MAP_COLUMN_NAME
        			+ ": " + new String(state));
        }
        else
			throw new DataAccessException("expected column '"
				+ GraphState.STATE_MAP_COLUMN_NAME + "' for row " 
				+ Bytes.toString(resultRow.getRow()) + "'"); 
        
        this.graphState = new GraphState(Bytes.toString(state));
        if (log.isDebugEnabled()) {
        	String stateStr = this.graphState.toString();
        	log.debug("STATE: " + stateStr);
        }
        
        this.columnKeyGen = new HBaseStatefullColumnKeyFactory(this.rootType,
        		graphState);
		
        // build the graph
    	PlasmaDataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	dataGraph.setId(resultRow.getRow());    	
    	this.root = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);				
		CoreNode rootNode = (CoreNode)this.root;
        
		// add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);

        // need to reconstruct the original graph, so need original UUID
		byte[] rootUuid = resultRow.getValue(Bytes.toBytes(
				this.tableConfig.getDataColumnFamilyName()), 
                Bytes.toBytes(CloudGraphConstants.ROOT_UUID_COLUMN_NAME));
		if (rootUuid == null)
			throw new GraphServiceException("expected column: "
				+ this.tableConfig.getDataColumnFamilyName() + ":"
				+ CloudGraphConstants.ROOT_UUID_COLUMN_NAME);
		String uuidStr = null;
		try {
			uuidStr = new String(rootUuid, HConstants.UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new GraphServiceException(e);
		}
		UUID uuid = UUID.fromString(uuidStr);
		rootNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				uuid);
		this.dataObjects.put(uuid, this.root);
		
		assemble(this.root, null, null, resultRow, uuid);
	}
	
	private void assemble(PlasmaDataObject target, 
			PlasmaDataObject source, PlasmaProperty sourceProperty, 
			Result resultRow,
			UUID uuid)
    {
		// set internal value bypassing SDO API which would
		// detect an undefined property
		CoreNode targetDataNode = (CoreNode)target;
		targetDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				uuid);
        // add concurrency fields
        if (this.snapshotDate != null)
        	targetDataNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);
        
        if (log.isDebugEnabled())
			log.debug("assembling: ("
				+ targetDataNode.getUUIDAsString() + ") "
					+ target.getType().getURI() + "#" 
					+ target.getType().getName());
		
		List<String> names = this.propertyMap.get(target.getType());
		if (log.isDebugEnabled())
			log.debug(target.getType().getName() + " names: " + names.toString());
		
        // data props
		for (String name : names) {
			PlasmaProperty prop = (PlasmaProperty)target.getType().getProperty(name);
			if (!prop.getType().isDataType())
				continue;
			
			//String qualifierStr = columnKeyGen.createColumnKey(
			//	target, prop);
			//byte[] qualifier = Bytes.toBytes(qualifierStr);
			byte[] qualifier = columnKeyGen.createColumnKey(
					target, prop);
			 
			
			if (!resultRow.containsColumn(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				qualifier)) {
				if (log.isDebugEnabled()) {
					String qualifierStr = Bytes.toString(qualifier);
					log.debug("qualifier not found: "
							+ qualifierStr + " - continuing...");
				}
				continue;
			}
			
			KeyValue keyValue = resultRow.getColumnLatest(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				qualifier);
			
			byte[] valueBytes = keyValue.getValue();
			Object value = HBaseDataConverter.INSTANCE.fromBytes(prop, 
					valueBytes);
	        
			if (log.isDebugEnabled())
				log.debug("set: (" + prop.getName() + ") " + String.valueOf(value));

			if (!prop.isReadOnly()) {
			    target.set(prop, value);
			}
			else {
				targetDataNode.setValue(prop.getName(), 
						value);
			}
		}

		// reference props
		for (String name : names) {
			PlasmaProperty prop = (PlasmaProperty)target.getType().getProperty(name);
			if (prop.getType().isDataType())
				continue;
			
			//String qualifierStr = columnKeyGen.createColumnKey(
			//	target, prop);
			//byte[] qualifier = Bytes.toBytes(qualifierStr);
			byte[] qualifier = columnKeyGen.createColumnKey(
					target, prop);
			
			if (!resultRow.containsColumn(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				qualifier))
				continue;
			
			KeyValue keyValue = resultRow.getColumnLatest(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				qualifier);
			
			byte[] valueBytes = keyValue.getValue();
			String stringArray = Bytes.toString(valueBytes);
			Edge[] edges = graphState.parseEdges(prop.getType(), stringArray);
			for (Edge edge : edges) {				
				UUID childUuid = UUID.fromString(edge.getUuid()); 
				
				PlasmaDataObject child = null;
				if ((child = this.dataObjects.get(childUuid)) == null) {
					child = (PlasmaDataObject)target.createDataObject(prop);
					if (log.isDebugEnabled())
						log.debug("traverse: (" + prop.getName() + ") " + String.valueOf(edge.getId()));					
					if (edge.getDirection().ordinal() == TraversalDirection.RIGHT.ordinal())
				        assemble(child, target, prop, resultRow, childUuid);
				}
				else {
					// TODO: 
					log.warn("TODO: create only a child link");
				}
			}
		}
    }	
	
	/**
	 * Returns the assembled data graph.
	 */
	@Override
	public PlasmaDataGraph getDataGraph() {
		return (PlasmaDataGraph)this.root.getDataGraph();
	}

	/**
	 * Resets the assembler.
	 */
	@Override
	public void clear() {
		this.root = null;
		this.dataObjects.clear();
	}
	
}
