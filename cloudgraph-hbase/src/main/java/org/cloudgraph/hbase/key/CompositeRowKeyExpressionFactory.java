package org.cloudgraph.hbase.key;

import java.nio.charset.Charset;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.key.GraphKeyException;
import org.cloudgraph.common.key.GraphRowKeyExpressionFactory;
import org.cloudgraph.common.key.KeyValue;
import org.cloudgraph.config.CloudGraphConfigurationException;
import org.cloudgraph.config.RowKeyField;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;

import commonj.sdo.Type;

/**
 * Generates an HBase row key based on the configured CloudGraph {@link org.cloudgraph.config.RowKeyModel Row Key
 * Model} for a specific {@link org.cloudgraph.config.Table HTable Configuration}. 
 * <p>
 * The initial creation and subsequent re-constitution for query retrieval
 * purposes of both row and column keys in CloudGraph&#8482; is efficient, 
 * as it leverages byte array level API in both Java and the current 
 * underlying SDO 2.1 implementation, <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a>. Both composite row and 
 * column keys are composed in part of structural metadata, and the 
 * lightweight metadata API within <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a> contains byte-array level, 
 * cached lookup of all basic metadata elements including logical and 
 * physical type and property names.  
 * </p>
 */
public class CompositeRowKeyExpressionFactory extends ByteBufferKeyFactory
    implements GraphRowKeyExpressionFactory 
{
	private static final Log log = LogFactory.getLog(CompositeRowKeyExpressionFactory.class);
	
	public CompositeRowKeyExpressionFactory(PlasmaType rootType) {	
		super(rootType);
	}
	
	@Override
	public String createRowKeyExpr(List<KeyValue> values) {
		byte[] result = createRowKeyExprBytes(values);
		return new String(result, 
			Charset.forName(CoreConstants.UTF8_ENCODING));
	}
	
	//@Override
	public String createRowKeyExprs(List<KeyValue> values) {
		StringBuilder result = new StringBuilder();
		
		if (values == null || values.size() == 0)
			throw new IllegalArgumentException("expected non-null, non-zero length list argument 'values'");
		
		List<RowKeyField> preDefinedTokens = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyField preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
        	    result.append(graph.getRowKeyFieldDelimiter());
       	    String tokenValue = this.getPredefinedTokenValue(this.rootType, 
       	    	hash, preDefinedToken);
       	    result.append(tokenValue);
        }		
        
        if (preDefinedTokens.size() > 0)
            result.append(graph.getRowKeySectionDelimiter());
        
        int count = 0;
		for (UserDefinedFieldConfig userTokenConfig : graph.getUserDefinedRowKeyFields()) {				
			if (count > 0)
			    result.append(graph.getRowKeyFieldDelimiter());
			
			KeyValue found = findTokenValue(userTokenConfig.getPropertyPath(), values);
            // user has a configuration for this path
			if (found != null) {
				String tokenValue = String.valueOf(found.getValue());	
				if (userTokenConfig.isHash()) {
					if (found.isWildcard())
						throw new GraphKeyException("cannot create wildcard expression for user"  
							+ " defined row-key token with XPath expression '" 
							+ userTokenConfig.getPathExpression() + "'"
							+ " for table '"
							+ table.getName() + "' - this token is defined as using an integral hash algorithm which prevents the use of wildcards");
						 
					int hashValue = hash.hash(tokenValue.getBytes());
					tokenValue = String.valueOf(hashValue);
				}
				else if (found.isWildcard()) {
					String expr = getDataFlavorRegex(found.getProp().getDataFlavor());
					String replaceExpr = "\\" + found.getWildcard();
					tokenValue = tokenValue.replaceAll(replaceExpr, expr);
				}
				result.append(tokenValue);
			}
			else {
				PlasmaProperty prop = (PlasmaProperty)userTokenConfig.getEndpointProperty();
				result.append(
					getDataFlavorRegex(prop.getDataFlavor()));
			}
			count++;
		}
		
		return result.toString();
	}
	
	@Override
	public byte[] createRowKeyExprBytes(List<KeyValue> values) {
		
		if (values == null || values.size() == 0)
			throw new IllegalArgumentException("expected non-null, non-zero length list argument 'values'");
		
		this.buf.clear();

		List<RowKeyField> preDefinedTokens = graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyField preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
    			this.buf.put(graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.getPredefinedTokenValueBytes(this.rootType, 
       	    	hash, preDefinedToken);
       	    this.buf.put(tokenValue);
        }		
        
        if (preDefinedTokens.size() > 0)
        	this.buf.put(graph.getRowKeySectionDelimiterBytes());
        
        int count = 0;
		for (UserDefinedFieldConfig userTokenConfig : graph.getUserDefinedRowKeyFields()) {				
			if (count > 0)
				this.buf.put(graph.getRowKeyFieldDelimiterBytes());
			
			KeyValue found = findTokenValue(userTokenConfig.getPropertyPath(), values);
            // user has a configuration for this path
			if (found != null) {
				String tokenValue = String.valueOf(found.getValue());	
				if (userTokenConfig.isHash()) {
					if (found.isWildcard())
						throw new GraphKeyException("cannot create wildcard expression for user"  
							+ " defined row-key token with XPath expression '" 
							+ userTokenConfig.getPathExpression() + "'"
							+ " for table '"
							+ table.getName() + "' - this token is defined as using an integral hash algorithm which prevents the use of wildcards");
						 
					int hashValue = hash.hash(tokenValue.getBytes());
					tokenValue = String.valueOf(hashValue);
				}
				else if (found.isWildcard()) {
					String expr = getDataFlavorRegex(found.getProp().getDataFlavor());
					String replaceExpr = "\\" + found.getWildcard();
					tokenValue = tokenValue.replaceAll(replaceExpr, expr);
				}
				this.buf.put(Bytes.toBytes(tokenValue));
			}
			else {
				PlasmaProperty prop = (PlasmaProperty)userTokenConfig.getEndpointProperty();
				this.buf.put(
					Bytes.toBytes(
						getDataFlavorRegex(prop.getDataFlavor())));
			}
			count++;
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

	private KeyValue findTokenValue(String path, List<KeyValue> values) {
		for (KeyValue pair : values) {
        	if (pair.getPropertyPath().equals(path))
                return pair;
        }
		return null;
	}
	
	private String getDataFlavorRegex(DataFlavor dataFlavor) {
		switch (dataFlavor) {
		case integral:
			return "[0-9\\-]+?";
		case real:
			return "[0-9\\-\\.]+?";
		default:
			return ".*?"; // any character zero or more times
		}
	}
	
	/**
	 * Returns a token value from the given Type
	 * @param type the SDO Type 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 */
	private String getPredefinedTokenValue(
			PlasmaType type, Hash hash, 
			RowKeyField token) {
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

	private byte[] getPredefinedTokenValueBytes(
			PlasmaType type, Hash hash, 
			RowKeyField token) {
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
	 * Returns true if the data graph configured for the given 
	 * {@link commonj.sdo.Type type} has a user defined token which
	 * maps to the given property path. 
	 * @param type the SDO type
	 * @param path the property path
	 * @return true if the data graph configured for the given 
	 * {@link commonj.sdo.Type type} has a user defined token which
	 * maps to the given property path. 
	 */
	@Override
    public boolean hasUserDefinedRowKeyToken(Type type, String path) {		
		return this.graph.getUserDefinedRowKeyField(path) != null;    	
    }	
	
}
