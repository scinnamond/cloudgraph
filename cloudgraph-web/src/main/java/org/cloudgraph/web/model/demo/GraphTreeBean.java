package org.cloudgraph.web.model.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.graph.CommonNode;
import org.cloudgraph.web.model.graph.DynamicGraphTreeBean;
import org.cloudgraph.web.model.graph.GraphTreeNodeType;
import org.cloudgraph.web.model.graph.NodeLabelFormat;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;

import commonj.sdo.DataGraph;

public class GraphTreeBean extends DynamicGraphTreeBean {

    private static Log log = LogFactory.getLog(GraphTreeBean.class);

    public GraphTreeBean() {
    	// this initialization hack makes RichFaces tree find it's tree state and
    	// be happy and not blow up even though the tree is not yet displayed 
    }
    
	public GraphTreeBean(DataGraph[] dataGraphs) {
		try {						
			typeMap = new GraphModelTreeNodeTypeMap();
			this.setLabelFormat(new NodeLabelFormat() {
				public String getLabel(CommonNode node) {
		        	return node.getLabel();
				}				
			});
			initTree(dataGraphs);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
		
	class GraphModelTreeNodeTypeMap implements TreeNodeTypeMap {

		/**
		 * Returns a business logic specific node type given the taxonomy level. We skip
		 * a level where a link node within the generic taxonomy structure is expected.
		 */
		public String getTreeNodeType(int level) {
			switch (level)
			{
			default: return GraphTreeNodeType.level_any.name(); 
			}
		}		
	}
}
