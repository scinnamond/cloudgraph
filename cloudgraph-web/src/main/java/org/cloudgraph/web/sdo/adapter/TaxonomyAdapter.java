package org.cloudgraph.web.sdo.adapter;

import java.util.List;

import org.cloudgraph.web.sdo.categorization.Taxonomy;


public class TaxonomyAdapter {
	private Taxonomy taxonomy;
	private List<CategorizationAdapter> categorizations;
	
    @SuppressWarnings("unused")
	private TaxonomyAdapter() {}

	public TaxonomyAdapter(Taxonomy taxonomy,
			List<CategorizationAdapter> categorizations) {
		super();
		this.taxonomy = taxonomy;
		this.categorizations = categorizations;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public List<CategorizationAdapter> getCategorizations() {
		return categorizations;
	}
    
	public String getName() {
		return this.taxonomy.getCategory().getName();
	}

	public String getVersion() {
		return this.taxonomy.getVersion();
	}
}
