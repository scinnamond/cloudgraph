package org.cloudgraph.web.data.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaQueryHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import org.cloudgraph.web.export.slot.InstanceSpecification;
import org.cloudgraph.web.export.slot.InstanceValue;
import org.cloudgraph.web.export.slot.Property;
import org.cloudgraph.web.export.slot.Slot;
import org.cloudgraph.web.export.slot.ValueSpecification;
import org.cloudgraph.web.export.slot.query.QInstanceSpecification;
import org.cloudgraph.web.export.slot.query.QProperty;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.helper.XMLDocument;

public class SlotLoader extends AbstractLoader 
    implements Loader
{
    private static Log log = LogFactory.getLog(SlotLoader.class);
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
        		"http://apls/export/datatype",
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
			
			Slot slot = (Slot)doc.getRootObject();
			PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)slot.getDataGraph().getChangeSummary();
            
			Property prop = slot.getDefiningFeature();
            changeSummary.clear(prop);
            prop = this.fetchProperty(prop.getExternalId());
            slot.setDefiningFeature(prop);

            InstanceSpecification inst = slot.getOwningInstance();
			changeSummary.clear(inst); // remove created info from change summary
			inst = fetchInstance(inst.getExternalId());			
			slot.unsetOwningInstance();
			slot.setOwningInstance(inst);
			
			if (slot.getValue() != null)
				for (ValueSpecification vs : slot.getValue()) {
					if (vs.getInstanceValue() != null)
					    for (InstanceValue iv : vs.getInstanceValue()) {
					    	InstanceSpecification valueInst = iv.getInstance();
					    	changeSummary.clear(valueInst);
					    	valueInst = fetchInstance(valueInst.getExternalId());
					    	iv.unsetInstance();
					    	iv.setInstance(valueInst);
					    }
				}
            
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

    protected InstanceSpecification fetchInstance(String uuid) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.seqId());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		InstanceSpecification result = (InstanceSpecification)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
    
    protected Property fetchProperty(String uuid) {
		
		QProperty query = QProperty.newQuery();
		query.select(query.seqId());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		Property result = (Property)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
}
