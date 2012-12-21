package org.cloudgraph.web.model.navigation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.Action;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.ActionHandler;
import org.cloudgraph.web.model.tree.TreeNodeAction;
import org.cloudgraph.web.model.tree.TreeSelectionModel;


public class NavigationBean extends ModelBean {
    
    private static Log log = LogFactory.getLog(NavigationBean.class);
    
    // RichFaces Toolbar cannot be loaded using a overview iterator such as overviewTable, i.e. dynamically without
    // using the 'binding' attribute which means we have to create all sub-components
    // dynamically as well. Hence declare top-nav items statically. :(
    private TreeNodeAction dashboardAction;
    private TreeNodeAction workspaceAction;
    
    private TreeNodeAction overviewAction;
    private TreeNodeAction demoAction;
    private TreeNodeAction downloadAction;
    private TreeNodeAction documentationAction;
    
    private TreeNodeAction administrationAction;
        
    private TreeNodeAction selectedTopAction;
    
    private TreeSelectionModel topSelectionModel;
    	
	public NavigationBean()
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
				dashboardAction.setSelected(false);
				overviewAction.setSelected(false);
				workspaceAction.setSelected(false);
				downloadAction.setSelected(false);
				documentationAction.setSelected(false); 
				demoAction.setSelected(false);
				administrationAction.setSelected(false);
			}

			@Override
			public void setSelection(TreeNodeAction selection) {
				clearSelection();
				selectedTopAction = selection;
				selectedTopAction.setSelected(true);				
			}			
		};
		
	    dashboardAction = new TreeNodeAction(Action.topnav_dashboard, 
	    		topActionHandler, topSelectionModel);
	    //dashboardAction.setSelected(true);
	    //selectedTopAction = dashboardAction;
	    overviewAction = new TreeNodeAction(Action.topnav_overview, 
	    		topActionHandler, topSelectionModel);
	    overviewAction.setSelected(true);
	    selectedTopAction = overviewAction;
	    workspaceAction = new TreeNodeAction(Action.topnav_workspace, 
	    		topActionHandler, topSelectionModel);
	    demoAction = new TreeNodeAction(Action.topnav_demo, 
	    		topActionHandler, topSelectionModel);
	    downloadAction = new TreeNodeAction(Action.topnav_download, 
	    		topActionHandler, topSelectionModel);
	    documentationAction = new TreeNodeAction(Action.topnav_documentation, 
	    		topActionHandler, topSelectionModel);
	    administrationAction = new TreeNodeAction(Action.topnav_administration, 
	    		topActionHandler, topSelectionModel);

		ActionHandler leftActionHandler = new ActionHandler() {

			@Override
			public String handleAction(Action action) {
				return selectedTopAction.getAction().toString();
			}
			
		};
	    
		// default selection
		setDocumentationSelected(new Boolean(true));
	}

	public void setDashboardSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.dashboardAction);
		else
			this.dashboardAction.setSelected(false);			
	}

	public void setOverviewSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.overviewAction);
		else
			this.overviewAction.setSelected(false);
	}
	
	public void setWorkspaceSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.workspaceAction);
		else
			this.workspaceAction.setSelected(false);
	}

	public void setDemoSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.demoAction);
		else
			this.demoAction.setSelected(false);
	}
	
	public void setDownloadSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.downloadAction);
		else
			this.downloadAction.setSelected(false);
	}
	
	public void setDocumentationSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.documentationAction);
		else
			this.documentationAction.setSelected(false);
	}

	public void setAdminitstrationSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.administrationAction);
		else
			this.administrationAction.setSelected(false);
	}

	public TreeNodeAction getDashboardAction() {
		return dashboardAction;
	}

	public void setDashboardAction(TreeNodeAction dashboardAction) {
		this.dashboardAction = dashboardAction;
	}

	public TreeNodeAction getOverviewAction() {
		return overviewAction;
	}

	public void setOverviewAction(TreeNodeAction overviewAction) {
		this.overviewAction = overviewAction;
	}
	
	public TreeNodeAction getDownloadAction() {
		return downloadAction;
	}

	public void setDownloadAction(TreeNodeAction downloadAction) {
		this.downloadAction = downloadAction;
	}
	
	public TreeNodeAction getDocumentationAction() {
		return documentationAction;
	}

	public void setDocumentationAction(TreeNodeAction documentationAction) {
		this.documentationAction = documentationAction;
	}

	public TreeNodeAction getDemoAction() {
		return demoAction;
	}

	public void setDemoAction(TreeNodeAction demoAction) {
		this.demoAction = demoAction;
	}
	public TreeNodeAction getWorkspaceAction() {
		return workspaceAction;
	}

	public void setWorkspaceAction(TreeNodeAction workspaceAction) {
		this.workspaceAction = workspaceAction;
	}
	
	public TreeNodeAction getAdministrationAction() {
		return administrationAction;
	}

	public void setAdministrationAction(TreeNodeAction administrationAction) {
		this.administrationAction = administrationAction;
	}

	public TreeNodeAction getSelectedTopAction() {
		return selectedTopAction;
	}

	public void setSelectedTopAction(TreeNodeAction selectedTopAction) {
		this.selectedTopAction = selectedTopAction;
	}	
    


    
}
