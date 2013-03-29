package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.personalization.Role;
import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.UserRole;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class UserLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(UserLoader.class);
    public UserLoader(DataImport dataImport) {
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
        		"http://apls/export/package",
        		"http://fs.fed.us/bao/apls/personalization");
	}
	
	@Override
	public void load(File file) {
        log.info("loading file " + file.getName());
        
        DefaultOptions options = new DefaultOptions("");
        //options.setRootElementNamespaceURI("123");
        options.setRootNamespacePrefix("xyz");
        options.setValidate(false);
        options.setFailOnValidationError(false);
        
        InputStream xmlloadis;
		try {
			xmlloadis = new FileInputStream(file);
			XMLDocument doc = PlasmaXMLHelper.INSTANCE.load(xmlloadis, 
					null, options);
			User user = (User)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)user.getDataGraph().getChangeSummary();
			
			for (UserRole userRole : user.getUserRole()) {
				Role roleTemp = userRole.getRole();
				Role role = fetchRole(roleTemp.getExternalId());			
				userRole.unsetRole();
				changeSummary.clear(roleTemp); // remove created info from change summary
				userRole.setRole(role);
			}

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
