package org.cloudgraph.test.hbase;




import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.test.datatypes.DatatypeNode;
import org.cloudgraph.test.datatypes.query.QDatatypeNode;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public abstract class DatatypesModelTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(DatatypesModelTest.class);

    protected DatatypeNode createGraph(long id) {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(DatatypeNode.class);
    	DatatypeNode root = (DatatypeNode)dataGraph.createRootObject(rootType);
    	root.setRootId(id); 
    	root.setName("root" + String.valueOf(id));
    	
    	// path 1
    	DatatypeNode child_1 = root.createChild();
    	child_1.setName("child" + String.valueOf(1));

    	// path 1_1
    	DatatypeNode child_1_1 = child_1.createChild();
    	child_1_1.setName("child" + String.valueOf(1) + "_" + String.valueOf(1));
    	
    	// path 2
    	DatatypeNode child_2 = root.createChild();
    	child_2.setName("child" + String.valueOf(2));

    	// path 2_1
    	DatatypeNode child_2_1 = child_2.createChild();
    	child_2_1.setName("child" + String.valueOf(2) + "_" + String.valueOf(1));
    	
    	// path 3
    	DatatypeNode child_3 = root.createChild();
    	child_3.setName("child" + String.valueOf(3));
    	
        return root;
    }
    
    protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("crackoo");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
    
    protected DatatypeNode fetchGraphFull(long id) {    	
    	QDatatypeNode root = QDatatypeNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());

    	root.where(root.rootId().eq(id));
    	//root.where(root.creationDate().between(new Date(), new Date()));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (DatatypeNode)result[0].getRootObject();
    }
    
    protected DatatypeNode fetchGraphSlice(long id, String name) {    	
    	QDatatypeNode root = QDatatypeNode.newQuery();
    	Expression predicate = root.name().eq(name);
    	root.select(root.wildcard());
    	root.select(root.child(predicate).wildcard());
    	root.select(root.child(predicate).child().wildcard());
    	root.select(root.child(predicate).child().child().wildcard());
    	root.select(root.child(predicate).child().child().child().wildcard());

    	root.where(root.rootId().eq(id));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (DatatypeNode)result[0].getRootObject();
    }
}