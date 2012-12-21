package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.core.query.QPropertyView;

public class PropertyViewQuery {
	
	public static Query createQuery() {
		QPropertyView query = QPropertyView.newQuery();
		query.select(query.wildcard());
        return query;		
	}

	public static Query createQueryByCatName(String catName) {
		QPropertyView query = QPropertyView.newQuery();
		query.select(query.wildcard());
		query.where(query.catName().eq(catName));
        return query;		
	}
	
	public static Query createQueryByCatName(String catName, Long classId) {
		QPropertyView query = QPropertyView.newQuery();
		query.select(query.wildcard());
		query.where(query.catName().eq(catName)
				.and(query.classId().eq(classId)));
        return query;		
	}

	public static Query createQueryByClassName(String className) {
		QPropertyView query = QPropertyView.newQuery();
		query.select(query.wildcard())
		     .select(query.name()) 
		     .select(query.classId()) 
		     .select(query.dataType()) 
		     .select(query.lowerValue()) 
		     .select(query.upperValue());
		query.where(query.className().eq(className));
		query.groupBy(query.seqId())
	     .groupBy(query.name()) 
	     .groupBy(query.classId()) 
	     .groupBy(query.dataType()) 
	     .groupBy(query.lowerValue()) 
	     .groupBy(query.upperValue());
        return query;		
	}
	
	public static Query createQueryByClassId(Long classId) {
		QPropertyView query = QPropertyView.newQuery();
		query.select(query.wildcard());
		query.where(query.classId().eq(classId));

        return query;		
	}
}
