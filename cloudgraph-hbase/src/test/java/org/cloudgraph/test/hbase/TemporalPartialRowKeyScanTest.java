package org.cloudgraph.test.hbase;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.test.datatypes.DatatypeNode;
import org.cloudgraph.test.datatypes.query.QDatatypeNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;

import com.crackoo.domain.Profile;
import com.crackoo.domain.query.QGoal;
import com.crackoo.domain.query.QProfile;
import commonj.sdo.DataGraph;

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
        DatatypeNode root = this.createGraph(id);
    	
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
        DatatypeNode root2 = this.createGraph(id);
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

        DatatypeNode root3 = this.createGraph(id);
        service.commit(root3.getDataGraph(), "test-user");
        
        
        // fetch a slice
        DatatypeNode fetched = this.fetchSingleGraph(id, 
        		"child2", root.getCreatedOn());
        String xml = serializeGraph(fetched.getDataGraph());
        log.info("SLICED GRAPH: " + xml);
        assertTrue(fetched.getRootId() == id);
        assertTrue(fetched.getChildCount() == 1); // expect single slice
        String name = fetched.getString(
        		"child[@name='child2']/@name");
        assertTrue(name.equals("child2"));         
    }  
     
    public void testBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        DatatypeNode root1 = this.createGraph(id1);
    	
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
        DatatypeNode root2 = this.createGraph(id2);
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
        DatatypeNode root3 = this.createGraph(id3);
        service.commit(root3.getDataGraph(), "test-user");
        
        DatatypeNode[] fetched = this.fetchGraphsBetween(
        		id1, id3, "child2", "child2", 
        		root1.getCreatedOn(), root3.getCreatedOn());
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
        DatatypeNode root1 = this.createGraph(id1);
    	
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
        DatatypeNode root2 = this.createGraph(id2);
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
        DatatypeNode root3 = this.createGraph(id3);
        service.commit(root3.getDataGraph(), "test-user");
        
        DatatypeNode[] fetched = this.fetchGraphsInclusive(
        		id1, id3, "child2", "child2", 
        		root1.getCreatedOn(), root3.getCreatedOn());
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
        DatatypeNode root1 = this.createGraph(id1);
    	
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
        DatatypeNode root2 = this.createGraph(id2);
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
        DatatypeNode root3 = this.createGraph(id3);
        service.commit(root3.getDataGraph(), "test-user");
        
        DatatypeNode[] fetched = this.fetchGraphsExclusive(
        		id1, id3, "child2", "child2", 
        		root1.getCreatedOn(), root3.getCreatedOn());
        assertTrue(fetched.length == 1);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        String xml = serializeGraph(fetched[0].getDataGraph());
        log.info("GRAPH1: " + xml);
    }    

    protected DatatypeNode fetchSingleGraph(long id, String name, Object date) {    	
    	QDatatypeNode profile = createSelect(name);
    	
    	//profile.where(profile.profileId().eq(id)
    	//	.and(profile.goal().ISBN().eq(isbn)
    	//	.and(profile.creationDate().eq(date))));
    	
    	profile.where(profile.createdOn().eq(date));
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (DatatypeNode)result[0].getRootObject();
    }

    protected DatatypeNode[] fetchGraphsBetween(long min, long max,
    		String minName, String maxName, 
    		Object minDate, Object maxDate) {    	
    	QDatatypeNode root = createSelect();
    	root.where(root.createdOn().between(minDate, maxDate));
    	//root.where(root.rootId().between(min, max)
    	//	.and(root.name().between(minName, maxName)));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	DatatypeNode[] profiles = new DatatypeNode[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (DatatypeNode)result[i].getRootObject();
    	return profiles;
    }

    protected DatatypeNode[] fetchGraphsInclusive(long min, long max,
    		String minName, String maxName, 
    		Object minDate, Object maxDate) {    	
    	QDatatypeNode root = createSelect();
    	root.where(root.createdOn().ge(minDate)
        		.and(root.createdOn().le(maxDate)));
    	//root.where(root.rootId().ge(min)
        //		.and(root.rootId().le(max)
        //    	.and(root.name().ge(minName)
        //    	.and(root.name().le(maxName)))));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	DatatypeNode[] profiles = new DatatypeNode[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (DatatypeNode)result[i].getRootObject();
    	return profiles;
    }
    
    protected DatatypeNode[] fetchGraphsExclusive(long min, long max,
    		String minIsbn, String maxIsbn, 
    		Object minDate, Object maxDate) {    	
    	QDatatypeNode root = createSelect();
    	root.where(root.createdOn().gt(minDate)
        		.and(root.createdOn().lt(maxDate)));
    	//root.where(root.rootId().gt(min)
    	//	.and(root.rootId().lt(max)
    	//    .and(root.name().gt(minIsbn)
    	//    .and(root.name().lt(maxIsbn)))));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	DatatypeNode[] profiles = new DatatypeNode[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (DatatypeNode)result[i].getRootObject();
    	return profiles;
    }
    
    private QDatatypeNode createSelect(String name)
    {
    	QDatatypeNode root = QDatatypeNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());
    	return root;
    }
    
    private QDatatypeNode createSelect()
    {
    	QDatatypeNode root = QDatatypeNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());
    	return root;
    }
}
