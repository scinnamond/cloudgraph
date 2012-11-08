package org.cloudgraph.hbase.key;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.key.GraphRowKeyFactory;
import org.cloudgraph.common.key.KeyValue;
import org.cloudgraph.config.KeyFieldConfig;
import org.cloudgraph.config.PreDefinedKeyFieldConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Generates an HBase row key based on the configured CloudGraph {@link org.cloudgraph.config.RowKeyModel Row Key
 * Model} for a specific {@link org.cloudgraph.config.Table HTable Configuration}. 
 * <p>
 * The initial creation and subsequent reconstitution for query retrieval
 * purposes of both row and column keys in CloudGraph&#8482; is efficient, 
 * as it leverages byte array level API in both Java and the current 
 * underlying SDO 2.1 implementation, <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a>. Both composite row and 
 * column keys are composed in part of structural metadata, and the 
 * lightweight metadata API within <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a> contains byte-array level, 
 * cached lookup of all basic metadata elements including logical and 
 * physical type and property names.  
 * </p>
 * @author Scott Cinnamond
 * @since 0.5
 */
public class CompositeRowKeyFactory extends ByteBufferKeyFactory
    implements GraphRowKeyFactory 
{
	private static final Log log = LogFactory.getLog(CompositeRowKeyFactory.class);
	
	public CompositeRowKeyFactory(PlasmaType rootType) {	
		super(rootType);
	}
	
	/**
	 * Creates a row key using only the given type information. The
	 * key is therefore composed of only "metadata" fields which are
	 * pre-defined. 
	 * @param type the data object type
	 * @return the row key
	 */
	public String createRowKey(Type type) {
		StringBuilder result = new StringBuilder();
		PlasmaType plasmaType = (PlasmaType)type;

		
		List<PreDefinedKeyFieldConfig> preDefinedFields = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	PreDefinedKeyFieldConfig preDefinedField = preDefinedFields.get(i);
    		if (i > 0)
        	    result.append(graph.getRowKeyFieldDelimiter());
       	    String tokenValue = this.keySupport.getPredefinedFieldValue(plasmaType, 
       	    	hash, preDefinedField);
       	    result.append(tokenValue);
        }		
		return result.toString();
	}

	@Override
	public byte[] createRowKeyBytes(Type type) {
		
		PlasmaType plasmaType = (PlasmaType)type;
		
		this.buf.clear();
		
		try {
			create(plasmaType);
		}
		catch (BufferOverflowException e) {
			this.bufsize = this.bufsize * 2;
			this.buf = ByteBuffer.allocate(this.bufsize);
			create(plasmaType);
		}        
        
		return this.buf.array();
	}
		
	private void create(PlasmaType type)
	{
		List<PreDefinedKeyFieldConfig> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	PreDefinedKeyFieldConfig preDefinedField = preDefinedFields.get(i);
    		if (i > 0)
    			this.buf.put(this.graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.keySupport.getPredefinedFieldValueBytes(type, 
    			this.hash, preDefinedField);
    		this.buf.put(tokenValue);
        }        		
	}		
	
	@Override
	public byte[] createRowKeyBytes(DataGraph dataGraph) {
	    return createRowKeyBytes(dataGraph.getRootObject());
	}	
	
	@Override
	public byte[] createRowKeyBytes(DataObject rootDataObject) {
		
		this.buf.clear();
		
		byte[] keyValue = null;
		int i = 0;
		for (KeyFieldConfig fieldConfig : this.graph.getRowKeyFields()) {
    		if (i > 0)
        	    this.buf.put(graph.getRowKeyFieldDelimiterBytes());
			keyValue = fieldConfig.getKeyBytes(rootDataObject);
			if (fieldConfig.isHash()) {
				int hashValue = hash.hash(keyValue);
				keyValue = String.valueOf(hashValue).getBytes(charset);
			}
	       	
		    this.buf.put(keyValue);
				
			i++;
		}

		// ByteBuffer.array() returns unsized array so don't sent that back to clients
		// to misuse. 
		// Use native arraycopy() method as it uses native memcopy to create result array
		// and because and
		// ByteBuffer.get(byte[] dst,int offset, int length) is not native
	    byte [] result = new byte[this.buf.position()];
	    System.arraycopy(this.buf.array(), this.buf.arrayOffset(), result, 0, this.buf.position()); 

		return result;
	}
	
	public byte[] createRowKeyBytes(List<KeyValue> values) {
		this.buf.clear();
		
		byte[] fieldValue = null;
		int i = 0;
		for (KeyFieldConfig fieldConfig : this.graph.getRowKeyFields()) {
    		if (i > 0)
        	    this.buf.put(graph.getRowKeyFieldDelimiterBytes());
    		if (fieldConfig instanceof PreDefinedKeyFieldConfig) {
    			PreDefinedKeyFieldConfig predefinedConfig = (PreDefinedKeyFieldConfig)fieldConfig;
    			fieldValue = predefinedConfig.getKeyBytes(this.rootType);
    		}
    		else if (fieldConfig instanceof UserDefinedRowKeyFieldConfig) {
    			UserDefinedRowKeyFieldConfig userFieldConfig = (UserDefinedRowKeyFieldConfig)fieldConfig;
    			KeyValue keyValue = this.keySupport.findKeyValue(userFieldConfig, values);
    			
    			if (keyValue != null) {
        			// FIXME: do we want to invoke a converter here?
        			// FIXME: do we want to transform this value somehow?
     				String stringValue = String.valueOf(keyValue.getValue());
    				fieldValue = stringValue.getBytes(this.charset);
    			}
    			else {
    				continue; // could be a partial row key scan
    			}
    		}
    		
			if (fieldConfig.isHash()) {
				int hashValue = hash.hash(fieldValue);
				fieldValue = String.valueOf(hashValue).getBytes(charset);
			}    		
       	    this.buf.put(fieldValue);
			i++;
		}	
		
		// ByteBuffer.array() returns unsized array so don't sent that back to clients
		// to misuse. 
		// Use native arraycopy() method as it uses native memcopy to create result array
		// and because and
		// ByteBuffer.get(byte[] dst,int offset, int length) is not native
	    byte [] result = new byte[this.buf.position()];
	    System.arraycopy(this.buf.array(), this.buf.arrayOffset(), result, 0, this.buf.position()); 

		return result;
	}
	
}
