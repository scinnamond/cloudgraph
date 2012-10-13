package org.cloudgraph.test.hbase;

import java.io.IOException;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.DateTimeNode;
import org.cloudgraph.test.datatypes.query.QDateTimeNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class TemporalPartialRowKeyScanTest extends DatatypesModelTest {
    private static Log log = LogFactory.getLog(TemporalPartialRowKeyScanTest.class);

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(TemporalPartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testEqual() throws IOException       
    {
        long id = System.currentTimeMillis();
        Date now = new Date();
        Node root = this.createGraph(id, now);
    	
    	//save the graph
        service.commit(root.getDataGraph(), "test-user");
       	Object lock = new Object();
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        // create a couple more so we know the scan really grabbed
        // only 1 not just the last one. 
        now = new Date();
        Node root2 = this.createGraph(id, now);
        service.commit(root2.getDataGraph(), "test-user");
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        now = new Date();
        Node root3 = this.createGraph(id, now);
        service.commit(root3.getDataGraph(), "test-user");
        
        
        // fetch a slice
        Node fetched = this.fetchSingleGraph(id, 
        		root.getChild(3).getName(), root.getDateTimeField());
        String xml = serializeGraph(fetched.getDataGraph());
        log.info("SLICED GRAPH: " + xml);
        assertTrue(fetched.getRootId() == id);
        //assertTrue(fetched.getChildCount() == 1); // expect single slice
        //String name = fetched.getString(
        //		"child[@name='child2']/@name");
        //assertTrue(name.equals("child2"));         
    }  

/*    
    public void testBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
    	
    	//save the graph
        service.commit(root1.getDataGraph(), "test-user");
       	Object lock = new Object();
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        // create a couple more so we know the scan really grabbed
        // only 1 not just the last one. 
        long id2 = System.currentTimeMillis();
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), "test-user");
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        long id3 = System.currentTimeMillis();
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), "test-user");
        
        Node[] fetched = this.fetchGraphsBetween(
        		id1, id3, "child2", "child2", 
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 3);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        String xml = serializeGraph(fetched[0].getDataGraph());
        log.info("GRAPH1: " + xml);

        //assertTrue(fetchedProfiles[1].getProfileId() == id2);
        xml = serializeGraph(fetched[1].getDataGraph());
        log.info("GRAPH2: " + xml);
        
        //assertTrue(fetchedProfiles[2].getProfileId() == id3);
        xml = serializeGraph(fetched[2].getDataGraph());
        log.info("GRAPH3: " + xml);
        
    } 
     
    public void testInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
    	
    	//save the graph
        service.commit(root1.getDataGraph(), "test-user");
       	Object lock = new Object();
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        // create a couple more so we know the scan really grabbed
        // only 1 not just the last one. 
        long id2 = System.currentTimeMillis();
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), "test-user");
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        long id3 = System.currentTimeMillis();
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), "test-user");
        
        Node[] fetched = this.fetchGraphsInclusive(
        		id1, id3, "child2", "child2", 
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 3);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        String xml = serializeGraph(fetched[0].getDataGraph());
        log.info("GRAPH1: " + xml);

        //assertTrue(fetchedProfiles[1].getProfileId() == id2);
        xml = serializeGraph(fetched[1].getDataGraph());
        log.info("GRAPH2: " + xml);
        
        //assertTrue(fetchedProfiles[2].getProfileId() == id3);
        xml = serializeGraph(fetched[2].getDataGraph());
        log.info("GRAPH3: " + xml);
    }  
    
    public void testExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
    	
    	//save the graph
        service.commit(root1.getDataGraph(), "test-user");
       	Object lock = new Object();
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        // create a couple more so we know the scan really grabbed
        // only 1 not just the last one. 
        long id2 = System.currentTimeMillis();
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), "test-user");
        synchronized (lock) {
	        try {
	        	log.info("waiting 10 millis...");
	        	lock.wait(10);
	        }
	        catch (InterruptedException e) {
	        	log.error(e.getMessage(), e);
	        }
        }
        log.info("...continue");

        long id3 = System.currentTimeMillis();
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), "test-user");
        
        Node[] fetched = this.fetchGraphsExclusive(
        		id1, id3, "child2", "child2", 
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 1);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        String xml = serializeGraph(fetched[0].getDataGraph());
        log.info("GRAPH1: " + xml);
    }    
*/ 
    protected Node fetchSingleGraph(long id, String name, Object date) {    	
    	QDateTimeNode profile = createSelect(name);
    	
    	//profile.where(profile.profileId().eq(id)
    	//	.and(profile.goal().ISBN().eq(isbn)
    	//	.and(profile.creationDate().eq(date))));
    	
    	profile.where(profile.dateTimeField().eq(date));
    	this.marshal(profile.getModel(), id);
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }

    protected Node[] fetchGraphsBetween(long min, long max,
    		String minName, String maxName, 
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().between(minDate, maxDate));
    	//root.where(root.rootId().between(min, max)
    	//	.and(root.name().between(minName, maxName)));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }

    protected Node[] fetchGraphsInclusive(long min, long max,
    		String minName, String maxName, 
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().ge(minDate)
        		.and(root.dateTimeField().le(maxDate)));
    	//root.where(root.rootId().ge(min)
        //		.and(root.rootId().le(max)
        //    	.and(root.name().ge(minName)
        //    	.and(root.name().le(maxName)))));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    protected Node[] fetchGraphsExclusive(long min, long max,
    		String minIsbn, String maxIsbn, 
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().gt(minDate)
        		.and(root.dateTimeField().lt(maxDate)));
    	//root.where(root.rootId().gt(min)
    	//	.and(root.rootId().lt(max)
    	//    .and(root.name().gt(minIsbn)
    	//    .and(root.name().lt(maxIsbn)))));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    private QDateTimeNode createSelect(String name)
    {
    	QDateTimeNode root = QDateTimeNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());
    	return root;
    }
    
    private QDateTimeNode createSelect()
    {
    	QDateTimeNode root = QDateTimeNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());
    	return root;
    }
    
    protected DateTimeNode createGraph(long id, Date now) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(DateTimeNode.class);
    	DateTimeNode root = (DateTimeNode)dataGraph.createRootObject(rootType);
    	fillNode(root, id, now, 0, 0);
    	fillGraph(root, id, now);
        return root;
    }
}
