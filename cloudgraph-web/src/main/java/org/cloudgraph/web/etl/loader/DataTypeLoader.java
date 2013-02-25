package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PrimitiveType;
import org.cloudgraph.web.sdo.meta.query.QPackage;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class DataTypeLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(DataTypeLoader.class);
	
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
			
			PrimitiveType type = (PrimitiveType)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)type.getDataGraph().getChangeSummary();
            Package pkg = type.getDataType().getClassifier().getPackageableType().get_package();
            changeSummary.clear(pkg);
            pkg = this.fetchPackage(pkg.getExternalId());
            type.getDataType().getClassifier().getPackageableType().set_package(pkg);

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

    protected Package fetchPackage(String uuid) {
		
		QPackage query = QPackage.newQuery();
		query.select(query.externalId()) 
	     .select(query.name());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no package results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Package result = (Package)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
}
