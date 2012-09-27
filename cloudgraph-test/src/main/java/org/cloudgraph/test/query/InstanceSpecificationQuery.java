package org.cloudgraph.test.query;

import org.plasma.query.Expression;
import org.plasma.query.Query;

import org.cloudgraph.web.sdo.meta.query.QInstanceSpecification;
import org.cloudgraph.web.sdo.meta.query.QProperty;
import org.cloudgraph.web.sdo.meta.query.QSlot;

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
	
	public static Query createSliceQueryByPropertyName(
			String classifierName, String propertyName) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		QProperty property = QProperty.newQuery();
		QSlot slot = QSlot.newQuery();
		Expression slotPredicate = slot.definingFeature().name().eq(propertyName);
		
		query.select(query.wildcard())
             .select(query.clazz().wildcard())
             .select(query.clazz().classifier().wildcard())
             .select(query.instanceCategorization().wildcard())
             .select(query.instanceCategorization().categorization().wildcard())
             .select(query.instanceCategorization().categorization().category().name())
             .select(query.instanceCategorization().categorization().category().parent().name())
             .select(query.slot(slotPredicate).wildcard())
             .select(query.slot(slotPredicate).definingFeature(property.name().eq(propertyName)).wildcard())
             .select(query.slot(slotPredicate).value().wildcard())  
             .select(query.slot(slotPredicate).value().instanceValue().wildcard())  
             .select(query.slot(slotPredicate).value().instanceValue().instance().seqId()) // links to another instance
             .select(query.slot(slotPredicate).value().literalString().wildcard())
             .select(query.slot(slotPredicate).value().literalClob().wildcard())
             .select(query.slot(slotPredicate).value().literalShort().wildcard())
             .select(query.slot(slotPredicate).value().literalInteger().wildcard())
             .select(query.slot(slotPredicate).value().literalLong().wildcard())
             .select(query.slot(slotPredicate).value().literalFloat().wildcard())
             .select(query.slot(slotPredicate).value().literalDouble().wildcard())
             .select(query.slot(slotPredicate).value().literalDate().wildcard())
             .select(query.slot(slotPredicate).value().literalBoolean().wildcard());		
		query.where(query.clazz().classifier().name().eq(classifierName));
		return query;
	}
	
	public static Query createQueryByClassifierName(String name) {
		
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
             .select(query.slot().value().instanceValue().instance().slot().wildcard())
             .select(query.slot().value().instanceValue().instance().slot().definingFeature().seqId())
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
