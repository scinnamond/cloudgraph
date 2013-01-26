package org.cloudgraph.examples.hl7.pocd;




import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.hl7.pocd.hd000040.Act;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedAuthor;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedCustodian;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssignedEntity;
import org.cloudgraph.examples.hl7.pocd.hd000040.AssociatedEntity;
import org.cloudgraph.examples.hl7.pocd.hd000040.Author;
import org.cloudgraph.examples.hl7.pocd.hd000040.Birthplace;
import org.cloudgraph.examples.hl7.pocd.hd000040.ClinicalDocument;
import org.cloudgraph.examples.hl7.pocd.hd000040.Component2;
import org.cloudgraph.examples.hl7.pocd.hd000040.Component3;
import org.cloudgraph.examples.hl7.pocd.hd000040.Component4;
import org.cloudgraph.examples.hl7.pocd.hd000040.Custodian;
import org.cloudgraph.examples.hl7.pocd.hd000040.CustodianOrganization;
import org.cloudgraph.examples.hl7.pocd.hd000040.DocumentationOf;
import org.cloudgraph.examples.hl7.pocd.hd000040.Entry;
import org.cloudgraph.examples.hl7.pocd.hd000040.EntryRelationship;
import org.cloudgraph.examples.hl7.pocd.hd000040.LegalAuthenticator;
import org.cloudgraph.examples.hl7.pocd.hd000040.Observation;
import org.cloudgraph.examples.hl7.pocd.hd000040.Organization;
import org.cloudgraph.examples.hl7.pocd.hd000040.Organizer;
import org.cloudgraph.examples.hl7.pocd.hd000040.Participant1;
import org.cloudgraph.examples.hl7.pocd.hd000040.Patient;
import org.cloudgraph.examples.hl7.pocd.hd000040.PatientRole;
import org.cloudgraph.examples.hl7.pocd.hd000040.Performer1;
import org.cloudgraph.examples.hl7.pocd.hd000040.Person;
import org.cloudgraph.examples.hl7.pocd.hd000040.Place;
import org.cloudgraph.examples.hl7.pocd.hd000040.RecordTarget;
import org.cloudgraph.examples.hl7.pocd.hd000040.RelatedSubject;
import org.cloudgraph.examples.hl7.pocd.hd000040.Section;
import org.cloudgraph.examples.hl7.pocd.hd000040.ServiceEvent;
import org.cloudgraph.examples.hl7.pocd.hd000040.StructuredBody;
import org.cloudgraph.examples.hl7.pocd.hd000040.Subject;
import org.cloudgraph.examples.hl7.pocd.hd000040.SubjectPerson;
import org.cloudgraph.examples.hl7.pocd.hd000040.query.QClinicalDocument;
import org.cloudgraph.test.HL7Test;
import org.junit.Test;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Property;
import commonj.sdo.Type;

/**
 * HL7 POCD Clinical Document Example
 */
public class ClinicalDocumentExample extends HL7Test {
    private static Log log = LogFactory.getLog(ClinicalDocumentExample.class);

    private static String effectiveTime;
    public void setUp() throws Exception {
        super.setUp();
        if (effectiveTime == null) {
        	Type type = PlasmaTypeHelper.INSTANCE.getType(ClinicalDocument.class);
        	Property effectiveTimeProp = type.getProperty(ClinicalDocument.PTY_EFFECTIVE_TIME);
        	effectiveTime = (String)DataConverter.INSTANCE.convert(effectiveTimeProp.getType(), new Date());
        }
    }        

    /**
     * HL7 POCD Clinical Document example
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
    	    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(ClinicalDocument.class);
    	ClinicalDocument clinicalDocument = (ClinicalDocument)dataGraph.createRootObject(rootType);
    	clinicalDocument.setClassCode("DOCCLIN");
    	clinicalDocument.setMoodCode("EVN");  
    	clinicalDocument.setId(UUID.randomUUID().toString());
    	clinicalDocument.setEffectiveTime(effectiveTime);
    	clinicalDocument.setConfidentialityCode("R");
    	clinicalDocument.setTitle("Good Health Clinic Continuity of Care Document");
    	///////////////////////////////////////////////////
    	//CDA Header
    	///////////////////////////////////////////////////    	
    	
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
    	Component2 component = clinicalDocument.createComponent();
    	StructuredBody body = (StructuredBody)component.createBodyChoice(StructuredBody.class);
    	//Purpose Section
    	Component3 purposeComponent = body.createComponent();
    	Section purposeSection = purposeComponent.createSection();
    	purposeSection.setTitle("Summary Purpose");
    	purposeSection.setCode("48764-5");
    	purposeSection.setText("Transfer of care");
    	Entry purposeEntry = purposeSection.createEntry();
    	Act purposeAct = (Act)purposeEntry.createClinicalStatement(Act.class);
    	purposeAct.setClassCode("ACT");
    	purposeAct.setMoodCode("EVN");
    	purposeAct.setCode("23745001");
    	purposeAct.setStatusCode("completed");
    	purposeAct.setText("Purpose Activity");
    	EntryRelationship entryRel = purposeAct.createEntryRelationship();
    	entryRel.setTypeCode("RSON");
    	Act relAct = (Act)entryRel.createClinicalStatement(Act.class);
    	relAct.setCode("308292007");
    	relAct.setStatusCode("completed");
    	//Payers section
    	//Advance Directives section
    	//Functional Status section
    	//Problems section
    	//Family History section
    	Component3 historyComponent = body.createComponent();
    	Section historySection = historyComponent.createSection();
    	historySection.setTitle("Family history");
    	historySection.setCode("10157-6");
    	historySection.setText("Father (deceased)");
		
    	Entry historyEntry = historySection.createEntry();
    	historyEntry.setTypeCode("DRIV");
    	Organizer organizer = (Organizer)historyEntry.createClinicalStatement(Organizer.class);
    	organizer.setStatusCode("completed");
    	Subject subject = organizer.createSubject();
    	RelatedSubject relatedSubject = subject.createRelatedSubject();
    	relatedSubject.setClassCode("PRS");
    	relatedSubject.setCode("9947008");
    	SubjectPerson subjectPerson = relatedSubject.createSubject();
    	subjectPerson.setAdministrativeGenderCode("M");
    	subjectPerson.setBirthTime("1912");
    	Component4 observationComp = organizer.createComponent();
    	Observation observation = (Observation)observationComp.createClinicalStatement(Observation.class);
    	observation.setMoodCode("EVN");
    	observation.setClassCode("OBS");
    	observation.setCode("ASSERTION");
    	observation.setStatusCode("completed");
    	observation.setValue(new String[] {"22298006"});
    	EntryRelationship assertionEntryRelationship = observation.createEntryRelationship();
    	assertionEntryRelationship.setTypeCode("CAUS");
    	Observation causeObservation = (Observation)assertionEntryRelationship.createClinicalStatement(Observation.class);
    	causeObservation.setClassCode("OBS");
    	causeObservation.setMoodCode("EVN");
    	causeObservation.setId(new String[] {UUID.randomUUID().toString()});
    	causeObservation.setCode("ASSERTION");
    	causeObservation.setStatusCode("completed");
    	//...
    	//Social History section
    	//Alerts section
    	//Medications section
    	//Medical Equipment section
    	//Immunizations section
    	//Vital Signs section
    	//Results section
    	//Procedures section
    	//Encounters section
    	//Plan of Care section
    	
    	String xml = this.serializeGraph(clinicalDocument.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(clinicalDocument.getDataGraph(), 
    			USERNAME);
    
    }
    
    /**
     * Queries for an existing POCD Clinical Document message
     * @throws IOException
     */
    @Test
    public void testQuery() throws IOException {
    	QClinicalDocument document = QClinicalDocument.newQuery();
    	document.select(document.wildcard())
    	       .select(document.recordTarget().patientRole().patient().wildcard())
    	       .select(document.recordTarget().patientRole().patient().birthplace().wildcard())
    	       .select(document.recordTarget().patientRole().providerOrganization().wildcard())
    	       .select(document.authenticator().wildcard())
    	       .select(document.author().assignedAuthor().wildcard())
    	       .select(document.authenticator().assignedEntity().wildcard())
    	       .select(document.authorization().wildcard())
    	       .select(document.custodian().assignedCustodian().wildcard());
    	
    	document.where(document.classCode().eq("DOCCLIN")
    		   .and(document.moodCode().eq("EVN"))
    		   .and(document.confidentialityCode().eq("R")
    		   .and(document.recordTarget().patientRole().patient().administrativeGenderCode().eq("M"))		   
    		   .and(document.effectiveTime().eq(effectiveTime))));
    	
    	DataGraph[] results = this.service.find(document);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	//log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	ClinicalDocument result = (ClinicalDocument)results[0].getRootObject();
    	assertTrue(result.getEffectiveTime().equals(effectiveTime.toString()));   	
    }
}