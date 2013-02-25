package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;


import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.categorization.query.QCategory;
import org.cloudgraph.web.sdo.meta.query.QClassifier;
import org.cloudgraph.web.sdo.meta.query.QClazz;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class PropertyLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(PropertyLoader.class);
	
    @Override
	public void define(File queryFile) {
    	
	    log.info("defining Query so new export-specific types are known");
	    InputStream stream;
		try {
			stream = new FileInputStream(queryFile);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	    PlasmaQueryHelper.INSTANCE.define(stream, 
        		"http://apls/export/property",
        		"http://fs.fed.us/bao/apls/meta");
	}
	
	@Override
	public void load(File file) {
        log.info("loading file " + file.getName());
        
        DefaultOptions options = new DefaultOptions("");
        options.setRootNamespacePrefix("xyz");
        options.setValidate(false);
        options.setFailOnValidationError(false);
        
        InputStream xmlloadis;
		try {
			xmlloadis = new FileInputStream(file);
			XMLDocument doc = PlasmaXMLHelper.INSTANCE.load(xmlloadis, 
					null, options);
			
			// do some fix up not yet accommodated by SDO XML load
			Property prop = (Property)doc.getRootObject();
			CoreDataObject obj = ((CoreDataObject)prop);
			//obj.removeValue(Property.PTY_PROPERTY_CATEGORIZATION);
			//prop.unsetPropertyCategorization(); // triggers trace below
			
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)prop.getDataGraph().getChangeSummary();
			if (prop.getPropertyCategorization() != null) {
				for (PropertyCategorization pcatz : prop.getPropertyCategorization()) {
					Categorization catz = pcatz.getCategorization();
					
					Category catTemp = catz.getCategory();
					Category cat = fetchCat(catTemp.getExternalId());
					catz.unsetCategory();
					changeSummary.clear(catTemp); // remove the cat as a created DO
					catz.setCategory(cat);
				}	
			}
			
			Clazz clazzTemp = prop.getSourceClass();
			Clazz clazz = fetchClazz(clazzTemp.getExternalId());			
			prop.unsetSourceClass();
			changeSummary.clear(clazzTemp); // remove created info from change summary
			prop.setSourceClass(clazz);
			
			Classifier clsfrTemp = prop.getDataType();
			Classifier clsfr = fetchClassifier(clsfrTemp.getExternalId());	
			prop.unsetDataType();
			changeSummary.clear(clsfrTemp); // remove created info from change summary
			prop.setDataType(clsfr);

			log.info(prop.dump());
			
			service.commit(doc.getRootObject().getDataGraph(), 
					"dataloader");
			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} 

        //doc.setSchemaLocation(options.getRootElementNamespaceURI()
        //		+ " " + prefix + ".xsd");
	}

}

/*
java.lang.ClassCastException: java.util.ArrayList cannot be cast to org.plasma.sdo.PlasmaDataLink
    at org.plasma.sdo.core.CoreDataObject.oppositeModified(CoreDataObject.java:1031)
    at org.plasma.sdo.core.CoreDataObject.unset(CoreDataObject.java:1011)
    at org.plasma.sdo.core.CoreDataObject.unset(CoreDataObject.java:902)
    at org.cloudgraph.web.export.property.impl.PropertyImpl.unsetPropertyCategorization(PropertyImpl.java:15

    at org.cloudgraph.web.data.loader.PropertyLoader.load(PropertyLoader.java:72)
    at org.cloudgraph.web.data.DataImport.processLoaders(DataImport.java:220)
    at org.cloudgraph.web.data.DataImport.main(DataImport.java:99)
Exception in thread "main" java.lang.RuntimeException: java.lang.ClassCastException: java.util.ArrayList can
to org.plasma.sdo.PlasmaDataLink
    at org.cloudgraph.web.data.loader.PropertyLoader.load(PropertyLoader.java:109)
    at org.cloudgraph.web.data.DataImport.processLoaders(DataImport.java:220)
    at org.cloudgraph.web.data.DataImport.main(DataImport.java:99)
Caused by: java.lang.ClassCastException: java.util.ArrayList cannot be cast to org.plasma.sdo.PlasmaDataLink

    at org.plasma.sdo.core.CoreDataObject.oppositeModified(CoreDataObject.java:1031)
    at org.plasma.sdo.core.CoreDataObject.unset(CoreDataObject.java:1011)
    at org.plasma.sdo.core.CoreDataObject.unset(CoreDataObject.java:902)
    at org.cloudgraph.web.export.property.impl.PropertyImpl.unsetPropertyCategorization(PropertyImpl.java:15 
*/