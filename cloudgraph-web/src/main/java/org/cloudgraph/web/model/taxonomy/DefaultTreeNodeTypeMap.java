package org.cloudgraph.web.model.taxonomy;

import org.cloudgraph.web.model.tree.TreeNodeTypeMap;

public class DefaultTreeNodeTypeMap implements TreeNodeTypeMap {

	public String getTreeNodeType(int level) {
		switch (level)
		{
		case 0: return TaxonomyTreeNodeType.level_zero.name(); 
		case 1: return TaxonomyTreeNodeType.level_one.name(); 
		case 2: return TaxonomyTreeNodeType.level_two.name(); 
		case 3: return TaxonomyTreeNodeType.level_three.name(); 
		case 4: return TaxonomyTreeNodeType.level_four.name(); 
		case 5: return TaxonomyTreeNodeType.level_five.name(); 
		case 6: return TaxonomyTreeNodeType.level_six.name(); 
		case 7: return TaxonomyTreeNodeType.level_seven.name(); 
		case 8: return TaxonomyTreeNodeType.level_eight.name(); 
		case 9: return TaxonomyTreeNodeType.level_nine.name(); 
		case 10: return TaxonomyTreeNodeType.level_ten.name(); 
		default: return TaxonomyTreeNodeType.level_any.name(); 
		}
	}

}
