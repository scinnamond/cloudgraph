package org.cloudgraph.examples.hl7;




import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequest;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedAuthor;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedCustodian;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedEntity;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssociatedEntity;
import org.cloudgraph.examples.hl7.pocd.hd000040.Author;
import org.cloudgraph.examples.hl7.pocd.hd000040.Birthplace;
import org.cloudgraph.examples.hl7.pocd.hd000040.ClinicalDocument;
import org.cloudgraph.examples.hl7.pocd.hd000040.Custodian;
import org.cloudgraph.examples.hl7.pocd.hd000040.CustodianOrganization;
import org.cloudgraph.examples.hl7.pocd.hd000040.DocumentationOf;
import org.cloudgraph.examples.hl7.pocd.hd000040.Informant12;
import org.cloudgraph.examples.hl7.pocd.hd000040.LegalAuthenticator;
import org.cloudgraph.examples.hl7.pocd.hd000040.Organization;
import org.cloudgraph.examples.hl7.pocd.hd000040.Participant1;
import org.cloudgraph.examples.hl7.pocd.hd000040.Patient;
import org.cloudgraph.examples.hl7.pocd.hd000040.PatientRole;
import org.cloudgraph.examples.hl7.pocd.hd000040.Performer1;
import org.cloudgraph.examples.hl7.pocd.hd000040.Person;
import org.cloudgraph.examples.hl7.pocd.hd000040.Place;
import org.cloudgraph.examples.hl7.pocd.hd000040.RecordTarget;
import org.cloudgraph.examples.hl7.pocd.hd000040.ServiceEvent;
import org.cloudgraph.examples.hl7.pocd.hd000040.query.QClinicalDocument;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * HL7 POCD Model Tests
 */
public class HL7POCDTest extends HL7Test {
    private static Log log = LogFactory.getLog(HL7POCDTest.class);

    /**
     * Coverage Extension Request Pharmacy message
     * @throws IOException
     */
    public void testPOCD_HD000040() throws IOException {
    	    	
    	Random rand = new Random(System.currentTimeMillis());
    	
    	///////////////////////////////////////////////////
    	//CDA Header
    	///////////////////////////////////////////////////
    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(ClinicalDocument.class);
    	ClinicalDocument clinicalDocument = (ClinicalDocument)dataGraph.createRootObject(rootType);
    	clinicalDocument.setClassCode("DOCCLIN");
    	clinicalDocument.setMoodCode("EVN"); // readony
    	clinicalDocument.setId(UUID.randomUUID().toString());
    	clinicalDocument.setEffectiveTime(now.toString());
    	clinicalDocument.setConfidentialityCode("R");
    	clinicalDocument.setTitle("Good Health Clinic Continuity of Care Document");
    	
    	RecordTarget recordTarget = clinicalDocument.createRecordTarget();
    	PatientRole patientRole = recordTarget.createPatientRole();
    	Patient patient = patientRole.createPatient();
    	patient.setName(new String[] {"Henry", "Levin"});
    	patient.setAdministrativeGenderCode("M");
    	patient.setBirthTime(now.toString());
    	
    	Birthplace birthplace = patient.createBirthplace();
    	birthplace.setClassCode("BIRTHPL");
    	Place place = birthplace.createBirthplace();
    	place.setClassCode("PLC");
    	place.setDeterminerCode("INSTANCE");
    	place.setName("Berkeley");
    	place.setAddr("1288 Univsity Ave, Berkeley CA 97202");
    	
    	Organization goodHealthClinic = patientRole.createProviderOrganization();
    	goodHealthClinic.setName(new String[] {"Good Health Clinic"});
    	
    	Author author = clinicalDocument.createAuthor();
    	author.setTime(now.toString());
    	AssignedAuthor assignedAuthor = author.createAssignedAuthor();
    	assignedAuthor.setId(new String[] {UUID.randomUUID().toString()});
    	assignedAuthor.setRepresentedOrganization(goodHealthClinic);
    	
    	Custodian custodian = clinicalDocument.createCustodian();
    	AssignedCustodian assignedCustodian = custodian.createAssignedCustodian();
    	CustodianOrganization custodianOrganization = assignedCustodian.createRepresentedCustodianOrganization();
    	custodianOrganization.setName("Good Health Clinic");
    	
    	LegalAuthenticator legalAuthenticator = clinicalDocument.createLegalAuthenticator();
    	legalAuthenticator.setTime(now.toString());
    	legalAuthenticator.setSignatureCode("S");
    	AssignedEntity assignedEntity = legalAuthenticator.createAssignedEntity();
    	assignedEntity.setRepresentedOrganization(goodHealthClinic);
    	
    	Participant1 participant = clinicalDocument.createParticipation();
    	participant.setTypeCode("IND");
    	AssociatedEntity associatedEntity = participant.createAssociatedEntity();
    	associatedEntity.setClassCode("GUAR");
    	associatedEntity.setId(new String[] {UUID.randomUUID().toString()});		
    	associatedEntity.setAddr(new String[] {"17 Daws Rd.", "Blue Bell", "MA", "02368"});
    	associatedEntity.setTelecom(new String[] {"tel:(888)555-1212"});
    	
    	Person associatedPerson = associatedEntity.createAssociatedPerson();
    	associatedPerson.setName(new String[] {"Kenneth", "Ross"});
    	
    	Participant1 participant2 = clinicalDocument.createParticipation();
    	participant2.setTypeCode("IND");
    	AssociatedEntity associatedEntity2 = participant2.createAssociatedEntity();
    	associatedEntity2.setClassCode("NOK");
    	associatedEntity2.setId(new String[] {UUID.randomUUID().toString()});		
    	associatedEntity2.setTelecom(new String[] {"tel:(777)555-1212"});
    	Person associatedPerson2 = associatedEntity2.createAssociatedPerson();
    	associatedPerson2.setName(new String[] {"Henrietta", "Levin"});
    	
    	DocumentationOf documentationOf = clinicalDocument.createDocumentationOf();
    	ServiceEvent serviceEvent = documentationOf.createServiceEvent();
    	serviceEvent.setClassCode("PCPR");
    	serviceEvent.setEffectiveTime(now.toString());
    	Performer1 performer = serviceEvent.createPerformer();
    	performer.setTypeCode("PRF");
    	performer.setFunctionCode("PCP");
    	performer.setTime(now.toString());
    	
    	AssignedEntity assignedEntity2 = performer.createAssignedEntity();
    	assignedEntity2.setRepresentedOrganization(goodHealthClinic);
    	Person assignedPerson = assignedEntity2.createAssignedPerson();
    	assignedPerson.setName(new String[] {"Dr.", "Robert", "Dolin"});

    	///////////////////////////////////////////////////
    	//CDA Body
    	///////////////////////////////////////////////////
    	
    	log.info(clinicalDocument.dump());
    	String xml = this.serializeGraph(clinicalDocument.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(clinicalDocument.getDataGraph(), 
    			USERNAME_BASE);
    	    	
		Select select = new Select(new String[] {
				"*",	
				"*/*",	
				"*/*/*",	
				"*/*/*/*",	
				"*/*/*/*/*",	
			});
		From from = new From(
			ClinicalDocument.ETY_CLINICAL_DOCUMENT,
			ClinicalDocument.NAMESPACE_URI
		);
		Query query = new Query(select, from);
		DataGraph[] results = service.find(query);
		if (results != null)
			for (DataGraph graph : results) {
				xml = serializeGraph(graph);
				log.info(xml);
			}    
    }
    
}