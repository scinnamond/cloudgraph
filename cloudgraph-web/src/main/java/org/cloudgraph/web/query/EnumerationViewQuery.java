package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.core.query.QEnumerationView;
import org.plasma.query.Query;

public class EnumerationViewQuery {
	
	public static Query createQuery() {
		QEnumerationView query = QEnumerationView.newQuery();
		query.select(query.wildcard());
        return query;		
	}

}
