package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.meta.query.QPrimitiveType;
import org.plasma.query.Query;

public class PrimitiveTypeQuery {
	
	public static Query createQuery() {
		QPrimitiveType root = QPrimitiveType.newQuery();
		root.select(root.wildcard())
	       .select(root.dataType().classifier().wildcard())
	       .select(root.dataType().classifier().wildcard())
	       .select(root.dataType().classifier().wildcard())
		   .select(root.dataType().classifier().packageableType().wildcard())
		   .select(root.dataType().classifier().packageableType()._package().wildcard());
		return root;		
	}
	
	public static Query createExportQuery() {
		QPrimitiveType primitiveType = QPrimitiveType.newQuery();
		primitiveType.select(primitiveType.wildcard())
	       .select(primitiveType.dataType().wildcard())
	       .select(primitiveType.dataType().classifier().wildcard())
		   .select(primitiveType.dataType().classifier().packageableType().wildcard())
		   .select(primitiveType.dataType().classifier().packageableType()._package().wildcard());
		return primitiveType;		
	}
	
	
}
