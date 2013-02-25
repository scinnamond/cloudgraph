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
package org.cloudgraph.examples.bioxsd.sequence;




import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.bioxsd.AminoacidSequenceRecord;
import org.cloudgraph.examples.bioxsd.RecommendedDatabaseName;
import org.cloudgraph.examples.bioxsd.SequenceReference;
import org.cloudgraph.examples.bioxsd.Species;
import org.cloudgraph.examples.cml.Atom;
import org.cloudgraph.examples.cml.AtomArray;
import org.cloudgraph.examples.cml.Bond;
import org.cloudgraph.examples.cml.BondArray;
import org.cloudgraph.examples.cml.ElementTypeType;
import org.cloudgraph.examples.cml.Molecule;
import org.cloudgraph.examples.cml.query.QAtom;
import org.cloudgraph.examples.cml.query.QMolecule;
import org.cloudgraph.test.BioXSDTest;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * BioXSD 1.1 - Amino Acid Sequence Tests
 */
public class AminoAcidSequenceExample extends BioXSDTest {
    private static Log log = LogFactory.getLog(AminoAcidSequenceExample.class);

    private static String idgen;
    public void setUp() throws Exception {
        super.setUp();
        if (idgen == null) {
        	idgen = String.valueOf(System.currentTimeMillis());
        }
    }        
   
    public void testCreate() throws IOException {
    	    	
        DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
        dataGraph.getChangeSummary().beginLogging(); // log changes from this point
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(AminoacidSequenceRecord.class);
    	
    	AminoacidSequenceRecord aminoAcid = (AminoacidSequenceRecord)dataGraph.createRootObject(rootType);
    	aminoAcid.setSequence_("MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGPDEAPRMPEAAPPVAPAPAAPTPAAPAPAPSWPLSSSVPSQKTYQGSYGFRLGFLHSGTAKSVTCTYSPALNKMFCQLAKTCPVQLWVDSTPPPGTRVRAMAIYKQSQHMTEVVRRCPHHERCSDSDGLAPPQHLIRVEGNLRVEYLDDRNTFRHSVVVPYEPPEVGSDCTTIHYNYMCNSSCMGGMNRRPILTIITLEDSSGNLLGRNSFEVRVCACPGRDRRTEEENLRKKGEPHHELPPGSTKRALPNNTSSSPQPKKKPLDGEYFTLQIRGRERFEMFRELNEALELKDAQAGKEPGGSRAHSSHLKSKKGQSTSRHKKLMFKTEGPDSD");
    	aminoAcid.setName("P53_HUMAN Cellular tumor antigen p53 " 
    	    + String.valueOf(System.currentTimeMillis()));
    	aminoAcid.setNote("Gene: TP53; Evidence at protein level");
    	
    	Species species = aminoAcid.createSpecies();
    	species.setDbName(RecommendedDatabaseName.NCBI__TAXONOMY.getInstanceName());
    	species.setDbUri("http://www.ncbi.nlm.nih.gov/Taxonomy");//==taxonomy (db) identified by both name & uri. 1 of them would be enough for identification==-->
    	species.setAccession("9606");
    	species.setEntryUri("http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=9606"); //==organism (db entry) identifier by both name & uri. 1 of them would be enough for identification==-->
    	species.setSpeciesName("Human"); //any name for human reader or none==-->

    	SequenceReference reference = aminoAcid.createReference();
    	reference.setDbName("UniProt/Swiss-Prot");
    	reference.setDbUri("http://www.uniprot.org/uniprot");
    	reference.setAccession("P04637");
    	reference.setEntryUri("http://www.uniprot.org/uniprot/P04637"); 
    	reference.setSequenceVersion("4");
    	
    	String xml = this.serializeGraph(aminoAcid.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(aminoAcid.getDataGraph(), 
    			USERNAME);
    	
    }
    
    
}