package org.cloudgraph.web.model.cache;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.plasma.query.model.From;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Path;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;


public class TaxonomyQuery {
	
    public static Query createQuery() {
    	
        Select select = new Select(new String[] { 
                "*",
                "category/*",
            });    	

    	From from = new From(Taxonomy.TYPE_NAME_TAXONOMY,
    			Taxonomy.NAMESPACE_URI);        
 		OrderBy orderBy = new OrderBy();
		orderBy.addProperty(Property.forName(Category.PROPERTY.name.name(), 
				new Path(Taxonomy.PROPERTY.category.name())));
        
        Query query = new Query(select, from, orderBy);
        return query;
    }	
	
	
    public static Query createQuery(String name) {
    	
        Select select = new Select(new String[] { 
                "*",
                "category/*",
                "category/child/*",
                "category/child/child/*",
                "category/child/child/child/*",
                "category/child/child/child/child/*",
                "category/child/child/child/child/child/*",
                "category/child/child/child/child/child/child/*",
                "category/child/child/child/child/child/child/child/*",
                "category/child/child/child/child/child/child/child/child/*",
            });    	

    	From from = new From(Taxonomy.TYPE_NAME_TAXONOMY,
    			Taxonomy.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(Property.forName(Category.PROPERTY.name.name(),
        		new Path(Taxonomy.PROPERTY.category.name())).eq(
        				name));
        
        Query query = new Query(select, from, where);
        return query;
    }
    
    /**
     * Query which uses wildcard paths '*.*' e.g. all entities linked
     * to the root and all their properties. We use this to "discover"
     * any entities directly linked to any category or subcatrgory being
     * deleted, so we can inform the user, then delete the whole graph.  
     * @param seqId
     * @return Query the query
     */
    public static Query createDeleteConfirmQuery(long seqId) {
    	
        Select select = new Select(new String[] { 
                "*",   // all props for root
                "*/*", // all props for entities linked (1 hop away) to root 
                "child/*",   // all props for children 
                "child/*/*", // all props for entities linked (1 hop away) to children  
                "child/child/*",   // all props for grand children
                "child/child/*/*", // all props for entities linked (1 hop away) to grand children  
                "child/child/child/*",   // all props for great grand children
                "child/child/child/*/*", // all props for entities linked (1 hop away) to great grand children etc/// 
                "child/child/child/child/*", // etc///
                "child/child/child/child/*/*", // etc///
                "child/child/child/child/child/*", // etc///
                "child/child/child/child/child/*/*", // etc///
                "child/child/child/child/child/child/*", // etc///
                "child/child/child/child/child/child/*/*", // etc///
                "child/child/child/child/child/child/child/*", // etc///
                "child/child/child/child/child/child/child/*/*", // etc///
                "child/child/child/child/child/child/child/child/*", // etc///
                "child/child/child/child/child/child/child/child/*/*", // etc///
            });    	

    	From from = new From(Category.TYPE_NAME_CATEGORY,
    			Category.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(
        	Property.forName(Category.PROPERTY.seqId.name()).eq(seqId));
        
        Query query = new Query(select, from, where);
        return query;
    }
    
}
