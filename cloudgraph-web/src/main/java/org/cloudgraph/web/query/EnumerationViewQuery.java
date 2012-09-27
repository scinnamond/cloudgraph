package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.core.query.QEnumerationView;

public class EnumerationViewQuery {
	
	public static Query createQuery() {
		QEnumerationView query = QEnumerationView.newQuery();
		query.select(query.wildcard());
        return query;		
	}

}
