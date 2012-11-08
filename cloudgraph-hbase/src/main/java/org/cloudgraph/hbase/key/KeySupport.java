package org.cloudgraph.hbase.key;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.key.KeyValue;
import org.cloudgraph.config.CloudGraphConfigurationException;
import org.cloudgraph.config.PreDefinedKeyFieldConfig;
import org.cloudgraph.config.PredefinedField;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

/**
 * Delegate class supporting composite key generation.
 * @author Scott Cinnamond
 * @since 0.5
 */
public class KeySupport {
	private static final Log log = LogFactory.getLog(CompositeRowKeyFactory.class);

	/**
	 * Returns the specific configured hash algorithm 
	 * configured for an HTable, or if not configured
	 * returns the configured HBase hash algorithm as
	 * configured within HBase using the 'hbase.hash.type'
	 * property.
	 * @return the specific configured hash algorithm 
	 * configured for an HTable.
	 */
	public Hash getHashAlgorithm(TableConfig table) {
		Hash hash = null; 
		if (table.hasHashAlgorithm()) {
			String hashName = table.getTable().getHashAlgorithm().getName().value();
		    hash = Hash.getInstance(Hash.parseHashType(hashName));
		}
		else {
		    String algorithm = CloudGraphContext.instance().getConfig().get(
		    		CloudGraphConstants.PROPERTY_CONFIG_HASH_TYPE);
		    if (algorithm != null)
		        hash = Hash.getInstance(Hash.parseHashType(algorithm));			
		}
		return hash;
	}

    public KeyValue findKeyValue(UserDefinedRowKeyFieldConfig fieldConfig, List<KeyValue> pairs) {
    	
    	commonj.sdo.Property fieldProperty = fieldConfig.getEndpointProperty();
    	commonj.sdo.Type fieldPropertyType = fieldProperty.getContainingType();
    	
		for (KeyValue keyValue : pairs) {
			if (keyValue.getProp().getName().equals(fieldConfig.getEndpointProperty().getName())) {
				if (keyValue.getProp().getContainingType().getName().equals(fieldPropertyType.getName())) {
					if (keyValue.getProp().getContainingType().getURI().equals(fieldPropertyType.getURI())) {
					    if (fieldConfig.getPropertyPath() != null) {
					    	if (keyValue.getPropertyPath() != null && 
					    		keyValue.getPropertyPath().equals(fieldConfig.getPropertyPath())) {
					    		return keyValue;
					    	}
					    }
					}					
				}
			}
		}
		return null;
    }    

	/**
	 * Returns a token value from the given Type
	 * @param type the SDO Type 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 */
    public String getPredefinedFieldValue(
			PlasmaType type, Hash hash, 
			PreDefinedKeyFieldConfig token) {
		String result = null;
		switch (token.getName()) {
		case URI: 
			result = type.getURI();
			break;
		case TYPE:
			QName qname = type.getQualifiedName();
			
			result = type.getPhysicalName();
			if (result == null || result.length() == 0) {
				if (log.isDebugEnabled())
				    log.debug("no physical name for type, "
				    		+ qname.getNamespaceURI() + "#" + type.getName() 
				    		+ ", defined - using logical name");
				result = type.getName();
			}
			break;
		default:
		    throw new CloudGraphConfigurationException("invalid row key token name, "
		    		+ token.getName().name() + " - cannot get this token from a SDO Type");
		}
		
		if (token.isHash()) {
			int hashValue = hash.hash(result.getBytes());
			result = String.valueOf(hashValue);
		}
		
		return result;
	}

    public byte[] getPredefinedFieldValueBytes(
			PlasmaType type, Hash hash, 
			PreDefinedKeyFieldConfig token) {
		byte[] result = null;
		switch (token.getName()) {
		case URI: 
			result = type.getURIBytes();
			break;
		case TYPE:
			QName qname = type.getQualifiedName();
			
			result = type.getPhysicalNameBytes();
			if (result == null || result.length == 0) {
				if (log.isDebugEnabled())
				    log.debug("no physical name for type, "
				    		+ qname.getNamespaceURI() + "#" + type.getName() 
				    		+ ", defined - using logical name");
				result = type.getNameBytes();
			}
			break;
		default:
		    throw new CloudGraphConfigurationException("invalid row key token name, "
		    		+ token.getName().name() + " - cannot get this token from a SDO Type");
		}
		
		if (token.isHash()) {
			int hashValue = hash.hash(result);
			// convert integer hash values to string-bytes so readable
			// in third party tools			
			result = Bytes.toBytes(String.valueOf(hashValue));
		}
		
		return result;
	}
	
	/**
	 * Returns a token value from the given Data Graph
	 * @param dataGraph the data graph 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 */
	public byte[] getPredefinedFieldValueBytes(
			DataGraph dataGraph, Hash hash, 
			PredefinedField token) {
		return getPredefinedFieldValueBytes(dataGraph.getRootObject(),
			hash, token);
	}
    
    /**
	 * Returns a token value from the given data object
	 * @param dataObject the root data object 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 */
	public byte[] getPredefinedFieldValueBytes(
			DataObject dataObject, Hash hash, 
			PredefinedField token) {
		PlasmaType rootType = (PlasmaType)dataObject.getType();
		
		byte[] result = null;
		switch (token.getName()) {
		case URI: 
			result = rootType.getURIBytes();
			break;
		case TYPE:
			QName qname = rootType.getQualifiedName();
			
			result = rootType.getPhysicalNameBytes();
			if (result == null || result.length == 0) {
				if (log.isDebugEnabled())
				    log.debug("no physical name for type, "
				    		+ qname.getNamespaceURI() + "#" + rootType.getName() 
				    		+ ", defined - using logical name");
				result = rootType.getNameBytes();
			}
			break;
		case UUID:
			result = Bytes.toBytes(((PlasmaDataObject)dataObject).getUUIDAsString());
			break;
		default:
		    throw new CloudGraphConfigurationException("invalid row key token name, "
		    		+ token.getName().name() + " - cannot get this token from a Data Graph");
		}
		
		if (token.isHash()) {
			int hashValue = hash.hash(result);
			result = Bytes.toBytes(String.valueOf(hashValue));
		}
		
		return result;
	}
}
