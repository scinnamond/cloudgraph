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
 * Tests federated operations on a graph. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class SocialGraphFederationTest extends SocialGraphModelTest {
    private static Log log = LogFactory.getLog(SocialGraphFederationTest.class);
    
    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(SocialGraphFederationTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
          
    public void testFederatedInsert() throws IOException       
    {
    	GraphInfo info = createGraph();
    	
    	String xml = this.serializeGraph(info.actor.getDataGraph());
    	log.debug("inserting initial graph:");
    	log.debug(xml);
    	this.service.commit(info.actor.getDataGraph(), 
    			"test1");    	
    	
    	log.debug("fetching initial graph");
    	Actor fetchedActor = fetchGraph(
    		createGraphQuery(info.actor.getName()));    	
    	xml = this.serializeGraph(fetchedActor.getDataGraph());
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
    	
    	log.debug("fetching follower graph");
    	Actor fetchedFollowerRoot = fetchGraph(
    			createFollowerGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.debug(xml);
        //Since actor is a "bound" root type, there are always
        // two actor rows, and the edge between
        // actor and follower is created in the first/parent row.
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
     
    
    public void testFederatedUpdate() throws IOException       
    {
    	GraphInfo info = createGraph();
    	
    	String xml = this.serializeGraph(info.actor.getDataGraph());
    	log.debug("inserting initial graph:");
    	log.debug(xml);
    	this.service.commit(info.actor.getDataGraph(), 
    			"test1"); 
    	
        Blog blog = info.follower.createBlog();
        blog.setName("Fiscal Cliff");
        blog.setDescription("A blog about the fiscal \"cliff\" scenario post election");
        blog.addTopic(info.politics); 
    	log.debug("comitting blog update");
    	this.service.commit(info.actor.getDataGraph(), 
    		"test2"); 
        
    	log.debug("fetching follower graph");
    	Actor fetchedFollowerRoot = fetchGraph(
    		createGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.debug(xml);
        
    	assertTrue(fetchedFollowerRoot.getBlogCount() == 1);
    	Blog fetchedBlog = (Blog)fetchedFollowerRoot.get("blog[@name='"+blog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(info.politics.getName()));        
            	
    	//fetchedFollowerRoot.unsetBlog(); FIXME: isSet="true" in change summary
    	fetchedBlog.delete(); // note deletes topics fetched in containment graph with blog
    	log.debug("comitting blog remove update");
    	this.service.commit(fetchedFollowerRoot.getDataGraph(), 
    		"test2");     	
    	log.debug("fetching follower graph again");
    	fetchedFollowerRoot = fetchGraph(
    		createGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.debug(xml);        
    	assertTrue(fetchedFollowerRoot.getBlogCount() == 0);
    	
    	
    	log.debug("comitting follower graph delete");
    	fetchedFollowerRoot.delete();
    	this.service.commit(fetchedFollowerRoot.getDataGraph(), 
    		"test2");     	
    	log.debug("fetching deleted follower graph");
    	fetchedFollowerRoot = findGraph(
        		createGraphQuery(info.follower.getName()));    	
        assertTrue(fetchedFollowerRoot == null);

    	log.debug("fetching actor graph");
    	Actor fetchedRoot = fetchGraph(
    		createGraphQuery(info.actor.getName()));    	
        xml = this.serializeGraph(fetchedRoot.getDataGraph());
        log.debug(xml);
        
    	assertTrue(fetchedRoot.getTargetEdgeCount() == 1);
    	assertFalse(fetchedRoot.getTargetEdge(0).isSetSource());
    	Actor fetchedFollower = (Actor)fetchedRoot.getTargetEdge(0).getSource();
    	assertTrue(fetchedFollower == null);
    
    }   
     
     
    public void testTopicInsertAndLink() throws IOException       
    {
    	Topic physics = createRootTopic("Physics");
    	Topic plasmaPhysics = physics.createChild();
    	plasmaPhysics.setName("Plasma Physics");
    	
    	Topic ionization = plasmaPhysics.createChild();
    	ionization.setName("Plasma Ionization");
    	
    	Topic magnetization = plasmaPhysics.createChild();
    	magnetization.setName("Plasma Magnetization");
    	
    	Topic darkEnergy = physics.createChild();
    	darkEnergy.setName("Dark Energy");
    	
    	Topic darkMatter = physics.createChild();
    	darkMatter.setName("Dark Metter");
    	
    	this.service.commit(physics.getDataGraph(), 
    			"test1");
    	
    	String name = USERNAME_BASE 
        	+ String.valueOf(System.currentTimeMillis())
        	+ "_example.com";	
    	Actor actor = createRootActor(name);
    	actor.setName(name);
    	actor.setDescription("Guy who likes plasma physics...");
    	
    	Blog physicsBlog = actor.createBlog();
    	physicsBlog.setName("Thoughts on Plasma Magnetization");
    	physicsBlog.setDescription("Magnetization parameters and temperature...");
    	
    	// separate it from its graph so we can 
    	// add to another graph
    	magnetization.detach();
    	
    	physicsBlog.addTopic(magnetization);
    	
    	this.service.commit(actor.getDataGraph(), 
    			"test2");
    	
    	Actor fetchedActor = fetchGraph(
        		createGraphQuery(name));    	
        String xml = this.serializeGraph(fetchedActor.getDataGraph());
        log.debug(xml);
        
    	assertTrue(fetchedActor.getBlogCount() == 1);
    	Blog fetchedBlog = (Blog)fetchedActor.get("blog[@name='"+physicsBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(magnetization.getName()));
        
    }    
   
    public void testBlogAdd() throws IOException       
    {
    	  
    	Topic rocks = createRootTopic("Rocks");
 
    	Topic igneousRocks = rocks.createChild();
    	igneousRocks.setName("Igneous Rocks");
    	
    	Topic metamorphicRocks = rocks.createChild();
    	metamorphicRocks.setName("Metamorphic Rocks");    	

    	Topic sedementaryRocks = rocks.createChild();
    	sedementaryRocks.setName("Sedementary Rocks");    	
    	
    	// commit some topics we can use
    	this.service.commit(rocks.getDataGraph(), 
    			"test1");
    	  
    	String name = USERNAME_BASE 
        	+ String.valueOf(System.currentTimeMillis())
        	+ "@example.com";	
    	Actor actor = createRootActor(name);
    	actor.setName(name);
    	actor.setDescription("Guy who likes rocks...");
    	
    	Blog igneousRocksBlog = actor.createBlog();
    	igneousRocksBlog.setName("Thoughts on Igneous Rocks");
    	igneousRocksBlog.setDescription("Igneous rocks are cool because...");
    	
    	// separate it from its graph so we can 
    	// add to another graph
    	//igneousRocks.detach();
    	
    	//igneousRocksBlog.addTopic(igneousRocks);
    	
    	// commit the actor and his first blog
    	this.service.commit(actor.getDataGraph(), 
    			"test2");

    	// re-fetch the actor w/o his new blog and see if
    	// adding blogs works as expected
    	Actor simpleActor = fetchGraph(
    			createSimpleActorQuery(name));    	
    	// 
    	Blog metamorphicRocksBlog = simpleActor.createBlog();
    	metamorphicRocksBlog.setName("Thoughts on Metamorphic Rocks");
    	metamorphicRocksBlog.setDescription("Metamorphic rocks are cool because...");
    	//metamorphicRocks.detach();
    	//metamorphicRocksBlog.addTopic(metamorphicRocks);

    	this.service.commit(simpleActor.getDataGraph(), 
    			"test2");
    	Actor fetchedActor = fetchGraph(
    			createActorBlogGraphQuery(name));    	
        String xml = this.serializeGraph(fetchedActor.getDataGraph());
        log.debug(xml);  
        // FIXME: pending graph writer fix 
    	assertTrue(fetchedActor.getBlogCount() == 2);

    	Blog sedementaryRocksBlog = simpleActor.createBlog();
    	sedementaryRocksBlog.setName("Thoughts on Sedementary Rocks");
    	sedementaryRocksBlog.setDescription("Sedementary rocks are cool because...");
    	//sedementaryRocks.detach();
    	//sedementaryRocksBlog.addTopic(sedementaryRocks);

    	this.service.commit(simpleActor.getDataGraph(), 
    			"test2");
    	
    	
    	fetchedActor = fetchGraph(
    			createActorBlogGraphQuery(name));    	
        xml = this.serializeGraph(fetchedActor.getDataGraph());
        log.debug(xml);        
        // FIXME: pending graph writer fix 
    	assertTrue(fetchedActor.getBlogCount() == 3);
        
    }    
    
    
    
}
