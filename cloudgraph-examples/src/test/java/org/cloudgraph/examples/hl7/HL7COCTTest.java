package org.cloudgraph.examples.hl7;




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
import org.cloudgraph.examples.hl7.coct.hd010000.Referrer;
import org.cloudgraph.examples.hl7.coct.hd010000.ResponsibleParty1;
import org.cloudgraph.examples.hl7.coct.hd010000.ServiceDeliveryLocation;
import org.cloudgraph.examples.hl7.coct.hd010000.TransportedBy;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * HL7 COCT Model Tests
 */
public class HL7COCTTest extends HL7Test {
    private static Log log = LogFactory.getLog(HL7COCTTest.class);

   
    public void testCOCTHD010000() throws IOException {
    	    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Encounter.class);
    	Encounter encounter = (Encounter)dataGraph.createRootObject(rootType);
    	encounter.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	encounter.setActivityTime(now.toString());
    	encounter.setAdmissionReferralSourceCode("REF");
    	encounter.setClassCode("ENC"); // readony
    	encounter.setCode("TEMP");
    	encounter.setConfidentialityCode(new String[] {"R","N"});
    	encounter.setDischargeDispositionCode("63"); //Discharged/transferred to a Medicare certified long term care hospital 
    	encounter.setEffectiveTime(now.toString());
    	encounter.setLengthOfStayQuantity("24");
    	encounter.setMoodCode("EVN"); // readony
    	encounter.setPreAdmitTestInd(true);
    	encounter.setPriorityCode(new String[] {"EM"});
    	encounter.setReasonCode(new String[] {"SDUPTHER"}); // duplicate therapy
    	//encounter.setSpecialArrangementCode();
    	encounter.setStatusCode("completed");
    	
    	Admitter admitter = encounter.createAdmitter();
    	admitter.setModeCode("EVN");
    	admitter.setTime(now.toString());
    	admitter.setTypeCode("SONINLAW"); // readony
    	R_AssignedPersonUniversal admitterPerson = admitter.createAssignedPerson();    	 
    	
    	Attender attender = encounter.createAttender();
    	attender.setModeCode("EVN");
    	attender.setTime(now.toString());
    	attender.setTypeCode("SONINLAW"); // readony
    	R_AssignedPersonUniversal attenderPerson = admitter.createAssignedPerson();    	     	
    	
    	Authorization auth = encounter.createAuthorization();
    	auth.setTypeCode("AUTH"); // readony
    	A_ConsentUniversal consent = auth.createConsent();
    	    	
    	CauseOf causeOf = encounter.createCauseOf();
    	Component comp = encounter.createComponentOf();    	
    	Consultant consultant = encounter.createConsultant();
    	
    	Discharger discharger = encounter.createDischarger();
    	discharger.setTypeCode("DIS"); // readony
    	discharger.setModeCode("EVN");
    	discharger.setTime(now.toString());
    	
    	InFulfillmentOf inFulfillmentOf = encounter.createInFulfillmentOf();
    	inFulfillmentOf.setTypeCode("FLFS"); // read-only
    	A_AppointmentUniversal appointment = inFulfillmentOf.createAppointment();
    	
    	Location1 location = encounter.createLocation();
    	location.setTypeCode("LOC"); // read-only
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
    	
    	//ResponsibleParty1 responsibleParty = encounter.createResponsibleParty();
    	//TransportedBy transportedBy = encounter.createTransportedBy();    	 
    	
    	log.info(encounter.dump());
    	String xml = this.serializeGraph(encounter.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(encounter.getDataGraph(), 
    			USERNAME_BASE);
    	
    }
    
}