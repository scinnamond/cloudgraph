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
import org.cloudgraph.hbase.DataTypeGraphModelTest;
import org.cloudgraph.test.datatypes.FloatNode;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.query.QFloatNode;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Float SDO datatype specific partial row-key scan 
 * operations test. 
 */
public class FloatPartialRowKeyScanTest extends DataTypeGraphModelTest {
    private static Log log = LogFactory.getLog(FloatPartialRowKeyScanTest.class);
    private long INCREMENT = 1500;
    private String USERNAME = "float_test";

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(FloatPartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testEqual() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        log.info("equal id1:" + id1);
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);
        log.info("equal float id1:" + Float.valueOf(root1.getFloatField()).toString());

        long id2 = System.currentTimeMillis();
        log.info("equal id2:" + id2);
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);
        log.info("equal float id2:" + Float.valueOf(root2.getFloatField()));

        long id3 = System.currentTimeMillis();
        log.info("equal id3:" + id3);
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        log.info("equal float id3:" + Float.valueOf(root3.getFloatField()));
        
        // fetch a slice
        String sliceName = root1.getChild(3).getName();
        Node fetched = this.fetchSingleGraph(root1.getFloatField(), 
        		sliceName);
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
        log.info("between id1:" + id1);
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);
        log.info("between float id1:" + Float.valueOf(root1.getFloatField()));

        long id2 = id1 + INCREMENT;
        log.info("between id2:" + id2);
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);
        log.info("between float id2:" + Float.valueOf(root2.getFloatField()));

        long id3 = id2 + INCREMENT;
        log.info("between id3:" + id3);
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        log.info("between float id3:" + Float.valueOf(root3.getFloatField()));
        
        Node[] fetched = this.fetchGraphsBetween(
        		root1.getFloatField(), root3.getFloatField());
        assertTrue(fetched.length == 3);

        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
        
    } 
     
    public void testInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        log.info("inclusive id1:" + id1);
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);
        log.info("inclusive float id1:" + Float.valueOf(root1.getFloatField()));

        long id2 = id1 + INCREMENT;
        log.info("inclusive id2:" + id2);
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);
        log.info("inclusive float id2:" + Float.valueOf(root2.getFloatField()));

        long id3 = id2 + INCREMENT;
        log.info("inclusive id3:" + id3);
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g4");
        service.commit(root3.getDataGraph(), USERNAME);
        log.info("inclusive float id3:" + Float.valueOf(root3.getFloatField()));
        
        Node[] fetched = this.fetchGraphsInclusive(
        		root1.getFloatField(), root3.getFloatField());
        assertTrue(fetched.length == 3);
        logGraph(fetched[0].getDataGraph());
        logGraph(fetched[1].getDataGraph());
        logGraph(fetched[2].getDataGraph());
    }  
    
    public void testExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();
        log.info("exclusive id1:" + id1);
        Date now1 = new Date(id1);
        Node root1 = this.createGraph(id1, now1, "g1");
        service.commit(root1.getDataGraph(), USERNAME);
        log.info("exclusive float id1:" + Float.valueOf(root1.getFloatField()));

        long id2 = id1 + INCREMENT;
        log.info("exclusive id2:" + id2);
        Date now2 = new Date(id2);
        Node root2 = this.createGraph(id2, now2, "g2");
        service.commit(root2.getDataGraph(), USERNAME);
        log.info("exclusive float id2:" + Float.valueOf(root2.getFloatField()));

        long id3 = id2 + INCREMENT;
        log.info("exclusive id3:" + id3);
        Date now3 = new Date(id3);
        Node root3 = this.createGraph(id3, now3, "g3");
        service.commit(root3.getDataGraph(), USERNAME);
        log.info("exclusive float id3:" + Float.valueOf(root3.getFloatField()));
        
        Node[] fetched = this.fetchGraphsExclusive(
        		root1.getFloatField(), root3.getFloatField());
        assertTrue(fetched.length == 1);
        logGraph(fetched[0].getDataGraph());
    }    
  
    protected Node fetchSingleGraph(Float id, String name) {    	
    	QFloatNode root = createSelect(name);
    	root.where(root.floatField().eq(id));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }

    protected Node[] fetchGraphsBetween(Float min, Float max) {    	
    	QFloatNode root = createSelect();
    	root.where(root.floatField().between(min, max));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }

    protected Node[] fetchGraphsInclusive(Float min, Float max) {    	
    	QFloatNode root = createSelect();
    	root.where(root.floatField().ge(min)
        		.and(root.floatField().le(max)));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    protected Node[] fetchGraphsExclusive(Float min, Float max) {    	
    	QFloatNode root = createSelect();
    	root.where(root.floatField().gt(min)
        		.and(root.floatField().lt(max)));
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	
    	Node[] profiles = new Node[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Node)result[i].getRootObject();
    	return profiles;
    }
    
    private QFloatNode createSelect(String name)
    {
    	QFloatNode root = QFloatNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());
    	return root;
    }
    
    private QFloatNode createSelect()
    {
    	QFloatNode root = QFloatNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());
    	return root;
    }
    
    protected FloatNode createGraph(long id, Date now, String namePrefix) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(FloatNode.class);
    	FloatNode root = (FloatNode)dataGraph.createRootObject(rootType);
    	fillNode(root, id, now, namePrefix, 0, 0);
    	fillGraph(root, id, now, namePrefix);
        return root;
    }
    
}
