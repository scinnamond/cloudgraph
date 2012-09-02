package org.cloudgraph.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.UserDefinedToken;

import commonj.sdo.Type;

public class UserDefinedTokenConfig {
    private static Log log = LogFactory.getLog(UserDefinedTokenConfig.class);
	
    private DataGraphConfig dataGraph;
    private UserDefinedToken userToken;
    private String propertyPath;
    private commonj.sdo.Property endpointProperty;
    
    @SuppressWarnings("unused")
	private UserDefinedTokenConfig() {}
    
	public UserDefinedTokenConfig(DataGraphConfig dataGraph, 
			UserDefinedToken userToken) {
		super();
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
				endpointProperty = prop;
			buf.append(prop.getName());
		}
		this.propertyPath = buf.toString();	
	}
	
	public DataGraphConfig getDataGraph() {
		return dataGraph;
	}

	public UserDefinedToken getUserToken() {
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
