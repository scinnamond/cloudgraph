package org.cloudgraph.web;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CommonTest;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.model.Query;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.From;
import org.plasma.query.model.Select;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

/**
 */
public class WebTestCase extends CommonTest {
    private static Log log = LogFactory.getLog(WebTestCase.class);
    protected SDODataAccessClient service;
    protected String classesDir = System.getProperty("classes.dir");
    protected String targetDir = System.getProperty("target.dir");
    
    public void setUp() throws Exception {
        service = new SDODataAccessClient(
        		new HBasePojoDataAccessClient());
    }
    
    public void testGenericQuery() throws IOException {
		Select select = new Select(new String[] {
			"*",	
			"*/*",	
			"*/*/*",	
			"*/*/*/*",	
			"*/*/*/*/*",	
		});
		From from = new From(
			"PaymentRequest",
			"http://plasma.servicelabs.org/platform/hl7/ficr/hd400200"
		);
		Query query = new Query(select, from);
		marshal(query, "test");
		DataGraph[] results = service.find(query);
		if (results != null)
		for (DataGraph graph : results) {
			String xml = serializeGraph(graph);
			log.info(xml);
		}
    }    
        
    protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("test");
        
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
    
    protected void logGraph(DataGraph dataGraph) throws IOException 
    {
        String xml = serializeGraph(dataGraph);
        //log.info("GRAPH: " + xml);    	
    }
    
    protected Query marshal(Query query, long id) {
    	return marshal(query, String.valueOf(id));
    }   
    
    protected Query marshal(Query query, String id) {
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			        new DefaultValidationEventHandler());
			String xml = binding.marshal(query);
	        //log.info("query: " + xml);
			String name = "query-" + id+ ".xml";
			FileOutputStream fos = new FileOutputStream(
					new File(name));
			binding.marshal(query, fos);
	        FileInputStream fis = new FileInputStream(
	        		new File(name));
	        Query q2 = (Query)binding.unmarshal(fis);
	    	return q2;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
    
    protected void waitForMillis(long time) {
       	Object lock = new Object();
        synchronized (lock) {
	        try {
	        	log.info("waiting "+time+" millis...");
	        	lock.wait(time);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");
    }
    
}