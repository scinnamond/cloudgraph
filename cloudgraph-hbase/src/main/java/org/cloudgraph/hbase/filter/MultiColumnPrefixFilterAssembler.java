package org.cloudgraph.hbase.filter;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.hbase.key.CompositeColumnKeyFactory;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

/**
 */
public class MultiColumnPrefixFilterAssembler extends FilterListAssembler
{
    private static Log log = LogFactory.getLog(MultiColumnPrefixFilterAssembler.class);
	private GraphColumnKeyFactory columnKeyFac;

	@SuppressWarnings("unused")
	private MultiColumnPrefixFilterAssembler() {}
	
	public MultiColumnPrefixFilterAssembler( 
			List<String> propertyNames,
			PlasmaType contextType,
			PlasmaType rootType) 
	{
		this.rootType = rootType;
		this.contextType = contextType;
    	this.columnKeyFac = new CompositeColumnKeyFactory(rootType);
		
    	this.rootFilter = new FilterList(
    		FilterList.Operator.MUST_PASS_ONE);
        
    	// Note: using many binary prefix qualifier filters
    	// rather than a single MultipleColumnPrefixFilter under the
    	// assumption that the binary compare is more
    	// efficient than the string conversion
    	// required by the MultipleColumnPrefixFilter (?)
    	for (String name : propertyNames) {
    		PlasmaProperty prop = (PlasmaProperty)this.contextType.getProperty(name);
    	    this.contextProperty = prop;
    		byte[] key = this.columnKeyFac.createColumnKey(this.contextType, prop);
            QualifierFilter qualFilter = new QualifierFilter(
                CompareFilter.CompareOp.EQUAL,
                new BinaryPrefixComparator(key)); 
            this.rootFilter.addFilter(qualFilter);
    	}
	}	
}
