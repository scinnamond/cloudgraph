package org.cloudgraph.examples.hl7;




import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class HL7Test extends ExamplesTestCase {
    private static Log log = LogFactory.getLog(HL7Test.class);

    protected long WAIT_TIME = 1000;
    protected String USERNAME_BASE = "hl7";  
    protected Date now = new Date();
   
}