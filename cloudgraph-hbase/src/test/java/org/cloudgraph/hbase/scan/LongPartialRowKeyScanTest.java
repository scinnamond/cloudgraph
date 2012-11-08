package org.cloudgraph.hbase.scan;

import java.io.IOException;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.DataTypeGraphModelTest;
import org.cloudgraph.test.datatypes.LongNode;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.query.QLongNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Long SDO datatype specific partial row-key scan operations test. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class LongPartialRowKeyScanTest extends DataTypeGraphModelTest {
    private static Log log = LogFactory.getLog(LongPartialRowKeyScanTest.class);
    private long WAIT_TIME = 4;
    private String USERNAME = "long_test";

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(LongPartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testEqual() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = System.currentTimeMillis();
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = System.currentTimeMillis();
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        
        // fetch a slice
        String sliceName = root1.getChild(3).getName();
        Node fetched = this.fetchSingleGraph(id1, sliceName, 
        		root1.getDateTimeField());
        logGraph(fetched.getDataGraph());
        assertTrue(fetched.getChildCount() == 1); // expect single slice
        assertTrue(fetched.getRootId() == id1);
        String name = fetched.getString(
        		"child[@name='"+sliceName+"']/@name");
        assertTrue(name.equals(sliceName));         
    }  
    
    public void testBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsBetween(
        		id1, id3);
        assertTrue(fetched.length == 3);

        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
        
    } 
     
    public void testInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g4");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsInclusive(
        		id1, id3);
        assertTrue(fetched.length == 3);
        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
    }  
    
    public void testExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsExclusive(
        		id1, id3);
        assertTrue(fetched.length == 1);
        logGraph(fetched[0].getDataGraph());
    }    
 
    protected Node fetchSingleGraph(long id, String name, Object date) {    	
    	QLongNode root = createSelect(name);
    	root.where(root.longField().eq(id));
    	this.marshal(root.getModel(), id);
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }

    protected Node[] fetchGraphsBetween(long min, long max) {    	
    	QLongNode root = createSelect();
    	root.where(root.longField().between(min, max));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }

    protected Node[] fetchGraphsInclusive(long min, long max) {    	
    	QLongNode root = createSelect();
    	root.where(root.longField().ge(min)
        		.and(root.longField().le(max)));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    protected Node[] fetchGraphsExclusive(long min, long max) {    	
    	QLongNode root = createSelect();
    	root.where(root.longField().gt(min)
        		.and(root.longField().lt(max)));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    private QLongNode createSelect(String name)
    {
    	QLongNode root = QLongNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());
    	return root;
    }
    
    private QLongNode createSelect()
    {
    	QLongNode root = QLongNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());
    	return root;
    }
    
    protected LongNode createGraph(long id, Date now, String namePrefix) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(LongNode.class);
    	LongNode root = (LongNode)dataGraph.createRootObject(rootType);
    	fillNode(root, id, now, namePrefix, 0, 0);
    	fillGraph(root, id, now, namePrefix);
        return root;
    }
    
}
