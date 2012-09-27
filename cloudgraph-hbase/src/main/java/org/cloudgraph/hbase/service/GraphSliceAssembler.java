package org.cloudgraph.hbase.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.hbase.service.ColumnMap;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.GraphState;
import org.cloudgraph.common.service.GraphState.Edge;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.filter.BinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateColumnFilterAssembler;
import org.cloudgraph.hbase.filter.StatefullBinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.cloudgraph.hbase.util.FilterUtil;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.model.Where;
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
 * {@link HBaseGraphAssembler#assemble(Result resultRow)} method which
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
 */
public class GraphSliceAssembler
    implements HBaseGraphAssembler {

    private static Log log = LogFactory.getLog(GraphSliceAssembler.class);
	private PlasmaType rootType;
	private PlasmaDataObject root;
	private PropertySelectionCollector collector;
	private Map<Type, List<String>> propertyMap;
	private Map<commonj.sdo.Property, Where> predicateMap; 
	private Timestamp snapshotDate;
	private GraphState graphState;		
	private GraphStatefullColumnKeyFactory columnKeyFac;
	private Map<UUID, PlasmaDataObject> dataObjects = new HashMap<UUID, PlasmaDataObject>();
	private TableConfig tableConfig;
	private HTableInterface con;
	private byte[] rowKey;
	private ColumnMap columnMap;
	private int scanCount = 1; 
	private PredicateColumnFilterAssembler graphSliceColumnFilterAssembler;
	private BinaryPrefixColumnFilterAssembler multiColumnPrefixFilterAssembler;
	private StatefullBinaryPrefixColumnFilterAssembler multiColumnStatefullPrefixFilterAssembler;
	
	@SuppressWarnings("unused")
	private GraphSliceAssembler() {}
	
	/**
	 * Constructor.
	 * @param rootType the SDO root type for the result data graph
	 * @param collector a collection of selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
	 * @param snapshotDate the query snapshot date which is populated
	 * into every data object in the result data graph. 
	 * @param con the HBase table client interface
	 */
	public GraphSliceAssembler(PlasmaType rootType,
			PropertySelectionCollector collector, 
			Timestamp snapshotDate,
			TableConfig tableConfig,
			HTableInterface con) {
		this.rootType = rootType;
		this.collector = collector;
		this.snapshotDate = snapshotDate;
		this.tableConfig =tableConfig;
		this.con = con;
		

	}
	
	/**
     * Re-constitutes a data graph from the given HBase client
     * result (row). 
	 * @param resultRow the HBase client
     * result (row).
	 */
	public void assemble(Result resultRow) {
		
		this.rowKey = resultRow.getRow();
		if (this.columnMap == null)
			this.columnMap = new ColumnMap(resultRow);
		
		this.propertyMap = this.collector.getResult();
		this.predicateMap = this.collector.getPredicateMap();
		
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
        
        this.columnKeyFac = new StatefullColumnKeyFactory(this.rootType,
        		graphState);
		this.graphSliceColumnFilterAssembler = 
            	new PredicateColumnFilterAssembler( 
            		this.graphState,  
        			this.rootType);
		this.multiColumnPrefixFilterAssembler = 
				new BinaryPrefixColumnFilterAssembler(this.rootType);
		this.multiColumnStatefullPrefixFilterAssembler = 
				new StatefullBinaryPrefixColumnFilterAssembler( 
				this.graphState, this.rootType);
		
        // build the graph
    	PlasmaDataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	dataGraph.setId(resultRow.getRow());    	
    	this.root = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);				
		CoreNode rootNode = (CoreNode)this.root;
        
		// add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);

        // need to reconstruct the original graph, so need original UUID
		byte[] rootUuid = resultRow.getValue(Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
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
		
		assemble(this.root, null, null, uuid);
	}
	
	private void assemble(PlasmaDataObject target, 
			PlasmaDataObject source, PlasmaProperty sourceProperty, 
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
			
			byte[] qualifier = columnKeyFac.createColumnKey(
					target, prop);
			 			
			if (!this.columnMap.containsColumn(
				this.tableConfig.getDataColumnFamilyNameBytes(), 
				qualifier)) {
				if (log.isDebugEnabled()) {
					String qualifierStr = Bytes.toString(qualifier);
					log.debug("data column qualifier not found: "
							+ qualifierStr + " - continuing...");
				}
				continue;
			}
			
			byte[] valueBytes = this.columnMap.getColumnValue(
				Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
				qualifier);
			
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
			PlasmaType propertyType = (PlasmaType)prop.getType();
			byte[] qualifier = columnKeyFac.createColumnKey(
					target, prop);
			
			// NOTE: can we have predicates on singular props? 
			Where where = this.predicateMap.get(prop);
			List<Long> sequences = null;
			if (prop.isMany() && where != null) {
		    	if (log.isDebugEnabled())
					log.debug("fetching path predicate results for ("
						+ target.getUUIDAsString() + ") "
						+ target.getType().getURI() + "#" 
						+ target.getType().getName() + "."
						+ prop.getName());
		    	sequences = fetch((PlasmaType)prop.getType(), 
		    			where, this.rowKey);
				List<String> childPropertyNames = this.propertyMap.get(prop.getType());
		    	load(sequences, childPropertyNames,
		    			propertyType, this.rowKey);
			}
			else {
		    	if (log.isDebugEnabled())
					log.debug("fetching path results for ("
						+ target.getUUIDAsString() + ") "
						+ target.getType().getURI() + "#" 
						+ target.getType().getName() + "."
						+ prop.getName());
			    List<String> childPropertyNames = this.propertyMap.get(prop.getType());
		    	load(childPropertyNames,
		    			propertyType, this.rowKey);
			}			
			
			if (!this.columnMap.containsColumn(
				Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
				qualifier)) {
				if (log.isDebugEnabled()) {
					String qualifierStr = Bytes.toString(qualifier);
					log.debug("reference column qualifier not found: "
							+ qualifierStr + " - continuing...");
				}
				continue; // nothing more to do at this point
			}
			
			byte[] valueBytes = this.columnMap.getColumnValue(
					Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
					qualifier);
			String stringArray = Bytes.toString(valueBytes);
			Edge[] edges = graphState.parseEdges(prop.getType(), stringArray);
			for (Edge edge : edges) {	
				if (sequences != null && !found(edge, sequences))
					continue;
				
				UUID childUuid = UUID.fromString(edge.getUuid()); 
				
				PlasmaDataObject child = null;
				if ((child = this.dataObjects.get(childUuid)) == null) {
					child = (PlasmaDataObject)target.createDataObject(prop);
					if (log.isDebugEnabled())
						log.debug("traverse: (" + prop.getName() + ") " + String.valueOf(edge.getId()));					
					if (edge.getDirection().ordinal() == TraversalDirection.RIGHT.ordinal())
				        assemble(child, target, prop, childUuid);
				}
				else {
					// TODO: 
					log.warn("TODO: create only a child link");
				}
			}
		}
    }	
	
	private boolean found(Edge edge, List<Long> sequences) {
		for (Long seq : sequences)
			if (seq.longValue() == edge.getId().longValue())
				return true;
		return false;
	}
	
	private void load(List<String> propertyNames,
			PlasmaType contextType, byte[] rowKey)
	{
        Scan scan = new Scan();
        scan.setStartRow(rowKey);
        scan.setStopRow(rowKey);
        this.multiColumnPrefixFilterAssembler.clear();
        this.multiColumnPrefixFilterAssembler.assemble(propertyNames,
        		contextType);
        Filter filter = this.multiColumnPrefixFilterAssembler.getFilter();
        scan.setFilter(filter);
        load(scan);
	}

	private void load(List<Long> sequences, List<String> propertyNames,
			PlasmaType contextType, byte[] rowKey)
	{
        Scan scan = new Scan();
        scan.setStartRow(rowKey);
        scan.setStopRow(rowKey);
        this.multiColumnStatefullPrefixFilterAssembler.clear();
        this.multiColumnStatefullPrefixFilterAssembler.assemble(propertyNames, sequences, contextType);
        Filter filter = this.multiColumnStatefullPrefixFilterAssembler.getFilter();
        scan.setFilter(filter);
        load(scan);
	}

	private void load(Scan scan)
	{        
        if (log.isDebugEnabled() )
			try {
				log.debug("scan filter: " 
			    + FilterUtil.printFilterTree(scan.getFilter()));
			} catch (IOException e1) {
			}		
        
        if (log.isDebugEnabled())
        	log.debug("executing scan...");
        ResultScanner scanner = null;
		try {
			scanner = con.getScanner(scan);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}
        for (Result row : scanner) {
        	if (log.isDebugEnabled()) 
      	        log.debug("row: " + new String(row.getRow()));              	  
            if (log.isDebugEnabled())
            	log.debug("returned " + row.size() + " columns");
      	    for (KeyValue keyValue : row.list()) {
      	    	this.columnMap.addColumn(keyValue);
      	    	if (log.isDebugEnabled()) {
          	    	String qual = Bytes.toString(keyValue.getQualifier());
      	    	    log.debug("\tkey: " + qual
      	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
      	    	}
      	    }
        }		
        this.scanCount++;
	}	
	
	private List<Long> fetch(PlasmaDataObject contextDataObject,
			PlasmaProperty contextProp, 
			byte[] rowKey) {
        Scan scan = new Scan();
        scan.setStartRow(rowKey);
        scan.setStopRow(rowKey);
	    byte[] key = this.columnKeyFac.createColumnKey( 
    	    contextDataObject, contextProp);
        QualifierFilter qualFilter = new QualifierFilter(
            CompareFilter.CompareOp.EQUAL,
            new BinaryPrefixComparator(key)); 
        scan.setFilter(qualFilter);
        return fetch(scan);
	}
	
	/**
	 * Creates a column filter hierarchy based on the given path
	 * predicate for a single row specified by the given row key. 
	 * @param contextType
	 * @param where the predicate
	 * @param rowKey the row key
	 * @return a collection of sequence ids
	 */
	private List<Long> fetch(PlasmaType contextType,
			Where where, byte[] rowKey) {
        Scan scan = new Scan();
        scan.setStartRow(rowKey);
        scan.setStopRow(rowKey);
        this.graphSliceColumnFilterAssembler.clear();
        this.graphSliceColumnFilterAssembler.assemble(where, contextType);
        Filter filter = this.graphSliceColumnFilterAssembler.getFilter();
        scan.setFilter(filter);
        return fetch(scan);
	}
	
	private List<Long> fetch(Scan scan)
	{
        if (log.isDebugEnabled() )
			try {
				log.debug("scan filter: " + FilterUtil.printFilterTree(scan.getFilter()));
			} catch (IOException e1) {
			}
        
        if (log.isDebugEnabled())
        	log.debug("executing scan...");
        ResultScanner scanner = null;
		try {
			scanner = con.getScanner(scan);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}
				 
		DataGraphConfig graphConf = CloudGraphConfig.getInstance().getDataGraph(
				this.rootType.getQualifiedName());
		String delim = graphConf.getColumnKeySectionDelimiter();
				
		List<Long> result = new ArrayList<Long>();
        for (Result row : scanner) {
        	if (log.isDebugEnabled()) 
      	        log.debug("row: " + new String(row.getRow()));              	  
      	    for (KeyValue keyValue : row.list()) {
                if (log.isDebugEnabled())
                	log.debug("returned " + row.size() + " columns");
      	    	// FIXME: no parsing here !!
                String qual = Bytes.toString(keyValue.getQualifier());
      	    	if (log.isDebugEnabled()) 
      	    	    log.debug("\tkey: " + qual
      	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
      	        String[] sections = qual.split(delim);
      	        Long seq = Long.valueOf(sections[1]);
      	    	result.add(seq);
      	    }
        }
        this.scanCount++;
        return result;
	}	
	
	public int getScanCount() {
		return scanCount;
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
		if (this.dataObjects != null)
		    this.dataObjects.clear();
		if (this.columnMap != null)
		    this.columnMap.clear();
		this.scanCount = 1;
	}
	
}
