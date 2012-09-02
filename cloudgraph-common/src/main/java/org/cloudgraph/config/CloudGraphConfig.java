package org.cloudgraph.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.CloudGraphConfiguration;
import org.cloudgraph.config.HTable;
import org.cloudgraph.config.Property;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.common.env.EnvProperties;
import org.xml.sax.SAXException;


/**
 * Configuration marshaling and un-marshaling with 
 * data access convenience methods. Looks for the Java
 * command line '-Dcloudgraph.configuration' setting for the
 * name of the configuration file. If not found looks for
 * the default file name 'cloudgraph-config.xml'. The CloudGraph
 * configuration file must be somewhere on the Java class path.     
 */
public class CloudGraphConfig {

    private static Log log = LogFactory.getLog(CloudGraphConfig.class);
    private static CloudGraphConfig instance = null;
    private static final String PROPERTY_NAME_HGRAPH_CONFIG = "cloudgraph.configuration";     
    private static final String defaultConfigFileName = "cloudgraph-config.xml";     
    
    private CloudGraphConfiguration config;
    
    private Map<QName, TableConfig> graphURIToTableMap = new HashMap<QName, TableConfig>();
    private Map<QName, DataGraphConfig> graphURIToGraphMap = new HashMap<QName, DataGraphConfig>();
        
    private CloudGraphConfig()
    {
        log.debug("initializing...");
        try {
            
            String fileName = EnvProperties.instance().getProperty(
            		PROPERTY_NAME_HGRAPH_CONFIG);
            
            if (fileName == null)
                fileName = defaultConfigFileName;
            
            CloudGraphConfigDataBinding configBinding = new CloudGraphConfigDataBinding(
	        		new CloudGraphConfigValidationEventHandler());
	        
            config = unmarshalConfig(fileName, configBinding);
            
            for (HTable table : config.hTables) {
            	for (DataGraph graph : table.getDataGraphs()) {
            		QName qname = new QName(graph.getUri(), graph.getType());
            		if (graphURIToTableMap.get(qname) != null)
            			throw new CloudGraphConfigurationException("a data graph definition already exists within HTable '"
            					+ table.getName() + "' for type (uri/name), " 
            					+ graph.getUri() + "#" + graph.getType());
            		graphURIToTableMap.put(qname, new TableConfig(table));
            		graphURIToGraphMap.put(qname, new DataGraphConfig(graph));
            	}
            }
        }
        catch (SAXException e) {
            throw new CloudGraphConfigurationException(e);
        }
        catch (JAXBException e) {
            throw new CloudGraphConfigurationException(e);
        }
    }    
    
    @SuppressWarnings("unchecked")
    private CloudGraphConfiguration unmarshalConfig(String configFileName, CloudGraphConfigDataBinding binding)
    {
    	try {
	        InputStream stream = CloudGraphConfiguration.class.getResourceAsStream(configFileName);
	        if (stream == null)
	            stream = CloudGraphConfig.class.getClassLoader().getResourceAsStream(configFileName);
	        if (stream == null)
	            throw new CloudGraphConfigurationException("could not find configuration file resource '" 
	                    + configFileName 
	                    + "' on the current classpath");        
	        
	        CloudGraphConfiguration result = (CloudGraphConfiguration)binding.validate(stream);
            return result;
    	}
        catch (UnmarshalException e) {
            throw new CloudGraphConfigurationException(e);
        }
        catch (JAXBException e) {
            throw new CloudGraphConfigurationException(e);
        }
    }
    
    public void marshal(OutputStream stream) {
        try {
        	CloudGraphConfigDataBinding configBinding = new CloudGraphConfigDataBinding(
                    new DefaultValidationEventHandler());
            configBinding.marshal(this.config, stream);
        } catch (JAXBException e1) {
            throw new CloudGraphConfigurationException(e1);
        } catch (SAXException e1) {
            throw new CloudGraphConfigurationException(e1);
        }
    }
    
    public static CloudGraphConfig getInstance()
        throws CloudGraphConfigurationException
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }
    
    private static synchronized void initializeInstance()
    {
        if (instance == null)
            instance = new CloudGraphConfig();
    }

    public CloudGraphConfiguration getConfig() {
        return config;
    } 
    
    public List<Property> getProperties() {
        return config.properties;
    } 
    
    public TableConfig getHTable(QName qname) {
    	TableConfig result = this.graphURIToTableMap.get(qname);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no HTable configured for" +
    				" CloudGraph '" + qname.toString() + "'");
    	return result;
    }
    
    public String getHTableName(QName qname) {
    	TableConfig result = this.graphURIToTableMap.get(qname);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no HTable configured for" +
    				" CloudGraph '" + qname.toString() + "'");
    	return result.getTable().getName();
    }
    
    public DataGraphConfig getCloudGraph(QName qname) {
    	DataGraphConfig result = this.graphURIToGraphMap.get(qname);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no CloudGraph configured for" +
    				" '" + qname.toString() + "'");
    	return result;
    }
}
