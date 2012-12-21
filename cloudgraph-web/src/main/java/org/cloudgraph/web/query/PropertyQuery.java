package org.cloudgraph.web.query;

import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QProperty;

public class PropertyQuery {

	public static Query createTreeQuery(Long seqId) {
		QProperty pty = QProperty.newQuery();
		pty.select(pty.seqId())
		   .select(pty.name())
	       .select(pty.dataType().wildcard())
	       .select(pty.association().wildcard())
	       .select(pty.association().classifier().wildcard());
		pty.where(pty.seqId().eq(seqId));		
        return pty;		
	}
	
	public static Query createEditQuery(Long seqId) {
		QProperty pty = QProperty.newQuery();
		pty.select(pty.wildcard())
	       .select(pty.dataType().wildcard())
	       .select(pty.sourceClass().wildcard())
		   .select(pty.sourceClass().classifier().wildcard())
		   .select(pty.sourceClass().classifier().packageableType().wildcard())
		   .select(pty.sourceClass().classifier().packageableType()._package().wildcard())
	       .select(pty.association().wildcard())
	       .select(pty.association().classifier().wildcard())
	       .select(pty.propertyCategorization().categorization().category().seqId());	        
		pty.where(pty.seqId().eq(seqId));		
        return pty;		
	}
	
	public static Query createDeleteQuery(Long seqId) {
		QProperty pty = QProperty.newQuery();
		pty.select(pty.wildcard())
	       .select(pty.slot().seqId())
	       .select(pty.slot().value().seqId())
           .select(pty.slot().value().literalString().seqId())
           .select(pty.slot().value().literalClob().seqId())
           .select(pty.slot().value().literalShort().seqId())
           .select(pty.slot().value().literalInteger().seqId())
           .select(pty.slot().value().literalLong().seqId())
           .select(pty.slot().value().literalFloat().seqId())
           .select(pty.slot().value().literalDouble().seqId())
           .select(pty.slot().value().literalDate().seqId())
           .select(pty.slot().value().literalBoolean().seqId())
	       .select(pty.slot().value().instanceValue().seqId())
	       .select(pty.association().wildcard())
	       .select(pty.propertyCategorization().seqId()) 	        
	       .select(pty.propertyCategorization().categorization().seqId());	        
		pty.where(pty.seqId().eq(seqId));		
        return pty;		
	}
	
	public static Query createQueryBySourceClassId(Long classId) {
		QProperty pty = QProperty.newQuery();
		pty.select(pty.wildcard())
           .select(pty.propertyCategorization().seqId())
           .select(pty.propertyCategorization().categorization().seqId())
           .select(pty.propertyCategorization().categorization().category().seqId())
           .select(pty.propertyCategorization().categorization().category().parent().seqId())
           .select(pty.propertyCategorization().categorization().category().parent().parent().seqId())
	       .select(pty.dataType().wildcard())
	       .select(pty.dataType().dataType().wildcard())
	       .select(pty.dataType().dataType().primitiveType().wildcard())
	       .select(pty.dataType().dataType().enumeration().wildcard())
	       .select(pty.dataType().dataType().enumeration().ownedLiteral().wildcard())
	       .select(pty.dataType().clazz().seqId())
	       //.select(pty.dataType().clazz().classifier().wildcard())
	       .select(pty.sourceClass().wildcard())
		   .select(pty.sourceClass().classifier().wildcard());
		pty.where(pty.sourceClass().seqId().eq(classId));		
        return pty;		
	}
	
	public static Query createQueryBySourceClassifierName(String className) {
		QProperty pty = QProperty.newQuery();
		pty.select(pty.wildcard())
           .select(pty.propertyCategorization().seqId())
           .select(pty.propertyCategorization().categorization().seqId())
           .select(pty.propertyCategorization().categorization().category().seqId())
           .select(pty.propertyCategorization().categorization().category().parent().seqId())
           .select(pty.propertyCategorization().categorization().category().parent().parent().seqId())
	       .select(pty.dataType().wildcard())
	       .select(pty.dataType().dataType().wildcard())
	       .select(pty.dataType().dataType().primitiveType().wildcard())
	       .select(pty.dataType().dataType().enumeration().wildcard())
	       .select(pty.dataType().dataType().enumeration().ownedLiteral().wildcard())
	       .select(pty.dataType().clazz().seqId())
	       //.select(pty.dataType().clazz().classifier().wildcard())
	       .select(pty.sourceClass().wildcard())
		   .select(pty.sourceClass().classifier().wildcard());
		pty.where(pty.sourceClass().classifier().name().eq(className));		
        return pty;		
	}	
	public static Query createExportQuery() {
		QProperty pty = QProperty.newQuery();
		
		pty.select(pty.wildcard())
	       .select(pty.dataType().wildcard())
	       .select(pty.sourceClass().wildcard())
	       .select(pty.association().externalId())
	       .select(pty.propertyCategorization().wildcard())	        
	       .select(pty.propertyCategorization().categorization().wildcard())	        
	       .select(pty.propertyCategorization().categorization().category().wildcard());	        
        return pty;		
	}
}
