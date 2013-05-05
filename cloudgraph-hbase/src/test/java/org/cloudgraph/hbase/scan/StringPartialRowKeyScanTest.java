/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.hbase.scan;

import java.io.IOException;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.test.DataTypeGraphModelTest;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.StringNode;
import org.cloudgraph.test.datatypes.query.QStringNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * String SDO datatype specific partial row-key scan operations test. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */

public class StringPartialRowKeyScanTest extends DataTypeGraphModelTest {
    private static Log log = LogFactory.getLog(StringPartialRowKeyScanTest.class);
    private long WAIT_TIME = 1;
    private String USERNAME = "string_test";

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StringPartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testEqual() throws IOException       
    {
        long id = System.currentTimeMillis();
        Date now = new Date(id);
        Node root = this.createGraph(id, now, "AAA");
        service.commit(root.getDataGraph(), USERNAME);

        // create 2 more w/same id but new date
        Date now2 = new Date(id + WAIT_TIME);
        Node root2 = this.createGraph(id, now2, "BBB");
        service.commit(root2.getDataGraph(), USERNAME);

        Date now3 = new Date(id + WAIT_TIME + WAIT_TIME);
        Node root3 = this.createGraph(id, now3, "CCC");
        service.commit(root3.getDataGraph(), USERNAME);                
        
        // fetch  
        Node fetched = this.fetchSingleGraph(root2.getRootId(), 
        		root2.getStringField());
        String xml = serializeGraph(fetched.getDataGraph());
        log.info("GRAPH: " + xml);
        assertTrue(fetched.getRootId() == id);
    }  
     
    public void testBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "AAA");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "BBB");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "CCC");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsBetween(
        		id1, id3, 
        		root1.getStringField(), root3.getStringField());
        assertTrue("expected 3 results", fetched.length == 3);
        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
        
    } 
     
    public void testInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "AAA");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "BBB");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "CCC");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsInclusive(
        		id1, id3, 
        		root1.getStringField(), root3.getStringField());
        assertTrue(fetched.length == 3);
        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
    }  
    
    public void testExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "AAA");
        service.commit(root1.getDataGraph(), USERNAME);

        long id2 = id1 + WAIT_TIME;
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "BBB");
        service.commit(root2.getDataGraph(), USERNAME);

        long id3 = id2 + WAIT_TIME;
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "CCC");
        service.commit(root3.getDataGraph(), USERNAME);
        
        Node[] fetched = this.fetchGraphsExclusive(
        		id1, id3, root1.getStringField(), root3.getStringField());
        assertTrue(fetched.length == 1);

        logGraph(fetched[0].getDataGraph());
    }    
  
    protected Node fetchSingleGraph(long id, String name) {    	
    	QStringNode root = createSelect();
    	
    	root.where(root.rootId().eq(id)
        		.and(root.stringField().eq(name)));
    	
    	this.marshal(root.getModel(), id);
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	//FIXME: failing
    	assertTrue("expected 1 results", result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }

    protected Node[] fetchGraphsBetween(long min, long max,
    		String minName, String maxName) {    	
    	QStringNode root = createSelect();
    	root.where(root.rootId().between(min, max)
    		.and(root.stringField().between(minName, maxName)));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }

    protected Node[] fetchGraphsInclusive(long min, long max,
    		String minName, String maxName) {    	
    	QStringNode root = createSelect();
    	root.where(root.rootId().ge(min)
        		.and(root.rootId().le(max))
        		.and(root.stringField().ge(minName))
        		.and(root.stringField().le(maxName)));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    protected Node[] fetchGraphsExclusive(long min, long max,
    		String minName, String maxName) {    	
    	QStringNode root = createSelect();
    	root.where(root.rootId().gt(min)
    		.and(root.rootId().lt(max))
    		.and(root.stringField().gt(minName))
    		.and(root.stringField().lt(maxName)));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    private QStringNode createSelect(String name)
    {
    	QStringNode root = QStringNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());
    	return root;
    }
    
    private QStringNode createSelect()
    {
    	QStringNode root = QStringNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());
    	return root;
    }
    
    protected StringNode createGraph(long id, Date now, String prefix) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(StringNode.class);
    	StringNode root = (StringNode)dataGraph.createRootObject(rootType);
    	fillNode(root, id, now, prefix, 0, 0);
    	fillGraph(root, id, now, prefix);
        return root;
    }
}
