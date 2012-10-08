package org.cloudgraph.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.UserDefinedField;

import commonj.sdo.Type;

public class UserDefinedFieldConfig {
    private static Log log = LogFactory.getLog(UserDefinedFieldConfig.class);
	
    private DataGraphConfig dataGraph;
    private UserDefinedField userToken;
    private String propertyPath;
    private commonj.sdo.Property endpointProperty;
    private int sequenceNum;
    
    @SuppressWarnings("unused")
	private UserDefinedFieldConfig() {}
    
	public UserDefinedFieldConfig(DataGraphConfig dataGraph, 
			UserDefinedField userToken,
			int sequenceNum) {
		super();
		this.dataGraph = dataGraph;
		this.userToken = userToken;
		this.sequenceNum = sequenceNum;
		
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
				endpointProperty = prop;
			buf.append(prop.getName());
		}
		this.propertyPath = buf.toString();	
	}

    public boolean equals(Object obj) {
    	UserDefinedFieldConfig other = (UserDefinedFieldConfig)obj;
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
	
}
