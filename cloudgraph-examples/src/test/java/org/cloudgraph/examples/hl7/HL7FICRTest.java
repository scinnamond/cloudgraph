package org.cloudgraph.examples.hl7;




import java.io.IOException;
import java.util.Date;
import java.util.Random;

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
import org.cloudgraph.examples.hl7.ficr.hd400200.ContactParty;
import org.cloudgraph.examples.hl7.ficr.hd400200.ContactPerson;
import org.cloudgraph.examples.hl7.ficr.hd400200.HealthDocumentAttachment;
import org.cloudgraph.examples.hl7.ficr.hd400200.InvoiceElementGroup;
import org.cloudgraph.examples.hl7.ficr.hd400200.InvoiceElementGroupAttachment;
import org.cloudgraph.examples.hl7.ficr.hd400200.InvoiceElementReason;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequest;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequestAttention;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequestReason;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * HL7 FICR Model Tests
 */
public class HL7FICRTest extends HL7Test {
    private static Log log = LogFactory.getLog(HL7FICRTest.class);

    /**
     * Coverage Extension Request Pharmacy message
     * @throws IOException
     */
    public void testFICRHD400200() throws IOException {
    	    	
    	Random rand = new Random(System.currentTimeMillis());
    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(PaymentRequest.class);
    	PaymentRequest paymentRequest = (PaymentRequest)dataGraph.createRootObject(rootType);
    	paymentRequest.setAmt(rand.nextFloat());
    	paymentRequest.setClassCode("XACT");
    	paymentRequest.setId(new String[] {String.valueOf(rand.nextLong())});
    	paymentRequest.setMoodCode("PRP"); // readony
    	
    	PaymentRequestAttention performer = paymentRequest.createPrimaryPerformer();
    	performer.setTypeCode("PPRF");    	 
    	
    	ContactParty contactParty = performer.createContactParty();
    	contactParty.setClassCode("CON");
    	contactParty.setCode("PAYOR");
    	contactParty.setId(String.valueOf(System.currentTimeMillis()));
    	
    	ContactPerson contactPerson = contactParty.createContactPerson();
    	contactPerson.setName("Albert Dunhurst");
    	contactPerson.setClassCode("PSN");
    	contactPerson.setDeterminerCode("INSTANCE");
    	contactPerson.setTelecom("540-364-2293");
    	
    	PaymentRequestReason paymentReason = paymentRequest.createReasonOf();
    	paymentReason.setTypeCode("RSON");
    	InvoiceElementGroup invoiceElementGroup = paymentReason.createInvoiceElementGroup();
    	invoiceElementGroup.setClassCode("INME");
    	invoiceElementGroup.setMoodCode("PRP");
    	invoiceElementGroup.setConfidentialityCode("N");
    	invoiceElementGroup.setNetAmt(rand.nextFloat());
    	
    	InvoiceElementGroupAttachment groupAttachment = invoiceElementGroup.createPertinentInformation1();
    	groupAttachment.setTypeCode("PERT"); 
    	HealthDocumentAttachment attachment = groupAttachment.createHealthDocumentAttachment();
    	attachment.setClassCode("OBS");
    	attachment.setMoodCode("EVN");
    	attachment.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	//attachment.setCode(value)
    	attachment.setValue("<Content>attachment content</<Content>");
    	
    	//InvoiceElementReason invoiceReason = invoiceElementGroup.createReasonOf();
    	
    	log.info(paymentRequest.dump());
    	String xml = this.serializeGraph(paymentRequest.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(paymentRequest.getDataGraph(), 
    			USERNAME_BASE);
    	
    }
    
}