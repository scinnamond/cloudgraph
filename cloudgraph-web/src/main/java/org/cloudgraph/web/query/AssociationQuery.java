package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.meta.query.QAssociation;
import org.plasma.query.Query;

public class AssociationQuery {
	
	public static Query createExportQuery() {
		
		QAssociation query = QAssociation.newQuery();
		query.select(query.wildcard())
		     .select(query.classifier().wildcard())
		     .select(query.memberEnd().externalId())
		;
		return query;
	}
}
