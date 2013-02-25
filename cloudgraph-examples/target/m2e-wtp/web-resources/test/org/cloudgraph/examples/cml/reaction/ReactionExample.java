package org.cloudgraph.examples.cml.reaction;




import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.cml.Atom;
import org.cloudgraph.examples.cml.AtomArray;
import org.cloudgraph.examples.cml.Bond;
import org.cloudgraph.examples.cml.BondArray;
import org.cloudgraph.examples.cml.ConditionList;
import org.cloudgraph.examples.cml.ElementTypeType;
import org.cloudgraph.examples.cml.Identifier;
import org.cloudgraph.examples.cml.Molecule;
import org.cloudgraph.examples.cml.Name;
import org.cloudgraph.examples.cml.ProductList;
import org.cloudgraph.examples.cml.Reactant;
import org.cloudgraph.examples.cml.ReactantList;
import org.cloudgraph.examples.cml.Reaction;
import org.cloudgraph.examples.cml.Scalar;
import org.cloudgraph.examples.cml.Substance;
import org.cloudgraph.examples.cml.SubstanceList;
import org.cloudgraph.examples.cml.query.QReactant;
import org.cloudgraph.examples.cml.query.QReaction;
import org.cloudgraph.test.CMLTest;
import org.plasma.query.Expression;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Chemical Markup Language (CML) - Reaction Tests
 */
public class ReactionExample extends CMLTest {
    private static Log log = LogFactory.getLog(ReactionExample.class);

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
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Reaction.class);
    	
    	Reaction reaction = (Reaction)dataGraph.createRootObject(rootType);
    	reaction.setId(idgen);
    	Identifier identifier = reaction.createIdentifier();
    	identifier.setValue("CH2O2+CH4O-->CH5O2+H2O");
    	identifier.setTitle("Formic Acid + Methanol produces Methl Formate, Water");
    	
    	ReactantList reactants = reaction.createReactantList();
    	
    	// Formic Acid Reactant
    	Reactant formicAcidReactant = reactants.createReactant();
    	formicAcidReactant.setTitle("Formic Acid");
    	Molecule formicAcid = formicAcidReactant.createMolecule();
    	formicAcid.setId("formic");
    	formicAcid.setIdgen(idgen);
    	formicAcid.setFormula("C H2 O2");
    	AtomArray formicAtoms = formicAcid.createAtomArray();
    	
    	Atom carbon = formicAtoms.createAtom();
    	carbon.setElementType(ElementTypeType.C.getInstanceName());
    	carbon.setId("c1");
    	carbon.setHydrogenCount(BigInteger.valueOf(1)); // note: xsd:nonNegativeInteger maps to Java BigInteger in SDO 2.1 spec 
    	
    	Atom oxygen1 = formicAtoms.createAtom();
    	oxygen1.setElementType(ElementTypeType.O.getInstanceName());
    	oxygen1.setId("o1");
    	oxygen1.setHydrogenCount(BigInteger.valueOf(1));  
    	
    	Atom oxygen2 = formicAtoms.createAtom();
    	oxygen2.setElementType(ElementTypeType.O.getInstanceName());
    	oxygen2.setId("o2");
    	oxygen2.setHydrogenCount(BigInteger.valueOf(0));  
    	
    	BondArray formicBonds = formicAcid.createBondArray();
    	Bond b_1 = formicBonds.createBond();
    	b_1.setId("b1");
    	b_1.setOrder("S");
    	b_1.addAtomRefs(carbon.getId());
    	b_1.addAtomRefs(oxygen1.getId());

    	Bond b_2 = formicBonds.createBond();
    	b_2.setId("b2");
    	b_2.setOrder("D");
    	b_2.addAtomRefs(carbon.getId());
    	b_2.addAtomRefs(oxygen2.getId());    	
    	
    	// Methanol Reactant
    	Reactant methanolAcidReactant = reactants.createReactant();
    	methanolAcidReactant.setTitle("Methanol");
    	Molecule methanol = methanolAcidReactant.createMolecule();
    	methanol.setId("formic");
    	methanol.setIdgen(idgen);
    	methanol.setFormula("C H4 O");
    	AtomArray methanolAtoms = methanol.createAtomArray();

    	carbon = methanolAtoms.createAtom();
    	carbon.setElementType(ElementTypeType.C.getInstanceName());
    	carbon.setId("c1");
    	carbon.setHydrogenCount(BigInteger.valueOf(3)); // note: xsd:nonNegativeInteger maps to Java BigInteger in SDO 2.1 spec 
    	
    	oxygen1 = methanolAtoms.createAtom();
    	oxygen1.setElementType(ElementTypeType.O.getInstanceName());
    	oxygen1.setId("o1");
    	oxygen1.setHydrogenCount(BigInteger.valueOf(1));  

    	BondArray methanolBonds = methanol.createBondArray();
    	b_1 = methanolBonds.createBond();
    	b_1.setId("b1");
    	b_1.setOrder("S");
    	b_1.addAtomRefs(carbon.getId());
    	b_1.addAtomRefs(oxygen1.getId());
    	
    	ProductList products = reaction.createProductList();
    	
    	// Methl Formate (Meformate) Product
    	Molecule meformate = products.createProduct().createMolecule();
    	meformate.setId("meformate");
    	meformate.setIdgen(idgen);
    	meformate.setFormula("C H5 O2");
    	AtomArray meformateAtoms = meformate.createAtomArray();
    	
    	carbon = meformateAtoms.createAtom();
    	carbon.setElementType(ElementTypeType.C.getInstanceName());
    	carbon.setId("c1");
    	carbon.setHydrogenCount(BigInteger.valueOf(3)); // note: xsd:nonNegativeInteger maps to Java BigInteger in SDO 2.1 spec 

    	Atom carbon2 = meformateAtoms.createAtom();
    	carbon2.setElementType(ElementTypeType.C.getInstanceName());
    	carbon2.setId("c2");
    	carbon2.setHydrogenCount(BigInteger.valueOf(1));  
    	
    	oxygen1 = meformateAtoms.createAtom();
    	oxygen1.setElementType(ElementTypeType.O.getInstanceName());
    	oxygen1.setId("o1");
    	oxygen1.setHydrogenCount(BigInteger.valueOf(1));  

    	oxygen2 = meformateAtoms.createAtom();
    	oxygen2.setElementType(ElementTypeType.O.getInstanceName());
    	oxygen2.setId("o2");
    	oxygen2.setHydrogenCount(BigInteger.valueOf(0));  

    	BondArray meformateBonds = meformate.createBondArray();
    	b_1 = meformateBonds.createBond();
    	b_1.setId("b1");
    	b_1.setOrder("S");
    	b_1.addAtomRefs(carbon.getId());
    	b_1.addAtomRefs(oxygen1.getId());
    	
    	b_2 = meformateBonds.createBond();
    	b_2.setId("b2");
    	b_2.setOrder("S");
    	b_2.addAtomRefs(carbon2.getId());
    	b_2.addAtomRefs(oxygen1.getId());
    	
    	Bond b_3 = meformateBonds.createBond();
    	b_3.setId("b3");
    	b_3.setOrder("D");
    	b_3.addAtomRefs(carbon2.getId());
    	b_3.addAtomRefs(oxygen2.getId());

    	// Water Product
    	Molecule water = products.createProduct().createMolecule();
    	water.setId("water");
    	water.setIdgen(idgen);
    	water.setFormula("H2 O");
    	
    	// Reaction Conditions
    	ConditionList conditions = reaction.createConditionList();
    	
    	Scalar temp = conditions.createScalar();
    	temp.setDictRef("cml:temp");
    	temp.setUnits("cml:Celsius");
    	temp.setValue("70");
    	
    	Scalar duration = conditions.createScalar();
    	duration.setDictRef("cml:timeDuration");
    	duration.setUnits("xsd:date");
    	duration.setValue("04:00");
    	
    	// Reaction Substances
    	SubstanceList substances = reaction.createSubstanceList();
    	
    	Substance waterSubstance = substances.createSubstance();
    	waterSubstance.setRole("solvent");
    	waterSubstance.setDictRef("cmlSolvent:water");
    	Name name = waterSubstance.createName(); 
    	name.setValue("water");

    	Substance hPlus = substances.createSubstance();
    	hPlus.setRole("catalyst");
    	hPlus.setDictRef("cmlSubstance:acid");
    	Name name2 = hPlus.createName(); 
    	name2.setValue("H+");
    	
    	String xml = this.serializeGraph(reaction.getDataGraph());
    	log.info(xml);
    	
    	this.service.commit(reaction.getDataGraph(), 
    			USERNAME);
    	
    }
    
    /**
     * Queries for an existing Reaction but "slices" the reactants  
     * by Methanol. 
     * @throws IOException
     */
    public void testQuery() throws IOException {
    	QReaction reaction = QReaction.newQuery();
    	Expression predicate = QReactant.newQuery().title().eq("Methanol");
    	
    	reaction.select(reaction.id())
    	   .select(reaction.identifier().wildcard())
    	   .select(reaction.reactantList().reactant(predicate).wildcard()) // filter reactants by molecule formulas  by "*H2*"
    	   .select(reaction.reactantList().reactant(predicate)
               .molecule().wildcard()); // filter reactants by molecule formulas  by "*H2*"
    	
    	reaction.where(reaction.identifier().value().eq("CH2O2+CH4O-->CH5O2+H2O")
     		   .and(reaction.id().eq(this.idgen)));
    	
    	DataGraph[] results = this.service.find(reaction);
    	assertTrue(results != null);
    	for (DataGraph graph : results) {
        	String xml = this.serializeGraph(graph);
        	log.info(xml);
    	}
    	
    	assertTrue(results.length == 1);
    	
    	Reaction reactionResult = (Reaction)results[0].getRootObject();
    	assertTrue(reactionResult.getId().equals(this.idgen)); 
    	
    	assertTrue(reactionResult.getReactantList(0).getReactantCount() == 1);
    	Reactant reactant = reactionResult.getReactantList(0).getReactant(0);
    	assertTrue("Methanol".equals(reactant.getTitle()));
    }
    
}