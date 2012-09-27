package org.cloudgraph.web.pagination;

import java.util.ArrayList;
import java.util.List;

import org.plasma.query.model.Property;

import commonj.sdo.DataGraph;


/**
 */
public class BasicProcessor implements Processor {

	protected PagedList pagedList = null;
	
	public String getSortColumnString(String sort) {
		return null;
	}

	public Property getSortColumnProperty(String sort) {
		return null;
	}
	
	public List processResults(DataGraph[] results) {
		List l = new ArrayList();
		for (int i = 0; i < results.length; i++)
			l.add(results[i]);
		return l;
	}
	
	public PagedList getPagedList() {
		return this.pagedList;
	}
	
	public void setPagedList(PagedList pagedList) {
		this.pagedList = pagedList;
	}
}
