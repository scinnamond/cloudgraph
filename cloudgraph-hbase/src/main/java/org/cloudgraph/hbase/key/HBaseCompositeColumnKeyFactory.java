package org.cloudgraph.hbase.key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.config.ColumnKeyToken;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.PreDefinedTokenName;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Generates an HBase column key based on the configured Cloudgraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}. 
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.Table
 */
public class HBaseCompositeColumnKeyFactory extends HBaseKeyFactory  
    implements GraphColumnKeyFactory 
{
	private static final Log log = LogFactory.getLog(HBaseCompositeColumnKeyFactory.class);
		
	public HBaseCompositeColumnKeyFactory(PlasmaType rootType) {
		super(rootType);
	}

	@Override
	public byte[] createColumnKey( 
		PlasmaType type, PlasmaProperty property)
	{		
		byte[] typeNameToken = type.getPhysicalNameBytes();
		if (typeNameToken == null || typeNameToken.length == 0) {
			if (log.isDebugEnabled())
			    log.debug("no physical name for type, "
			    		+ type.getURI() + "#" + type.getName() 
			    		+ ", defined - using logical type name");
			typeNameToken = type.getNameBytes();
		}
		byte[] propertyNameToken = property.getPhysicalNameBytes();
		if (propertyNameToken == null || propertyNameToken.length == 0) {
			if (log.isDebugEnabled())
			    log.debug("no physical name for property, "
			    		+ type.getURI() + "#" + type.getName()
			    		+ "." + property.getName()
			    		+ ", defined - using logical property name");
			propertyNameToken = property.getNameBytes();
		}
		
		// URI
		byte[] uriToken = configureTokenBytes(type.getURIBytes(), graph, hash, PreDefinedTokenName.URI);

		// local type name
		byte[] typeToken = configureTokenBytes(typeNameToken, graph, hash, PreDefinedTokenName.TYPE);

		// property name
		byte[] propToken = configureTokenBytes(propertyNameToken, graph, hash, PreDefinedTokenName.PROPERTY);

		int tokensLen = uriToken.length + typeToken.length + propToken.length;
		byte[] delim = graph.getColumnKeyFieldDelimiterBytes();
		 
		byte[] result = new byte[tokensLen + (2 * delim.length)];
		
		int destPos = 0;
		System.arraycopy(uriToken, 0, result, destPos, uriToken.length);
		
		destPos += uriToken.length;
		System.arraycopy(delim, 0, result, destPos, delim.length);
		
		destPos += delim.length;
		System.arraycopy(typeToken, 0, result, destPos, typeToken.length);
		
		destPos += typeToken.length;
		System.arraycopy(delim, 0, result, destPos, delim.length);
		
		destPos += delim.length;
		System.arraycopy(propToken, 0, result, destPos, propToken.length);		
		
		return result;
	}

	protected byte[] configureTokenBytes(byte[] token, 
			DataGraphConfig graph, Hash hash, 
			PreDefinedTokenName tokenName)
	{
		byte[] result = token;
		ColumnKeyToken tokenConfig = graph.getColumnKeyToken(tokenName);
		if (tokenConfig != null) {
			if (tokenConfig.isHash()) {
				int hashValue = hash.hash(result);
				// hash to an integer but use the bytes of the 
				// String representation so column names so we can read
				// the column names in third party tools. 
				result = Bytes.toBytes(String.valueOf(hashValue));
			}
		}
		return result;
	}

}
