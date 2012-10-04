package org.cloudgraph.test.hbase;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTestSetup;

import com.crackoo.domain.Citation;
import com.crackoo.domain.Goal;
import com.crackoo.domain.Profile;
import com.crackoo.domain.StudyItem;
import com.crackoo.domain.Tag;
import junit.framework.Test;

public class StudyModelCRUDTest extends StudyModelTest {
    private static Log log = LogFactory.getLog(StudyModelCRUDTest.class);

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StudyModelCRUDTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    

    public void testProfileCRUD() throws IOException       
    {
        long id = System.currentTimeMillis();
        ISBN1 = "ISBN1_" + String.valueOf(id);
        ISBN2 = "ISBN2_" + String.valueOf(id);
        ISBN3 = "ISBN3_" + String.valueOf(id);

        //int countBefore = getProfileCount();
    	
    	Profile profile = this.createProfileGraph(id);
    	
    	//save the graph
        service.commit(profile.getDataGraph(), "test-user");
    	
        //int countAfter = getProfileCount();
        //assertTrue(countAfter == countBefore+1); 
                
        // fetch the full graph
        Profile fetchedProfile = this.fetchProfileGraphFull(id);
        
        // update a property
        // make a change
        String newRef = "updated ref";
        //fetchedProfile.set("goal[@name='"+GOAL_2+"']/studyItem/citation[position()=1]/@reference", newRef);
        fetchedProfile.getGoal(1).getStudyItem(0).getCitation(0).setReference(newRef);
        service.commit(fetchedProfile.getDataGraph(), "test-user2");
        
        fetchedProfile = this.fetchProfileGraphFull(id);
        String xml = serializeGraph(fetchedProfile.getDataGraph());
        log.info("UPDATED GRAPH: " + xml);        
        assertTrue(fetchedProfile.getProfileId() == id);
        String updated = fetchedProfile.getGoal(1).getStudyItem(0).getCitation(0).getReference();
        assertTrue(newRef.equals(updated));
        
        
        //  check we did not create a dup etc...
        //countAfter = getProfileCount();
        //assertTrue(countAfter == countBefore+1); 
        
        // delete a section of graph
        Goal goal = (Goal)profile.get("goal[@name='"+GOAL_1+"']");
        Goal goal3 = (Goal)profile.get("goal[@name='"+GOAL_3+"']");
        StudyItem studyItem = goal.getStudyItem(0);
        Tag commonTag = studyItem.getTag(0);
        Citation citation = studyItem.getCitation(0);
        Citation citation1 = studyItem.getCitation(1);
        // FIXME: this check for a study item with 2 parent goals
        // is super important but fails currently for a fetched profile
        assertTrue(studyItem.getGoalCount() == 2);
        goal.delete();
        // now check the state of the client graph before commit
        assertTrue(profile.getDataGraph().getChangeSummary().isDeleted(goal));
        assertTrue(profile.getDataGraph().getChangeSummary().isDeleted(studyItem));
        assertTrue(profile.getDataGraph().getChangeSummary().isDeleted(citation));
        assertTrue(profile.getDataGraph().getChangeSummary().isDeleted(citation1));
        // expect these non-containment references to trigger a modification
        // as link object is deleted.
        assertTrue(profile.getDataGraph().getChangeSummary().isModified(profile));
        assertTrue(profile.getGoalCount() == 2);
        assertTrue(profile.getDataGraph().getChangeSummary().isModified(commonTag));
        assertTrue(goal3.getStudyItemCount() == 0);
        assertTrue(profile.getDataGraph().getChangeSummary().isModified(goal3));
               
        service.commit(profile.getDataGraph(), "test-user2");        
               
        //  check we did not create a dup etc...
        //countAfter = getProfileCount();
        //assertTrue(countAfter == countBefore+1); 

        fetchedProfile = this.fetchProfileGraphFull(id);
        xml = serializeGraph(fetchedProfile.getDataGraph());
        log.info("GRAPH W/O Goal 1 PATH: " + xml);        
        assertTrue(fetchedProfile.getProfileId() == id);
        assertTrue(fetchedProfile.getGoalCount() == 2);   
        
    }

}
