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
package org.cloudgraph.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Encapsulates logic related to access of a configured
 * user-defined row key field. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class UserDefinedRowKeyFieldConfig extends KeyFieldConfig {
    private static Log log = LogFactory.getLog(UserDefinedRowKeyFieldConfig.class);
	
    private DataGraphConfig dataGraph;
    private UserDefinedField userToken;
    private String propertyPath;
    private commonj.sdo.Property endpointProperty;
        
	public UserDefinedRowKeyFieldConfig(DataGraphConfig dataGraph, 
			UserDefinedField userToken,
			int sequenceNum) {
		super(userToken, sequenceNum);
		this.dataGraph = dataGraph;
		this.userToken = userToken;
		
		try {
			construct(this.userToken.getPath());
		}
		catch (IllegalArgumentException e) {
			throw new CloudGraphConfigurationException(e);
		}
		finally {			
		}
	}
	
	private void construct(String xpath) {
		Type contextType = this.getDataGraph().getRootType();
		StringBuilder buf = new StringBuilder();
		String[] tokens = xpath.split("/");
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0)
				buf.append("/");
			String token = tokens[i];
			int right = token.indexOf("[");
			if (right >= 0) // remove predicate
				token = token.substring(0, right);	
			int attr = token.indexOf("@");
			if (attr == 0)
				token = token.substring(1);
			commonj.sdo.Property prop = contextType.getProperty(token);
			if (!prop.getType().isDataType()) 				
				contextType = prop.getType(); // traverse
			else
				this.endpointProperty = prop;
			buf.append(prop.getName());
		}
		this.propertyPath = buf.toString();	
	}

    public boolean equals(Object obj) {
    	UserDefinedRowKeyFieldConfig other = (UserDefinedRowKeyFieldConfig)obj;
	    return (this.sequenceNum == other.sequenceNum);
    }
    
	public int getSequenceNum() {
		return sequenceNum;
	}

	public DataGraphConfig getDataGraph() {
		return dataGraph;
	}

	public UserDefinedField getUserToken() {
		return userToken;
	}

	public String getPathExpression() {
		return this.userToken.getPath();
	}
	
	public boolean isHash() {
		return this.userToken.isHash();
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public commonj.sdo.Property getEndpointProperty() {
		return endpointProperty;
	}

	/**
	 * Returns a token value from the given Data Graph
	 * @param dataGraph the data graph 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 */
	public byte[] getKeyBytes(
			commonj.sdo.DataGraph dataGraph) {
		return this.getKeyBytes(dataGraph.getRootObject());
	}

	/**
	 * Returns a user defined key value from the given data object, or null
	 * if the
	 * @param dataObject the root data object 
	 * @param hash the hash algorithm to use in the event the
	 * row key token is to be hashed 
	 * @param token the pre-defined row key token configuration
	 * @return the token value
	 * @throws UnresolvedPathExpressionException if the configured XPath expression resolves to a null value
	 */
	public byte[] getKeyBytes(
		DataObject dataObject) 
	{
		byte[] result = null;
		// FIXME: do we want to invoke a converter here?
		// FIXME: do we want to transform this value somehow?
		String tokenValue = dataObject.getString(
			this.getPathExpression());
		if (tokenValue != null) {			
			result = tokenValue.getBytes(this.charset);
		}
		else {
			throw new UnresolvedPathExpressionException(
				"the configured XPath expression '" 
				+ this.getPathExpression() + "'"
				+ " for graph root type '" + dataGraph.getRootType().getName() + "'"
				+ " within table '" + dataGraph.getTable().getName() + "'"
				+ " resolved to a null value - "
				+ "use an XPath expressions which terminate with a mandatory property, and return mandatory properties for data graph root types");
		}
		
		return result;
	}

}
