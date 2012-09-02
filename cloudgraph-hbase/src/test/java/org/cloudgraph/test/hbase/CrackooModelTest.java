package org.cloudgraph.test.hbase;




import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.test.PlasmaTestSetup;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import com.crackoo.domain.Citation;
import com.crackoo.domain.Goal;
import com.crackoo.domain.Profile;
import com.crackoo.domain.StudyItem;
import com.crackoo.domain.Tag;
import com.crackoo.domain.query.QProfile;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

/**
 * Performs and tests various SDO CRUD operations.
 */
public class CrackooModelTest extends HBaseTestCase {
    private static Log log = LogFactory.getLog(CrackooModelTest.class);

    private String GOAL_1 = "Goal 1";
    private String GOAL_2 = "Goal 2";
    private String GOAL_3 = "Goal 3";
    private long id = System.currentTimeMillis();
    private String ISBN1 = "ISBN1_" + String.valueOf(id);
    private String ISBN2 = "ISBN2_" + String.valueOf(id);
    private String ISBN3 = "ISBN3_" + String.valueOf(id);

    public static Test suite() {
        return PlasmaTestSetup.newTestSetup(CrackooModelTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
    } 
    
    public void testInsertUpdateDelete() throws IOException       
    {
    	int countBefore = getProfileCount();
    	
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

    	StudyItem studyItem = goal.createStudyItem();
    	studyItem.addTag(commonTag); // link existing tag (non containment reference)
    	Tag tagFF = studyItem.createTag();
    	tagFF.setTag("tag 1");
    	    	
    	Citation citation = studyItem.createCitation();
    	citation.setReference("ref1");
    	Citation citation1 = studyItem.createCitation();
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
    	goal3.addStudyItem(studyItem);
        assertTrue(studyItem.getGoalCount() == 2);
    	
    	//save the graph
        service.commit(dataGraph, "test-user");
    	
        int countAfter = getProfileCount();
        assertTrue(countAfter == countBefore+1); 
        
        // fetch the complete graph
        Profile fetchedProfile = this.fetchProfileGraph(id);
        //String xml = serializeGraph(fetchedProfile.getDataGraph());
        //log.info("NEW GRAPH: " + xml);
        // FIXME: need stax-utils working in maven repo
        assertTrue(fetchedProfile.getProfileId() == id);
        String isbn2 = fetchedProfile.getString(
        		"goal[@name='"+GOAL_2+"']/@ISBN");
        assertTrue(ISBN2.equals(isbn2)); 
        
        
        // update a property
        citation3.setReference("updated ref"); // make a change        
        service.commit(dataGraph, "test-user2");
        
        fetchedProfile = this.fetchProfileGraph(id);
        //xml = serializeGraph(fetchedProfile.getDataGraph());
        //log.info("UPDATED GRAPH: " + xml);        
        // FIXME: need stax-utils working in maven repo
        assertTrue(fetchedProfile.getProfileId() == id);
        
        //  check we did not create a dup etc...
        countAfter = getProfileCount();
        assertTrue(countAfter == countBefore+1); 
        
        // delete a section of graph
        assertTrue(studyItem.getGoalCount() == 2);
        goal.delete();
        assertTrue(dataGraph.getChangeSummary().isDeleted(goal));
        assertTrue(dataGraph.getChangeSummary().isDeleted(studyItem));
        assertTrue(dataGraph.getChangeSummary().isDeleted(citation));
        assertTrue(dataGraph.getChangeSummary().isDeleted(citation1));
        
        // expect these non-containment references to trigger a modification
        // as link object is deleted.
        assertTrue(dataGraph.getChangeSummary().isModified(profile));
        assertTrue(profile.getGoalCount() == 2);
        assertTrue(dataGraph.getChangeSummary().isModified(commonTag));
        assertTrue(goal3.getStudyItemCount() == 0);
        assertTrue(dataGraph.getChangeSummary().isModified(goal3));
               
        service.commit(dataGraph, "test-user2");        
               
        //  check we did not create a dup etc...
        countAfter = getProfileCount();
        assertTrue(countAfter == countBefore+1); 

        fetchedProfile = this.fetchProfileGraph(id);
        //xml = serializeGraph(fetchedProfile.getDataGraph());
        //log.info("GRAPH W/O Goal 1 PATH: " + xml);        
        // FIXME: need stax-utils working in maven repo
        assertTrue(fetchedProfile.getProfileId() == id);
        assertTrue(fetchedProfile.getGoalCount() == 2);
        
   }
      
    private String serializeGraph(DataGraph graph) throws IOException
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

    protected Profile fetchProfileGraph(long id) {    	
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
    
}