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
package org.cloudgraph.hbase.key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.config.ColumnKeyFieldConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.PreDefinedFieldName;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;


/**
 * Creates an HBase column key based on the configured CloudGraph column key {@link org.cloudgraph.config.ColumnKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}. 
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
 * @see org.cloudgraph.config.ColumnKeyModel
 * @see org.cloudgraph.config.Table
 * @author Scott Cinnamond
 * @since 0.5
 */
public class CompositeColumnKeyFactory extends ByteBufferKeyFactory  
    implements GraphColumnKeyFactory 
{
	private static final Log log = LogFactory.getLog(CompositeColumnKeyFactory.class);
		
	public CompositeColumnKeyFactory(PlasmaType rootType) {
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
		byte[] uriToken = configureTokenBytes(type.getURIBytes(), graph, hash, PreDefinedFieldName.URI);

		// local type name
		byte[] typeToken = configureTokenBytes(typeNameToken, graph, hash, PreDefinedFieldName.TYPE);

		// property name
		byte[] propToken = configureTokenBytes(propertyNameToken, graph, hash, PreDefinedFieldName.PROPERTY);

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
			PreDefinedFieldName tokenName)
	{
		byte[] result = token;
		ColumnKeyFieldConfig tokenConfig = graph.getColumnKeyField(tokenName);
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
