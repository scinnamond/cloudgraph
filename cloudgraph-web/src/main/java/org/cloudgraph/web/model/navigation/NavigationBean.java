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
    
    // RichFaces Toolbar cannot be loaded using a data iterator such as dataTable, i.e. dynamically without
    // using the 'binding' attribute which means we have to create all sub-components
    // dynamically as well. Hence declare top-nav items statically. :(
    private TreeNodeAction dashboardAction;
    private TreeNodeAction dataAction;
    private TreeNodeAction workspaceAction;
    private TreeNodeAction campaignAction;
    private TreeNodeAction configurationAction;
   
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
				dataAction.setSelected(false);
				workspaceAction.setSelected(false);
				configurationAction.setSelected(false);
				campaignAction.setSelected(false);
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
	    dataAction = new TreeNodeAction(Action.topnav_data, 
	    		topActionHandler, topSelectionModel);
	    dataAction.setSelected(true);
	    selectedTopAction = dataAction;
	    workspaceAction = new TreeNodeAction(Action.topnav_workspace, 
	    		topActionHandler, topSelectionModel);
	    campaignAction = new TreeNodeAction(Action.topnav_campaign, 
	    		topActionHandler, topSelectionModel);
	    configurationAction = new TreeNodeAction(Action.topnav_configuration, 
	    		topActionHandler, topSelectionModel);
	    administrationAction = new TreeNodeAction(Action.topnav_administration, 
	    		topActionHandler, topSelectionModel);

		ActionHandler leftActionHandler = new ActionHandler() {

			@Override
			public String handleAction(Action action) {
				return selectedTopAction.getAction().toString();
			}
			
		};
	    
		
	}

	public void setDashboardSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.dashboardAction);
		else
			this.dashboardAction.setSelected(false);			
	}

	public void setDataSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.dataAction);
		else
			this.dataAction.setSelected(false);
	}
	
	public void setWorkspaceSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.workspaceAction);
		else
			this.workspaceAction.setSelected(false);
	}

	public void setCampaignSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.campaignAction);
		else
			this.campaignAction.setSelected(false);
	}
	
	public void setConfigurationSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.configurationAction);
		else
			this.configurationAction.setSelected(false);
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

	public TreeNodeAction getDataAction() {
		return dataAction;
	}

	public void setDataAction(TreeNodeAction dataAction) {
		this.dataAction = dataAction;
	}
	
	public TreeNodeAction getConfigurationAction() {
		return configurationAction;
	}

	public void setConfigurationAction(TreeNodeAction configurationAction) {
		this.configurationAction = configurationAction;
	}

	public TreeNodeAction getCampaignAction() {
		return campaignAction;
	}

	public void setCampaignAction(TreeNodeAction campaignAction) {
		this.campaignAction = campaignAction;
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
