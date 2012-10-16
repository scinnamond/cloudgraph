package org.cloudgraph.hbase.scan;

import java.io.IOException;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.DataTypeGraphModelTest;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.DateTimeNode;
import org.cloudgraph.test.datatypes.query.QDateTimeNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class DateTimePartialRowKeyScanTest extends DataTypeGraphModelTest {
    private static Log log = LogFactory.getLog(DateTimePartialRowKeyScanTest.class);
    private long WAIT_TIME = 4;
    private String USERNAME = "datetime_test";
    
    
    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(DateTimePartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testEqual() throws IOException       
    {
        long id = System.currentTimeMillis();
        Date now = new Date(id);
        Node root = this.createGraph(id, now);
        service.commit(root.getDataGraph(), USERNAME);

        // create 2 more w/same id but new date
        Date now2 = new Date(id + WAIT_TIME);
        Node root2 = this.createGraph(id, now2);
        service.commit(root2.getDataGraph(), USERNAME);

        Date now3 = new Date(id + WAIT_TIME + WAIT_TIME);
        Node root3 = this.createGraph(id, now3);
        service.commit(root3.getDataGraph(), USERNAME);        
        
        // fetch a slice
        Node fetched = this.fetchSingleGraph(id, 
        		root.getChild(3).getName(), root.getDateTimeField());
        logGraph(fetched.getDataGraph());
    }  
    
    public void testBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsBetween(
        		id1, id3,  
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 3);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
    } 
     
    public void testInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsInclusive(
        		id1, id3,  
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 3);

        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
    }  
    
    public void testExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1);
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2);
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3);
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsExclusive(
        		id1, id3,  
        		root1.getDateTimeField(), root3.getDateTimeField());
        assertTrue(fetched.length == 1);

        logGraph(fetched[0].getDataGraph());
    }    
 
    protected Node fetchSingleGraph(long id, String name, Object date) {    	
    	QDateTimeNode profile = createSelect(name);
    	
    	profile.where(profile.dateTimeField().eq(date));
    	this.marshal(profile.getModel(), id);
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }

    protected Node[] fetchGraphsBetween(long min, long max,
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().between(minDate, maxDate));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }

    protected Node[] fetchGraphsInclusive(long min, long max,
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().ge(minDate)
        		.and(root.dateTimeField().le(maxDate)));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    protected Node[] fetchGraphsExclusive(long min, long max,
    		Object minDate, Object maxDate) {    	
    	QDateTimeNode root = createSelect();
    	root.where(root.dateTimeField().gt(minDate)
        		.and(root.dateTimeField().lt(maxDate)));
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
    	fillNode(root, id, now, "datetime", 0, 0);
    	fillGraph(root, id, now, "datetime");
        return root;
    }
    
}
