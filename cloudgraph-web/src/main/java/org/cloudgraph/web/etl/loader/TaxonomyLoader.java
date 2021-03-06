package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class TaxonomyLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(TaxonomyLoader.class);
    public TaxonomyLoader(DataImport dataImport) {
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
        		"http://apls/export/clazz",
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
			Taxonomy tax = (Taxonomy)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)tax.getDataGraph().getChangeSummary();
			changeSummary.endLogging();
            
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
