package org.cloudgraph.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.cloudgraph.common.CommonTest;
import org.cloudgraph.web.etl.DataLoad;

 
public class DataLoadTest extends CommonTest {
    public void setUp() throws Exception {
    }

    public void testLoad() throws TransformerConfigurationException, MalformedURLException, IOException, TransformerException, SQLException {
    	
    	DataLoad.main(new String[] {
    		"load", 
    	    "data",
    	    "target"
    	});
    }
}
