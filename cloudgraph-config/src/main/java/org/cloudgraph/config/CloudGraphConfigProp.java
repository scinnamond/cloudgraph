package org.cloudgraph.config;

import org.plasma.query.Query;

public class CloudGraphConfigProp {
	
	public static QueryFetchType getQueryFetchType(Query query)
	{
    	QueryFetchType fetchType = QueryFetchType.SERIAL;
		String fetchTypeValue = System.getProperty(ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value());
		if (fetchTypeValue != null)  
    		try {
    		    fetchType = QueryFetchType.fromValue(fetchTypeValue);
    		}
    	    catch (IllegalArgumentException e) {
    	    	throw new CloudGraphConfigurationException("unknown query configuration value '"
    	    		+ fetchTypeValue + "' for property, "
    	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value(), e);
    	    }
		// override it with query specific value
		fetchTypeValue = query.getConfigurationProperty(
    			ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value());      	       	
    	if (fetchTypeValue != null)
    		try {
    		    fetchType = QueryFetchType.fromValue(fetchTypeValue);
    		}
    	    catch (IllegalArgumentException e) {
    	    	throw new CloudGraphConfigurationException("unknown query configuration value '"
    	    		+ fetchTypeValue + "' for property, "
    	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value(), e);
    	    }
		return fetchType;
	}
	
	public static int getQueryPoolMin(Query query)
	{
   	    int minPool = 10;
		String value = System.getProperty(ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value());
		if (value != null)  
   	    	try {
   	    	    minPool = Integer.valueOf(value);
   	    	}
   	        catch (NumberFormatException nfe) {
    	    	throw new CloudGraphConfigurationException("invalid system query configuration value '"
        	    		+ value + "' for property, "
        	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value(), nfe);
   	        }
        // override it with query specific value
   	    String minPoolValue = query.getConfigurationProperty(
    			ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value());      	       	
   	    if (minPoolValue != null)
   	    	try {
   	    	    minPool = Integer.valueOf(minPoolValue);
   	    	}
   	        catch (NumberFormatException nfe) {
    	    	throw new CloudGraphConfigurationException("invalid query configuration value '"
        	    		+ minPoolValue + "' for property, "
        	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value(), nfe);
   	        }
		return minPool;
	}
	
	public static int getQueryPoolMax(Query query)
	{
   	    int maxPool = 10;
		String value = System.getProperty(ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value());
		if (value != null)  
   	    	try {
   	    		maxPool = Integer.valueOf(value);
   	    	}
   	        catch (NumberFormatException nfe) {
    	    	throw new CloudGraphConfigurationException("invalid system query configuration value '"
        	    		+ value + "' for property, "
        	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value(), nfe);
   	        }
        // override it with query specific value
   	    String maxPoolValue = query.getConfigurationProperty(
    			ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value());      	       	
   	    if (maxPoolValue != null)
   	    	try {
   	    		maxPool = Integer.valueOf(maxPoolValue);
   	    	}
   	        catch (NumberFormatException nfe) {
    	    	throw new CloudGraphConfigurationException("invalid query configuration value '"
        	    		+ maxPoolValue + "' for property, "
        	    		+ ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value(), nfe);
   	        }
		return maxPool;
	}

}
