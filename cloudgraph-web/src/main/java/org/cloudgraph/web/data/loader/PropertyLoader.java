package org.cloudgraph.web.data.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import org.cloudgraph.web.export.property.Categorization;
import org.cloudgraph.web.export.property.Category;
import org.cloudgraph.web.export.property.Classifier;
import org.cloudgraph.web.export.property.Clazz;
import org.cloudgraph.web.export.property.Property;
import org.cloudgraph.web.export.property.PropertyCategorization;
import org.cloudgraph.web.export.property.query.QCategory;
import org.cloudgraph.web.export.property.query.QClassifier;
import org.cloudgraph.web.export.property.query.QClazz;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class PropertyLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(PropertyLoader.class);
	protected SDODataAccessClient service = new SDODataAccessClient();
	
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
        		"http://org.cloudgraph/web/meta");
	}
	
	@Override
	public void load(File file) {
        log.info("loading data");
        
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
					
					Category temp = catz.getCategory();
					changeSummary.clear(temp); // remove the cat as a created DO
					//Category cat = fetchCat(temp.getExternalId());
					Category cat = fetchCat(temp.getId(), temp.getName());
					catz.unsetCategory();
					catz.setCategory(cat);
					
				}	
			}
			
			Clazz clazz = prop.getSourceClass();
			changeSummary.clear(clazz); // remove created info from change summary
			clazz = fetchClazz(clazz.getExternalId());			
			prop.unsetSourceClass();
			prop.setSourceClass(clazz);
			
			Classifier clsfr = prop.getDataType();
			changeSummary.clear(clsfr); // remove created info from change summary
			clsfr = fetchClassifier(clsfr.getExternalId());	
			prop.unsetDataType();
			prop.setDataType(clsfr);

			log.info(prop.dump());
			
			SDODataAccessClient service = new SDODataAccessClient();
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
    protected Category fetchCat(String uuid) {
		
		QCategory query = QCategory.newQuery();
		query.select(query.seqId());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		Category result = (Category)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
	
    protected Clazz fetchClazz(String uuid) {
		
		QClazz query = QClazz.newQuery();
		query.select(query.seqId());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		Clazz result = (Clazz)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
	
    protected Category fetchCat(int id, String name) {
		
		QCategory query = QCategory.newQuery();
		query.select(query.seqId());
		query.where(query.id().eq(id)
			.and(query.name().eq(name)));
		
		DataGraph[] results = service.find(query);
		Category result = (Category)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
	
    protected Clazz fetchClazz(Long seqId) {
		
		QClazz query = QClazz.newQuery();
		query.select(query.seqId());
		query.where(query.seqId().eq(seqId));
		
		DataGraph[] results = service.find(query);
		Clazz result = (Clazz)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
		
    protected Classifier fetchClassifier(String uuid) {
		
		QClassifier query = QClassifier.newQuery();
		query.select(query.seqId());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		Classifier result = (Classifier)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
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