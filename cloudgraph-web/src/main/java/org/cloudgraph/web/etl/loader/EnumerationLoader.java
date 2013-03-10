package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.query.QPackage;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class EnumerationLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(EnumerationLoader.class);
    public EnumerationLoader(DataImport dataImport) {
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
        		"http://apls/export/enumeration",
        		"http://fs.fed.us/bao/apls/meta");
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
			
			Enumeration enm = (Enumeration)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)enm.getDataGraph().getChangeSummary();            
			Package pkg = enm.getDataType().getClassifier().getPackageableType().get_package();
            changeSummary.clear(pkg);
            pkg = this.fetchPackage(pkg.getExternalId());
            enm.getDataType().getClassifier().getPackageableType().set_package(pkg);

            enm.getDataType().setExternalId(UUID.randomUUID().toString()); // not exporting for some reason !!
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
