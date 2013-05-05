package org.cloudgraph.examples.cml.molecule;

import org.cloudgraph.examples.cml.Atom;
import org.cloudgraph.examples.cml.AtomArray;
import org.cloudgraph.examples.cml.Molecule;
import org.cloudgraph.examples.cml.query.QAtom;
import org.cloudgraph.examples.cml.query.QMolecule;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;
import commonj.sdo.helper.TypeHelper;

public class CreateMoleculeSample {
public Molecule createMolecule() {
  DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
  dataGraph.getChangeSummary().beginLogging();
  Type rootType = TypeHelper.INSTANCE.getType(Molecule.class);    
  Molecule molecule = (Molecule)dataGraph.createRootObject(rootType);
  molecule.setTitle("Caffeine Molecule");
  molecule.setId("mol_caffeine");
  molecule.setFormula("C8 H10 N4 O2");
  AtomArray atoms = molecule.createAtomArray();
  Atom atom = atoms.createAtom();
  atom.setId("caffeine_karne_a_1");
  atom.setX3(-2.8709); atom.setY3(-1.0499); atom.setZ3(0.1718);
  atom.setElementType(ElementType.CARBON.name());
  HBasePojoDataAccessClient client = new HBasePojoDataAccessClient();
  client.commit(dataGraph, "username");  
  return molecule;
}

public Molecule findMolecule() {
  QMolecule molecule = QMolecule.newQuery();
  QAtom atom = QAtom.newQuery();
  molecule.select(molecule.formula())
	      .select(molecule.atomArray() // slice by carbon atoms
	          .atom(atom.elementType().eq("C")).wildcard())
	      .select(molecule.bondArray().bond().wildcard());    
  molecule.where(molecule.id().like("*caffeine*")
	      .and(molecule.formula().eq("C8 H10 N4 O2")));
  DataGraph[] result = 
		(new HBasePojoDataAccessClient()).find(molecule);
  return (Molecule)result[0].getRootObject();
}

}
