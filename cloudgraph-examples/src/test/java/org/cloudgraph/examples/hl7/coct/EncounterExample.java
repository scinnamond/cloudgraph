package org.cloudgraph.examples.hl7.coct;




import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.hl7.cmet.A_AccountUniversal;
import org.cloudgraph.examples.hl7.cmet.A_AppointmentUniversal;
import org.cloudgraph.examples.hl7.cmet.A_ConsentUniversal;
import org.cloudgraph.examples.hl7.cmet.R_AssignedPersonUniversal;
import org.cloudgraph.examples.hl7.coct.hd010000.Admitter;
import org.cloudgraph.examples.hl7.coct.hd010000.Attender;
import org.cloudgraph.examples.hl7.coct.hd010000.Authorization;
import org.cloudgraph.examples.hl7.coct.hd010000.CauseOf;
import org.cloudgraph.examples.hl7.coct.hd010000.Component;
import org.cloudgraph.examples.hl7.coct.hd010000.Consultant;
import org.cloudgraph.examples.hl7.coct.hd010000.Discharger;
import org.cloudgraph.examples.hl7.coct.hd010000.Encounter;
import org.cloudgraph.examples.hl7.coct.hd010000.InFulfillmentOf;
import org.cloudgraph.examples.hl7.coct.hd010000.Location1;
import org.cloudgraph.examples.hl7.coct.hd010000.NotificationContact;
import org.cloudgraph.examples.hl7.coct.hd010000.PertinentInformation2;
import org.cloudgraph.examples.hl7.coct.hd010000.Reference;
import org.cloudgraph.examples.hl7.coct.hd010000.ServiceDeliveryLocation;
import org.cloudgraph.examples.hl7.coct.hd010000.query.QEncounter;
import org.cloudgraph.test.HL7Test;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * HL7 COCT Patient Encounter Tests
 */
public class EncounterExample extends HL7Test {
    private static Log log = LogFactory.getLog(EncounterExample.class);

    private static Date effectiveTime;
    public void setUp() throws Exception {
        super.setUp();
        if (effectiveTime == null) {
        	effectiveTime = new Date();
        }
    }        
   
    public void testCreate() throws IOException {
    	    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Encounter.class);
    	Encounter encounter = (Encounter)dataGraph.createRootObject(rootType);
    	encounter.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	encounter.setActivityTime(now.toString());
    	encounter.setAdmissionReferralSourceCode("REF");
    	encounter.setClassCode("ENC"); 
    	encounter.setCode("TEMP");
    	encounter.setConfidentialityCode(new String[] {"R","N"});
    	encounter.setDischargeDispositionCode("63"); //Discharged/transferred to a Medicare certified long term care hospital 
    	encounter.setEffectiveTime(effectiveTime.toString());
    	encounter.setLengthOfStayQuantity("24");
    	encounter.setMoodCode("EVN"); 
    	encounter.setPreAdmitTestInd(true);
    	encounter.setPriorityCode(new String[] {"EM"});
    	encounter.setReasonCode(new String[] {"SDUPTHER"}); // duplicate therapy
    	encounter.setStatusCode("completed");
    	
    	Admitter admitter = encounter.createAdmitter();
    	admitter.setModeCode("EVN");
    	admitter.setTime(now.toString());
    	admitter.setTypeCode("SONINLAW"); 
    	R_AssignedPersonUniversal admitterPerson = admitter.createAssignedPerson();    	 
    	
    	Attender attender = encounter.createAttender();
    	attender.setModeCode("EVN");
    	attender.setTime(now.toString());
    	attender.setTypeCode("SONINLAW"); 
    	R_AssignedPersonUniversal attenderPerson = admitter.createAssignedPerson();    	     	
    	
    	Authorization auth = encounter.createAuthorization();
    	auth.setTypeCode("AUTH"); 
    	A_ConsentUniversal consent = auth.createConsent();
    	    	
    	CauseOf causeOf = encounter.createCauseOf();
    	Component comp = encounter.createComponentOf();    	
    	Consultant consultant = encounter.createConsultant();
    	
    	Discharger discharger = encounter.createDischarger();
    	discharger.setTypeCode("DIS"); 
    	discharger.setModeCode("EVN");
    	discharger.setTime(now.toString());
    	
    	InFulfillmentOf inFulfillmentOf = encounter.createInFulfillmentOf();
    	inFulfillmentOf.setTypeCode("FLFS"); 
    	A_AppointmentUniversal appointment = inFulfillmentOf.createAppointment();
    	
    	Location1 location = encounter.createLocation();
    	location.setTypeCode("LOC"); 
    	location.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	location.setTime(now.toString());
    	ServiceDeliveryLocation serviceDeliveryLocation = location.createServiceDeliveryLocation();
    	serviceDeliveryLocation.setClassCode("SDLOC");
    	serviceDeliveryLocation.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	serviceDeliveryLocation.setEffectiveTime(now.toString());
    	serviceDeliveryLocation.setTelecom(new String[] {"1816 Wiskey Row, Prescott AZ, 86301"});
    	
    	NotificationContact notificationContact = encounter.createNotificationContact();
    	notificationContact.setTypeCode("NOT");
    	
    	PertinentInformation2 pertinentInformation = encounter.createPertinentInformation1();
    	pertinentInformation.setTypeCode("PERT");
    	pertinentInformation.setPriorityNumber(2);
    	
    	Reference reference = encounter.createReference();
    	reference.setTypeCode("REFR");
    	A_AccountUniversal account = reference.createAccount();
    	
    	String xml = this.serializeGraph(encounter.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(encounter.getDataGraph(), 
    			USERNAME);
    	
    }
    
    /**
     * Queries for an existing COCT Patient Encounter message
     * @throws IOException
     */
    public void testQuery() throws IOException {
    	QEncounter encounter = QEncounter.newQuery();
    	encounter.select(encounter.wildcard())
    	       .select(encounter.admitter().wildcard())
    	       .select(encounter.attender().wildcard())
    	       .select(encounter.discharger().wildcard())
    	       .select(encounter.inFulfillmentOf().wildcard())
    	       .select(encounter.causeOf().wildcard())
    	       .select(encounter.authorization().wildcard())
    	       .select(encounter.location().serviceDeliveryLocation().wildcard())
    	       .select(encounter.notificationContact().wildcard()) 
    	       .select(encounter.pertinentInformation1().wildcard());
    	
    	encounter.where(encounter.statusCode().eq("completed")
    		   .and(encounter.admitter().typeCode().eq("SONINLAW"))
    		   .and(encounter.lengthOfStayQuantity().eq(24)
    		   .and(encounter.effectiveTime().eq(effectiveTime.toString()))));
    	
    	DataGraph[] results = this.service.find(encounter);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	//log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	Encounter encounterResult = (Encounter)results[0].getRootObject();
    	assertTrue(encounterResult.getEffectiveTime().equals(effectiveTime.toString()));   	
    }

    /**
     * Queries for an existing COCT Patient Encounter message
     * @throws IOException
     */
    public void testUpdate() throws IOException {
    	QEncounter encounter = QEncounter.newQuery();
    	encounter.select(encounter.wildcard())
    	         .select(encounter.admitter().wildcard())
    	         .select(encounter.pertinentInformation1().wildcard());
    	
    	encounter.where(encounter.statusCode().eq("completed")
    		   .and(encounter.admitter().typeCode().eq("SONINLAW"))
    		   .and(encounter.lengthOfStayQuantity().eq(24)
    		   .and(encounter.effectiveTime().eq(effectiveTime.toString()))));
    	
    	DataGraph[] results = this.service.find(encounter);
    	assertTrue(results != null);
    	assertTrue(results.length == 1);
    	
    	Encounter encounterResult = (Encounter)results[0].getRootObject();
    	assertTrue(encounterResult.getEffectiveTime().equals(effectiveTime.toString())); 
    	encounterResult.setDischargeDispositionCode("64"); //Nursing facility certified under Medicaid
    	
    	PertinentInformation2 pertinentInformation = encounterResult.getPertinentInformation1(0);
    	pertinentInformation.setPriorityNumber(1);
    	
    	this.service.commit(encounterResult.getDataGraph(), 
    			USERNAME);
    	
    	results = this.service.find(encounter);
    	assertTrue(results != null);
    	assertTrue(results.length == 1);    
    	encounterResult = (Encounter)results[0].getRootObject();
    	assertTrue(encounterResult.getEffectiveTime().equals(effectiveTime.toString())); 
    	assertTrue("64".equals(encounterResult.getDischargeDispositionCode())); //Nursing facility certified under Medicaid
    	
    	pertinentInformation = encounterResult.getPertinentInformation1(0);
    	assertTrue(pertinentInformation.getPriorityNumber() == 1);
    }
    
}