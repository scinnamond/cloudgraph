package org.cloudgraph.hbase.key;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.key.GraphRowKeyFactory;
import org.cloudgraph.common.key.KeyValue;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.CloudGraphConfigurationException;
import org.cloudgraph.config.RowKeyField;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataGraph;
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
 */
public class CompositeRowKeyFactory extends ByteBufferKeyFactory
    implements GraphRowKeyFactory 
{
	private static final Log log = LogFactory.getLog(CompositeRowKeyFactory.class);
	
	public CompositeRowKeyFactory(PlasmaType rootType) {	
		super(rootType);
	}
	
	//@Override
	public String createRowKey(Type type) {
		StringBuilder result = new StringBuilder();
		PlasmaType plasmaType = (PlasmaType)type;

		
		List<RowKeyField> preDefinedFields = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	RowKeyField preDefinedField = preDefinedFields.get(i);
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
		List<RowKeyField> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	RowKeyField preDefinedField = preDefinedFields.get(i);
    		if (i > 0)
    			this.buf.put(this.graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.keySupport.getPredefinedFieldValueBytes(type, 
    			this.hash, preDefinedField);
    		this.buf.put(tokenValue);
        }        		
	}		
	
	@Override
	public byte[] createRowKeyBytes(DataGraph dataGraph) {
		
		this.buf.clear();
		
		List<RowKeyField> preDefinedFields = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	RowKeyField preDefinedField = preDefinedFields.get(i);
    		if (i > 0)
        	    this.buf.put(graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.keySupport.getPredefinedFieldValueBytes(dataGraph, 
       	    		hash, preDefinedField);
       	    this.buf.put(tokenValue);
        }		
		
		if (!graph.hasUserDefinedRowKeyFields())
			return this.buf.array();
		
		if (preDefinedFields.size() > 0)
		    this.buf.put(graph.getRowKeySectionDelimiterBytes());

		int count = 0;
		for (UserDefinedFieldConfig userFieldConfig : graph.getUserDefinedRowKeyFields()) {				
			
			// invoke SDO xpath fetch
			// FIXME: do we want to invoke a converter here?
			// FIXME: do we want to transform this value somehow?
			String tokenValue = dataGraph.getRootObject().getString(
				userFieldConfig.getPathExpression());
			
			if (tokenValue != null) {
				if (count > 0)
					this.buf.put(graph.getRowKeyFieldDelimiterBytes());
						
				if (userFieldConfig.isHash()) {
					int hashValue = hash.hash(tokenValue.getBytes());
					tokenValue = String.valueOf(hashValue);
				}
				
				this.buf.put(Bytes.toBytes(tokenValue));
				count++;
			}
			else
				log.warn("null value resulted from user defined row-key token with XPath expression '" 
					+ userFieldConfig.getPathExpression() + "'"
					+ " for HTable '"
					+ table.getName() + "' - excluding token "
					+ "(suggest using XPath which resolves to a mandatory property)");
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
		
		List<RowKeyField> preDefinedFields = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	RowKeyField preDefinedField = preDefinedFields.get(i);
    		if (i > 0)
        	    this.buf.put(graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.keySupport.getPredefinedFieldValueBytes(this.rootType, 
       	    		hash, preDefinedField);
       	    this.buf.put(tokenValue);
        }		
		
		if (!graph.hasUserDefinedRowKeyFields())
			return this.buf.array();
		
		if (preDefinedFields.size() > 0)
		    this.buf.put(graph.getRowKeySectionDelimiterBytes());

		int count = 0;
		for (UserDefinedFieldConfig userFieldConfig : graph.getUserDefinedRowKeyFields()) {				
			
			// FIXME: do we want to invoke a converter here?
			// FIXME: do we want to transform this value somehow?
			KeyValue keyValue = this.keySupport.findKeyValue(userFieldConfig, values);
			
			if (keyValue != null) {
				if (count > 0)
					this.buf.put(graph.getRowKeyFieldDelimiterBytes());
				
				String stringValue = String.valueOf(keyValue.getValue());
				if (userFieldConfig.isHash()) {
					int hashValue = hash.hash(stringValue.getBytes(this.charset));
					stringValue = String.valueOf(hashValue);
				}
				
				this.buf.put(stringValue.getBytes(this.charset));
				count++;
			}
			else
				log.warn("null value resulted from user defined row-key token with XPath expression '" 
					+ userFieldConfig.getPathExpression() + "'"
					+ " for HTable '"
					+ table.getName() + "' - excluding token "
					+ "(suggest using XPath which resolves to a mandatory property)");
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
