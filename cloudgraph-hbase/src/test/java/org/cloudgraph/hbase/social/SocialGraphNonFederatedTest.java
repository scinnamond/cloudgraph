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
package org.cloudgraph.hbase.social;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.test.SocialGraphModelTest;
import org.cloudgraph.test.socialgraph.actor.Actor;
import org.cloudgraph.test.socialgraph.actor.Photo;
import org.cloudgraph.test.socialgraph.actor.Topic;
import org.cloudgraph.test.socialgraph.story.Blog;
import org.plasma.common.test.PlasmaTestSetup;

/**
 * Tests standard operations on a graph. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class SocialGraphNonFederatedTest extends SocialGraphModelTest
{    
    private static Log log = LogFactory.getLog(SocialGraphNonFederatedTest.class);
    private long WAIT_TIME = 1000;
    private String USERNAME_BASE = "social";
    
    
    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(SocialGraphNonFederatedTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testNonFederatedInsert() throws IOException       
    {
    	GraphInfo info = createGraph();
    	
    	log.debug("inserting initial graph:");
    	this.service.commit(info.actor.getDataGraph(), 
    			"test1");
    	    	
    	log.debug("fetching initial graph");
    	Actor fetchedActor = fetchGraph(
    		createGraphQuery(info.actor.getName()));    	
    	String xml = this.serializeGraph(fetchedActor.getDataGraph());
    	log.debug(xml);
    	
    	assertTrue(fetchedActor.getTargetEdgeCount() == 1);
    	Actor fetchedFollower = (Actor)fetchedActor.getTargetEdge(0).getSource();
    	assertTrue(fetchedFollower.getName() != null);
    	assertTrue(info.follower.getName().equals(fetchedFollower.getName()));
    	
    	assertTrue(fetchedActor.getBlogCount() == 2);
    	Blog fetchedBlog = (Blog)fetchedActor.get("blog[@name='"+info.electionBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(info.politics.getName()));
    	
    	log.debug("fetching follower");
    	Actor fetchedFollowerRoot = fetchGraph(
    		createFollowerGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.debug(xml);
        //Since actor is a "bound" root type, there are always
        // two actor rows, and the edge between
        // actor and follower is created in the first row.
        // Because edge and its derivatives are not "bound"
        // we cannot traverse from the follower back through the edge to
        // the actor. If the edge becomes a bound root, we could. 
        assertTrue(fetchedFollowerRoot != null);
        assertTrue(fetchedFollowerRoot.getSourceEdgeCount() == 0);
        
    	log.debug("fetching blog slice");
    	Actor fetchedActorSliceRoot = fetchGraph(
    			createBlogPredicateQuery(info.actor.getName(), 
    					info.electionBlog.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.debug(xml);
    	assertTrue(fetchedActorSliceRoot.getBlogCount() == 1);
    	fetchedBlog = (Blog)fetchedActorSliceRoot.get("blog[@name='"+info.electionBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedActor.getTargetEdgeCount() == 1);
    	fetchedFollower = (Actor)fetchedActor.getTargetEdge(0).getSource();
    	assertTrue(fetchedFollower.getName() != null);
    	assertTrue(info.follower.getName().equals(fetchedFollower.getName()));
                
    	log.debug("fetching photo slice");
    	fetchedActorSliceRoot = fetchGraph(
    			createPhotoPredicateQuery(info.actor.getName(), 
    					info.photo2.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.debug(xml);
    	assertTrue(fetchedActorSliceRoot.getPhotoCount() == 1);
    	Photo fetchedPhoto = (Photo)fetchedActorSliceRoot.get("photo[@name='"+info.photo2.getName()+"']");
    	assertTrue(fetchedPhoto != null);
    	assertTrue(fetchedActor.getTargetEdgeCount() == 1);
    	fetchedFollower = (Actor)fetchedActor.getTargetEdge(0).getSource();
    	assertTrue(fetchedFollower.getName() != null);
    	assertTrue(info.follower.getName().equals(fetchedFollower.getName()));
    }  
    
}
