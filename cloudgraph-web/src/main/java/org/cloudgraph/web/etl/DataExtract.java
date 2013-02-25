package org.cloudgraph.web.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.output.XMLOutputter;
import org.plasma.common.bind.DataBinding;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.config.ConfigurationException;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.Query;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.core.CoreXMLDocument;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.helper.PlasmaXSDHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.plasma.sdo.xml.XMLOptions;
import org.plasma.xml.uml.UMLModelAssembler;
import org.xml.sax.SAXException;

import org.cloudgraph.web.config.ImportExportConfig;
import org.cloudgraph.web.config.imex.DataEntity;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;


public class DataExtract {
    private static Log log = LogFactory.getLog(DataExtract.class);

    private File baseExportDir = new File("./");
    private File defaultExportDir = new File(baseExportDir, "export");
    private File baseDataDir = new File("./");
	
    public DataExtract(String[] args) throws JAXBException, SAXException, IOException { 	
    	
		for (org.cloudgraph.web.config.imex.DataExport dataExport : ImportExportConfig.getInstance().getConfig().getDataExport()) 
		{
			log.info("processing data export: " + dataExport.getName());
			processDataEntities(dataExport.getDataEntity(), 
					dataExport.getTargetDir());
		}   	    	
    }
    
    private void processDataEntities(List<DataEntity> list, String targetDir) throws IOException {
        for (DataEntity dataEntity : list) {
        	File exportDir = defaultExportDir;
        	
        	if (dataEntity.getTargetDir() != null) {
        		if (targetDir != null) {
        	        exportDir = new File(new File(baseExportDir, targetDir), 
        	        		dataEntity.getTargetDir());
        		}
        		else {
        	        exportDir = new File(baseExportDir, 
        	        		dataEntity.getTargetDir());
        		}
        	}
        	else if (targetDir != null) {
        		exportDir = new File(baseExportDir, targetDir);
        	}
        	else
        		log.warn("no export dir defined, using '" + exportDir.getAbsolutePath() + "'");
        	exportDir.mkdirs();
        	
        	if (dataEntity.getTargetNamespaceUri() == null || dataEntity.getTargetNamespaceUri().length() == 0)
        		throw new IllegalArgumentException("expected target namespace URI for data entity");
            File queryFile = new File(baseDataDir, dataEntity.getSource());
            Query query = unmarshalQuery(queryFile);
            DataBinding binding = null; 
            
            if (dataEntity.getDataBindingClassName() != null)
	            try {
	            	binding = createDataBinding(dataEntity.getDataBindingClassName());
	            }
	            catch (Throwable t) {
	            	throw new IllegalArgumentException(t);
	            }
            File exportFile = null;
            if (dataEntity.getTarget() != null && dataEntity.getTarget().length() > 0)
            	exportFile = new File(exportDir, dataEntity.getTarget());
            processExport(dataEntity, query, 	
            	binding, exportDir, exportFile);        	
        }
    	
    }
    
    private DataBinding createDataBinding(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> c = Class.forName(className);
        Class<?>[] paramTypes = new Class[0];
        Object[] args = new Object[0];
        Constructor<?> constructor = c.getConstructor(paramTypes);
        DataBinding result = (DataBinding)constructor.newInstance(args);
        return result;
    }  

    private void processExport(DataEntity dataEntity,
    		Query query,  
    		DataBinding dataBinding, File exportDir, File exportFile) throws IOException {
        
        try {
            PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
                    new DefaultValidationEventHandler());
            log.debug("marshaling query");
            String xml = binding.marshal(query);
            log.debug("validating marshaled query");
            query = (Query)binding.validate(xml); 
            assert(query != null);
            log.debug(xml);
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }        

        log.info("generating query XSD");
        File xmlSchemaFile = new File(exportDir, 
        		dataEntity.getName() + ".xsd");
        OutputStream xsdos = new FileOutputStream(xmlSchemaFile);
        PlasmaXSDHelper.INSTANCE.generate(query, 
        		dataEntity.getTargetNamespaceUri(), "ex", null, 
        		xsdos);
        xsdos.flush();
        xsdos.close();  
        assert(query != null);
                
        log.info("defining XSD so new export-specific types are known");
        FileInputStream xsdis = new FileInputStream(xmlSchemaFile);
        try {
            PlasmaXSDHelper.INSTANCE.define(xsdis, null);
        }
        catch (ConfigurationException e) {
        	log.warn(e.getMessage());
        }
        
        log.info("generating query XMI");
        File xmiFile = new File(exportDir, dataEntity.getName() + ".xmi");
        OutputStream xmios = new FileOutputStream(xmiFile);
	    UMLModelAssembler assembler = new UMLModelAssembler(query, 
	    		dataEntity.getTargetNamespaceUri(), "tns");	    
	    XMLOutputter outputer = new XMLOutputter();
	    outputer.output(assembler.getDocument(), xmios);
	    xmios.flush();
	    xmios.close();
	    assert(query != null);
        SDODataAccessClient service = new SDODataAccessClient();
        DataGraph[] graphs = service.find(query);
        
        XMLOptions options = new DefaultOptions(
        		dataEntity.getTargetNamespaceUri(), null);
        if (exportFile == null) {
	        for (int i = 0; i < graphs.length; i++) {
	        	File file = new File(exportDir, 
	        			dataEntity.getName() + "-" + String.valueOf(i+1) + ".xml");
	            OutputStream xmlos = new FileOutputStream(file);
	        	XMLDocument doc = new CoreXMLDocument(graphs[i].getRootObject(), options);
	        	doc.setXMLDeclaration(true);
	            PlasmaXMLHelper.INSTANCE.save(doc, xmlos, options);        
		        xmlos.flush();
		        xmlos.close();	            
	        }
        }
        else { // all results to a single file	        
	        OutputStream xmlos = new FileOutputStream(exportFile);
	        if (graphs.length > 1) {
	        	String start = "<" + dataEntity.getName() 
	        			+ " xmlns:" + options.getRootNamespacePrefix() + "=\"" 
	        			+ dataEntity.getTargetNamespaceUri() 
	        			+ "\"" + ">\n";
	        	xmlos.write(start.getBytes());
	        }
	        for (DataGraph graph : graphs) {
	        	XMLDocument doc = new CoreXMLDocument(graph.getRootObject(), options);
	        	doc.setXMLDeclaration(false);
	            PlasmaXMLHelper.INSTANCE.save(doc, xmlos, options);        
	        }
	        if (graphs.length > 1) {
	        	String end = "\n</" + dataEntity.getName() + ">";
	        	xmlos.write(end.getBytes());
	        }
	        xmlos.flush();
	        xmlos.close();	            
	        if (dataBinding != null) {
	            // validate the XML we just produced against the given Schema/binding
	            log.info("validating XML file: " + exportFile.getAbsolutePath());
	            try {
	                InputStream xmlis = new FileInputStream(exportFile);
	                dataBinding.validate(xmlis);
	            }
	            catch (Throwable t) {
	            	log.error(t.getMessage(), t);
	            } 
	        }
        }
        
    }  
    
    private Query unmarshalQuery(File queryFile) {
        Query query = null;
        try {
            PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
                    new DefaultValidationEventHandler());
                  
            FileInputStream is = new FileInputStream(queryFile);
            query = (Query)binding.validate(is); 
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
		} 
        return query;
    }
	
    public static void main(String[] args) throws JAXBException, SAXException, IOException {
        new DataExtract(args);	
    }
}
