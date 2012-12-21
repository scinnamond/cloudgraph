package org.cloudgraph.web.model.graph;

import org.cloudgraph.web.model.tree.TreeNodeTypeMap;

public class DefaultTreeNodeTypeMap implements TreeNodeTypeMap {

	public String getTreeNodeType(int level) {
		switch (level)
		{
		case 0: return GraphTreeNodeType.level_zero.name(); 
		case 1: return GraphTreeNodeType.level_one.name(); 
		case 2: return GraphTreeNodeType.level_two.name(); 
		case 3: return GraphTreeNodeType.level_three.name(); 
		case 4: return GraphTreeNodeType.level_four.name(); 
		case 5: return GraphTreeNodeType.level_five.name(); 
		case 6: return GraphTreeNodeType.level_six.name(); 
		case 7: return GraphTreeNodeType.level_seven.name(); 
		case 8: return GraphTreeNodeType.level_eight.name(); 
		case 9: return GraphTreeNodeType.level_nine.name(); 
		case 10: return GraphTreeNodeType.level_ten.name(); 
		default: return GraphTreeNodeType.level_any.name(); 
		}
	}

}
