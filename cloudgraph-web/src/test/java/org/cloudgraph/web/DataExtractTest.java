package org.cloudgraph.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.cloudgraph.common.CommonTest;
import org.cloudgraph.web.etl.DataExtract;
import org.cloudgraph.web.etl.DataLoad;
import org.xml.sax.SAXException;

 
public class DataExtractTest extends CommonTest {
    public void setUp() throws Exception {
    }

    public void testImport() throws TransformerConfigurationException, MalformedURLException, IOException, TransformerException, SQLException, JAXBException, SAXException {
    	
    	DataExtract.main(new String[] {
    		"transform", 
    	    "data-out",
    	    "target"
    	});
    }
}
