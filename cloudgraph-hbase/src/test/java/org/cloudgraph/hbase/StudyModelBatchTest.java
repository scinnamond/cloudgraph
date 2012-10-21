package org.cloudgraph.hbase;

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTestSetup;

import com.crackoo.domain.Goal;
import com.crackoo.profile.Profile;

public class StudyModelBatchTest extends StudyModelTest {
    private static Log log = LogFactory.getLog(StudyModelBatchTest.class);
    private static int numRuns = 1;

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(StudyModelBatchTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testGoalGraphInsert() throws IOException {
    	for (int run = 0; run < this.numRuns; run++) {
            long id = System.currentTimeMillis();
            Goal goal = this.createGoalGraph(id);
        	
        	//save the graph
            service.commit(goal.getDataGraph(), "test-user");
    		
			String xml = this.serializeGraph(goal.getDataGraph());
            log.info("goal: " + xml);
    	}
    }
}
