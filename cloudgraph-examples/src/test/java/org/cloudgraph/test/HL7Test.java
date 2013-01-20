package org.cloudgraph.test;




import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class HL7Test extends ExamplesTestCase {
    private static Log log = LogFactory.getLog(HL7Test.class);

    protected String USERNAME = "hl7";  
    protected Date now = new Date();
   
}