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
package org.cloudgraph.state;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.state.RowKey;
import org.cloudgraph.state.StateModel;
import org.cloudgraph.state.TypeEntry;
import org.cloudgraph.state.URI;
import org.cloudgraph.state.UUID;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * State management and mapping implementation class 
 * designed to facilitate: 
 * 1.) Fast XML marshalling and unmarshalling (using JAXB though
 * StAX is a potential replacement candidate)
 * 2.) Minimal data footprint using appropriate data groupings, e.g.
 * types grouped under URI's, to reduce redundancy
 * 3.) Minimal data footprint using mappings, e.g. types mapped to QName hash codes
 * and integral sequence numbers, to reduce redundancy 
 * 4.) Integral sequence number management for all types and data object
 * instances (represented by UUID)
 * 5.) Row key mapping and management   
 * 
 * <p>
 * In general, mappings within the state structure are 
 * designed and included for the purpose of consolidation of 
 * potentially repetitive data which would otherwise be included
 * within cells as column data or as part of composite column
 * qualifiers.  
 * </p>
 * 
 * @see org.cloudgraph.state.StateModel
 * @see org.cloudgraph.state.URI
 * @see org.cloudgraph.state.TypeEntry
 * @see org.cloudgraph.state.UUID
 * @see org.cloudgraph.state.RowKey
 * 
 * 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphState implements State {

    private static Log log = LogFactory.getLog(GraphState.class);
    public static final Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );
	
	/**
     * The name of the table column which stores the UUID for the
     * data object which is the data graph root. 
     */
	public static final String ROOT_UUID_COLUMN_NAME = "__ROOT__";
	
	/**
     * The name bytes of the table column which stores the UUID for the
     * data object which is the data graph root. 
     */
	public static final byte[] ROOT_UUID_COLUMN_NAME_BYTES = ROOT_UUID_COLUMN_NAME.getBytes(charset);
    

	/**
	 * The name of the table column containing the mapped row key state
	 * of a graph.  
	 */
	public static final String STATE_COLUMN_NAME = "__STATE__";
	
	/**
	 * The name bytes of the table column containing the mapped row key state
	 * of a graph.  
	 */
	public static final byte[] STATE_COLUMN_NAME_BYTES = STATE_COLUMN_NAME.getBytes(charset);

	/**
	 * The name of the table column containing the toumbstone
	 * of a graph.  
	 */
	public static final String TOUMBSTONE_COLUMN_NAME = "__TOUMBSTONE__";

	/**
	 * The name of the table column containing the toumbstone
	 * of a graph.  
	 */
	public static final byte[] TOUMBSTONE_COLUMN_NAME_BYTES = TOUMBSTONE_COLUMN_NAME.getBytes(charset);

	private static final String EDGE_RIGHT = "R";
    private static final String EDGE_LEFT = "L";
    private static final String EDGE_DELIM = ":";
    
    private StringBuilder buf = new StringBuilder();
   
    private StateMarshalingContext context;
    
    /** Maps UUID strings to UUID state structures */
    private Map<String, UUID> uuidMap = new HashMap<String, UUID>();
    private Map<String, UUID> uuidHistoryMap = new HashMap<String, UUID>();
    
    /** 
     * Maps qualified type names to list of UUIDs (including 
     * UUID's archived in history) linked to the type 
     */
    private Map<QName, Map<Integer, UUID>> uuidTypeMap = new HashMap<QName, Map<Integer, UUID>>();
    
    /** Maps UUID strings to row Keys */
    private Map<String, RowKey> rowKeyMap;
    private Map<String, RowKey> rowKeyHistoryMap;
    
    /** Maps URI strings to URI state structures */
    private Map<String, URI> uriMap = new HashMap<String, URI>();
    /** Maps qualified names to types */
    private Map<QName, TypeEntry> typeNameMap = new HashMap<QName, TypeEntry>();
    /** Maps sequence identifiers to types */
    private Map<Integer, TypeEntry> typeIdMap = new HashMap<Integer, TypeEntry>();
    
    public GraphState(StateMarshalingContext context) {    	
    	this.context = context;
    }    
     
    public GraphState(String state, StateMarshalingContext context) {
    	 
    	this.context = context;
    	
    	if (log.isDebugEnabled())
    		log.debug("unmarshal raw: " + state);
    	StateModel model = null;
		NonValidatingDataBinding binding = this.context.getBinding();
		try {
			model = (StateModel)binding.unmarshal(state);
		} catch (JAXBException e) {
			throw new StateException(e);
		} 
		finally {
			if (binding != null)
			    this.context.returnBinding(binding);
		}
		
		for (URI uri : model.getURIS()) {
			this.uriMap.put(uri.getUri(), uri);
			for (TypeEntry type : uri.getTypes()) {
				type.setUri(uri.getUri()); // remove before marshalling
				QName qname = new QName(uri.getUri(), type.getName());
				this.typeNameMap.put(qname, type);
				this.typeIdMap.put(type.getId(), type);
			}
		}
		
		for (UUID uuid : model.getUUIDS()) {
			this.uuidMap.put(uuid.getValue(), uuid);
			TypeEntry type = this.typeIdMap.get(uuid.getTypeId());
			
			QName qname = new QName(type.getUri(), type.getName());
			Map<Integer, UUID> uuids = this.uuidTypeMap.get(qname); 
			if (uuids == null) {
				uuids = new HashMap<Integer, UUID>();
				this.uuidTypeMap.put(qname, uuids);
			}
			uuids.put(uuid.getId(), uuid);
		}
		
		History hist = model.getHistory();
    	if (hist != null && hist.getUUIDS() != null && hist.getUUIDS().size() > 0) {
    		this.uuidHistoryMap = new HashMap<String, UUID>();
    		for (UUID uuid : model.getHistory().getUUIDS()) {
    			this.uuidHistoryMap.put(uuid.getValue(), uuid);
    			TypeEntry type = this.typeIdMap.get(uuid.getTypeId());
    			
    			QName qname = new QName(type.getUri(), type.getName());
    			Map<Integer, UUID> uuids = this.uuidTypeMap.get(qname); 
    			if (uuids == null) {
    				uuids = new HashMap<Integer, UUID>();
    				this.uuidTypeMap.put(qname, uuids);
    			}
    			uuids.put(uuid.getId(), uuid);
    		}	    	
    	}		
		
    	if (model.getRowKeies() != null && model.getRowKeies().size() > 0) {
    		this.rowKeyMap = new HashMap<String, RowKey>();
    		for (RowKey entry : model.getRowKeies())
    			this.rowKeyMap.put(entry.getUuid(), entry);
    	}
    	
    	if (model.getHistory() != null) {
	    	if (model.getHistory().getRowKeies() != null && model.getHistory().getRowKeies().size() > 0) {
	    		this.rowKeyHistoryMap = new HashMap<String, RowKey>();
	    		for (RowKey entry : model.getHistory().getRowKeies())
	    			this.rowKeyHistoryMap.put(entry.getUuid(), entry);
	    	}
    	}
	}
        
	public void close() {
	}
	 
	/**
	 * Creates and adds a sequence number mapped to the UUID within the
	 * given data object.  
	 * @param dataObject the data object
	 * @return the new sequence number
	 * @throws IllegalArgumentException if the data object is already mapped
	 */
    public Integer addSequence(DataObject dataObject) {
    	
    	PlasmaType type = (PlasmaType)dataObject.getType();
    	TypeEntry typeEntry = this.typeNameMap.get(type.getQualifiedName());
    	if (typeEntry == null) { // type not mapped
    		typeEntry = new TypeEntry();
    		typeEntry.setName(type.getName());
    		typeEntry.setId(this.typeNameMap.size() + 1);
    		typeEntry.setHashCode(type.getQualifiedNameHashCode());
    		if (log.isDebugEnabled())
    			log.debug("adding type " + type.getQualifiedName() 
    				+ " seq: " + typeEntry.getId() + " hash: "
    				+ typeEntry.getHashCode());
    		this.typeNameMap.put(type.getQualifiedName(), typeEntry);
    		if (this.typeIdMap.get(typeEntry.getId()) == null) {
			    this.typeIdMap.put(typeEntry.getId(), typeEntry);
    		}
    		else {
    			TypeEntry dupEntry = this.typeIdMap.get(typeEntry.getId());
    			throw new StateException("duplicate type sequence found (type: "
    			    + dupEntry.getName() + " seq: " + dupEntry.getId() 
    			    + ") when adding type " + type.getQualifiedName() 
        			+ " seq: " + typeEntry.getId() + " hash: "
        			+ typeEntry.getHashCode());    		 
    		}
    		URI uri = this.uriMap.get(type.getURI());
    		if (uri == null) { // uri not mapped
    			uri = new URI();
    			uri.setUri(type.getURI());
    			this.uriMap.put(type.getURI(), uri);
    		}
    		typeEntry.setUri(uri.getUri());
    		uri.getTypes().add(typeEntry);
    	}
    	
    	Map<Integer, UUID> uuids = this.uuidTypeMap.get(type.getQualifiedName());
    	if (uuids == null) {
    		uuids = new HashMap<Integer, UUID>();
    		this.uuidTypeMap.put(type.getQualifiedName(), uuids);
    	}
    	
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		UUID uid = this.uuidMap.get(uuid);
		if (uid == null) {
			uid = this.uuidHistoryMap.get(uuid);
			if (uid == null) {
				uid = new UUID();
				uid.setValue(uuid);
				uid.setTypeId(typeEntry.getId());
				uid.setId(uuids.size() + 1);
				this.uuidMap.put(uuid, uid);
				uuids.put(uid.getId(), uid);
				return uid.getId();
			}
			else { // return it from hist to current
				this.uuidHistoryMap.remove(uuid);
				this.uuidMap.put(uuid, uid);
				return uid.getId();
			}
		}
		else
		    throw new IllegalArgumentException("found existing mapping for UUID " 
				+ uuid);
    }
    
    /**
	 * Removes the sequence number mapped to the UUID within the
	 * given data object. If no existing sequence exists
	 * for the given data object, a warning is logged and
	 * null is returned. 
     * @param dataObject the data object
     * @return the removed sequence if exists
     */
    public Integer archiveSequence(DataObject dataObject) {
    	Integer result = null;

    	PlasmaType type = (PlasmaType)dataObject.getType();
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		UUID uid = this.uuidMap.get(uuid);
    	if (uid == null) {
    		log.warn("could not remove UUID - no existing mapping found for UUID " 
    				+ uuid + " - ignoring");
            return result;    		
    	}
    	else {
    		this.uuidMap.remove(uuid);
    		this.uuidHistoryMap.put(uuid, uid);    			
    		result = uid.getId();
    	}
    	
    	/*
    	Map<Integer, UUID> uuids = this.uuidTypeMap.get(type.getQualifiedName());
    	if (uuids == null) {
    		throw new StateException("expected UUID for type, "
    			+ type.getQualifiedName());
    	}
    	if (uuids.remove(uid.getId()) == null)
    		throw new StateException("cloud not remove UUID for type, "
        			+ type.getQualifiedName());
    	*/
		
    	// no more UUIDs mapped for type, remove the type
    	/*
    	if (uuids.size() == 0) {
    		this.uuidTypeMap.remove(type.getQualifiedName());
    		
    		TypeEntry typeEntry = this.typeNameMap.remove(type.getQualifiedName());    		
    		if (typeEntry == null)
    		    throw new StateException("cloud not remove type "
        			+ type.getQualifiedName() + " from type mapping");
    		typeEntry = this.typeIdMap.remove(typeEntry.getId());
    		if (typeEntry == null)
		        throw new StateException("cloud not remove type "
        			+ type.getQualifiedName() + " from type-id mapping");
    		
    		URI uri = this.uriMap.get(type.getURI());
    		if (!uri.getTypes().remove(typeEntry))
        		throw new StateException("cloud not remove type "
            			+ type.getQualifiedName() + " from URI mapping");
    		if (log.isDebugEnabled())
    			log.debug("removed type " + type.getQualifiedName() 
    				+ " seq: " + typeEntry.getId() + " hash: "
    				+ typeEntry.getHashCode());
    	    // no mode types mapped for URI
    		if (uri.getTypes().size() == 0)
    	    	this.uriMap.remove(type.getURI());
    	}
    	*/
    	
		return result;
    }
     
	/**
	 * Returns an existing sequence number for the given 
	 * data object, or null if none exists. 
	 * Each sequence number generated is
     * a simply the number of existing elements for a particular
     * SDO type (plus 1).
	 * @return the existing sequence or null if not exists for the given 
	 * data object
	 */
	public Integer findSequence(DataObject dataObject) {	
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		UUID uid = this.uuidMap.get(uuid);
    	if (uid != null) {
    		return uid.getId();
    	}
    	else
    		return null;
	}
	
	/**
	 * Returns an existing sequence number for the given 
	 * data object, or null if none exists. 
	 * Each sequence number generated is
     * a simply the number of existing elements for a particular
     * SDO type (plus 1).
	 * @return the existing sequence or null if not exists for the given 
	 * data object
	 * @throws IllegalArgumentException if the given data object UUID is not
	 * already mapped
	 */
	public Integer getSequence(DataObject dataObject) {	
		Integer result = findSequence(dataObject);
		if (result != null) {
			return result;
		}
		else {
			String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		    throw new IllegalArgumentException("no sequence mapped to type "
				+ dataObject.getType().getURI() + "#" + dataObject.getType().getName() 
				+ " for the given UUID, "
				+ String.valueOf(uuid));
		}
	}
		
	/**
	 * Returns an existing UUID for the given 
	 * sequence number, or null if none exists. 
	 * @return the existing UUID or null if not exists for the given 
	 * sequence.
	 */
	public String findUUID(Type type, Integer sequence) {		
    	PlasmaType plasmaType = (PlasmaType)type;
    	Map<Integer, UUID> uuids = this.uuidTypeMap.get(plasmaType.getQualifiedName());
		if (uuids != null) {
		    UUID uuid = uuids.get(sequence);
		    if (uuid != null)
			    return uuid.getValue();
		}
    	return null;
	}	
	
	/**
	 * Returns an existing UUID for the given 
	 * sequence number, or null if none exists. 
	 * @return the existing UUID or null if not exists for the given 
	 * sequence.
	 * @param type the SDO type
	 * @param sequence the sequence number
	 * @return the UUID mapped to the given sequence
	 * @throws IllegalArgumentException if the given sequence is not
	 * already mapped
	 */
	public String getUUID(Type type, Integer sequence) {
		String result = findUUID(type, sequence);
		if (result != null)
		    return result;
		throw new IllegalArgumentException("no UUID mapped for the given sequence ("
				+ String.valueOf(sequence) + ") for given type, "
				+ type.getURI() + "#" + type.getName());
	}
		
	/**
	 * Returns an existing mapped row key for the given data object, 
	 * or null if none exists. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object, 
	 * or null if none exists. 
	 */
	public byte[] findRowKey(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return findRowKey(uuid);
	}
	
	/**
	 * Returns an existing mapped row key for the given data object uuid, 
	 * or null if none exists. 
	 * @param uuid the data object uuid
	 * @return an existing mapped row key for the given data object uuid, 
	 * or null if none exists. 
	 */
	public byte[] findRowKey(String uuid) {
		if (this.rowKeyMap != null) {
			RowKey key = this.rowKeyMap.get(uuid);
			if (key != null)
				return key.getValue().getBytes(this.charset);
		}
		return null;
	}	

	/**
	 * Returns the table name for an existing mapped row key 
	 * for the given data object uuid. 
	 * @param uuid the data object uuid
	 * @return an existing mapped row key for the given data object, 
	 * or null if none exists. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object uuid
	 */
	public String getRowKeyTable(String uuid) {
		if (this.rowKeyMap != null) {
			RowKey key = this.rowKeyMap.get(uuid);
			if (key != null)
				return key.getTable();
		}
	    throw new IllegalArgumentException("no row key mapped for the given UUID ("
			+ String.valueOf(uuid) + ")");
	}
	
	/**
	 * Returns an existing mapped row key for the given data object. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object 
	 */
	public byte[] getRowKey(DataObject dataObject) {
		byte[] result = findRowKey(dataObject);
		if (result != null)
			return result;
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		throw new IllegalArgumentException("no row key mapped for the given UUID ("
				+ String.valueOf(uuid) + ") for given type, "
				+ dataObject.getType().getURI() + "#" + dataObject.getType().getName());
	}
	
	/**
	 * Returns an existing mapped row key for the given data object UUID. 
	 * @param uuid the data object uuid
	 * @return an existing mapped row key for the given data object uuid. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object UUID, the UUID is null or the incorrect length
	 */
	public byte[] getRowKey(String uuid) {
		byte[] result = findRowKey(uuid);
		if (result != null) {
			if (log.isDebugEnabled())
				log.debug("returning row-key: " 
			        + uuid + "->" + new String(result, this.charset));
			return result;			
		}
		throw new IllegalArgumentException("no row key mapped for the given UUID ("
				+ String.valueOf(uuid) + ")");
	}	
	
	/**
	 * Creates a new mapping for the given row key and
	 * data object.
	 * @param dataObject the data object
	 * @param key the row key;
	 */
	public void addRowKey(DataObject dataObject, TableConfig table, byte[] key) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		if (log.isDebugEnabled())
			log.debug("adding row-key: " 
		        + uuid + "->" + new String(key, this.charset));
		if (this.rowKeyMap == null) {
			this.rowKeyMap = new HashMap<String, RowKey>();
		}
		
		RowKey rowKey = this.rowKeyMap.get(uuid);
		if (rowKey == null) {
			rowKey = new RowKey();
			rowKey.setUuid(uuid);
			rowKey.setTable(table.getName());
			rowKey.setValue(new String(key, this.charset));
			this.rowKeyMap.put(uuid, rowKey);
		}
	}

	/**
	 * Removes an existing mapping for the given data object.
	 * @param dataObject the data object
	 * @param key the row key
	 * @throws IllegalArgumentException if no row key is mapped for the UUID associated with the given
	 * data object.  
	 */
	public void archiveRowKey(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		RowKey rowKey = this.rowKeyMap.remove(uuid);
		if (rowKey == null) {
			throw new IllegalArgumentException("could not remove key - no row key mapped to UUID, "
					+ uuid);
		}
		else {
			if (this.rowKeyHistoryMap == null)
				this.rowKeyHistoryMap = new HashMap<String, RowKey>();
		    if (this.rowKeyHistoryMap.get(rowKey.getUuid()) == null)
		    	this.rowKeyHistoryMap.put(rowKey.getUuid(), rowKey);
			if (log.isDebugEnabled())
			    log.debug("removed row-key: " 
		            + uuid + "->" + rowKey.getValue());
		}
	}
	
	/**
	 * Returns a count of the current row keys
	 * @return a count of the current row keys
	 */	
	public int getRowKeyCount() {
		if (this.rowKeyMap != null)
	        return this.rowKeyMap.size();
		return 0;
	}

	public void addEdges(PlasmaNode dataNode, 
			List<PlasmaEdge> edges) {
		if (edges != null) {
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();
	    		if (findSequence(opposite) == null)	    		
	    		    addSequence(opposite);
			}
		}
    }
    
    /**
     * Returns a formatted string representation for the graph 
     * edge(s) found linked from the given data object.
     * @param dataNode the source data node
     * @param edges the edges
     * @return a formatted string representation for the graph 
     * edge(s) found linked from the given data object.
     */
	public String marshalEdges(PlasmaNode dataNode, List<PlasmaEdge> edges) {
		String[] result = new String[0];
		if (edges != null) {
			result = new String[edges.size()];
			int i = 0;
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();	    		
	    		result[i] = marshalEdge(edge, opposite);
	         	i++;
			}
		}
		//NOTE: returns '[]' for zero length array
		// use Arrays formatting
		return Arrays.toString(result);
    }
	
	public String marshalEdges(Edge[] edges) {
		String[] result = new String[0];
		if (edges != null) {
			result = new String[edges.length];
			int i = 0;
			for (Edge edge : edges) {
	    		result[i] = marshalEdge(edge);
	         	i++;
			}
		}
		//NOTE: returns '[]' for zero length array
		// use Arrays formatting
		return Arrays.toString(result);
    }
	
	public Edge[] unmarshalEdges(byte[] data) {
		String edges = new String(data, this.charset);
		return unmarshalEdges(edges);
	}
	
	public Edge[] unmarshalEdges(String data) {
		// replace Arrays formatting and whitespace, then split
		String[] array = data.replaceAll("[\\[\\]\\s]", "").split(",");
		if (array.length == 1 && array[0].length() == 0)
			return new Edge[0];
		
		Edge[] result = new Edge[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Edge(array[i]);
		}		
		return result;
	}

    private String marshalEdge(PlasmaEdge edge, DataObject opposite)
    {
    	PlasmaType type = (PlasmaType)opposite.getType();    	
    	TypeEntry typeEntry = this.typeNameMap.get(type.getQualifiedName());    	
    	Integer typeSeq = typeEntry.getId();
    	Integer seq = this.getSequence(opposite);    	    	
    	//String dir = formatDirection(edge.getDirection());
    	
    	this.buf.setLength(0);        	
        //this.buf.append(dir);
        //this.buf.append(EDGE_DELIM);
        this.buf.append(String.valueOf(typeSeq));        	
        this.buf.append(EDGE_DELIM);
        this.buf.append(String.valueOf(seq));        	
    	return this.buf.toString();   	
    }
    
    private String marshalEdge(Edge edge)
    {
    	this.buf.setLength(0);        	
        this.buf.append(String.valueOf(edge.getTypeId()));        	
        this.buf.append(EDGE_DELIM);
        this.buf.append(String.valueOf(edge.getId()));        	
    	return this.buf.toString();   	
    }
    
    /**
     * Returns edge structures for the given data edges
     * @param dataNode the source data node
     * @param edges the edges
     * @return edge structures for the given data edges
     */
	public Edge[] createEdges(PlasmaNode dataNode, List<PlasmaEdge> edges) {
		Edge[] result = new Edge[0];
		if (edges != null) {
			result = new Edge[edges.size()];
			int i = 0;
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();	    		
	    		result[i] = createEdge(edge, opposite);
	         	i++;
			}
		}
		return result;
    }
	
    /**
     * Returns edge structures for the given data objects
     * @param edges the edges
     * @return edge structures for the given data edges
     */
	public Edge[] createEdges(List<DataObject> dataObjects) {
		Edge[] result = new Edge[0];
		if (dataObjects != null) {
			result = new Edge[dataObjects.size()];
			int i = 0;
			for (DataObject dataObject : dataObjects) {
	    		result[i] = createEdge((PlasmaDataObject)dataObject);
	         	i++;
			}
		}
		return result;
    }
    
    private Edge createEdge(PlasmaEdge edge, DataObject opposite)
    {
    	PlasmaType type = (PlasmaType)opposite.getType();    	
    	TypeEntry typeEntry = this.typeNameMap.get(type.getQualifiedName());    	
		if (typeEntry == null)	
    	    throw new StateException("no type mapped for, "
    	    	+ type.getQualifiedName()
    	    	+ " (" + String.valueOf(type.getQualifiedName().hashCode())+ ")");
    	Integer typeSeq = typeEntry.getId();
		String uuid = ((PlasmaDataObject)opposite).getUUIDAsString();
    	Integer seq = this.getSequence(opposite);  
    	Edge result = new Edge(type, 
    		typeSeq, uuid, seq);
    	return result;   	
    } 
    
    private Edge createEdge(DataObject opposite)
    {
    	PlasmaType type = (PlasmaType)opposite.getType();    	
    	TypeEntry typeEntry = this.typeNameMap.get(type.getQualifiedName());    	
		if (typeEntry == null)	
    	    throw new StateException("no type mapped for, "
    	    	+ type.getQualifiedName()
    	    	+ " (" + String.valueOf(type.getQualifiedName().hashCode())+ ")");
    	Integer typeSeq = typeEntry.getId();
		String uuid = ((PlasmaDataObject)opposite).getUUIDAsString();
    	Integer seq = this.getSequence(opposite);  
    	Edge result = new Edge(type, 
    		typeSeq, uuid, seq);
    	return result;   	
    }    
        
    public String marshal() {
    	return marshal(false);
    }
    
    public String marshal(boolean formatted) {
    	NonValidatingDataBinding binding = null;
    	String xml = "";
		try {
			StateModel model = new StateModel();
	    	if (this.rowKeyMap != null) {
	    		for (Entry<String, RowKey> entry : this.rowKeyMap.entrySet())
	    			model.getRowKeies().add(entry.getValue());
	    	}			
	    	if (this.rowKeyHistoryMap != null) {
	    		if (model.getHistory() == null) 	    			
	    		    model.setHistory(new History());
	 	    	for (Entry<String, RowKey> entry : this.rowKeyHistoryMap.entrySet())
	    			model.getHistory().getRowKeies().add(entry.getValue());
	    	}			
	    	for (UUID uuid : this.uuidMap.values()) {
	    		model.getUUIDS().add(uuid);
	    	}
	    	if (this.uuidHistoryMap != null) {
	    		if (model.getHistory() == null) 	    			
	    		    model.setHistory(new History());
	 	    	for (Entry<String, UUID> entry : this.uuidHistoryMap.entrySet())
	    			model.getHistory().getUUIDS().add(entry.getValue());
	    	}			
	    	
	    	for (URI uri : this.uriMap.values()) {
	    		model.getURIS().add(uri);
	    	}
	    	
	    	// null out URI on individual types as uri
	    	// found on URI container
	    	for (TypeEntry entry : this.typeNameMap.values())
	    		entry.setUri(null);
	    	
			binding = this.context.getBinding();
			xml = binding.marshal(model); // no formatting
		} catch (JAXBException e1) {
			throw new StateException(e1);
		} 
		finally {
			if (binding != null)
			    this.context.returnBinding(binding);			
		}

    	return xml;
    }
   
    /**
     * Simple immutable structure to contain and
     * manage the parse result for a single edge.  
     */ 
    public class Edge {
    	private PlasmaType type;
    	private Integer typeId;
    	private String uuid;
    	private Integer id;
    	private int hashCode;
    	
    	@SuppressWarnings("unused")
		private Edge() {}
    	
    	public Edge(PlasmaType type, Integer typeId, String uuid, Integer id) {
			super();
			this.type = type;
			this.typeId = typeId;
			this.uuid = uuid;
			this.id = id;
		}

		/**
    	 * COnstructor which parses the given marshalled raw edge data into
    	 * an edge structure. 
    	 * @param data
    	 */
    	public Edge(String data) {
    		this.type = (PlasmaType)type; // FIXME WTF?
    	    String[] tokens = data.split(EDGE_DELIM);
    	    
    	    this.typeId = Integer.valueOf(tokens[0]);
    	    
    	    TypeEntry typeEntry = typeIdMap.get(this.typeId);
    	    
    		this.type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(typeEntry.getUri(), 
    				typeEntry.getName());
    	    
    	    this.id = Integer.valueOf(tokens[1]);    	        	    
    	    this.uuid = getUUID(this.type, this.id);
    	}
    	
    	public boolean equals(Edge other) {
    		return this.hashCode() == other.hashCode();
    	}
    		
    	public int hashCode() {
    		if (this.hashCode == 0)
    			this.hashCode = this.typeId.intValue() ^ this.id.intValue();
    		return this.hashCode;
    	}
    	
		public PlasmaType getType() {
			return type;
		}		
		
		public Integer getTypeId() {
			return typeId;
		}
		
		/**
		 * Returns the UUID string for the target data object
		 * for this edge. 
		 * @return the UUID string for the target data object
		 * for this edge.
		 */ 
        public String getUuid() {
			return uuid;
		}
 		
		/**
		 * Returns the sequence id for the target data object
		 * for this edge. 
		 * @return the sequence id for the target data object
		 * for this edge.
		 */ 
        public Integer getId() {
			return id;
		}
    }
 
}
