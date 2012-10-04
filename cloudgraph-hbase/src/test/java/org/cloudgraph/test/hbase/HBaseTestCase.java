package org.cloudgraph.test.hbase;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTest;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;

/**
 * 
 */
public abstract class HBaseTestCase extends PlasmaTest {
    private static Log log = LogFactory.getLog(HBaseTestCase.class);
    protected SDODataAccessClient service;
    protected String classesDir = System.getProperty("classes.dir");
    protected String targetDir = System.getProperty("target.dir");
    
    public void setUp() throws Exception {
        service = new SDODataAccessClient(
        		new HBasePojoDataAccessClient());
    }
        
    
}