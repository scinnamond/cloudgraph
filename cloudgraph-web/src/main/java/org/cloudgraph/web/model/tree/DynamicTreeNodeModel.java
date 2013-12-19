package org.cloudgraph.web.model.tree;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class DynamicTreeNodeModel extends TreeNodeModel {
	
//    private static Log log = LogFactory.getLog(DynamicTreeNodeModel.class);
    
    private int level = 0;
    private boolean leaf = false;

	public DynamicTreeNodeModel(Object id) {super(id);}
    
    public int getLevel() {return level;}
    public void setLevel(int level) {this.level = level;}

    public boolean isLeaf() {return leaf;}
    public void setLeaf(boolean leaf) {this.leaf = leaf;}

}
