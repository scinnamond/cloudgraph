package org.cloudgraph.web.model.administration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.taxonomy.CategoryLabelFormat;
import org.cloudgraph.web.model.taxonomy.DynamicTaxonomyTreeBean;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;

public class TaxonomyTreeBean extends DynamicTaxonomyTreeBean {

    private static Log log = LogFactory.getLog(TaxonomyTreeBean.class);

    public TaxonomyTreeBean() {
    	// this initialization hack makes RichFaces tree find it's tree state and
    	// be happy and not blow up even though the tree is not yet displayed 
    }
    
	public TaxonomyTreeBean(Taxonomy model) {
		try {						
			typeMap = new TaxonomyModelTreeNodeTypeMap();
			this.setLabelFormat(new CategoryLabelFormat() {
				public String getLabel(Category category) {
		        	String label = "(" + category.getId() + ") "
	        	        + category.getName();
		        	return label;
				}				
			});
			initTree(model);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
		
	class TaxonomyModelTreeNodeTypeMap implements TreeNodeTypeMap {

		/**
		 * Returns a business logic specific node type given the taxonomy level. We skip
		 * a level where a link node within the generic taxonomy structure is expected.
		 */
		public String getTreeNodeType(int level) {
			switch (level)
			{
			default: return TaxonomyTreeNodeType.category.name(); 
			}
		}		
	}
}
