package org.cloudgraph.state;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Manages a minimal set of "state" information persisted with each 
 * data graph in order to reduce overall storage space. 
 * In general mappings from various space intensive properties 
 * required for graph management, such as UUIDs, are mapped 
 * to integral values and the mapping stored in specific 
 * graph management columns within each row. Federation across
 * tables is enabled by mapping any UUID's to row keys for any references
 * to external tables.  
 * 
 * <p>
 * Sequence Numbers. Each sequence number is unique to a Type within
 * the context of a single data graph. Each new sequence number 
 * generated is simply the number of existing elements 
 * already mapped for a particular SDO Type (plus 1).  
 * </p>
 * 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphState implements State {

    private static Log log = LogFactory.getLog(GraphState.class);
	
	/**
	 * The name of the table column containing the mapped UUID state
	 * of a graph.  
	 */
	public static final String UUID_MAP_COLUMN_NAME = "__UUIDMAP__";

	/**
	 * The name of the table column containing the mapped row key state
	 * of a graph.  
	 */
	public static final String KEY_MAP_COLUMN_NAME = "__KEYMAP__";
    
	/**
     * The name of the table column which stores the UUID for the
     * data object which is the data graph root. 
     */
	public static final String ROOT_UUID_COLUMN_NAME = "__ROOT__";
    
    private static final String EDGE_RIGHT = "R";
    private static final String EDGE_LEFT = "L";
    private static final String EDGE_DELIM = ":";

    // Warning these are used in regex expressions (so avoid e.g. '|')
    private static final String MAP_DELIM_TYPES = ";";
    private static final String MAP_DELIM_URI = "@";
    private static final String MAP_DELIM_UUID = ",";
    private static final String MAP_DELIM_ID = ":";
    private static final String MAP_DELIM_KEY = "@";

    /** maps types to UUID/sequence number sub-maps */
    private Map<Type, Map<String, Long>> seqMap;
    /** maps types to sequence number/UUID sub-maps */
    private Map<Type, Map<Long, String>> uuidMap;
    /** maps UUIDs to row keys */
    private Map<String, byte[]> keyMap;
    
    private StringBuilder buf = new StringBuilder();
    private Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );
   
    public GraphState() {
		this.seqMap = new HashMap<Type, Map<String, Long>>();
		this.uuidMap = new HashMap<Type, Map<Long, String>>();
		this.keyMap = new HashMap<String, byte[]>();
    }
    
    public GraphState(String state) {
    	this();
		
		String[] typeArray = state.split(GraphState.MAP_DELIM_TYPES);
		for (String typeToken : typeArray) {
			String[] typePairArray = typeToken.split(GraphState.MAP_DELIM_URI);
			String qualifiedType = typePairArray[0];
			String[] typeTokens = qualifiedType.split("#");
			String[] pairs = typePairArray[1].split(GraphState.MAP_DELIM_UUID); 	
			Type type = PlasmaTypeHelper.INSTANCE.getType(typeTokens[0], typeTokens[1]);
			Map<String, Long> seqSubMap = new HashMap<String, Long>();
			this.seqMap.put(type, seqSubMap);
			Map<Long, String> uuidSubMap = new HashMap<Long, String>();
			this.uuidMap.put(type, uuidSubMap);
			for (String token : pairs) {
				String[] pair =  token.split(GraphState.MAP_DELIM_ID);
				Long id = new Long(pair[1]);
				String uuid = pair[0];
				if (uuid == null || uuid.length() == 0)
					throw new IllegalArgumentException("found null or zero length UUID while parsing uuid map");
				if (uuid.length() != 36)
					throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID while parsing uuid map");
				seqSubMap.put(pair[0], id);
				uuidSubMap.put(id, pair[0]);
			}
		}
	}
    
    public GraphState(String uuidSequenceMap, String uuidRowKeyMap) {
    	this(uuidSequenceMap);
		String[] uuidArray = uuidRowKeyMap.split(GraphState.MAP_DELIM_UUID);
		for (String token : uuidArray) {
			String[] pairArray = token.split(GraphState.MAP_DELIM_KEY);
			String uuid = pairArray[0];
			if (uuid == null || uuid.length() == 0)
				throw new IllegalArgumentException("found null or zero length UUID while parsing row-key map");
			if (uuid.length() != 36)
				throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID while parsing row-key map");
			byte[] rowKey = pairArray[1].getBytes(this.charset);
			this.keyMap.put(uuid, rowKey);
		}
    }
    
	public void close() {
		if (seqMap != null)
			seqMap.clear();
	}
	 
	/**
	 * Adds a the given number mapped to the UUID within the
	 * given data object. If an existing sequence exists
	 * for the given data object, a warning is logged. 
	 * @param dataObject the data object
	 * @param value the sequence value
	 */
    public void addSequence(DataObject dataObject, Long value) {
    	Map<String, Long> subMap = this.seqMap.get(dataObject.getType());
		if (subMap == null) {
			subMap = new HashMap<String, Long>();
			this.seqMap.put(dataObject.getType(), subMap);
		}
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		Long existing = subMap.get(uuid);
		if (existing != null)
			log.warn("found existing mapping for UUID " 
					+ uuid + " - overwriting");
		subMap.put(uuid, value);
    }
    
    /**
	 * Removes the sequence number mapped to the UUID within the
	 * given data object. If an existing sequence exists
	 * for the given data object, a warning is logged. 
     * @param dataObject
     * @return the removed sequence if exists
     */
    public Long removeSequence(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	Map<String, Long> seqSubmap = this.seqMap.get(dataObject.getType());
		Map<Long, String> uuidSubMap = this.uuidMap.get(dataObject.getType());
    	if (seqSubmap != null) {
    		Long existing = seqSubmap.remove(uuid);
    		if (existing != null) {
    			uuidSubMap.remove(existing);
    	        return existing;
    		}
    	}
		log.warn("no existing sequence mapping found for UUID " 
				+ uuid + " - ignoring");
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
	 */
	public Long findSequence(DataObject dataObject) {	
		Map<String, Long> subMap = this.seqMap.get(dataObject.getType());
		if (subMap == null) 
			return null;
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return subMap.get(uuid);
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
	public Long getSequence(DataObject dataObject) {	
		Map<String, Long> subMap = this.seqMap.get(dataObject.getType());
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (subMap != null) {
			Long result = subMap.get(uuid);
			if (result != null)
				return result;
		}
		throw new IllegalArgumentException("no sequence mapped for the given UUID, "
				+ String.valueOf(uuid));
	}

	/**
	 * Returns an existing or creates/adds and returns a new sequence  
	 * number for the given data object. Each sequence number generated is
     * a simply the number of existing elements for a particular
     * SDO type (plus 1).
	 * @param dataObject the data object
	 * @return the new or existing sequence
	 */
	public Long createSequence(DataObject dataObject) {
	
		Map<String, Long> seqSubMap = this.seqMap.get(dataObject.getType());
		Map<Long, String> uuidSubMap = this.uuidMap.get(dataObject.getType());
		if (seqSubMap == null) {
			seqSubMap = new HashMap<String, Long>();
			this.seqMap.put(dataObject.getType(), seqSubMap);
			uuidSubMap = new HashMap<Long, String>();
			this.uuidMap.put(dataObject.getType(), uuidSubMap);
		}
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		Long id = seqSubMap.get(uuid);
		if (id == null) {
			id = new Long(seqSubMap.size() + 1);
			seqSubMap.put(uuid, id);
			uuidSubMap.put(id, uuid);
		}
		return id;
	}
	
	
	/**
	 * Returns an existing UUID for the given 
	 * sequence number, or null if none exists. 
	 * @return the existing UUID or null if not exists for the given 
	 * sequence.
	 */
	public String findUUID(Long sequence) {
	
		Map<Long, String> uuidSubMap = this.uuidMap.get(sequence);
		if (uuidSubMap == null) 
			return null;
		return uuidSubMap.get(sequence);
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
	public String getUUID(Type type, Long sequence) {
	
		Map<Long, String> uuidSubMap = this.uuidMap.get(type);
		if (uuidSubMap != null) 
		{
			String uuid = uuidSubMap.get(sequence);
			if (uuid != null)
				return uuid;
		}
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
		byte[] key = this.keyMap.get(uuid);
		return key;
	}	
	/**
	 * Returns an existing mapped row key for the given data object. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object 
	 */
	public byte[] getRowKey(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		return getRowKey(uuid);
	}
	
	/**
	 * Returns an existing mapped row key for the given data object UUID. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object UUID, the UUID is null or the incorrect length
	 */
	public byte[] getRowKey(String uuid) {
	
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		byte[] key = this.keyMap.get(uuid);
		if (key == null)
			throw new IllegalArgumentException("no key mapped for UUID, " +
					uuid);
		if (log.isDebugEnabled())
			log.debug("returning row-key: " 
		        + uuid + "->" + new String(key, this.charset));
		return key;
	}	
	
	/**
	 * Creates a new mapping for the given row key and
	 * data object.
	 * @param dataObject the data object
	 * @param key the row key;
	 */
	public void addRowKey(DataObject dataObject, byte[] key) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		if (log.isDebugEnabled())
			log.debug("adding row-key: " 
		        + uuid + "->" + new String(key, this.charset));
		this.keyMap.put(uuid, key);
	}

	/**
	 * Removes an existing mapping, if exists, for the given data object.
	 * @param dataObject the data object
	 * @param key the row key;
	 */
	public void removeRowKey(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		byte[] key = this.keyMap.remove(uuid);
		if (key == null) {
			log.warn("could not remove key - no row key mapped to UUID, "
					+ uuid);
		}
		else
		    if (log.isDebugEnabled())
			    log.debug("removed row-key: " 
		            + uuid + "->" + new String(key, this.charset));
	}
	
	/**
	 * Returns a count of the current row keys
	 * @return a count of the current row keys
	 */ 
	public int getRowKeyCount() {
	    return this.keyMap.size();
	}
	
    public String dump() {
		StringBuilder buf = new StringBuilder();
		buf.append("\nUUID: ");
		buf.append(formatUUIDMap());
		buf.append("\nROKEY: ");
		buf.append(formatKeyMap());
		return buf.toString();
	}
	
	/**
	 * Returns a string representation of the UUID map.
	 * @return a string representation of the UUID map.
	 */
    public String formatUUIDMap() {
    	StringBuilder buf = new StringBuilder();
    	Iterator<Type> mapiter = seqMap.keySet().iterator();
    	int i = 0;
    	while (mapiter.hasNext()) {
    		if (i > 0)
    			buf.append(GraphState.MAP_DELIM_TYPES);
    		Type type = mapiter.next();
    		buf.append(type.getURI());
    		buf.append("#");
    		buf.append(type.getName());
    		buf.append(GraphState.MAP_DELIM_URI);
    		Map<String, Long> submap = seqMap.get(type);
    		Iterator<String> submapiter = submap.keySet().iterator();
        	int j = 0;
        	while (submapiter.hasNext()) {
        		if (j > 0)
        			buf.append(GraphState.MAP_DELIM_UUID);   
        		String uuid = submapiter.next();
	    		Number value = submap.get(uuid);
	    		buf.append(uuid);
	    		buf.append(GraphState.MAP_DELIM_ID);
	    		buf.append(value);
	    		j++;
        	}
    		i++;
    	}
    	return buf.toString();
    }

	/**
	 * Returns a string representation of the external key map.
	 * @return a string representation of the external key map.
	 */
    public String formatKeyMap() {
    	StringBuilder buf = new StringBuilder();
    	Iterator<String> mapiter = keyMap.keySet().iterator();
    	int i = 0;
    	while (mapiter.hasNext()) {
    		if (i > 0)
    			buf.append(GraphState.MAP_DELIM_UUID);
    		String uuid = mapiter.next();
    		buf.append(uuid);
    		buf.append(GraphState.MAP_DELIM_KEY);
    		byte[] rowKey = keyMap.get(uuid);
    		String rowKeyStr = new String(rowKey, this.charset);
    		buf.append(rowKeyStr);
    		i++;
    	}
    	return buf.toString();
    }
    
	public void addEdges(PlasmaNode dataNode, List<PlasmaEdge> edges) {
		if (edges != null) {
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();
	    		createSequence(opposite);
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
	public String formatEdges(PlasmaNode dataNode, List<PlasmaEdge> edges) {
		String[] result = new String[0];
		if (edges != null) {
			result = new String[edges.size()];
			int i = 0;
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();
	    		Long seq = getSequence(opposite);
	    		result[i] = formatEdge(edge, seq);
	         	i++;
			}
		}
		// use Arrays formatting
		return Arrays.toString(result);
    }
	
	public Edge[] parseEdges(Type type, byte[] data) {
		String edges = new String(data, this.charset);
		return parseEdges(type, edges);
	}
	
	public Edge[] parseEdges(Type type, String data) {
		// replace Arrays formatting and whitespace, then split
		String[] array = data.replaceAll("[\\[\\]\\s]", "").split(",");
		if (array.length == 1 && array[0].length() == 0)
			return new Edge[0];
		
		Edge[] result = new Edge[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Edge(type, array[i]);
		}		
		return result;
	}

    private String formatEdge(PlasmaEdge edge, Long seq)
    {
        String dir = formatDirection(edge.getDirection());
    	this.buf.setLength(0);        	
        this.buf.append(dir);
        this.buf.append(EDGE_DELIM);
        this.buf.append(String.valueOf(seq));        	
    	return this.buf.toString();   	
    }
    
    private String formatEdge(PlasmaEdge edge, byte[] key)
    {
        String dir = formatDirection(edge.getDirection());
    	this.buf.setLength(0);        	
        this.buf.append(dir);
        this.buf.append(EDGE_DELIM);        
        this.buf.append(new String(key, this.charset));        	
    	return this.buf.toString();   	
    }

    private String formatDirection(TraversalDirection dir) {
	    if (dir.ordinal() == TraversalDirection.RIGHT.ordinal()) {
	    	return EDGE_RIGHT;
	    }
	    else if (dir.ordinal() == TraversalDirection.LEFT.ordinal()) {
	    	return EDGE_LEFT;
	    }
	    else
	    	throw new IllegalStateException("unknown traversal direction, "
	    			+ dir.name());
	}
   
    /**
     * Simple immutable structure to contain and
     * manage the parse result for a single edge.  
     */
    public class Edge {
    	private TraversalDirection direction;
    	private String uuid;
    	private Long id;
    	
    	@SuppressWarnings("unused")
		private Edge() {}
    	public Edge(Type type, String data) {
    	    String[] tokens = data.split(EDGE_DELIM);
    	    if (EDGE_RIGHT.equals(tokens[0])) {
    	    	this.direction = TraversalDirection.RIGHT;
    	    }
    	    else if (EDGE_LEFT.equals(tokens[0])) {
    	    	this.direction = TraversalDirection.LEFT;
    	    }
    	    else
    	    	throw new IllegalStateException("could not parse traversal direction token from, '"
    	    			+ data + "'");
    	    
    	    this.id = Long.valueOf(tokens[1]);
    	    this.uuid = getUUID(type, this.id);
    	}
		public TraversalDirection getDirection() {
			return direction;
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
		public Long getId() {
			return id;
		}
     }
}
