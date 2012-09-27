package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.core.query.QApplicationView;

public class ApplicationViewQuery {

	public static Query createQuery(Long deputyArea,
			Long businessUnit, String name) {
		
		QApplicationView query = QApplicationView.newQuery();
		query.select(query.wildcard());
		return query;
	}
}
