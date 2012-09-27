package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.core.query.QClassView;

public class ClassViewQuery {
	
	public static Query createQuery() {
		QClassView query = QClassView.newQuery();
		query.select(query.wildcard());
        return query;		
	}

}
