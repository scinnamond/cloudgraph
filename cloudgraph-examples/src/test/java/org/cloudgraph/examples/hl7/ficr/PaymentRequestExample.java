/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.examples.hl7.ficr;




import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.hl7.ficr.hd400200.ContactParty;
import org.cloudgraph.examples.hl7.ficr.hd400200.ContactPerson;
import org.cloudgraph.examples.hl7.ficr.hd400200.HealthDocumentAttachment;
import org.cloudgraph.examples.hl7.ficr.hd400200.InvoiceElementGroup;
import org.cloudgraph.examples.hl7.ficr.hd400200.InvoiceElementGroupAttachment;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequest;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequestAttention;
import org.cloudgraph.examples.hl7.ficr.hd400200.PaymentRequestReason;
import org.cloudgraph.examples.hl7.ficr.hd400200.query.QPaymentRequest;
import org.cloudgraph.test.HL7Test;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * HL7 FICR Coverage Extension Request (Pharmacy)
 */
public class PaymentRequestExample extends HL7Test {
    
	private static Log log = LogFactory.getLog(PaymentRequestExample.class);
    private static Random rand;
    
    private static Float baseAmount = 100.0f;
    private static Float amount;
    
    public void setUp() throws Exception {
        super.setUp();
        if (rand == null) {
        	rand = new Random(System.currentTimeMillis());
            amount = baseAmount + rand.nextFloat();         
            amount = Math.round(amount*100.0)/100.0f; // round to 2 decimal places
            log.info("amount: " + String.valueOf(amount));
        }
    }        
    
    /**
     * Creates and persists a FICR Coverage Extension Request Pharmacy message
     * @throws IOException
     */
    public void testCreate() throws IOException {
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(PaymentRequest.class);
    	PaymentRequest paymentRequest = (PaymentRequest)dataGraph.createRootObject(rootType);
    	paymentRequest.setAmt(this.amount);
    	paymentRequest.setClassCode("XACT");
    	paymentRequest.setId(new String[] {String.valueOf(rand.nextLong())});
    	paymentRequest.setMoodCode("PRP");  
    	
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
    	contactPerson.setTelecom("(540)364-2293");
    	
    	PaymentRequestReason paymentReason = paymentRequest.createReasonOf();
    	paymentReason.setTypeCode("RSON");
    	InvoiceElementGroup invoiceElementGroup = paymentReason.createInvoiceElementGroup();
    	invoiceElementGroup.setClassCode("INME");
    	invoiceElementGroup.setMoodCode("PRP");
    	invoiceElementGroup.setConfidentialityCode("N");
    	invoiceElementGroup.setNetAmt(this.amount);
    	
    	InvoiceElementGroupAttachment groupAttachment = invoiceElementGroup.createPertinentInformation1();
    	groupAttachment.setTypeCode("PERT"); 
    	HealthDocumentAttachment attachment = groupAttachment.createHealthDocumentAttachment();
    	attachment.setClassCode("OBS");
    	attachment.setMoodCode("EVN");
    	attachment.setId(new String[] {String.valueOf(System.currentTimeMillis())});
    	//attachment.setCode(value)
    	attachment.setValue("<Content>attachment content</<Content>");
    	
    	//InvoiceElementReason invoiceReason = invoiceElementGroup.createReasonOf();
    	
    	log.info(this.serializeGraph(paymentRequest.getDataGraph()));
    	this.service.commit(paymentRequest.getDataGraph(), 
    			USERNAME);
    	
    }
    
    /**
     * Queries for an existing FICR Coverage Extension Request Pharmacy message
     * @throws IOException
     */
    public void testQuery() throws IOException {
    	QPaymentRequest request = QPaymentRequest.newQuery();
    	request.select(request.wildcard())
    	       .select(request.primaryPerformer().wildcard())
    	       .select(request.primaryPerformer().contactParty().wildcard())
    	       .select(request.primaryPerformer().contactParty().contactPerson().wildcard())
    	       .select(request.reasonOf().wildcard()) 
    	       .select(request.reasonOf().invoiceElementGroup().wildcard());
    	
    	request.where(request.classCode().eq("XACT")
    		   .and(request.moodCode().eq("PRP")
    		   .and(request.amt().eq(this.amount))));
    	
    	DataGraph[] results = this.service.find(request);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	//log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	PaymentRequest paymentRequest = (PaymentRequest)results[0].getRootObject();
    	assertTrue(paymentRequest.getAmt() == this.amount);   	
    }
    
    /**
     * Queries for an existing FICR Coverage Extension Request Pharmacy message
     * then modifies and commits several changes. 
     * @throws IOException
     */
    public void testUpdate() throws IOException {
    	QPaymentRequest query = QPaymentRequest.newQuery();
    	query.select(query.wildcard())
    	     .select(query.reasonOf().invoiceElementGroup().wildcard());
    	
    	query.where(query.classCode().eq("XACT")
    		   .and(query.moodCode().eq("PRP")
    		   .and(query.amt().eq(this.amount))));
     	
    	DataGraph[] results = this.service.find(query);
    	assertTrue(results != null);
    	assertTrue(results.length == 1);
    	
    	PaymentRequest paymentRequest = (PaymentRequest)results[0].getRootObject();
    	assertTrue(paymentRequest.getAmt() == this.amount);  
    	
    	PaymentRequestReason requestReason = paymentRequest.getReasonOf();
    	assertTrue(requestReason != null);
    	requestReason.getInvoiceElementGroup().setConfidentialityCode("Y");
    	
    	this.service.commit(paymentRequest.getDataGraph(), 
    			USERNAME);
    	
    	results = this.service.find(query);
    	assertTrue(results != null);
    	assertTrue(results.length == 1);
    	requestReason = paymentRequest.getReasonOf();
    	assertTrue(requestReason != null);
    	assertTrue("Y".equals(requestReason.getInvoiceElementGroup().getConfidentialityCode()));
    }
    
}