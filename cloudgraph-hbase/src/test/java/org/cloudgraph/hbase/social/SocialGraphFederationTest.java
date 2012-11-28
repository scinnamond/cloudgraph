package org.cloudgraph.hbase.social;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.SocialGraphModelTest;
import org.cloudgraph.test.social.Actor;
import org.cloudgraph.test.social.Blog;
import org.cloudgraph.test.social.Friendship;
import org.cloudgraph.test.social.Photo;
import org.cloudgraph.test.social.Topic;
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
    	log.info("inserting initial graph:");
    	log.info(xml);
    	this.service.commit(info.actor.getDataGraph(), 
    			"test1");    	
    	
    	log.info("fetching initial graph");
    	Actor fetchedActor = fetchGraph(
    		createGraphQuery(info.actor.getName()));    	
    	xml = this.serializeGraph(fetchedActor.getDataGraph());
    	log.info(xml);

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
    	
    	log.info("fetching follower graph");
    	Actor fetchedFollowerRoot = fetchGraph(
    			createFollowerGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.info(xml);
        //Since actor is a "bound" root type, there are always
        // two actor rows, and the edge between
        // actor and follower is created in the first/parent row.
        // Because edge and its derivatives are not "bound"
        // we cannot traverse from the follower back through the edge to
        // the actor. If the edge becomes a bound root, we could. 
        assertTrue(fetchedFollowerRoot != null);
        assertTrue(fetchedFollowerRoot.getSourceEdgeCount() == 0);
        
        
    	log.info("fetching blog slice");
    	Actor fetchedActorSliceRoot = fetchGraph(
    			createBlogPredicateQuery(info.actor.getName(), 
    					info.electionBlog.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.info(xml);
    	assertTrue(fetchedActorSliceRoot.getBlogCount() == 1);
    	fetchedBlog = (Blog)fetchedActorSliceRoot.get("blog[@name='"+info.electionBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedActor.getTargetEdgeCount() == 1);
    	fetchedFollower = (Actor)fetchedActor.getTargetEdge(0).getSource();
    	assertTrue(fetchedFollower.getName() != null);
    	assertTrue(info.follower.getName().equals(fetchedFollower.getName()));
                
    	log.info("fetching photo slice");
    	fetchedActorSliceRoot = fetchGraph(
    			createPhotoPredicateQuery(info.actor.getName(), 
    					info.photo2.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.info(xml);
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
    	log.info("inserting initial graph:");
    	log.info(xml);
    	this.service.commit(info.actor.getDataGraph(), 
    			"test1"); 
    	
        Blog blog = info.follower.createBlog();
        blog.setName("Fiscal Cliff");
        blog.setDescription("A blog about the fiscal \"cliff\" scenario post election");
        blog.addTopic(info.politics); 
    	log.info("comitting blog update");
    	this.service.commit(info.actor.getDataGraph(), 
    		"test2"); 
        
    	log.info("fetching follower graph");
    	Actor fetchedFollowerRoot = fetchGraph(
    		createGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.info(xml);
        
    	assertTrue(fetchedFollowerRoot.getBlogCount() == 1);
    	Blog fetchedBlog = (Blog)fetchedFollowerRoot.get("blog[@name='"+blog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(info.politics.getName()));        
            	
    	//fetchedFollowerRoot.unsetBlog(); FIXME: isSet="true" in change summary
    	fetchedBlog.delete(); // note deletes topics fetched in containment graph with blog
    	log.info("comitting blog remove update");
    	this.service.commit(fetchedFollowerRoot.getDataGraph(), 
    		"test2");     	
    	log.info("fetching follower graph again");
    	fetchedFollowerRoot = fetchGraph(
    		createGraphQuery(info.follower.getName()));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.info(xml);        
    	assertTrue(fetchedFollowerRoot.getBlogCount() == 0);
    	
    	
    	log.info("comitting follower graph delete");
    	fetchedFollowerRoot.delete();
    	this.service.commit(fetchedFollowerRoot.getDataGraph(), 
    		"test2");     	
    	log.info("fetching deleted follower graph");
    	fetchedFollowerRoot = findGraph(
        		createGraphQuery(info.follower.getName()));    	
        assertTrue(fetchedFollowerRoot == null);

    	log.info("fetching actor graph");
    	Actor fetchedRoot = fetchGraph(
    		createGraphQuery(info.actor.getName()));    	
        xml = this.serializeGraph(fetchedRoot.getDataGraph());
        log.info(xml);
        
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
        log.info(xml);
        
    	assertTrue(fetchedActor.getBlogCount() == 1);
    	Blog fetchedBlog = (Blog)fetchedActor.get("blog[@name='"+physicsBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(magnetization.getName()));
        
    }    
      
}
