package org.cloudgraph.web.sdo.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.plasma.sdo.PlasmaDataGraphVisitor;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public abstract class CategorizationCollector implements PlasmaDataGraphVisitor {
    private static Log log = LogFactory.getLog(CategorizationCollector.class);
	private ReferenceDataCache cache;
	private Type type;
	private boolean collectOnlyInitializedTaxonomies = false;
	
	private Map<Taxonomy, List<CategorizationAdapter>> result = 
		new HashMap<Taxonomy, List<CategorizationAdapter>>();
	
	@SuppressWarnings("unused")
	private CategorizationCollector() {}
	
	public CategorizationCollector(Type type, ReferenceDataCache cache) {
		this.type = type;
		this.cache = cache;
	}
	
	public boolean isCollectOnlyInitializedTaxonomies() {
		return collectOnlyInitializedTaxonomies;
	}

	public void setCollectOnlyInitializedTaxonomies(
			boolean collectOnlyInitializedTaxonomies) {
		this.collectOnlyInitializedTaxonomies = collectOnlyInitializedTaxonomies;
	}

	/**
	 * initializes the given taxonomy such that at least
	 * it will be represented in the results mapped to an empty
	 * list even if no categorizations were found associated with it. 
	 */
	public void initializeTaxonomy(Taxonomy taxonomy) {
		List<CategorizationAdapter> pcats = new ArrayList<CategorizationAdapter>();
		result.put(taxonomy, pcats);
	}
	
	protected abstract CategorizationAdapter newAdapter(DataObject dataObject, 
			Category cat);
	
	protected abstract Category getCategory(DataObject target);
	
	public void visit(DataObject target, DataObject source,
			String sourceKey, int level) {
		if (!target.getType().getName().equals(this.type.getName()) ||
			!target.getType().getURI().equals(this.type.getURI()))
		    return;

		Category lookupCat = getCategory(target);
		if (lookupCat == null) {
			log.warn("ignoring incomplete project categorization");
			return;
		}
		
		// here we lookup the full cat with name, descr etc...   		
    	Category cat = cache.getCategory(lookupCat.getSeqId()); 
    	if (cat == null) {
    		log.warn("ignoring non-cached category, " + lookupCat.getSeqId());
    		return;
    	}
    	Category tempCat = cat;
    	Taxonomy tax = null;
    	while (tax == null) {
    		if (tempCat.getTaxonomyCount() > 0) {
    			tax = tempCat.getTaxonomy(0);
    			if (tempCat.getTaxonomyCount() > 1)
    				log.warn("WTF? more than one taxonomy");
    		}	
    		else
    			tempCat = tempCat.getParent();
    	}
    	if (tax == null)
    		throw new IllegalStateException("expected non-null taxonomy");
    	
    	List<CategorizationAdapter> taxCatz = result.get(tax);
    	if (taxCatz == null && !this.collectOnlyInitializedTaxonomies) {
    		taxCatz = new ArrayList<CategorizationAdapter>();
    		result.put(tax, taxCatz);
    	}
    	if (taxCatz != null) {
	    	taxCatz.add(newAdapter(target, cat));	
    	}
	}

	public Map<Taxonomy, List<CategorizationAdapter>> getResult() {
		return result;
	}

}
