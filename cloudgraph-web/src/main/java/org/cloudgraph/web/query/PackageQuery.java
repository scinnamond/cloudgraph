package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QClazz;
import org.cloudgraph.web.sdo.meta.query.QPackage;

public class PackageQuery {

	public static Query createQuery(String name) {
		QPackage query = QPackage.newQuery();
		query.select(query.wildcard());
		query.where(query.name().eq(name));
        return query;		
	}
	
	public static Query createQuery() {
		QPackage query = QPackage.newQuery();
		query.select(query.wildcard());
        return query;		
	}
	
	public static Query createEditQuery(Long seqId) {
		QPackage query = QPackage.newQuery();
		query.select(query.wildcard())
		     .select(query.parent().wildcard())
		     .select(query.child().wildcard());	        
		query.where(query.seqId().eq(seqId));		
        return query;		
	}

	public static Query createExportQuery() {
		QPackage query = QPackage.newQuery();
		query.select(query.wildcard());
        return query;		
	}
}
