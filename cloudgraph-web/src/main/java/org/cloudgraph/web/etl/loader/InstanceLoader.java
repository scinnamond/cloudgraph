package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.categorization.Categorization;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.InstanceCategorization;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Package;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class InstanceLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(InstanceLoader.class);
    public InstanceLoader(DataImport dataImport) {
    	super(dataImport);
    }
	
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
        		"http://apls/export/datatype",
        		"http://fs.fed.us/bao/apls/meta");
	}
	
	@Override
	public void load(File file) {
        log.info("loading " + file.getName());
        
        DefaultOptions options = new DefaultOptions("");
        options.setRootNamespacePrefix("xyz");
        options.setValidate(false);
        options.setFailOnValidationError(false);
        
        InputStream xmlloadis;
		try {
			xmlloadis = new FileInputStream(file);
			XMLDocument doc = PlasmaXMLHelper.INSTANCE.load(xmlloadis, 
					null, options);
			
			InstanceSpecification inst = (InstanceSpecification)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)inst.getDataGraph().getChangeSummary();
            Package pkg = inst.getPackageableType().get_package();
            changeSummary.clear(pkg);
            pkg = this.fetchPackage(pkg.getExternalId());
            inst.getPackageableType().set_package(pkg);

			if (inst.getInstanceCategorization() != null) {
				for (InstanceCategorization icatz : inst.getInstanceCategorization()) {
					Categorization catz = icatz.getCategorization();
					
					Category catTemp = catz.getCategory();
					Category cat = fetchCat(catTemp.getExternalId());
					catz.unsetCategory();
					changeSummary.clear(catTemp); // remove the cat as a created DO
					catz.setCategory(cat);
				}	
			}
            
			Clazz clazzTemp = inst.getClazz();
			inst.unsetClazz();
			changeSummary.clear(clazzTemp); // remove created info from change summary
			Clazz clazz = fetchClazz(clazzTemp.getExternalId());			
			inst.setClazz(clazz);
            
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
