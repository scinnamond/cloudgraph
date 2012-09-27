package org.cloudgraph.web.config;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.ImportExportConfiguration;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.common.env.EnvProperties;
import org.xml.sax.SAXException;


public class ImportExportConfig {

    private static Log log = LogFactory.getLog(ImportExportConfig.class);
    private static ImportExportConfig instance = null;
    
    private static final String defaultConfigFileName = "import-export-config.xml";      
    private static final String configFilePropertyName = "import-export.configuration";      
    private ImportExportConfiguration config;
        
    private ImportExportConfig()
    {
        log.info("initializing...");
        try {
            
            String fileName = EnvProperties.instance().getProperty(
            		configFilePropertyName);
            
            if (fileName == null)
                fileName = defaultConfigFileName;
            
            ImportExportConfigDataBinding configBinding = new ImportExportConfigDataBinding(
	        		new DefaultValidationEventHandler());
	        
            config = unmarshalConfig(fileName, configBinding);
            
  
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ImportExportConfiguration unmarshalConfig(String configFileName, ImportExportConfigDataBinding binding)
    {
    	try {
	        InputStream stream = ImportExportConfig.class.getResourceAsStream(configFileName);
	        if (stream == null)
	            stream = ImportExportConfig.class.getClassLoader().getResourceAsStream(configFileName);
	        if (stream == null)
	            throw new RuntimeException("could not find configuration file resource '" 
	                    + configFileName 
	                    + "' on the current classpath");        
	        
	        JAXBElement root = (JAXBElement)binding.validate(stream);
	        
	        ImportExportConfiguration result = (ImportExportConfiguration)root.getValue();
            return result;
    	}
        catch (UnmarshalException e) {
            throw new RuntimeException(e);
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ImportExportConfig getInstance()
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }
    
    private static synchronized void initializeInstance()
    {
        if (instance == null)
            instance = new ImportExportConfig();
    }

    public ImportExportConfiguration getConfig() {
        return config;
    } 
    
 }
