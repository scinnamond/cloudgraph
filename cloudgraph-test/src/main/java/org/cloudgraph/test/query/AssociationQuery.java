package org.cloudgraph.test.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QAssociation;
import org.cloudgraph.web.sdo.meta.query.QSlot;

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
