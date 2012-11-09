package org.cloudgraph.hbase.federation;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.HBaseTestCase;
import org.cloudgraph.test.social.Actor;
import org.cloudgraph.test.social.Blog;
import org.cloudgraph.test.social.Photo;
import org.cloudgraph.test.social.Topic;
import org.cloudgraph.test.social.query.QActor;
import org.cloudgraph.test.social.query.QBlog;
import org.cloudgraph.test.social.query.QPhoto;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Tests federated operations on a graph. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class SocialGraphFederationTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(SocialGraphFederationTest.class);
    private long WAIT_TIME = 1000;
    private String USERNAME_BASE = "social";    
    
    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(SocialGraphFederationTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testFederatedInsert() throws IOException       
    {
    	String name = USERNAME_BASE 
    		+ String.valueOf(System.currentTimeMillis())
    		+ "_example.com";	
    	Actor actor = createRootActor(name);
    	actor.setName(name);
    	actor.setDescription("I'm a guy who likes storms...");
    	
    	String followerName = "follower" 
        		+ String.valueOf(System.currentTimeMillis())
        		+ "_example.com";	
    	Actor follower = actor.createFollower();
    	follower.setName(followerName);
    	follower.setDescription("I'm a follower of the other guy...");
    	
    	Blog weatherBlog = actor.createBlog();
    	weatherBlog.setName("Hurricane Sandy");
    	weatherBlog.setDescription("The recent east coast hurricane...");
    	
    	Topic weather = weatherBlog.createTopic();
    	weather.setName("Weather");
    	weather.setDescription("a topic related to weather");

    	Topic atlanticWeather = weather.createChild();
    	atlanticWeather.setName("Atlantic Weather");
    	atlanticWeather.setDescription("a topic related to weather specific to the Atlantic");
 
    	Topic atlanticStorms = atlanticWeather.createChild();
    	atlanticStorms.setName("Atlantic Storms");
    	atlanticStorms.setDescription("a topic related to stormy weather specific to the Atlantic");
    	
    	weatherBlog.addTopic(atlanticStorms); // link to child topic
    	
    	Blog electionBlog = actor.createBlog();
    	electionBlog.setName("2012 Presidential Election");
    	electionBlog.setDescription("Thoughts on the 2012 election...");
    	
    	Topic politics = electionBlog.createTopic();
    	politics.setName("Politics");
    	politics.setDescription("a topic related to politics");

    	// Sandy changed the election so...
    	// now politics topic has 2 blog parents
    	weatherBlog.addTopic(politics); 
    	
    	Photo photo = actor.createPhoto();
    	photo.setName("sandy1");
    	photo.setDescription("a photo of hurricane Sandy");
    	photo.setContent(photo.getDescription().getBytes());
    	
    	Photo photo2 = actor.createPhoto();
    	photo2.setName("sandy2");
    	photo2.setDescription("another photo of hurricane Sandy");
    	photo2.setContent(photo.getDescription().getBytes());
    	
    	this.service.commit(actor.getDataGraph(), 
    			"test1");
    	
    	
    	Actor fetchedActor = fetchGraph(
    		createGraphQuery(name));    	
    	String xml = this.serializeGraph(fetchedActor.getDataGraph());
    	log.info(xml);
    	
    	assertTrue(fetchedActor.getFollowerCount() == 1);
    	Actor fetchedFollower = fetchedActor.getFollower(0);
    	assertTrue(fetchedFollower.getName() != null);
    	assertTrue(followerName.equals(fetchedFollower.getName()));
    	
    	assertTrue(fetchedActor.getBlogCount() == 2);
    	Blog fetchedBlog = (Blog)fetchedActor.get("blog[@name='"+electionBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
    	assertTrue(fetchedBlog.getTopicCount() == 1);
    	Topic fetchedTopic = fetchedBlog.getTopic(0);
    	assertTrue(fetchedTopic.getName() != null);
    	assertTrue(fetchedTopic.getName().equals(politics.getName()));
    	
    	Actor fetchedFollowerRoot = fetchGraph(
    		createFollowingGraphQuery(followerName));    	
        xml = this.serializeGraph(fetchedFollowerRoot.getDataGraph());
        log.info(xml);
        
        assertTrue(fetchedFollowerRoot.getFollowingCount() == 1);
        Actor fetchedFollowing = fetchedFollowerRoot.getFollowing(0);
        assertTrue(fetchedFollowing.getName() != null);
        assertTrue(fetchedFollowing.getName().equals(name));
        
    	Actor fetchedActorSliceRoot = fetchGraph(
    			createBlogPredicateQuery(actor.getName(), 
    				electionBlog.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.info(xml);
    	assertTrue(fetchedActorSliceRoot.getBlogCount() == 1);
    	fetchedBlog = (Blog)fetchedActorSliceRoot.get("blog[@name='"+electionBlog.getName()+"']");
    	assertTrue(fetchedBlog != null);
                
    	fetchedActorSliceRoot = fetchGraph(
    			createPhotoPredicateQuery(actor.getName(), 
    				photo2.getName()));    	
        xml = this.serializeGraph(fetchedActorSliceRoot.getDataGraph());
        log.info(xml);
    	assertTrue(fetchedActorSliceRoot.getPhotoCount() == 1);
    	Photo fetchedPhoto = (Photo)fetchedActorSliceRoot.get("photo[@name='"+photo2.getName()+"']");
    	assertTrue(fetchedPhoto != null);
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
    
    protected Actor createRootActor(String name) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Actor.class);
    	Actor root = (Actor)dataGraph.createRootObject(rootType);
    	root.setName(name);
    	
    	return root;
    }

    protected Topic createRootTopic(String name) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Topic.class);
    	Topic root = (Topic)dataGraph.createRootObject(rootType);
    	root.setName(name);
    	
    	return root;
    }
    
    protected Actor fetchGraph(QActor query) {    	
    	
    	this.marshal(query.getModel(), "social");
    	
    	DataGraph[] result = service.find(query);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Actor)result[0].getRootObject();
    }
    
    protected QActor createGraphQuery(String name) {
    	QActor root = QActor.newQuery();
    	root.select(root.wildcard())
    	    .select(root.follower().wildcard())
    	    .select(root.following().wildcard())
    	    .select(root.blog().wildcard())
    	    .select(root.blog().topic().wildcard())
    	    .select(root.blog().topic().child().wildcard())
    	    .select(root.blog().topic().child().child().wildcard())
    	    .select(root.blog().topic().parent().wildcard())
    	    .select(root.blog().topic().parent().parent().wildcard())
    	    .select(root.photo().wildcard());
        
    	root.where(root.name().eq(name));
    	
    	return root;
    }
    
    protected QActor createBlogPredicateQuery(String actorName, String blogName) {
    	QActor actor = QActor.newQuery();
    	Expression blogPredicate = QBlog.newQuery().name().eq(blogName);
    	
    	actor.select(actor.wildcard())
    	    .select(actor.follower().wildcard())
    	    .select(actor.following().wildcard())
    	    .select(actor.blog(blogPredicate).wildcard())
    	    .select(actor.blog(blogPredicate).topic().wildcard())
    	    .select(actor.blog(blogPredicate).topic().child().wildcard())
    	    .select(actor.blog(blogPredicate).topic().child().child().wildcard())
    	    .select(actor.photo().wildcard());
        
    	actor.where(actor.name().eq(actorName));
    	
    	return actor;
    }
    
    protected QActor createPhotoPredicateQuery(String actorName, String photoName) {
    	QActor actor = QActor.newQuery();
    	Expression photoPredicate = QPhoto.newQuery().name().eq(photoName);
    	
    	actor.select(actor.wildcard())
    	    .select(actor.follower().wildcard())
    	    .select(actor.following().wildcard())
    	    .select(actor.blog().wildcard())
    	    .select(actor.blog().topic().wildcard())
    	    .select(actor.blog().topic().child().wildcard())
    	    .select(actor.blog().topic().child().child().wildcard())
    	    .select(actor.photo(photoPredicate).wildcard());
        
    	actor.where(actor.name().eq(actorName));
    	
    	return actor;
    }
    
    protected QActor createFollowingGraphQuery(String name) {
    	QActor root = QActor.newQuery();
    	root.select(root.wildcard())
    	    .select(root.following().wildcard())
    	    .select(root.following().blog().wildcard())
    	    .select(root.following().blog().topic().wildcard())
    	    .select(root.following().photo().wildcard());
        
    	root.where(root.name().eq(name));
    	
    	return root;
    }
}
