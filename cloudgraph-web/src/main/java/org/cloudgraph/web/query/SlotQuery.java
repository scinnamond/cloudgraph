package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.meta.query.QSlot;
import org.plasma.query.Query;

public class SlotQuery {
	
	public static Query createExportQuery() {
		
		QSlot query = QSlot.newQuery();
		query.select(query.wildcard())
		     .select(query.owningInstance().wildcard())
             .select(query.slotCategorization().wildcard())
             .select(query.slotCategorization().categorization().wildcard())
             .select(query.slotCategorization().categorization().category().wildcard())
             .select(query.definingFeature().wildcard())
             .select(query.value().wildcard())  
             .select(query.value().instanceValue().wildcard())  
             .select(query.value().instanceValue().instance().wildcard()) // links to another instance
             .select(query.value().literalString().wildcard())
             .select(query.value().literalClob().wildcard())
             .select(query.value().literalShort().wildcard())
             .select(query.value().literalInteger().wildcard())
             .select(query.value().literalLong().wildcard())
             .select(query.value().literalFloat().wildcard())
             .select(query.value().literalDouble().wildcard())
             .select(query.value().literalDate().wildcard())
             .select(query.value().literalBoolean().wildcard());		
		return query;
	}
}
