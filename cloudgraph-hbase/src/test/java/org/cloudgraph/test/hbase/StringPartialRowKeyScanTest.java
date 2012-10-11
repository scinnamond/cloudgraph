package org.cloudgraph.test.hbase;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.query.Expression;

import com.crackoo.domain.Profile;
import com.crackoo.domain.query.QGoal;
import com.crackoo.domain.query.QProfile;
import commonj.sdo.DataGraph;

public class StringPartialRowKeyScanTest extends StudyModelTest {
    private static Log log = LogFactory.getLog(StringPartialRowKeyScanTest.class);

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StringPartialRowKeyScanTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testStringEqual() throws IOException       
    {
        long id = System.currentTimeMillis();
    	Profile profile = this.createProfileGraph(id);
    	
    	//save the graph
        service.commit(profile.getDataGraph(), "test-user");
    	
        // fetch a slice
        Profile fetchedProfile = this.fetchSingleGraph(id, ISBN1);
        String xml = serializeGraph(fetchedProfile.getDataGraph());
        log.info("SLICED GRAPH: " + xml);
        assertTrue(fetchedProfile.getProfileId() == id);
        assertTrue(fetchedProfile.getGoalCount() == 1); // expect single slice
        String isbn2 = fetchedProfile.getString(
        		"goal[@name='"+GOAL_2+"']/@ISBN");
        assertTrue(ISBN2.equals(isbn2));         
    }  
     
    public void testStringBetween() throws IOException       
    {
        long id1 = System.currentTimeMillis();    	
    	Profile profile1 = this.createProfileGraph(id1);
        service.commit(profile1.getDataGraph(), "test-user");
        String min_isbn = new String(ISBN2);

        long id2 = System.currentTimeMillis();    	
    	Profile profile2 = this.createProfileGraph(id2);
        service.commit(profile2.getDataGraph(), "test-user");

        long id3 = System.currentTimeMillis();    	
    	Profile profile3 = this.createProfileGraph(id3);
        service.commit(profile3.getDataGraph(), "test-user");
        String max_isbn = new String(ISBN2);
        
        Profile[] fetchedProfiles = this.fetchGraphsBetween(
        		id1, id3, min_isbn, max_isbn);
        assertTrue(fetchedProfiles.length == 3);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        assertTrue(fetchedProfiles[0].getGoalCount() == 1);
        String xml = serializeGraph(fetchedProfiles[0].getDataGraph());
        log.info("GRAPH1: " + xml);

        //assertTrue(fetchedProfiles[1].getProfileId() == id2);
        assertTrue(fetchedProfiles[1].getGoalCount() == 1);
        xml = serializeGraph(fetchedProfiles[1].getDataGraph());
        log.info("GRAPH2: " + xml);
        
        //assertTrue(fetchedProfiles[2].getProfileId() == id3);
        assertTrue(fetchedProfiles[2].getGoalCount() == 1);
        xml = serializeGraph(fetchedProfiles[2].getDataGraph());
        log.info("GRAPH3: " + xml);
        
    } 
     
    public void testStringInclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();    	
    	Profile profile1 = this.createProfileGraph(id1);
        service.commit(profile1.getDataGraph(), "test-user");
        String min_isbn = new String(ISBN2);

        long id2 = System.currentTimeMillis();    	
    	Profile profile2 = this.createProfileGraph(id2);
        service.commit(profile2.getDataGraph(), "test-user");

        long id3 = System.currentTimeMillis();    	
    	Profile profile3 = this.createProfileGraph(id3);
        service.commit(profile3.getDataGraph(), "test-user");
        String max_isbn = new String(ISBN2);
        
        Profile[] fetchedProfiles = this.fetchGraphsInclusive(
        		id1, id3, min_isbn, max_isbn);
        assertTrue(fetchedProfiles.length == 3);

        //assertTrue(fetchedProfiles[0].getProfileId() == id1);
        assertTrue(fetchedProfiles[0].getGoalCount() == 1);
        String xml = serializeGraph(fetchedProfiles[0].getDataGraph());
        log.info("GRAPH1: " + xml);

        //assertTrue(fetchedProfiles[1].getProfileId() == id2);
        assertTrue(fetchedProfiles[1].getGoalCount() == 1);
        xml = serializeGraph(fetchedProfiles[1].getDataGraph());
        log.info("GRAPH2: " + xml);
        
        //assertTrue(fetchedProfiles[2].getProfileId() == id3);
        assertTrue(fetchedProfiles[2].getGoalCount() == 1);
        xml = serializeGraph(fetchedProfiles[2].getDataGraph());
        log.info("GRAPH3: " + xml);        
    }  
    
    public void testStringExclusive() throws IOException       
    {
        long id1 = System.currentTimeMillis();    	
    	Profile profile1 = this.createProfileGraph(id1);
        service.commit(profile1.getDataGraph(), "test-user");
        String min_isbn = new String(ISBN2);

        long id2 = System.currentTimeMillis();    	
    	Profile profile2 = this.createProfileGraph(id2);
        service.commit(profile2.getDataGraph(), "test-user");

        long id3 = System.currentTimeMillis();    	
    	Profile profile3 = this.createProfileGraph(id3);
        service.commit(profile3.getDataGraph(), "test-user");
        String max_isbn = new String(ISBN2);
        
        Profile[] fetchedProfiles = this.fetchGraphsExclusive(
        	id1, id3, min_isbn, max_isbn);
        assertTrue(fetchedProfiles.length == 1);

        assertTrue(fetchedProfiles[0].getProfileId() == id2);
        assertTrue(fetchedProfiles[0].getGoalCount() == 1);
        String xml = serializeGraph(fetchedProfiles[0].getDataGraph());
        log.info("GRAPH1: " + xml);
    }    

    protected Profile fetchSingleGraph(long id, String isbn) {    	
    	QProfile profile = createSelect();
    	
    	profile.where(profile.profileId().eq(id)
    		.and(profile.goal().ISBN().eq(isbn)));
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Profile)result[0].getRootObject();
    }

    protected Profile[] fetchGraphsBetween(long min, long max,
    		String minIsbn, String maxIsbn) {    	
    	QProfile profile = createSelect();
    	profile.where(profile.profileId().between(min, max)
    		.and(profile.goal().ISBN().between(minIsbn, maxIsbn)));
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	
    	Profile[] profiles = new Profile[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Profile)result[i].getRootObject();
    	return profiles;
    }

    protected Profile[] fetchGraphsInclusive(long min, long max,
    		String minIsbn, String maxIsbn) {    	
    	QProfile profile = createSelect();
    	profile.where(profile.profileId().ge(min)
    		.and(profile.profileId().le(max)
        	.and(profile.goal().ISBN().ge(minIsbn)
        	.and(profile.goal().ISBN().le(maxIsbn)))));
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	
    	Profile[] profiles = new Profile[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Profile)result[i].getRootObject();
    	return profiles;
    }
    
    protected Profile[] fetchGraphsExclusive(long min, long max,
    		String minIsbn, String maxIsbn) {    	
    	QProfile profile = createSelect();
    	profile.where(profile.profileId().gt(min)
    		.and(profile.profileId().lt(max)
    	    .and(profile.goal().ISBN().gt(minIsbn)
    	    .and(profile.goal().ISBN().lt(maxIsbn)))));

    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	
    	Profile[] profiles = new Profile[result.length];
    	for (int i = 0; i < result.length; i++) 
    		profiles[i] = (Profile)result[i].getRootObject();
    	return profiles;
    }
    
    private QProfile createSelect()
    {
    	QProfile profile = QProfile.newQuery();
    	QGoal goal = QGoal.newQuery();
    	Expression predicate = goal.name().eq(GOAL_2);
    	profile.select(profile.profileId());
    	profile.select(profile.creationDate());
    	profile.select(profile.lastModification());
    	profile.select(profile.tag().tag());
   	    profile.select(profile.goal(predicate).wildcard());
    	profile.select(profile.goal(predicate).studyItem().seqId());
    	profile.select(profile.goal(predicate).studyItem().creationDate());
    	profile.select(profile.goal(predicate).studyItem().lastModification());
    	profile.select(profile.goal(predicate).studyItem().citation().reference());
    	profile.select(profile.goal(predicate).studyItem().tag().tag());
    	return profile;
    }
}
