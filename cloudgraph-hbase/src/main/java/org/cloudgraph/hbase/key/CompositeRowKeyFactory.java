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
import org.cloudgraph.config.CloudGraphConfigurationException;
import org.cloudgraph.config.RowKeyToken;
import org.cloudgraph.config.UserDefinedTokenConfig;
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

		
		List<RowKeyToken> preDefinedTokens = graph.getPreDefinedRowKeyTokens();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyToken preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
        	    result.append(graph.getRowKeyFieldDelimiter());
       	    String tokenValue = this.getPredefinedTokenValue(plasmaType, 
       	    	hash, preDefinedToken);
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
		List<RowKeyToken> preDefinedTokens = this.graph.getPreDefinedRowKeyTokens();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyToken preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
    			this.buf.put(this.graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.getPredefinedTokenValueBytes(type, 
    			this.hash, preDefinedToken);
    		this.buf.put(tokenValue);
        }        		
	}		
	
	//@Override
	public String createRowKey(DataGraph dataGraph) {
		
		StringBuilder result = new StringBuilder();
		
		
		List<RowKeyToken> preDefinedTokens = graph.getPreDefinedRowKeyTokens();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyToken preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
        	    result.append(graph.getRowKeyFieldDelimiter());
       	    String tokenValue = this.getPredefinedTokenValue(dataGraph, 
       	    		hash, preDefinedToken);
       	    result.append(tokenValue);
        }		
		
		if (!graph.hasUserDefinedRowKeyTokens())
			return result.toString();
		
		if (preDefinedTokens.size() > 0)
			result.append(graph.getRowKeySectionDelimiter());

		int count = 0;
		for (UserDefinedTokenConfig userTokenConfig : graph.getUserDefinedRowKeyTokens()) {				
			
			// invoke SDO xpath fetch
			// FIXME: do we want to invoke a converter here?
			// FIXME: do we want to transform this value somehow?
			String tokenValue = dataGraph.getRootObject().getString(
				userTokenConfig.getPathExpression());
			
			if (tokenValue != null) {
				if (count > 0)
				    result.append(graph.getRowKeyFieldDelimiter());
						
				if (userTokenConfig.isHash()) {
					int hashValue = hash.hash(tokenValue.getBytes());
					tokenValue = String.valueOf(hashValue);
				}
				
				result.append(tokenValue);
				count++;
			}
			else
				log.warn("null value resulted from user defined row-key token with XPath expression '" 
					+ userTokenConfig.getPathExpression() + "'"
					+ " for HTable '"
					+ table.getName() + "' - excluding token "
					+ "(suggest using XPath which resolves to a mandatory property)");
		}	

		return result.toString();
	}
	
	@Override
	public byte[] createRowKeyBytes(DataGraph dataGraph) {
		
		this.buf.clear();
		
		List<RowKeyToken> preDefinedTokens = graph.getPreDefinedRowKeyTokens();
        for (int i = 0; i < preDefinedTokens.size(); i++) {
        	RowKeyToken preDefinedToken = preDefinedTokens.get(i);
    		if (i > 0)
        	    this.buf.put(graph.getRowKeyFieldDelimiterBytes());
    		byte[] tokenValue = this.getPredefinedTokenValueBytes(dataGraph, 
       	    		hash, preDefinedToken);
       	    this.buf.put(tokenValue);
        }		
		
		if (!graph.hasUserDefinedRowKeyTokens())
			return this.buf.array();
		
		if (preDefinedTokens.size() > 0)
		    this.buf.put(graph.getRowKeySectionDelimiterBytes());

		int count = 0;
		for (UserDefinedTokenConfig userTokenConfig : graph.getUserDefinedRowKeyTokens()) {				
			
			// invoke SDO xpath fetch
			// FIXME: do we want to invoke a converter here?
			// FIXME: do we want to transform this value somehow?
			String tokenValue = dataGraph.getRootObject().getString(
				userTokenConfig.getPathExpression());
			
			if (tokenValue != null) {
				if (count > 0)
					this.buf.put(graph.getRowKeyFieldDelimiterBytes());
						
				if (userTokenConfig.isHash()) {
					int hashValue = hash.hash(tokenValue.getBytes());
					tokenValue = String.valueOf(hashValue);
				}
				
				this.buf.put(Bytes.toBytes(tokenValue));
				count++;
			}
			else
				log.warn("null value resulted from user defined row-key token with XPath expression '" 
					+ userTokenConfig.getPathExpression() + "'"
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
			RowKeyToken token) {
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
			RowKeyToken token) {
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
	private String getPredefinedTokenValue(
			DataGraph dataGraph, Hash hash, 
			RowKeyToken token) {
		String result = null;
		switch (token.getName()) {
		case URI: 
			result = dataGraph.getRootObject().getType().getURI();
			break;
		case TYPE:
			PlasmaType type = (PlasmaType)dataGraph.getRootObject().getType();
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
		case UUID:
			result = ((PlasmaDataObject)dataGraph.getRootObject()).getUUIDAsString();
			break;
		default:
		    throw new CloudGraphConfigurationException("invalid row key token name, "
		    		+ token.getName().name() + " - cannot get this token from a Data Graph");
		}
		
		if (token.isHash()) {
			int hashValue = hash.hash(result.getBytes());
			result = String.valueOf(hashValue);
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
	private byte[] getPredefinedTokenValueBytes(
			DataGraph dataGraph, Hash hash, 
			RowKeyToken token) {
		byte[] result = null;
		switch (token.getName()) {
		case URI: 
			result = this.rootType.getURIBytes();
			break;
		case TYPE:
			QName qname = this.rootType.getQualifiedName();
			
			result = this.rootType.getPhysicalNameBytes();
			if (result == null || result.length == 0) {
				if (log.isDebugEnabled())
				    log.debug("no physical name for type, "
				    		+ qname.getNamespaceURI() + "#" + this.rootType.getName() 
				    		+ ", defined - using logical name");
				result = this.rootType.getNameBytes();
			}
			break;
		case UUID:
			result = Bytes.toBytes(((PlasmaDataObject)dataGraph.getRootObject()).getUUIDAsString());
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
