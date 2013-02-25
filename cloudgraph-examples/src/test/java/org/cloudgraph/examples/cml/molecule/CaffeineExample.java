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
package org.cloudgraph.examples.cml.molecule;




import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.cml.Atom;
import org.cloudgraph.examples.cml.AtomArray;
import org.cloudgraph.examples.cml.Bond;
import org.cloudgraph.examples.cml.BondArray;
import org.cloudgraph.examples.cml.ElementTypeType;
import org.cloudgraph.examples.cml.Molecule;
import org.cloudgraph.examples.cml.query.QAtom;
import org.cloudgraph.examples.cml.query.QMolecule;
import org.cloudgraph.test.CMLTest;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Chemical Markup Language (CML) - Caffeine Molecule Tests
 */
public class CaffeineExample extends CMLTest {
    private static Log log = LogFactory.getLog(CaffeineExample.class);

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
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Molecule.class);
    	
    	Molecule caffeine = (Molecule)dataGraph.createRootObject(rootType);
    	caffeine.setTitle("Caffeine Molecule");
    	caffeine.setId("mol_caffeine");
    	caffeine.setIdgen(idgen);
    	caffeine.setFormula("C8 H10 N4 O2");
    	
    	AtomArray atoms = caffeine.createAtomArray();
    	Atom a_1 = atoms.createAtom();
    	a_1.setId("caffeine_karne_a_1");
    	a_1.setX3(-2.8709);
    	a_1.setY3(-1.0499);
    	a_1.setZ3(0.1718);
    	a_1.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_2 = atoms.createAtom();
    	a_2.setId("caffeine_karne_a_2");
    	a_2.setX3(-2.9099);
    	a_2.setY3(0.2747);
    	a_2.setZ3(0.1062);
    	a_2.setElementType(ElementTypeType.N.getInstanceName());
    	
    	Atom a_3 = atoms.createAtom();
    	a_3.setId("caffeine_karne_a_3");
    	a_3.setX3(-1.8026);
    	a_3.setY3(0.9662);
    	a_3.setZ3(-0.1184);
    	a_3.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_4 = atoms.createAtom();
    	a_4.setId("caffeine_karne_a_4");
    	a_4.setX3(-0.6411);
    	a_4.setY3(0.2954);
    	a_4.setZ3(-0.2316);
    	a_4.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_5 = atoms.createAtom();
    	a_5.setId("caffeine_karne_a_5");
    	a_5.setX3(-0.6549);
    	a_5.setY3(-1.0889);
    	a_5.setZ3(-1.0889);
    	a_5.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_6 = atoms.createAtom();
    	a_6.setId("caffeine_karne_a_6");
    	a_6.setX3(-1.7352);
    	a_6.setY3(-1.7187);
    	a_6.setZ3(0.0624);
    	a_6.setElementType(ElementTypeType.N.getInstanceName());

    	Atom a_7 = atoms.createAtom();
    	a_7.setId("caffeine_karne_a_7");
    	a_7.setX3(0.6052);
    	a_7.setY3(0.7432);
    	a_7.setZ3(-0.4434);
    	a_7.setElementType(ElementTypeType.N.getInstanceName());

    	Atom a_8 = atoms.createAtom();
    	a_8.setId("caffeine_karne_a_8");
    	a_8.setX3(1.2863);
    	a_8.setY3(-0.4175);
    	a_8.setZ3(-0.4514);
    	a_8.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_9 = atoms.createAtom();
    	a_9.setId("caffeine_karne_a_9");
    	a_9.setX3(0.5994);
    	a_9.setY3(-1.5633);
    	a_9.setZ3(-0.2698);
    	a_9.setElementType(ElementTypeType.N.getInstanceName());
   	
    	Atom a_10 = atoms.createAtom();
    	a_10.setId("caffeine_karne_a_10");
    	a_10.setX3(1.0875);
    	a_10.setY3(2.0867);
    	a_10.setZ3(-0.6139);
    	a_10.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_11 = atoms.createAtom();
    	a_11.setId("caffeine_karne_a_11");
    	a_11.setX3(-1.8349);
    	a_11.setY3(2.1699);
    	a_11.setZ3(-0.2205);
    	a_11.setElementType(ElementTypeType.O.getInstanceName());

    	Atom a_12 = atoms.createAtom();
    	a_12.setId("caffeine_karne_a_12");
    	a_12.setX3(-4.2178);
    	a_12.setY3(0.9810);
    	a_12.setZ3(0.2003);
    	a_12.setElementType(ElementTypeType.C.getInstanceName());
    	
    	Atom a_13 = atoms.createAtom();
    	a_13.setId("caffeine_karne_a_13");
    	a_13.setX3(-3.8944);
    	a_13.setY3(-1.6746);
    	a_13.setZ3(0.3323);
    	a_13.setElementType(ElementTypeType.O.getInstanceName());

    	Atom a_14 = atoms.createAtom();
    	a_14.setId("caffeine_karne_a_14");
    	a_14.setX3(-1.6764);
    	a_14.setY3(-3.1997);
    	a_14.setZ3(0.1458);
    	a_14.setElementType(ElementTypeType.C.getInstanceName());

    	Atom a_15 = atoms.createAtom();
    	a_15.setId("caffeine_karne_a_15");
    	a_15.setX3(2.3776);
    	a_15.setY3(-0.4481);
    	a_15.setZ3(-0.6036);
    	a_15.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_16 = atoms.createAtom();
    	a_16.setId("caffeine_karne_a_16");
    	a_16.setX3(2.1902);
    	a_16.setY3(2.0944);
    	a_16.setZ3(-0.7699);
    	a_16.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_17 = atoms.createAtom();
    	a_17.setId("caffeine_karne_a_17");
    	a_17.setX3(0.6074);
    	a_17.setY3(2.5547);
    	a_17.setZ3(-1.5032);
    	a_17.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_18 = atoms.createAtom();
    	a_18.setId("caffeine_karne_a_18");
    	a_18.setX3(0.8606);
    	a_18.setY3(2.6915);
    	a_18.setZ3(0.2934);
    	a_18.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_19 = atoms.createAtom();
    	a_19.setId("caffeine_karne_a_19");
    	a_19.setX3(-4.0942);
    	a_19.setY3(2.0097);
    	a_19.setZ3(0.6091);
    	a_19.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_20 = atoms.createAtom();
    	a_20.setId("caffeine_karne_a_20");
    	a_20.setX3(1.0875);
    	a_20.setY3(1.0338);
    	a_20.setZ3(-0.8167);
    	a_20.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_21 = atoms.createAtom();
    	a_21.setId("caffeine_karne_a_21");
    	a_21.setX3(-4.9101);
    	a_21.setY3(0.4518);
    	a_21.setZ3(0.8943);
    	a_21.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_22 = atoms.createAtom();
    	a_22.setId("caffeine_karne_a_22");
    	a_22.setX3(-2.3049);
    	a_22.setY3(-3.6334);
    	a_22.setZ3(-0.6659);
    	a_22.setElementType(ElementTypeType.H.getInstanceName());
    	
    	Atom a_23 = atoms.createAtom();
    	a_23.setId("caffeine_karne_a_23");
    	a_23.setX3(-0.6444);
    	a_23.setY3(-3.6030);
    	a_23.setZ3(0.0359);
    	a_23.setElementType(ElementTypeType.H.getInstanceName());

    	Atom a_24 = atoms.createAtom();
    	a_24.setId("caffeine_karne_a_24");
    	a_24.setX3(-2.0682);
    	a_24.setY3(-3.5218);
    	a_24.setZ3(1.1381);
    	a_24.setElementType(ElementTypeType.H.getInstanceName());
    	
    	BondArray bonds = caffeine.createBondArray();
    	Bond b_1 = bonds.createBond();
    	b_1.setId("caffeine_karne_b_1");
    	b_1.setOrder("1");
    	b_1.addAtomRefs(a_1.getId());
    	b_1.addAtomRefs(a_2.getId());

    	Bond b_2 = bonds.createBond();
    	b_2.setId("caffeine_karne_b_2");
    	b_2.setOrder("1");
    	b_2.addAtomRefs(a_1.getId());
    	b_2.addAtomRefs(a_6.getId());

    	Bond b_3 = bonds.createBond();
    	b_3.setId("caffeine_karne_b_3");
    	b_3.setOrder("2");
    	b_3.addAtomRefs(a_1.getId());
    	b_3.addAtomRefs(a_13.getId());

    	Bond b_4 = bonds.createBond();
    	b_4.setId("caffeine_karne_b_4");
    	b_4.setOrder("1");
    	b_4.addAtomRefs(a_2.getId());
    	b_4.addAtomRefs(a_3.getId());

    	Bond b_5 = bonds.createBond();
    	b_5.setId("caffeine_karne_b_5");
    	b_5.setOrder("1");
    	b_5.addAtomRefs(a_2.getId());
    	b_5.addAtomRefs(a_12.getId());

    	Bond b_6 = bonds.createBond();
    	b_6.setId("caffeine_karne_b_6");
    	b_6.setOrder("1");
    	b_6.addAtomRefs(a_3.getId());
    	b_6.addAtomRefs(a_4.getId());

    	Bond b_7 = bonds.createBond();
    	b_7.setId("caffeine_karne_b_7");
    	b_7.setOrder("2");
    	b_7.addAtomRefs(a_3.getId());
    	b_7.addAtomRefs(a_11.getId());

    	Bond b_8 = bonds.createBond();
    	b_8.setId("caffeine_karne_b_8");
    	b_8.setOrder("2");
    	b_8.addAtomRefs(a_4.getId());
    	b_8.addAtomRefs(a_5.getId());

    	Bond b_9 = bonds.createBond();
    	b_9.setId("caffeine_karne_b_9");
    	b_9.setOrder("1");
    	b_9.addAtomRefs(a_4.getId());
    	b_9.addAtomRefs(a_7.getId());

    	Bond b_10 = bonds.createBond();
    	b_10.setId("caffeine_karne_b_10");
    	b_10.setOrder("1");
    	b_10.addAtomRefs(a_5.getId());
    	b_10.addAtomRefs(a_6.getId());

    	Bond b_11 = bonds.createBond();
    	b_11.setId("caffeine_karne_b_11");
    	b_11.setOrder("1");
    	b_11.addAtomRefs(a_5.getId());
    	b_11.addAtomRefs(a_9.getId());

    	Bond b_12 = bonds.createBond();
    	b_12.setId("caffeine_karne_b_12");
    	b_12.setOrder("1");
    	b_12.addAtomRefs(a_6.getId());
    	b_12.addAtomRefs(a_11.getId());

    	Bond b_13 = bonds.createBond();
    	b_13.setId("caffeine_karne_b_13");
    	b_13.setOrder("2");
    	b_13.addAtomRefs(a_7.getId());
    	b_13.addAtomRefs(a_8.getId());

    	Bond b_14 = bonds.createBond();
    	b_14.setId("caffeine_karne_b_14");
    	b_14.setOrder("1");
    	b_14.addAtomRefs(a_7.getId());
    	b_14.addAtomRefs(a_10.getId());

    	Bond b_15 = bonds.createBond();
    	b_15.setId("caffeine_karne_b_15");
    	b_15.setOrder("2");
    	b_15.addAtomRefs(a_8.getId());
    	b_15.addAtomRefs(a_9.getId());

    	Bond b_16 = bonds.createBond();
    	b_16.setId("caffeine_karne_b_16");
    	b_16.setOrder("1");
    	b_16.addAtomRefs(a_8.getId());
    	b_16.addAtomRefs(a_15.getId());

    	Bond b_17 = bonds.createBond();
    	b_17.setId("caffeine_karne_b_17");
    	b_17.setOrder("1");
    	b_17.addAtomRefs(a_10.getId());
    	b_17.addAtomRefs(a_16.getId());

    	Bond b_18 = bonds.createBond();
    	b_18.setId("caffeine_karne_b_18");
    	b_18.setOrder("1");
    	b_18.addAtomRefs(a_10.getId());
    	b_18.addAtomRefs(a_17.getId());

    	Bond b_19 = bonds.createBond();
    	b_19.setId("caffeine_karne_b_19");
    	b_19.setOrder("1");
    	b_19.addAtomRefs(a_10.getId());
    	b_19.addAtomRefs(a_18.getId());    	

    	Bond b_20 = bonds.createBond();
    	b_20.setId("caffeine_karne_b_20");
    	b_20.setOrder("1");
    	b_20.addAtomRefs(a_12.getId());
    	b_20.addAtomRefs(a_19.getId());

    	Bond b_21 = bonds.createBond();
    	b_21.setId("caffeine_karne_b_21");
    	b_21.setOrder("1");
    	b_21.addAtomRefs(a_12.getId());
    	b_21.addAtomRefs(a_20.getId());

    	Bond b_22 = bonds.createBond();
    	b_22.setId("caffeine_karne_b_22");
    	b_22.setOrder("1");
    	b_22.addAtomRefs(a_12.getId());
    	b_22.addAtomRefs(a_21.getId());

    	Bond b_23 = bonds.createBond();
    	b_23.setId("caffeine_karne_b_23");
    	b_23.setOrder("1");
    	b_23.addAtomRefs(a_14.getId());
    	b_23.addAtomRefs(a_22.getId());

    	Bond b_24 = bonds.createBond();
    	b_24.setId("caffeine_karne_b_24");
    	b_24.setOrder("1");
    	b_24.addAtomRefs(a_14.getId());
    	b_24.addAtomRefs(a_23.getId());

    	Bond b_25 = bonds.createBond();
    	b_25.setId("caffeine_karne_b_25");
    	b_25.setOrder("1");
    	b_25.addAtomRefs(a_14.getId());
    	b_25.addAtomRefs(a_24.getId());

    	String xml = this.serializeGraph(caffeine.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(caffeine.getDataGraph(), 
    			USERNAME);
    	
    }
    
    /**
     * Queries for an existing Molecule sliced by its carbon atoms.
     * @throws IOException
     */
    public void testQuery() throws IOException {
    	QMolecule molecule = QMolecule.newQuery();
    	QAtom atom = QAtom.newQuery();
    	
    	molecule.select(molecule.wildcard())
    	   .select(molecule.atomArray() // slice by carbon atoms
               .atom(atom.elementType().eq("C")).wildcard())
    	   .select(molecule.bondArray().bond().wildcard());
    	
    	molecule.where(molecule.id().eq("mol_caffeine")
     		   .and(molecule.formula().eq("C8 H10 N4 O2")
     		   .and(molecule.idgen().eq(this.idgen))));
    	
    	DataGraph[] results = this.service.find(molecule);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	Molecule moleculeResult = (Molecule)results[0].getRootObject();
    	assertTrue(moleculeResult.getIdgen().equals(this.idgen));   	
    	// assure we returned only 8 (carbon) atoms
    	assertTrue(moleculeResult.getAtomArray(0).getAtomCount() == 8); // carbon
    }
    
    /**
     * Queries for an existing Molecule sliced by its carbon atoms
     * then updates each carbon atom.
     * @throws IOException
     */
    public void testUpdate() throws IOException {
    	QMolecule molecule = QMolecule.newQuery();
    	QAtom atom = QAtom.newQuery();
    	molecule.select(molecule.wildcard())
    	   .select(molecule.atomArray() // slice by carbon atoms
               .atom(atom.elementType().eq("C")).wildcard());
    	
    	molecule.where(molecule.id().eq("mol_caffeine")
    		   .and(molecule.formula().eq("C8 H10 N4 O2")
    		   .and(molecule.idgen().eq(this.idgen))));
    	
    	DataGraph[] results = this.service.find(molecule);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	Molecule moleculeResult = (Molecule)results[0].getRootObject();
    	assertTrue(moleculeResult.getIdgen().equals(this.idgen));  
    	for (Atom carbon : moleculeResult.getAtomArray(0).getAtom()) {
    		carbon.setRef("I'm a Carbon Atom");
    	}
    	
    	this.service.commit(moleculeResult.getDataGraph(), 
    			USERNAME);
    	
    	results = this.service.find(molecule);
    	assertTrue(results != null);
    	assertTrue(results.length == 1);
    	moleculeResult = (Molecule)results[0].getRootObject();
    	String xml = this.serializeGraph(moleculeResult.getDataGraph());
    	log.info(xml);
    }
    
}