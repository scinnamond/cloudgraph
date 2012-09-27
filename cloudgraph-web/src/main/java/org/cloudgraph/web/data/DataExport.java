package org.cloudgraph.web.data;

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
import org.cloudgraph.web.config.ImportExportConfig;
import org.cloudgraph.web.config.imex.DataEntity;
import org.jdom2.output.XMLOutputter;
import org.plasma.common.bind.DataBinding;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.Query;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.helper.PlasmaXSDHelper;
import org.plasma.xml.uml.UMLModelAssembler;
import org.xml.sax.SAXException;



public class DataExport {
    private static Log log = LogFactory.getLog(DataExport.class);

    private File baseExportDir = new File("..");
    private File defaultExportDir = new File(baseExportDir, "export");
    private File baseDataDir = new File("../data");
	
    public DataExport(String[] args) throws JAXBException, SAXException, IOException { 	
    	
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
        	
        	if (dataEntity.getTargetDir() != null)
        	    exportDir = new File(baseExportDir, dataEntity.getTargetDir());
        	else if (targetDir != null)
        		exportDir = new File(baseExportDir, targetDir);
        	else
        		log.warn("no export dir defined, using '" + exportDir.getAbsolutePath() + "'");
        	exportDir.mkdirs();
        	
        	if (dataEntity.getTargetNamespaceUri() == null || dataEntity.getTargetNamespaceUri().length() == 0)
        		throw new IllegalArgumentException("expected target namespace URI for data entity");
            File queryFile = new File(baseDataDir, dataEntity.getSource());
            Query query = unmarshalQuery(queryFile);
            DataBinding binding = null; 
            try {
            	binding = createDataBinding(dataEntity.getDataBindingClassName());
            }
            catch (Throwable t) {
            	throw new IllegalArgumentException(t);
            }
            File exportFile = null;
            if (dataEntity.getTarget() != null && dataEntity.getTarget().length() > 0)
            	exportFile = new File(exportDir, dataEntity.getTarget());
            processExport(query, 
            	dataEntity.getTargetNamespaceUri(), 
            	dataEntity.getName(),
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

    private void processExport(Query query, String targetNamespaceURI, String prefix, 
    		DataBinding dataBinding, File exportDir, File exportFile) throws IOException {
        
        try {
            PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
                    new DefaultValidationEventHandler());
            log.info("marshaling query");
            String xml = binding.marshal(query);
            log.info("validating marshaled query");
            query = (Query)binding.validate(xml); 
            assert(query != null);
            log.info(xml);
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
        File xmlSchemaFile = new File(exportDir, prefix + ".xsd");
        OutputStream xsdos = new FileOutputStream(xmlSchemaFile);
        PlasmaXSDHelper.INSTANCE.generate(query, 
        		targetNamespaceURI, "ex", null, 
        		xsdos);
        xsdos.flush();
        xsdos.close();  
        assert(query != null);
                
        log.info("defining XSD so new export-specific types are known");
        FileInputStream xsdis = new FileInputStream(xmlSchemaFile);
        PlasmaXSDHelper.INSTANCE.define(xsdis, null);
        
        log.info("generating query XMI");
        File xmiFile = new File(exportDir, prefix + ".xmi");
        OutputStream xmios = new FileOutputStream(xmiFile);
	    UMLModelAssembler assembler = new UMLModelAssembler(query, 
			   targetNamespaceURI, "tns");	    
	    XMLOutputter outputer = new XMLOutputter();	    
	    outputer.output(assembler.getDocument(), xmios);
	    xmios.flush();
	    xmios.close();
	    assert(query != null);
        
	    int oldStartRange = 0; //query.getStartRange();
	    int oldEndRange = 0; //query.getEndRange();
        log.info("executing count query");
        SDODataAccessClient service = new SDODataAccessClient();
        int count = service.count(query); 
        
        if (exportFile == null) {
	        for (int i = 0; i < count; i++) {
	            query.setStartRange(i);
	            query.setEndRange(i+1);
	        	PlasmaDataObject dataObject = (PlasmaDataObject)service.find(query)[0].getRootObject();
	        	Long id = dataObject.getLong("seqId");
	        	File file = new File(exportDir, 
	            		prefix + "-" + String.valueOf(id) + ".xml");
	            OutputStream xmlos = new FileOutputStream(file);
	            PlasmaXMLHelper.INSTANCE.save(dataObject, 
	            		targetNamespaceURI, null, xmlos);        
	            xmlos.flush();
	            xmlos.close();
	            
	            // validate the XML we just produced against the given Schema/binding
	            log.info("validating XML file: " + file.getName());
	            try {
	                InputStream xmlis = new FileInputStream(file);
	                dataBinding.validate(xmlis);
	            }
	            catch (Throwable t) {
	            	log.error(t.getMessage(), t);
	            }
	        } 
        }
        else {
            OutputStream xmlos = new FileOutputStream(exportFile);
	        for (int i = 0; i < count; i++) {
	            query.setStartRange(i);
	            query.setEndRange(i+1);
	        	PlasmaDataObject dataObject = (PlasmaDataObject)service.find(query)[0].getRootObject();
	            PlasmaXMLHelper.INSTANCE.save(dataObject, 
	            		targetNamespaceURI, null, xmlos);        
	        } 
            xmlos.flush();
            xmlos.close();	            
            // validate the XML we just produced against the given Schema/binding
            log.info("validating XML file: " + exportFile.getName());
            try {
                InputStream xmlis = new FileInputStream(exportFile);
                dataBinding.validate(xmlis);
            }
            catch (Throwable t) {
            	log.error(t.getMessage(), t);
            }        	
        }
        
        query.setStartRange(oldStartRange);
        query.setEndRange(oldEndRange);
        
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
        new DataExport(args);	
    }
}
