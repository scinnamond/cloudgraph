package org.cloudgraph.web.model.taxonomy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.cloudgraph.web.sdo.categorization.Taxonomy;

public class DefaultDynamicTaxonomyTreeBean extends DynamicTaxonomyTreeBean {

    private static Log log = LogFactory.getLog(DefaultDynamicTaxonomyTreeBean.class);
	
    
    public DefaultDynamicTaxonomyTreeBean() {
    	// this initialization hack makes RichFaces tree find it's tree state and
    	// be happy and not blow up even though the tree is not yet displayed 
    }
    
	public DefaultDynamicTaxonomyTreeBean(Taxonomy model) {
		try {						
			typeMap = new DefaultTreeNodeTypeMap();
			initTree(model);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
		
	class DefaultTreeNodeTypeMap implements TreeNodeTypeMap {

		/**
		 * Returns a business logic specific node type given the taxonomy level. We skip
		 * a level where a link node within the generic taxonomy structure is expected.
		 */
		public String getTreeNodeType(int level) {
			switch (level)
			{
			default: return TaxonomyTreeNodeType.level_any.name(); 
			}
		}		
	}
}
