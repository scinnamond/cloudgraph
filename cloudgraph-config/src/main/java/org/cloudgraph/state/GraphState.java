package org.cloudgraph.state;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.state.model.SequenceMapping;
import org.cloudgraph.state.model.StateModeNonValidatinglDataBinding;
import org.cloudgraph.state.model.StateModel;
import org.cloudgraph.state.model.URIMap;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Manages a minimal set of "state" information persisted with each 
 * data graph in order to reduce overall storage space. 
 * In general mappings from various space intensive properties 
 * required for graph management, such as UUIDs, are mapped 
 * to integral values and the mapping stored in specific 
 * graph management columns within each row. Federation across
 * tables is enabled by mapping UUID's to row keys for any references
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
     * The name of the table column which stores the UUID for the
     * data object which is the data graph root. 
     */
	public static final String ROOT_UUID_COLUMN_NAME = "__ROOT__";
    
	/**
	 * The name of the table column containing the mapped row key state
	 * of a graph.  
	 */
	public static final String STATE_COLUMN_NAME = "__STATE__";

	private static final String EDGE_RIGHT = "R";
    private static final String EDGE_LEFT = "L";
    private static final String EDGE_DELIM = ":";
    
    private StringBuilder buf = new StringBuilder();
    private Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );
   
    private StateModel model;
    private StateMarshallingContext context;
    
    public GraphState(StateMarshallingContext context) {    	
    	this.context = context;
    	this.model = new StateModel();
    }    
     
    public GraphState(String state, StateMarshallingContext context) {
    	 
    	this.context = context;
    	
    	if (log.isDebugEnabled())
    		log.debug("unmarshal: " + state);
    	
		try {
	    	//this.model = (StateModel)this.context.getBinding().validate(state);
	    	this.model = (StateModel)this.context.getBinding()
	    			.unmarshal(state);
	    	if (log.isDebugEnabled())
	    		log.debug("unmarshal: " + this.dump());
	    	
		} catch (JAXBException e) {
			throw new StateException(e);
		} 
	}
        
	public void close() {
	}
	 
	/**
	 * Adds a the given number mapped to the UUID within the
	 * given data object. If an existing sequence exists
	 * for the given data object, a warning is logged. 
	 * @param dataObject the data object
	 * @param value the sequence value
	 */
    public void addSequence(DataObject dataObject, Long value) {
    	
    	PlasmaType plasmaType = (PlasmaType)dataObject.getType();
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping == null) {
    		mapping = new SequenceMapping(plasmaType.getURI(), 
    				plasmaType.getName());
    		this.model.getSequenceMap().put(plasmaType.getQualifiedName(), 
    				mapping);
    	}
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		Integer existing = mapping.getSequence(uuid);
    	if (existing == null) {
    		mapping.put(uuid, value.intValue()); // FIXME truncation
    	}
    	else
			log.warn("found existing mapping for UUID " 
					+ uuid + " - overwriting");
    }
    
    /**
	 * Removes the sequence number mapped to the UUID within the
	 * given data object. If an existing sequence exists
	 * for the given data object, a warning is logged. 
     * @param dataObject
     * @return the removed sequence if exists
     */
    public Long removeSequence(DataObject dataObject) {
    	PlasmaType plasmaType = (PlasmaType)dataObject.getType();
    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping != null) {
    		Integer removed =  mapping.remove(uuid); 
    		if (removed != null)
    			return removed.longValue(); // FIXME truncation
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
    	PlasmaType plasmaType = (PlasmaType)dataObject.getType();
    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping != null) {
    		Integer result =  mapping.getSequence(uuid); 
    		if (result != null)
    			return result.longValue(); // FIXME truncation
    	}
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
	public Long getSequence(DataObject dataObject) {	
    	PlasmaType plasmaType = (PlasmaType)dataObject.getType();
    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping != null) {
    		Integer result =  mapping.getSequence(uuid); 
    		if (result != null)
    			return result.longValue(); // FIXME truncation
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
    	PlasmaType plasmaType = (PlasmaType)dataObject.getType();
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		Integer result = null;
		SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping == null) {
    		mapping = new SequenceMapping(plasmaType.getURI(), 
    				plasmaType.getName());
    		this.model.getSequenceMap().put(plasmaType.getQualifiedName(), 
    				mapping);
    	}
    	else  {
    		result = mapping.getSequence(uuid);
    		if (result != null)
    			return result.longValue();
    	}
    	result = mapping.create(uuid);
		return result.longValue();
	}
	
	
	/**
	 * Returns an existing UUID for the given 
	 * sequence number, or null if none exists. 
	 * @return the existing UUID or null if not exists for the given 
	 * sequence.
	 */
	public String findUUID(Type type, Long sequence) {
    	PlasmaType plasmaType = (PlasmaType)type;
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping != null) {
    		String result =  mapping.getUUID(sequence.intValue());
    		return result;
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
	public String getUUID(Type type, Long sequence) {
    	PlasmaType plasmaType = (PlasmaType)type;
    	SequenceMapping mapping = this.model.getSequenceMap().get(
    		plasmaType.getQualifiedName());
    	if (mapping != null) {
    		String result =  mapping.getUUID(sequence.intValue());
    		return result;
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
		return this.model.getRowKeyMap().get(uuid);
	}
	
	/**
	 * Returns an existing mapped row key for the given data object. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object 
	 */
	public byte[] getRowKey(DataObject dataObject) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		byte[] result = this.model.getRowKeyMap().get(uuid);
		if (result != null)
			return result;
		throw new IllegalArgumentException("no row key mapped for the given UUID ("
				+ String.valueOf(uuid) + ") for given type, "
				+ dataObject.getType().getURI() + "#" + dataObject.getType().getName());
	}
	
	/**
	 * Returns an existing mapped row key for the given data object UUID. 
	 * @param dataObject the data object
	 * @return an existing mapped row key for the given data object. 
	 * @throws IllegalArgumentException if no row key is mapped for the given data object UUID, the UUID is null or the incorrect length
	 */
	public byte[] getRowKey(String uuid) {
		byte[] result = this.model.getRowKeyMap().get(uuid);
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
	public void addRowKey(DataObject dataObject, byte[] key) {
		String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
		if (uuid == null || uuid.length() == 0)
			throw new IllegalArgumentException("found null or zero length UUID from data object");
		if (uuid.length() != 36)
			throw new IllegalArgumentException("found "+uuid.length()+" rather than 38 char length UUID from data object");
		if (log.isDebugEnabled())
			log.debug("adding row-key: " 
		        + uuid + "->" + new String(key, this.charset));
		this.model.getRowKeyMap().put(uuid, key);
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
		byte[] key = this.model.getRowKeyMap().remove(uuid);
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
	    return this.model.getRowKeyMap().size();
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
	public String marshalEdges(PlasmaNode dataNode, List<PlasmaEdge> edges) {
		String[] result = new String[0];
		if (edges != null) {
			result = new String[edges.size()];
			int i = 0;
			for (PlasmaEdge edge : edges) {
	    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();
	    		Long seq = getSequence(opposite);
	    		result[i] = marshalEdge(edge, seq);
	         	i++;
			}
		}
		// use Arrays formatting
		return Arrays.toString(result);
    }
	
	public Edge[] unmarshalEdges(Type type, byte[] data) {
		String edges = new String(data, this.charset);
		return unmarshalEdges(type, edges);
	}
	
	public Edge[] unmarshalEdges(Type type, String data) {
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

    private String marshalEdge(PlasmaEdge edge, Long seq)
    {
        String dir = formatDirection(edge.getDirection());
    	this.buf.setLength(0);        	
        this.buf.append(dir);
        this.buf.append(EDGE_DELIM);
        this.buf.append(String.valueOf(seq));        	
    	return this.buf.toString();   	
    }
    
    private String marshalEdge(PlasmaEdge edge, byte[] key)
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
    
    public String dump() {
    	StateModeNonValidatinglDataBinding binding;
    	String xml = "";
		try {
			binding = new StateModeNonValidatinglDataBinding();
			xml = binding.marshal(this.model);
		} catch (JAXBException e1) {
			log.error(e1);
		} catch (SAXException e1) {
			log.error(e1);
		}

    	return xml;
    }
    
    public String marshal() {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	
    	String xml = "";
		try {
			this.context.getBinding().marshal(this.model, stream, false); // no formatting
		} catch (JAXBException e1) {
			throw new StateException(e1);
		} 
		byte[] bytes = null;
		try {
			stream.flush();
			bytes = stream.toByteArray();
		} catch (IOException e) {
			throw new StateException(e);
		}
		finally {
			try {
				stream.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		xml = new String(bytes, this.charset);
    	if (log.isDebugEnabled())
    		log.debug("marshal: " + xml);

    	return xml;
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
