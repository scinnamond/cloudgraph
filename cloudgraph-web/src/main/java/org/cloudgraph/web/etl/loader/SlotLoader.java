package org.cloudgraph.web.etl.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.InstanceValue;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.Slot;
import org.cloudgraph.web.sdo.meta.ValueSpecification;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.helper.XMLDocument;

public class SlotLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(SlotLoader.class);
    public SlotLoader(DataImport dataImport) {
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
			
			Slot slot = (Slot)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)slot.getDataGraph().getChangeSummary();
            
			Property propTemp = slot.getDefiningFeature();
			Property prop = this.fetchProperty(propTemp.getExternalId());
            slot.unsetDefiningFeature();
            changeSummary.clear(propTemp);
            slot.setDefiningFeature(prop);

            InstanceSpecification instTemp = slot.getOwningInstance();
			InstanceSpecification inst = fetchInstance(instTemp.getExternalId());			
			slot.unsetOwningInstance();
			changeSummary.clear(instTemp); // remove created info from change summary
			slot.setOwningInstance(inst);
			
			if (slot.getValue() != null)
				for (ValueSpecification vs : slot.getValue()) {
					if (vs.getInstanceValue() != null)
					    for (InstanceValue iv : vs.getInstanceValue()) {
					    	InstanceSpecification valueInstTemp = iv.getInstance();
					    	InstanceSpecification valueInst = fetchInstance(valueInstTemp.getExternalId());
					    	iv.unsetInstance();
					    	changeSummary.clear(valueInstTemp);
					    	iv.setInstance(valueInst);
					    }
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
