package org.cloudgraph.web.model.dashboard;

import org.plasma.query.Query;

public interface TableDataSource {
	public Query createQuery();
	public Query countQuery();
}
