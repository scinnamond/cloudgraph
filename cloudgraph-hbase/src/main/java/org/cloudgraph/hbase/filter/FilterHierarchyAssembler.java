package org.cloudgraph.hbase.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.sdo.PlasmaType;


/**
 * Supports assembly of complex HBase filter hierarchies representing 
 * query predicate expression trees using a filter stack. 
 * <p>
 * HBase filters may be collected into 
 * lists using <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.html" target="#">FilterList</a>
 * each with a 
 * <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ALL" target="#">MUST_PASS_ALL</a> or <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/FilterList.Operator.html#MUST_PASS_ONE" target="#">MUST_PASS_ONE</a>
 *  (logical) operator. Lists may then be assembled into hierarchies 
 * used to represent complex expression trees filtering either rows
 * or columns in HBase.
 * </p> 
 */
public abstract class FilterHierarchyAssembler extends DefaultQueryVisitor 
    implements HBaseFilterAssembler
{
    private static Log log = LogFactory.getLog(FilterHierarchyAssembler.class);

	protected List<Object> params = new ArrayList<Object>();
	protected FilterList rootFilter;
    protected Stack<FilterList> filterStack = new Stack<FilterList>();
	protected PlasmaType rootType;
	
	@SuppressWarnings("unused")
	private FilterHierarchyAssembler() {}
	protected FilterHierarchyAssembler(PlasmaType rootType) {
		this.rootType = rootType;
		
    	this.rootFilter = new FilterList(
    		FilterList.Operator.MUST_PASS_ALL);
    	 
    	this.filterStack.push(this.rootFilter);  
	}

    /**
     * Returns the assembled filter, filter list or filter hierarchy root.
     * @return the assembled filter, filter list or  or filter hierarchy root.
     */
	public Filter getFilter() {
		return rootFilter;
	}

	
	public Object[] getParams() {
		Object[] result = new Object[params.size()];
		Iterator<Object> iter = params.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			Object param = iter.next();
			if (!(param instanceof NullLiteral))
				result[i] = param;
			else
				result[i] = null;
		}
		return result;
	}	
	
	
	protected void pushFilter() {
        FilterList top = this.filterStack.peek();
        FilterList next = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        top.addFilter(next);
        this.filterStack.push(next);  		
	}
	
	protected void popFilter() {
		this.filterStack.pop();
	}	
	
	// String.split() can cause empty tokens under some circumstances
	protected String[] filterTokens(String[] tokens) {
		int count = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0)
				count++;
		String[] result = new String[count];
		int j = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0) {
				result[j] = tokens[i];
				j++;
			}
		return result;
	}
	


}
