package org.cloudgraph.test.hbase;




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
import com.crackoo.domain.Profile;
import com.crackoo.domain.StudyItem;
import com.crackoo.domain.Tag;
import com.crackoo.domain.query.QGoal;
import com.crackoo.domain.query.QProfile;
import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public abstract class StudyModelTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(StudyModelTest.class);

    protected String GOAL_1 = "Goal 1";
    protected String GOAL_2 = "Goal 2";
    protected String GOAL_3 = "Goal 3";
    protected String ISBN1;
    protected String ISBN2;
    protected String ISBN3;
    

    protected Profile createProfileGraph(long id) {
        ISBN1 = "ISBN1_" + String.valueOf(id);
        ISBN2 = "ISBN2_" + String.valueOf(id);
        ISBN3 = "ISBN3_" + String.valueOf(id);

        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Profile.class);
    	Profile profile = (Profile)dataGraph.createRootObject(rootType);
    	profile.setProfileId(id);    	
    	
    	Tag commonTag = profile.createTag();
    	commonTag.setTag("common tag");
    	
    	// goal path 1
    	Goal goal = profile.createGoal();
    	goal.setName(GOAL_1);
    	goal.setSummary("This is a goal to study HBase");
    	goal.setDescription("A description of a goal");
    	goal.setISBN(ISBN1);

    	StudyItem commonStudyItem = goal.createStudyItem();
    	commonStudyItem.addTag(commonTag); // link existing tag (non containment reference)
    	Tag tagFF = commonStudyItem.createTag();
    	tagFF.setTag("tag 1");
    	    	
    	Citation citation = commonStudyItem.createCitation();
    	citation.setReference("ref1");
    	Citation citation1 = commonStudyItem.createCitation();
    	citation1.setReference("ref2");
    	
    	//goal path 2
    	Goal goal2 = profile.createGoal();
    	goal2.setName(GOAL_2);
    	goal2.setSummary("This is a goal to study some other book");
    	goal2.setDescription("A description of a goal");
    	goal2.setISBN(ISBN2);
    	
    	StudyItem studyItem2 = goal2.createStudyItem();
    	Tag tag3 = studyItem2.createTag();
    	tag3.setTag("tag3");
    	
    	Citation citation2 = studyItem2.createCitation();
    	citation2.setReference("ref4");
    	Citation citation3 = studyItem2.createCitation();
    	citation3.setReference("ref5");
    	
    	//goal path 3
    	Goal goal3 = profile.createGoal();
    	goal3.setName(GOAL_3);
    	goal3.setSummary("This is a goal to study snother area");
    	goal3.setDescription("A description of a goal 3");
    	goal3.setISBN(ISBN3);

    	// link existing study item to this goal giving the 
    	// study item 2 "parents"
    	goal3.addStudyItem(commonStudyItem);
        assertTrue(commonStudyItem.getGoalCount() == 2);
    	
        return profile;
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
    
    
    protected Profile fetchProfileGraphFull(long id) {    	
    	QProfile root = QProfile.newQuery();
    	root.select(root.profileId());
    	//root.select(root.seqId());
    	root.select(root.creationDate());
    	root.select(root.lastModification());
    	root.select(root.tag().tag());
   	    root.select(root.goal().wildcard());
   	    //root.select(root.goal().ISBN());
    	root.select(root.goal().studyItem().seqId());
    	root.select(root.goal().studyItem().creationDate());
    	root.select(root.goal().studyItem().lastModification());
    	root.select(root.goal().studyItem().citation().reference());
    	root.select(root.goal().studyItem().tag().tag());
    	
    	//root.where(root.profileId().eq(id));
    	//root.where(root.creationDate().lt(new Date()));
    	//root.where(root.goal().ISBN().eq(ISBN2));
    	
    	root.where(root.profileId().eq(id)
    		.and(root.goal().ISBN().like(ISBN2+"*")));
    	root.where(root.creationDate().lt(new Date()));
    	
    	/* 
    	root.where(
    		root.group(
    			root.profileId().eq(id)
	    	    .and(root.creationDate().lt(new Date()))
    	    )
    	);
    	*/    	 
    	
    	//root.where(root.creationDate().between(new Date(), new Date()));
    	
    	DataGraph[] result = service.find(root);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Profile)result[0].getRootObject();
    }
    
    protected Profile fetchProfileDSLGraphSlice(long id) {    	
    	QProfile profile = QProfile.newQuery();
    	QGoal goal = QGoal.newQuery();
    	Expression predicate = goal.name().eq(GOAL_2);
    	profile.select(profile.profileId());
    	profile.select(profile.creationDate());
    	profile.select(profile.lastModification());
    	profile.select(profile.tag().tag());
   	    profile.select(profile.goal(predicate).wildcard());
    	profile.select(profile.goal(predicate).studyItem().seqId());
    	profile.select(profile.goal(predicate).studyItem().creationDate());
    	profile.select(profile.goal(predicate).studyItem().lastModification());
    	profile.select(profile.goal(predicate).studyItem().citation().reference());
    	profile.select(profile.goal(predicate).studyItem().tag().tag());
    	
    	profile.where(profile.profileId().eq(id));
    	
    	DataGraph[] result = service.find(profile);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Profile)result[0].getRootObject();
    }
    
    protected Profile fetchProfileXPathGraphSlice(long id) {    	
    	 
    	Select select = new Select(new String[] {
    	    "profileId",		
    	    "creationDate",		
    	    "lastModification",		
    	    "tag/@tag",		
    	    "goal[@name = '"+GOAL_2+"']/*",		
    	    "goal[@name = '"+GOAL_2+"']/studyItem/*",		
    	    "goal[@name = '"+GOAL_2+"']/studyItem/citation/@reference",		
    	    "goal[@name = '"+GOAL_2+"']/studyItem/tag/@tag"
    	});
    	
    	Where where = new Where("[@profileId = '"+id+"']");
    	//Where where = new Where("[@profileId = '"+id+"'] and goal[@ISBN = 'ISBN2_"+id+"']");
        /*
java.lang.ClassCastException: org.jaxen.expr.DefaultAndExpr cannot be cast to org.jaxen.expr.LocationPath
	at org.plasma.query.xpath.QueryXPath.getSteps(QueryXPath.java:90)
	at org.plasma.query.model.Where.<init>(Where.java:104)
	at org.cloudgraph.test.hbase.CrackooModelTest.fetchProfileGraphByXPath(CrackooModelTest.java:259)
	at org.cloudgraph.test.hbase.CrackooModelTest.fetchProfileGraph(CrackooModelTest.java:200)
	at org.cloudgraph.test.hbase.CrackooModelTest.testInsertUpdateDelete(CrackooModelTest.java:121)
          
         */
    	From from = new From(Profile.ETY_PROFILE, 
    			Profile.NAMESPACE_URI);
    	
    	Query query = new Query (select, from, where);
    	
    	DataGraph[] result = service.find(query);
    	assertTrue(result != null);
    	assertTrue(result.length == 1);
    	
    	return (Profile)result[0].getRootObject();
    }
}