package org.cloudgraph.hbase;




import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.query.Expression;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import com.crackoo.domain.Citation;
import com.crackoo.domain.Goal;
import com.crackoo.domain.StudyItem;
import com.crackoo.domain.Tag;
import com.crackoo.domain.query.QGoal;
import com.crackoo.profile.Profile;
import com.crackoo.profile.query.QProfile;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public abstract class StudyModelTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(StudyModelTest.class);

    protected String GOAL_NAME = "Goal 1";
    protected String ISBN;

    protected Goal createGoalGraph(long id) {
        ISBN = "ISBN_" + String.valueOf(id);

        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Goal.class);
    	Goal goal = (Goal)dataGraph.createRootObject(rootType);
    	goal.setGoalId(id);
    	goal.setName(GOAL_NAME);
    	goal.setSummary("This is a goal to study HBase");
    	goal.setDescription("A description of a goal");
    	goal.setISBN(ISBN);
    	
    	Tag commonTag = goal.createTag();
    	commonTag.setTag("common tag");
    	

    	StudyItem commonStudyItem = goal.createStudyItem();
    	commonStudyItem.addTag(commonTag); // link existing tag (non containment reference)
    	Tag tagFF = commonStudyItem.createTag();
    	tagFF.setTag("tag 1");
    	    	
    	Citation citation = commonStudyItem.createCitation();
    	citation.setReference("ref1");
    	Citation citation1 = commonStudyItem.createCitation();
    	citation1.setReference("ref2");
    	    	
    	StudyItem studyItem2 = goal.createStudyItem();
    	Tag tag3 = studyItem2.createTag();
    	tag3.setTag("tag3");
    	
    	Citation citation2 = studyItem2.createCitation();
    	citation2.setReference("ref4");
    	Citation citation3 = studyItem2.createCitation();
    	citation3.setReference("ref5");
    	
        return goal;
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
    
    protected int getProfileCount() {    	
    	QProfile profile = QProfile.newQuery();
    	profile.select(profile.wildcard());
        return service.count(profile);
    }    
    
    protected Goal fetchGoalGraphFull(long id) {    	
    	QGoal root = QGoal.newQuery();
    	root.select(root.wildcard());
    	root.select(root.studyItem().wildcard());
    	root.select(root.studyItem().citation().wildcard());
    	root.select(root.studyItem().tag().wildcard());
    	
    	root.where(root.goalId().eq(id)
    		.and(root.creationDate().lt(new Date())));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Goal)result[0].getRootObject();
    }
    
}