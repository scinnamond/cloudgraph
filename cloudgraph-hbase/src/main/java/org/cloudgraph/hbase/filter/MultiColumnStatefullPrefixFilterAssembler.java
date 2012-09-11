package org.cloudgraph.hbase.filter;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.common.service.GraphState;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

/**
 */
public class MultiColumnStatefullPrefixFilterAssembler extends FilterListAssembler
{
    private static Log log = LogFactory.getLog(MultiColumnStatefullPrefixFilterAssembler.class);
	private GraphStatefullColumnKeyFactory columnKeyFac;
	private GraphState graphState;

	@SuppressWarnings("unused")
	private MultiColumnStatefullPrefixFilterAssembler() {}
	
	public MultiColumnStatefullPrefixFilterAssembler( 
			GraphState graphState,
			List<String> propertyNames,
			List<Long> sequences,
			PlasmaType contextType,
			PlasmaType rootType) 
	{
		this.contextType = contextType;
		this.rootType = rootType;
		this.graphState = graphState;
		
    	this.rootFilter = new FilterList(
    		FilterList.Operator.MUST_PASS_ONE);
    	 
        this.columnKeyFac = new StatefullColumnKeyFactory(rootType, this.graphState);
        
    	// Note: using many binary prefix qualifier filters
    	// rather than a single MultipleColumnPrefixFilter under the
    	// assumption that the binary compare is more
    	// efficient than the string conversion
    	// required by the MultipleColumnPrefixFilter (?)
        for (Long seq : sequences) {
        	for (String name : propertyNames) {
        		PlasmaProperty prop = (PlasmaProperty)this.contextType.getProperty(name);
        	    byte[] key = this.columnKeyFac.createColumnKey(this.contextType, 
        		    seq, prop);
                QualifierFilter qualFilter = new QualifierFilter(
                    CompareFilter.CompareOp.EQUAL,
                    new BinaryPrefixComparator(key)); 
                this.rootFilter.addFilter(qualFilter);
        	}
        }        
	}
	
}
