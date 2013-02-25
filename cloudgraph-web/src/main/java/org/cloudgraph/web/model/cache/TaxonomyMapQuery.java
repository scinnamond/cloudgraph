package org.cloudgraph.web.model.cache;

import org.plasma.query.model.From;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.query.model.Where;

import org.cloudgraph.web.sdo.categorization.TaxonomyMap;


public class TaxonomyMapQuery {
	
    public static Query createQuery() {
    	
        Select select = new Select(new String[] { 
                "*",
            });    	

    	From from = new From(TaxonomyMap.TYPE_NAME_TAXONOMY_MAP,
    			TaxonomyMap.NAMESPACE_URI);        
 		OrderBy orderBy = new OrderBy();
		orderBy.addProperty(Property.forName(TaxonomyMap.PROPERTY.name.name()));        
        Query query = new Query(select, from, orderBy);
        return query;
    }	
	
    public static Query createQuery(long seqId) {
    	
        Select select = new Select(new String[] { 
                "*",
                "left/*",
                "left/category/*",
                "right/*",
                "right/category/*",               
                "categoryLink/left/*",
                "categoryLink/right/*",
                "categoryLink/left/parent/*",
                "categoryLink/right/parent/*",
                "categoryLink/left/parent/parent/*",
                "categoryLink/right/parent/parent/*",
                "categoryLink/left/parent/parent/parent/*",
                "categoryLink/right/parent/parent/parent/*",
                "categoryLink/left/parent/parent/parent/parent/*",
                "categoryLink/right/parent/parent/parent/parent/*",
                "categoryLink/left/parent/parent/parent/parent/parent/*",
                "categoryLink/right/parent/parent/parent/parent/parent/*",
                "categoryLink/left/parent/parent/parent/parent/parent/parent/*",
                "categoryLink/right/parent/parent/parent/parent/parent/parent/*",
            });    	
    	From from = new From(TaxonomyMap.TYPE_NAME_TAXONOMY_MAP,
    			TaxonomyMap.NAMESPACE_URI);        
        Where where = new Where();
        where.addExpression(
        	Property.forName(TaxonomyMap.PROPERTY.seqId.name()).eq(seqId));
        return new Query(select, from, where);
    }	
}
