package org.cloudgraph.test.hbase;

import java.io.IOException;

import junit.framework.Test;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTestSetup;

import com.crackoo.domain.Profile;

public class StudyModelSliceTest extends StudyModelTest {
    private static Log log = LogFactory.getLog(StudyModelSliceTest.class);

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StudyModelSliceTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testProfileSlice() throws IOException       
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
        
        // fetch a slice
        Profile fetchedProfile = this.fetchProfileDSLGraphSlice(id);
        String xml = serializeGraph(fetchedProfile.getDataGraph());
        log.info("SLICED GRAPH: " + xml);
        assertTrue(fetchedProfile.getProfileId() == id);
        assertTrue(fetchedProfile.getGoalCount() == 1); // expect single slice
        String isbn2 = fetchedProfile.getString(
        		"goal[@name='"+GOAL_2+"']/@ISBN");
        assertTrue(ISBN2.equals(isbn2)); 
        
    }    

}
