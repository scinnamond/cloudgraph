package org.cloudgraph.test.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QClazz;
import org.cloudgraph.web.sdo.meta.query.QNamedElement;

public class ClassQuery {

	public static Query createQuery() {
		QClazz clazz = QClazz.newQuery();
		clazz.select(clazz.wildcard())
		     .select(clazz.classifier().seqId())
		     .select(clazz.classifier().name())
		     .select(clazz.classifier().definition())
		     .select(clazz.classifier().packageableType().wildcard())
		     .select(clazz.classifier().packageableType()._package().wildcard())
		     .select(clazz.ownedAttribute().seqId())
		     .select(clazz.ownedAttribute().name());
        return clazz;		
	}
	
	public static Query createQueryByPackageId(Long pkgId) {
		QClazz query = QClazz.newQuery();
		query.select(query.seqId()); 
		query.select(query.classifier().name()); 
		query.select(query.classifier().seqId()); 
		query.where(query.classifier().packageableType()._package().seqId().eq(pkgId));
        return query;		
	}
	
	public static Query createIdQuery() {
		QClazz query = QClazz.newQuery();
		query.select(query.seqId()); 
		query.select(query.classifier().name()); 
		query.select(query.classifier().seqId()); 
        return query;		
	}
	
	public static Query createEditQuery(Long seqId) {
		QClazz clazz = QClazz.newQuery();
		clazz.select(clazz.wildcard())
		     .select(clazz.classifier().wildcard())
		     .select(clazz.classifier().packageableType().wildcard())
		     .select(clazz.classifier().packageableType()._package().wildcard())
		     .select(clazz.generalization().wildcard())
		     .select(clazz.general().wildcard())
		     .select(clazz.ownedAttribute().wildcard()) 
	         .select(clazz.ownedAttribute().dataType().wildcard())
	         .select(clazz.ownedAttribute().association().wildcard())
	         .select(clazz.ownedAttribute().association().classifier().wildcard()) 
	         .select(clazz.classCategorization().categorization().category().seqId());	        
		clazz.where(clazz.seqId().eq(seqId));		
        return clazz;		
	}
	
	public static Query createExportQuery() {
		QClazz clazz = QClazz.newQuery();	
		
		clazz.select(clazz.wildcard())
		     .select(clazz.classifier().wildcard())
		     .select(clazz.classifier().packageableType().wildcard())
		     .select(clazz.classifier().packageableType()._package().wildcard())
		     // contains dependencies between classes
		     //.select(clazz.generalization().wildcard())
		     //.select(clazz.general().wildcard())
	         .select(clazz.classCategorization().categorization().category().seqId());	        
        return clazz;		
	}
}
