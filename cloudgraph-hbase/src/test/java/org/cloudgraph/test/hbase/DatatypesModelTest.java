package org.cloudgraph.test.hbase;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.test.datatypes.Node;
import org.cloudgraph.test.datatypes.query.QNode;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.Expression;
import org.plasma.query.Query;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public abstract class DatatypesModelTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(DatatypesModelTest.class);

    protected int maxLevels = 3;
    protected int maxRows = 5;        
        
    protected void fillGraph(Node root,
    		long id, Date now)
    {
    	Node parent = root;
    	for (int i = 0; i < maxRows; i++) {
    		Node child = parent.createChild();
        	fillNode(child, id, now, 1, i);  
        	 
        	for (int j = 0; j < maxRows; j++) {
        		Node child2 = child.createChild();
            	fillNode(child2, id, now, 2, j);        	
            	for (int k = 0; k < maxRows; k++) {
            		Node child3 = child2.createChild();
                	fillNode(child3, id, now, 3, k);        	
            	}
        	}
        	 
        	
    	}
    	
    	//addNodes(root, id, now,
    	//		maxLevels, 1, 
    	//		maxRows);    	
    }
    
    protected void addNodes(Node parent, 
    		long id, Date now,
    		long maxLevels, long level, 
    		long maxRows) {
    	
    	for (int i = 0; i < maxRows; i++) {
    		Node child = parent.createChild();
        	fillNode(child, id, now, level, i);
        	if (level < maxLevels)
        	    addNodes(child, id, now, 
        			maxLevels, level++, 
        			maxRows);
    	}
    }
    
    protected Node fillNode(
    		Node node,
    		long id,
    		Date now,
    		long level, long sequence) {
    	String name = level + "_" + sequence;
    	String floatIdStr = level + "." + sequence;
    	float floatId = Float.parseFloat(floatIdStr); 
    	node.setRootId(id);	
    	node.setLevelNum(level);
    	node.setSequenceNum(sequence);
    	
    	node.setName(name);
    	node.setBooleanField(true);    
    	node.setByteField((byte)1);       
    	node.setBytesField(name.getBytes());      
    	node.setCharacterField('c');  
    	node.setDateField(now);      	
       	
    	Property prop = node.getType().getProperty(Node.PTY_DATE_TIME_FIELD);
    	node.setDateTimeField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));   
    	
    	prop = node.getType().getProperty(Node.PTY_DAY_FIELD);
    	node.setDayField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));        
    	
    	prop = node.getType().getProperty(Node.PTY_MONTH_FIELD);
    	node.setMonthField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));      
    	
    	prop = node.getType().getProperty(Node.PTY_MONTH_DAY_FIELD);
    	node.setMonthDayField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));   
    	
    	prop = node.getType().getProperty(Node.PTY_YEAR_FIELD);
    	node.setYearField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));       
    	
    	prop = node.getType().getProperty(Node.PTY_YEAR_MONTH_FIELD);
    	node.setYearMonthField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));  
    	
    	prop = node.getType().getProperty(Node.PTY_YEAR_MONTH_DAY_FIELD);
    	node.setYearMonthDayField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));
    	
    	prop = node.getType().getProperty(Node.PTY_TIME_FIELD);
    	node.setTimeField((String)DataConverter.INSTANCE.fromDate(prop.getType(), now));       
    	
    	node.setDecimalField(new BigDecimal(floatId));    
    	node.setDoubleField(floatId);     
    	//node.setDurationField();   
    	node.setFloatField(floatId); 
    	
    	node.setIntField(Integer.MAX_VALUE);        
    	node.setIntegerField(BigInteger.valueOf(id));  
    	
    	node.setLongField(id);   
    	
    	node.setObjectField(name);     
    	node.setShortField(Short.MAX_VALUE);      
    	node.setStringField(name); 
    	List<String> list = new ArrayList<String>();
    	list.add(name);
    	node.setStringsField(list);    
    	node.setUriField(name);        
    	return node;
    }
    
    protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("test");
        
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
    
    protected Node fetchGraphFull(long id) {    	
    	QNode query = createGraphQueryFull(id);
    	this.marshal(query, id);
    	
    	DataGraph[] result = service.find(query);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Node)result[0].getRootObject();
    }
    
    protected QNode createGraphQueryFull(long id) {    	
    	QNode root = QNode.newQuery();
    	root.select(root.wildcard());
    	root.select(root.child().wildcard());
    	root.select(root.child().child().wildcard());
    	root.select(root.child().child().child().wildcard());
    	root.select(root.child().child().child().child().wildcard());

    	root.where(root.rootId().eq(id));
    	//root.where(root.creationDate().between(new Date(), new Date()));
    	return root;
    }
    
    protected Node fetchGraphSlice(long id, String name) {    	
    	QNode root = QNode.newQuery();
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
    	
    	return (Node)result[0].getRootObject();
    }
    
    protected Query marshal(Query query, long id) {
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			        new DefaultValidationEventHandler());
			String xml = binding.marshal(query);
	        //log.info("query: " + xml);
			String name = "query" + String.valueOf(id)+ ".xml";
			FileOutputStream fos = new FileOutputStream(
					new File(name));
			binding.marshal(query, fos);
	        FileInputStream fis = new FileInputStream(
	        		new File(name));
	        Query q2 = (Query)binding.unmarshal(fis);
	    	return q2;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
}