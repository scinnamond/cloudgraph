package org.cloudgraph.test.query;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.personalization.query.QProfile;
import org.cloudgraph.web.sdo.personalization.query.QProfileElementSetting;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.common.test.PlasmaTest;
import org.plasma.query.Query;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

/**
 * 
 */
public class QueryTestCase extends PlasmaTest {
    private static Log log = LogFactory.getLog(QueryTestCase.class);
    protected String classesDir = "./target/classes";
    protected String targetDir = "./target"; 
    int runs = 10;
    
    
    public void testInstanceSpecificationSliceQuery() {
    	String className = "Application";
    	String propName = "Short Name"; 
    	
    	Query query = InstanceSpecificationQuery.createSliceQueryByPropertyName(
    			className, propName);
        long before = System.currentTimeMillis();
        SDODataAccessClient service = new SDODataAccessClient();
        DataGraph[] results = service.find(query);
        
        for (int i = 0; i < results.length; i++) {
        	try {
				log.info(serializeGraph(results[i]));
			} catch (IOException e) {
				e.printStackTrace();
			}
            assertTrue(results[i].getRootObject() instanceof InstanceSpecification);
            InstanceSpecification spec = (InstanceSpecification)results[0].getRootObject();
            assertTrue(className.equals(spec.getClazz().getClassifier().getName()));
            assertTrue(spec.getSlotCount() == 1);
            assertTrue(spec.getSlot(0).getDefiningFeature().getName().equals(propName));
        }
        
        /*
        for (int count = 0; count < runs; count++) {
	        for (int i = 0; i < results.length; i++) {
	        	try {
					log.info(serializeGraph(results[i]));
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
        }
        */
        long after = System.currentTimeMillis();
        float timeSeconds = ((float)(after - before)) / 1000;
        log.info("testInstanceSpecificationQuery: " + String.valueOf(timeSeconds));
    }
    
    public void testPathPredicates() throws Exception {
    	
    	QProfile profile = QProfile.newQuery();
    	profile.setName("my predicate query");
    	QProfileElementSetting setting = QProfileElementSetting.newQuery();
    	profile.select(profile.wildcard())
    	       .select(profile.user().wildcard())
    	       .select(profile.profileElementSetting(setting.seqId().eq(12)
    	          .or(setting.seqId().gt(13))
    	          .or(setting.seqId().lt(14))).wildcard())
    	       .select(profile.profileElementSetting(
    	    		   setting.seqId().gt(777)).setting().wildcard())
    	       .select(profile.user().userRole().wildcard())
    	       .select(profile.user().userRole().role().wildcard());
    	profile.where(profile.user().username().eq("foo")
    		   .or(profile.user().username().eq("bar")
    		   .or(profile.user().username().eq("bas"))));

    	Query q2 = marshal(profile.getModel());
        
        long before = System.currentTimeMillis();
        SDODataAccessClient service = new SDODataAccessClient();
        for (int count = 0; count < runs; count++) {
            DataGraph[] results = service.find(q2);
	        for (int i = 0; i < results.length; i++) {
	            PlasmaDataObject dataObject = (PlasmaDataObject)results[i].getRootObject();
	            //log.info(dataObject.dump());
	        }
        }
        long after = System.currentTimeMillis();
        float timeSeconds = ((float)(after - before)) / 1000;
        log.info("testPathPredicates: " + String.valueOf(timeSeconds));
    }
    
    private Query marshal(Query query) throws JAXBException, FileNotFoundException, SAXException {
        PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
                new DefaultValidationEventHandler());
        String xml = binding.marshal(query);
        //log.info("query: " + xml);
        
        FileOutputStream fos = new FileOutputStream(new File(targetDir, "test-query1.xml"));
        binding.marshal(query, fos);
        
        FileInputStream fis = new FileInputStream(new File(targetDir, "test-query1.xml"));
        Query q2 = (Query)binding.unmarshal(fis);
    	return q2;
    }
    
    private String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("dump");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
}