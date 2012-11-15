package org.cloudgraph.state;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.state.model.StateModelDataBinding;
import org.cloudgraph.state.model.StateModel;
import org.xml.sax.SAXException;

/**
 * State marshalling related tests.
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public class StateMarshalingTestCase extends StateTestCase {
    private static Log log = LogFactory.getLog(StateMarshalingTestCase.class);
    
    private StateModelDataBinding binding;
    
    public void setUp() throws Exception {
    }
        
    public void testUnmarshal() throws JAXBException, SAXException, FileNotFoundException {
    	log.info("testUnmarshal");
    	this.binding = new StateModelDataBinding();
    	File file = new File("./src/test/resources/state-example.xml");
    	FileInputStream stream = new FileInputStream(file);
    	log.info("validate()");
    	StateModel root = (StateModel)this.binding.validate(stream);
    	log.info("marshal()");
    	String xml = this.binding.marshal(root);
    	log.info(xml);
    }
}