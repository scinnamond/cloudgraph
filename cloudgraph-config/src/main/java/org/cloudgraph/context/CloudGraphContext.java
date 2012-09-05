package org.cloudgraph.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.cloudgraph.config.CloudGraphConfig;
import org.plasma.config.DataAccessProviderName;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.Property;

/**
 * Provides context information for HBase such as an 
 * already initialized HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/HBaseConfiguration" target="#">configuration</a> instance
 * as well as other elements. The HBase configuration is setup
 * with values taken first from the PlasmaSDO NoSQL DAS <a href="http://docs.plasma-sdo.org/api/org/plasma/config/PlasmaConfig.html" target="#">configuration</a> for
 * HBase then overridden where key matches exist from the Cloudgraph {@link org.cloudgraph.config.CloudGraphConfig configuration}.  
 * Used by {@link org.cloudgraph.hbase.connect.HBaseConnectionManager} when creating pooled HTable client
 * instances.
 * @see org.cloudgraph.hbase.connect.HBaseConnectionManager
 */
public class CloudGraphContext {
	private static final Log log = LogFactory.getLog(CloudGraphContext.class);
    private static CloudGraphContext instance;
	private Configuration config;
	
	private CloudGraphContext() {		
		 
    	System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
	        "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
	    String oldFactory = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
	    
		if (log.isDebugEnabled())
		    log.debug("creating config...");
		try {
		    config = HBaseConfiguration.create();    	
		    config.clear();
		    // set DAS properties
		    for (org.plasma.config.Property property : PlasmaConfig.getInstance().getDataAccessProvider(DataAccessProviderName.HBASE).getProperties()) {
		    	config.set(property.getName(), property.getValue());
		    }
		    // override plasma DAS properties where matches exits
		    for (org.cloudgraph.config.Property property : CloudGraphConfig.getInstance().getProperties()) {
		    	config.set(property.getName(), property.getValue());
		    }
		    
		} catch (Exception e) {
			log.error("Error when attempting to connect to DB ", e);
		}
		finally {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
					oldFactory);
		}
	}
	
	public static CloudGraphContext instance()
    {
        if (instance == null)
            initInstance();   
        return instance;     
    }

    private static synchronized void initInstance()
    {
        if (instance == null)
            instance = new CloudGraphContext();
    }

	public Configuration getConfig() {
		return config;
	}
    
    
}
