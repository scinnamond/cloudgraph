package org.cloudgraph.web.model.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;

import commonj.sdo.DataGraph;

public class DefaultDynamicGraphTreeBean extends DynamicGraphTreeBean {

    private static Log log = LogFactory.getLog(DefaultDynamicGraphTreeBean.class);
	
    
    public DefaultDynamicGraphTreeBean() {
    	// this initialization hack makes RichFaces tree find it's tree state and
    	// be happy and not blow up even though the tree is not yet displayed 
    }
    
	public DefaultDynamicGraphTreeBean(DataGraph[] models) {
		try {						
			typeMap = new DefaultTreeNodeTypeMap();
			initTree(models);
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
			default: return GraphTreeNodeType.level_any.name(); 
			}
		}		
	}
}
