package org.cloudgraph.web.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ApplicationConfiguration;
import org.cloudgraph.web.config.web.CategoryChartDef;
import org.cloudgraph.web.config.web.ChartDef;
import org.cloudgraph.web.config.web.ColorDef;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PieChartDef;
import org.cloudgraph.web.config.web.StatusDef;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.common.env.EnvProperties;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;


import commonj.sdo.Type;

public class ApplicationConfig {

    private static Log log = LogFactory.getLog(ApplicationConfig.class);
    private static ApplicationConfig instance = null;
    private Map<String, CategoryChartDef> categoryChartMap = new HashMap<String, CategoryChartDef>();
    private Map<String, PieChartDef> pieChartMap = new HashMap<String, PieChartDef>();
    private Map<String, ColorDef> colorMap = new HashMap<String, ColorDef>();
    
    private static final String defaultConfigFileName = "cloudgraph-web-config.xml";      
    private static final String configFilePropertyName = "cloudgraph-web.configuration";      
    private ApplicationConfiguration config;
        
    private ApplicationConfig()
    {
        log.info("initializing...");
        try {
            
            String fileName = EnvProperties.instance().getProperty(
            		configFilePropertyName);
            
            if (fileName == null)
                fileName = defaultConfigFileName;
            
            ApplicationConfigDataBinding configBinding = new ApplicationConfigDataBinding(
	        		new DefaultValidationEventHandler());
	        
            config = unmarshalConfig(fileName, configBinding);
            
	        
            for (ColorDef colorDef : config.getColorDef())
            	colorMap.put(colorDef.getStatus().toString(), colorDef); 
            
            for (CategoryChartDef chartDef : config.getCategoryChartDef())
            {
            	this.validate(chartDef);
                categoryChartMap.put(chartDef.getName().toString(), chartDef); 
            }   
            
            for (PieChartDef chartDef : config.getPieChartDef())
            {
            	this.validate(chartDef);
                pieChartMap.put(chartDef.getName().toString(), chartDef); 
            }   
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ApplicationConfiguration unmarshalConfig(String configFileName, ApplicationConfigDataBinding binding)
    {
    	try {
	        InputStream stream = ApplicationConfig.class.getResourceAsStream(configFileName);
	        if (stream == null)
	            stream = ApplicationConfig.class.getClassLoader().getResourceAsStream(configFileName);
	        if (stream == null)
	            throw new RuntimeException("could not find configuration file resource '" 
	                    + configFileName 
	                    + "' on the current classpath");        
	        
	        JAXBElement root = (JAXBElement)binding.validate(stream);
	        
	        ApplicationConfiguration result = (ApplicationConfiguration)root.getValue();
            return result;
    	}
        catch (UnmarshalException e) {
            throw new RuntimeException(e);
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ApplicationConfig getInstance()
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }
    
    private static synchronized void initializeInstance()
    {
        if (instance == null)
            instance = new ApplicationConfig();
    }

    public ApplicationConfiguration getConfig() {
        return config;
    } 
    
    private void validate(CategoryChartDef chartDef)
    {
    	Type type = PlasmaTypeHelper.INSTANCE.getType(chartDef.getTypeUri(), 
    			chartDef.getTypeName());
    	type.getProperty(chartDef.getCategoryDef().getValuePropertyName());
    	
    	if (chartDef.getStatusValuePropertyName() != null)
        	type.getProperty(chartDef.getStatusValuePropertyName());
    	if (chartDef.getStatusKeyPropertyName() != null)
        	type.getProperty(chartDef.getStatusKeyPropertyName());
    	for (StatusDef statusDef : chartDef.getStatusDef())
    		if (statusDef.getValuePropertyName() != null)
            	type.getProperty(statusDef.getValuePropertyName());
    		else if (chartDef.getStatusValuePropertyName() == null)
    			throw new RuntimeException("Since status value property name is null, expected non-null status value property name for chart definition.");
    			
    }
    
    private void validate(PieChartDef chartDef)
    {
    	Type type = PlasmaTypeHelper.INSTANCE.getType(chartDef.getTypeUri(), 
    			chartDef.getTypeName());
    	if (chartDef.getStatusValuePropertyName() != null)
        	type.getProperty(chartDef.getStatusValuePropertyName());
    	if (chartDef.getStatusKeyPropertyName() != null)
        	type.getProperty(chartDef.getStatusKeyPropertyName());
    	for (StatusDef statusDef : chartDef.getStatusDef())
    		if (statusDef.getValuePropertyName() != null)
            	type.getProperty(statusDef.getValuePropertyName());
    		else if (chartDef.getStatusValuePropertyName() == null)
    			throw new RuntimeException("Since status value property name is null, expected non-null status value property name for chart definition.");
    }
    
    public ChartDef[] getAllChartDefs()
    {
    	ChartDef[] results = new ChartDef[this.categoryChartMap.size()];
    	Iterator iter = this.categoryChartMap.keySet().iterator();
    	for (int i = 0; iter.hasNext(); i++)
    		results[i] = (ChartDef)this.categoryChartMap.get(iter.next());
    	return results;
    }

    public ChartDef getChartDef(ComponentName name)
    {
    	return (ChartDef)this.categoryChartMap.get(name.toString());
    }
    
    public CategoryChartDef getCategoryChartDef(ComponentName name)
    {
    	CategoryChartDef result = this.categoryChartMap.get(name.toString());
    	if (result != null)
    		return result;
    	else
    		throw new IllegalStateException("no category chart definition found for, "
    				+ name.value());
    }
    
    public PieChartDef getPieChartDef(ComponentName name)
    {
    	PieChartDef result = this.pieChartMap.get(name.toString());
    	if (result != null)
    		return result;
    	else
    		throw new IllegalStateException("no pie chart definition found for, "
    				+ name.value());
    }

    public ColorDef getColorDef(String status)
    {
    	return (ColorDef)this.colorMap.get(status);
    }
 }
