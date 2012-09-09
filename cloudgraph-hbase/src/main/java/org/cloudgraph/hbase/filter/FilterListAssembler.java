package org.cloudgraph.hbase.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.plasma.query.model.NullLiteral;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

public abstract class FilterListAssembler
    implements HBaseFilterAssembler
{
    private static Log log = LogFactory.getLog(FilterListAssembler.class);

	protected List<Object> params;
	protected FilterList rootFilter;
	protected PlasmaType rootType;
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;

	/* (non-Javadoc)
	 * @see org.cloudgraph.hbase.filter.HBaseFilterAssembler#getFilter()
	 */
	public Filter getFilter() {
		return rootFilter;
	}

	/* (non-Javadoc)
	 * @see org.cloudgraph.hbase.filter.HBaseFilterAssembler#getParams()
	 */
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
	
	
}
