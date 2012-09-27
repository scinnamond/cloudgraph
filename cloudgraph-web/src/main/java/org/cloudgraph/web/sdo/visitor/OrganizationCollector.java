package org.cloudgraph.web.sdo.visitor;

import java.util.ArrayList;
import java.util.List;

import org.plasma.sdo.PlasmaDataGraphVisitor;

import org.cloudgraph.web.sdo.core.Organization;

import commonj.sdo.DataObject;

public class OrganizationCollector implements PlasmaDataGraphVisitor {
	
	private List<Organization> result = new ArrayList<Organization>();
	private int level; 
	
	@SuppressWarnings("unused")
	private OrganizationCollector() {}
	/**
	 * 
	 * @param level the hierarchical/graph level to collect where 
	 * the root level is 0. Use -1 
	 * for all levels.
	 */
	public OrganizationCollector(int level) {
		this.level = level;
	}
	
	public void visit(DataObject target, DataObject source,
		String sourceKey, int traversalLevel) {
	    if (level > 0) {
	    	if (traversalLevel == this.level) {
	    		result.add((Organization)target);
	    	}
	    	// else ignore
	    }
	    else
	    	result.add((Organization)target);
	}
	
	public List<Organization> getResult() {
		return result;
	}
	
}
