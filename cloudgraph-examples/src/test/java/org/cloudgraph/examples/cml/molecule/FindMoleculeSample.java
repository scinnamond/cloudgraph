package org.cloudgraph.examples.cml.molecule;

import org.cloudgraph.examples.cml.Molecule;
import org.cloudgraph.examples.cml.query.QAtom;
import org.cloudgraph.examples.cml.query.QMolecule;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;

import commonj.sdo.DataGraph;

public class FindMoleculeSample {
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
