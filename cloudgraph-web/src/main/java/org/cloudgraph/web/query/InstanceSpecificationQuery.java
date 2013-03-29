package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.meta.query.QInstanceSpecification;
import org.plasma.query.Query;

public class InstanceSpecificationQuery {
	
	public static Query createEditQueryById(Long id) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
             .select(query.clazz().classifier().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().wildcard())
             .select(query.slot().wildcard())
             .select(query.slot().definingFeature().wildcard())
             .select(query.slot().value().wildcard())  
             .select(query.slot().value().instanceValue().wildcard())  
             .select(query.slot().value().instanceValue().instance().seqId()) // links to another instance
             .select(query.slot().value().literalString().wildcard())
             .select(query.slot().value().literalClob().wildcard())
             .select(query.slot().value().literalShort().wildcard())
             .select(query.slot().value().literalInteger().wildcard())
             .select(query.slot().value().literalLong().wildcard())
             .select(query.slot().value().literalFloat().wildcard())
             .select(query.slot().value().literalDouble().wildcard())
             .select(query.slot().value().literalDate().wildcard())
             .select(query.slot().value().literalBoolean().wildcard());		
		query.where(query.seqId().eq(id));
		return query;
	}
	
	public static Query createDeleteQueryById(Long id) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.seqId())
             .select(query.instanceCategorization().seqId())
             .select(query.instanceCategorization().categorization().seqId())
             // delete slots that point to us
             .select(query.instanceValue().seqId())
             .select(query.instanceValue().valueSpecification().seqId())
             .select(query.instanceValue().valueSpecification().slot().seqId())
             // delete our slots
             .select(query.slot().seqId())
             .select(query.slot().value().seqId())  
             .select(query.slot().value().instanceValue().seqId())  
             .select(query.slot().value().literalString().seqId())
             .select(query.slot().value().literalClob().seqId())
             .select(query.slot().value().literalShort().seqId())
             .select(query.slot().value().literalInteger().seqId())
             .select(query.slot().value().literalLong().seqId())
             .select(query.slot().value().literalFloat().seqId())
             .select(query.slot().value().literalDouble().seqId())
             .select(query.slot().value().literalDate().seqId())
             .select(query.slot().value().literalBoolean().seqId());		
		query.where(query.seqId().eq(id));
		return query;
	}
	
	public static Query createQueueQueryByClassifierName(String name) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
             .select(query.clazz().classifier().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().name())
             .select(query.instanceCategorization().categorization().category().parent().name())
             .select(query.slot().wildcard())
             .select(query.slot().definingFeature().wildcard())
             .select(query.slot().definingFeature().propertyCategorization().categorization().category().seqId())
             .select(query.slot().value().wildcard())  
             .select(query.slot().value().instanceValue().wildcard())  
             .select(query.slot().value().instanceValue().instance().seqId()) // links to another instance
             .select(query.slot().value().literalString().wildcard())
             .select(query.slot().value().literalClob().wildcard())
             .select(query.slot().value().literalShort().wildcard())
             .select(query.slot().value().literalInteger().wildcard())
             .select(query.slot().value().literalLong().wildcard())
             .select(query.slot().value().literalFloat().wildcard())
             .select(query.slot().value().literalDouble().wildcard())
             .select(query.slot().value().literalDate().wildcard())
             .select(query.slot().value().literalBoolean().wildcard());		
		query.where(query.clazz().classifier().name().eq(name));
		return query;
	}
	
	public static Query createQueueQueryByInstanceId(String classifierName, Long id) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
             .select(query.clazz().classifier().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().name())
             .select(query.instanceCategorization().categorization().category().parent().name())
             .select(query.slot().wildcard())
             .select(query.slot().definingFeature().wildcard())
             .select(query.slot().definingFeature().propertyCategorization().categorization().category().seqId())
             .select(query.slot().value().wildcard())  
             .select(query.slot().value().instanceValue().wildcard())  
             .select(query.slot().value().instanceValue().instance().seqId()) // links to another instance
             .select(query.slot().value().literalString().wildcard())
             .select(query.slot().value().literalClob().wildcard())
             .select(query.slot().value().literalShort().wildcard())
             .select(query.slot().value().literalInteger().wildcard())
             .select(query.slot().value().literalLong().wildcard())
             .select(query.slot().value().literalFloat().wildcard())
             .select(query.slot().value().literalDouble().wildcard())
             .select(query.slot().value().literalDate().wildcard())
             .select(query.slot().value().literalBoolean().wildcard());		
		query.where(query.seqId().eq(id)
			 .and(query.clazz().classifier().name().eq(classifierName)));
		return query;
	}
	
	public static Query createQueueQueryByClassId(Long classId) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
             .select(query.clazz().classifier().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().name())
             .select(query.instanceCategorization().categorization().category().parent().name())
             .select(query.slot().wildcard())
             .select(query.slot().definingFeature().seqId())
             .select(query.slot().definingFeature().name())
             .select(query.slot().value().wildcard())  
             .select(query.slot().value().literalString().value())
             .select(query.slot().value().literalClob().value())
             .select(query.slot().value().literalShort().value())
             .select(query.slot().value().literalInteger().value())
             .select(query.slot().value().literalLong().value())
             .select(query.slot().value().literalFloat().value())
             .select(query.slot().value().literalDouble().value())
             .select(query.slot().value().literalDate().value())
             .select(query.slot().value().literalBoolean().value())
             
             // all this to resolve the instance value to some readable string
             .select(query.slot().value().instanceValue().seqId()) 
             .select(query.slot().value().instanceValue().instance().seqId()) // links to another instance
             .select(query.slot().value().instanceValue().instance().clazz().seqId())
             .select(query.slot().value().instanceValue().instance().clazz().classifier().name())
             .select(query.slot().value().instanceValue().instance().slot().wildcard())
             .select(query.slot().value().instanceValue().instance().slot().definingFeature().seqId())
             .select(query.slot().value().instanceValue().instance().slot().definingFeature().name())
             .select(query.slot().value().instanceValue().instance().slot().value().wildcard())  
             .select(query.slot().value().instanceValue().instance().slot().value().literalString().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalClob().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalShort().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalInteger().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalLong().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalFloat().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalDouble().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalDate().value())
             .select(query.slot().value().instanceValue().instance().slot().value().literalBoolean().value())      
             ;		
		query.where(query.clazz().seqId().eq(classId));
		return query;
	}

	public static Query createExportQuery() {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
		     .select(query.packageableType().wildcard())
		     .select(query.packageableType()._package().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().wildcard());
 		return query;
	}
}
