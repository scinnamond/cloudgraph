package org.cloudgraph.web.model.navigation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.Action;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.ActionHandler;
import org.cloudgraph.web.model.tree.TreeNodeAction;
import org.cloudgraph.web.model.tree.TreeSelectionModel;


public class ControlNavigationBean extends ModelBean {
    
    private static Log log = LogFactory.getLog(ControlNavigationBean.class);
    
    // RichFaces Toolbar cannot be loaded using a data iterator such as dataTable, i.e. dynamically without
    // using the 'binding' attribute which means we have to create all sub-components
    // dynamically as well. Hence declare top-nav items statically. :(
    private TreeNodeAction datafiltersAction;
        
    private TreeNodeAction selectedTopAction;
    
    private TreeSelectionModel topSelectionModel;
    	
	public ControlNavigationBean()
	{
		ActionHandler topActionHandler = new ActionHandler() {

			@Override
			public String handleAction(Action action) {
				// TODO Auto-generated method stub
				return action.toString();
			}

			
		};
		
		topSelectionModel = new TreeSelectionModel() {
			@Override
			public void clearSelection() {
				datafiltersAction.setSelected(false);
			}

			@Override
			public void setSelection(TreeNodeAction selection) {
				clearSelection();
				selectedTopAction = selection;
				selectedTopAction.setSelected(true);				
			}			
		};
		
		datafiltersAction = new TreeNodeAction(Action.topnav_datafilters, 
	    		topActionHandler, topSelectionModel);
		datafiltersAction.setSelected(true);
	    selectedTopAction = datafiltersAction;

		
	}

	public void setDatafiltersSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.datafiltersAction);
		else
			this.datafiltersAction.setSelected(false);			
	}


	public TreeNodeAction getDatafiltersAction() {
		return datafiltersAction;
	}

	public void setDatafiltersAction(TreeNodeAction datafiltersAction) {
		this.datafiltersAction = datafiltersAction;
	}
	
	public TreeNodeAction getSelectedTopAction() {
		return selectedTopAction;
	}

	public void setSelectedTopAction(TreeNodeAction selectedTopAction) {
		this.selectedTopAction = selectedTopAction;
	}	
    


    
}
