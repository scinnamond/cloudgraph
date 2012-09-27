package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QEnumeration;

public class EnumerationQuery {

	public static Query createQuery() {
		QEnumeration query = QEnumeration.newQuery();
		query.select(query.dataType().classifier().seqId())
		     .select(query.dataType().classifier().name())
		     .select(query.dataType().classifier().definition())
		     .select(query.ownedLiteral().seqId())
		     .select(query.ownedLiteral().name());
        return query;		
	}
	
	public static Query createEditQuery(Long seqId) {
		QEnumeration query = QEnumeration.newQuery();
		query.select(query.wildcard())
		     .select(query.dataType().classifier().wildcard())
		     .select(query.dataType().classifier().packageableType().wildcard())
		     .select(query.dataType().classifier().packageableType()._package().wildcard())
		     .select(query.ownedLiteral().wildcard());
		query.where(query.seqId().eq(seqId));		
        return query;		
	}
	
	public static Query createExportQuery() {
		QEnumeration query = QEnumeration.newQuery();
		query.select(query.wildcard())
		     .select(query.dataType().wildcard())
		     .select(query.dataType().classifier().wildcard())
		     .select(query.dataType().classifier().packageableType().wildcard())
		     .select(query.dataType().classifier().packageableType()._package().wildcard())
		     .select(query.ownedLiteral().wildcard());
        return query;		
	}
}
