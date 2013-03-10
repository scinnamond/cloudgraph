package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.core.query.QClassView;
import org.plasma.query.Query;

public class ClassViewQuery {
	
	public static Query createQuery() {
		QClassView query = QClassView.newQuery();
		query.select(query.wildcard());
        return query;		
	}

}
