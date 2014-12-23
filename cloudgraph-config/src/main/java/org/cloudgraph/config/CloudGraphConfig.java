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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.common.env.EnvProperties;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import commonj.sdo.Type;


/**
 * Configuration marshaling and un-marshaling with 
 * data access convenience methods. Looks for the Java
 * command line '-Dcloudgraph.configuration' setting for the
 * name of the configuration file. If not found looks for
 * the default file name 'cloudgraph-config.xml'. The CloudGraph
 * configuration file must be somewhere on the Java class path.     
 * @author Scott Cinnamond
 * @since 0.5
 */
public class CloudGraphConfig {

    private static Log log = LogFactory.getLog(CloudGraphConfig.class);
    private static CloudGraphConfig instance = null;
    private static final String PROPERTY_NAME_CLOUDGRAPH_CONFIG = "cloudgraph.configuration";     
    private static final String defaultConfigFileName = "cloudgraph-config.xml";     
    private Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );
    private CloudGraphConfiguration config;
    
    private Map<QName, TableConfig> graphURIToTableMap = new HashMap<QName, TableConfig>();
    private Map<QName, DataGraphConfig> graphURIToGraphMap = new HashMap<QName, DataGraphConfig>();
    private Map<String, TableConfig> tableNameToTableMap = new HashMap<String, TableConfig>();
    private Map<String, Property> propertyNameToPropertyMap = new HashMap<String, Property>();
        
    private CloudGraphConfig()
    {
        log.debug("initializing...");
        try {
            
            String fileName = EnvProperties.instance().getProperty(
            		PROPERTY_NAME_CLOUDGRAPH_CONFIG);
            
            if (fileName == null)
                fileName = defaultConfigFileName;
            
            CloudGraphConfigDataBinding configBinding = new CloudGraphConfigDataBinding(
	        		new CloudGraphConfigValidationEventHandler());
	        
            config = unmarshalConfig(fileName, configBinding);
            
            for (Property prop : config.getProperties())
            	propertyNameToPropertyMap.put(prop.getName(), prop);
            
            for (Table table : config.tables) {
            	TableConfig tableConfig = new TableConfig(table, this);
        		if (this.tableNameToTableMap.get(tableConfig.getName()) != null)
        			throw new CloudGraphConfigurationException("a table definition already exists for table '"
        					+ table.getName() + "'");
            	this.tableNameToTableMap.put(table.getName(), tableConfig);
            	for (DataGraph graph : table.getDataGraphs()) {
            		
            		DataGraphConfig dataGraphConfig = new DataGraphConfig(graph, tableConfig);
            		
            		QName qname = new QName(graph.getUri(), graph.getType());
            		PlasmaType configuredType = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(qname.getNamespaceURI(), 
            				qname.getLocalPart());
            		//if (configuredType.isAbstract())
            		//	throw new CloudGraphConfigurationException("a data graph definition within table '"
            		//			+ table.getName() + "' has an abstract type (uri/name), " 
            		//			+ graph.getUri() + "#" + graph.getType() + " - use a non abstract type");
            		if (graphURIToTableMap.get(qname) != null)
            			throw new CloudGraphConfigurationException("a data graph definition already exists within table '"
            					+ table.getName() + "' for type (uri/name), " 
            					+ graph.getUri() + "#" + graph.getType());
            		graphURIToTableMap.put(qname, tableConfig);
            		graphURIToGraphMap.put(qname, dataGraphConfig);
            		/*
            		Map<QName, PlasmaType> hierarchy = new HashMap<QName, PlasmaType>();
            		this.collectTypeHierarchy(configuredType, hierarchy);
            		
            		for (PlasmaType type : hierarchy.values()) {
            			qname = type.getQualifiedName();
                		if (graphURIToTableMap.get(qname) != null)
                			throw new CloudGraphConfigurationException("a data graph definition already exists within table '"
                					+ table.getName() + "' for type (uri/name), " 
                					+ graph.getUri() + "#" + graph.getType());
                		graphURIToTableMap.put(qname, tableConfig);
                		graphURIToGraphMap.put(qname, dataGraphConfig);
            		}
            		*/
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
    
    public Property findProperty(String name) {
    	return this.propertyNameToPropertyMap.get(name);
    }
    
    /**
     * Returns a table configuration for the given qualified SDO 
     * Type name or null if not found.
     * @param typeName the qualified name of an SDO Type 
     * @return the table configuration or null if not found
     */
    public TableConfig findTable(QName typeName) {
    	PlasmaType type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(typeName.getNamespaceURI(), 
    			typeName.getLocalPart());
		return this.graphURIToTableMap.get(
				type.getQualifiedName());
    }
    
    /**
     * Returns a table configuration for the given qualified SDO 
     * Type name.
     * @param typeName the qualified name of an SDO Type 
     * @return the table configuration
     * @throws CloudGraphConfigurationException if the given name is not found
     */
    public TableConfig getTable(QName typeName) {
    	TableConfig result = findTable(typeName);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no HTable configured for " +
    				" graph URI '" + typeName.toString() + "'");
    	return result;
    }

    /**
     * Returns a table configuration for the given SDO 
     * Type or null if not found.
     * @param type the SDO Type 
     * @return the table configuration or null if not found
     */
    public TableConfig findTable(Type type) {
		return this.graphURIToTableMap.get(
				((PlasmaType)type).getQualifiedName());
    }
    
    /**
     * Returns a table configuration for the given SDO 
     * Type.
     * @param type the SDO Type 
     * @return the table configuration
     * @throws CloudGraphConfigurationException if the given type is not found
     */
    public TableConfig getTable(Type type) {
    	TableConfig result = findTable(type);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no HTable configured for " +
    				" graph URI '" + ((PlasmaType)type).getQualifiedName() + "'");
    	return result;
    }
    
	private void collectTypeHierarchy(PlasmaType type, Map<QName, PlasmaType> map) {
		map.put(type.getQualifiedName(), type);
		// get ancestry
		collectBaseTypes(type, map);
		Collection<PlasmaType> coll = map.values();
		PlasmaType[] types = new PlasmaType[coll.size()];
		coll.toArray(types);
		
		// get all derived type for every ancestor
		for (int i = 0; i < types.length; i++) {
			PlasmaType baseType = types[i];
			collectSubTypes(baseType, map);
		}		
    }
	
	private void collectBaseTypes(PlasmaType type, Map<QName, PlasmaType> map) {
		for (Type t : type.getBaseTypes()) {
			PlasmaType baseType = (PlasmaType)t;
			map.put(baseType.getQualifiedName(), baseType);
			collectBaseTypes(baseType, map);
		}
	}
	
	private void collectSubTypes(PlasmaType type, Map<QName, PlasmaType> map) {
		for (Type t : type.getSubTypes()) {
			PlasmaType subType = (PlasmaType)t;
			map.put(subType.getQualifiedName(), subType);
			collectSubTypes(subType, map);
		}
 	}
    
    /**
     * Returns a table configuration based on the given table name.
     * @param tableName the table name or null if not found.
     * @return the table configuration  or null if not found.
     */
    public TableConfig findTable(String tableName) {
    	TableConfig result = this.tableNameToTableMap.get(tableName);
    	return result;
    }

    /**
     * Returns a table configuration based on the given table name.
     * @param tableName the table name
     * @return the table configuration
     * @throws CloudGraphConfigurationException if the given name is not found
     */
    public TableConfig getTable(String tableName) {
    	TableConfig result = this.tableNameToTableMap.get(tableName);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no table configured for" +
    				" name '" + tableName.toString() + "'");
    	return result;
    }
    
    /**
     * Returns a table name for the given qualified SDO 
     * Type name.
     * @param typeName the qualified name of an SDO Type 
     * @return the table name
     */
    public String getTableName(QName typeName) {
    	TableConfig result = this.graphURIToTableMap.get(typeName);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no HTable configured for" +
    				" CloudGraph '" + typeName.toString() + "'");
    	return result.getTable().getName();
    }
    
    /**
     * Returns a data graph config for the given qualified SDO 
     * Type name or null of not exists.
     * @param typeName the qualified name of an SDO Type 
     * @return a data graph config for the given qualified SDO 
     * Type name or null of not exists. 
     */
    public DataGraphConfig findDataGraph(QName qname) {
    	DataGraphConfig result = this.graphURIToGraphMap.get(qname);
    	return result;
    }

    /**
     * Returns a data graph config for the given qualified SDO 
     * Type name.
     * @param typeName the qualified name of an SDO Type 
     * @return a data graph config for the given qualified SDO 
     * Type name.
     * @throws CloudGraphConfigurationException if no configured data graph
     * exists for the given qualified SDO 
     * Type name
     */
    public DataGraphConfig getDataGraph(QName qname) {
    	DataGraphConfig result = this.graphURIToGraphMap.get(qname);
    	if (result == null)
    		throw new CloudGraphConfigurationException("no configured for" +
    				" '" + qname.toString() + "'");
    	return result;
    }

	public Charset getCharset() {
		return charset;
	}  
	
}
