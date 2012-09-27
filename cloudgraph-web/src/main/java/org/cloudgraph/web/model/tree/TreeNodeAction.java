package org.cloudgraph.web.model.tree;

import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.cloudgraph.web.model.Action;



public class TreeNodeAction {

	private Action action;
	private boolean selected;
	private boolean enabled;
	private TreeSelectionModel selectionModel;
	private ActionHandler actionHandler;
	
	@SuppressWarnings("unused")
	private TreeNodeAction() {}
	
	public TreeNodeAction(Action action, ActionHandler actionHandler, TreeSelectionModel selectionModel) {
		this.action = action;
		this.actionHandler = actionHandler;
		this.selectionModel = selectionModel;
	}	

	public boolean isSelected() {
		return selected;
	}

	public String select() {
		this.selected = true;
		return null;
	}
	
	public String deselect() {
		this.selected = false;
		return null;
	}

	public String toggle() {
		if (this.selected)
			this.selected = false;
		else
			this.selected = true;
		return null;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String onAction() {
		selectionModel.clearSelection();
		selectionModel.setSelection(this);
		this.selected = true;
		
		
		return actionHandler.handleAction(this.action);
	}
	
    public String getLabel()
    {
    	return ResourceManager.instance().getString(action.toString(), ResourceType.LABEL);
    }

    public String getTooltip()
    {
    	return ResourceManager.instance().getString(action.toString(), ResourceType.TOOLTIP);
    }
    
    public String getIcon()
    {
    	return ResourceManager.instance().getString(action.toString(), 
    			ResourceType.ICON);
    }
	
}
