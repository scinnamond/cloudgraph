package org.cloudgraph.hbase;

import junit.framework.Test;

import org.plasma.common.test.PlasmaTestSetup;

import com.crackoo.profile.Profile;

public class StudyModelBatchTest extends StudyModelTest {
    private static int numRuns = 3;

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StudyModelBatchTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testProfileGraphInsert() {
    	for (int run = 0; run < this.numRuns; run++) {
            long id = System.currentTimeMillis();
            ISBN1 = "ISBN1_" + String.valueOf(id);
            ISBN2 = "ISBN2_" + String.valueOf(id);
            ISBN3 = "ISBN3_" + String.valueOf(id);
        	Profile profile = this.createProfileGraph(id);
        	
        	//save the graph
            service.commit(profile.getDataGraph(), "test-user");
    		
    	}
    }
}
