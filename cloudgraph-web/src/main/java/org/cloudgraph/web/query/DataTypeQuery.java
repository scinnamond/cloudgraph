package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.meta.query.QDataType;
import org.plasma.query.Query;

public class DataTypeQuery {
	
	public static Query createQuery() {
		QDataType dataType = QDataType.newQuery();
		dataType.select(dataType.wildcard())
	       .select(dataType.classifier().wildcard())
		   .select(dataType.classifier().packageableType().wildcard())
		   .select(dataType.classifier().packageableType()._package().wildcard());
		return dataType;		
	}

	public static Query createEditQuery(Long seqId) {
		QDataType dataType = QDataType.newQuery();
		dataType.select(dataType.wildcard())
	       .select(dataType.classifier().wildcard())
		   .select(dataType.classifier().packageableType().wildcard())
		   .select(dataType.classifier().packageableType()._package().wildcard());
		dataType.where(dataType.seqId().eq(seqId));
		
		return dataType;		
	}
	
	public static Query createExportQuery() {
		QDataType dataType = QDataType.newQuery();
		dataType.select(dataType.wildcard())
	       .select(dataType.classifier().wildcard())
		   .select(dataType.classifier().packageableType().wildcard())
		   .select(dataType.classifier().packageableType()._package().wildcard());
		return dataType;		
	}
	
	
}
