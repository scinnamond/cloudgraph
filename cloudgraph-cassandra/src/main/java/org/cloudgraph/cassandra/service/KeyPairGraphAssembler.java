package org.cloudgraph.cassandra.service;

import java.util.List;

import org.plasma.sdo.access.DataGraphAssembler;
import org.plasma.sdo.access.provider.common.PropertyPair;

public interface KeyPairGraphAssembler extends DataGraphAssembler {
	/**
	 * Initiates the assembly of a data graph based on the 
	 * given property key value pair results list. 
	 * @param results the results list
	 * 
	 * @see DataGraphAssembler.getDataGraph()
	 */
	public void assemble(List<PropertyPair> results);

}
