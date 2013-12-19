package org.cloudgraph.web.model.navigation;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.Action;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.ActionHandler;
import org.cloudgraph.web.model.tree.TreeNodeAction;
import org.cloudgraph.web.model.tree.TreeSelectionModel;


@ManagedBean(name="NavigationBean")
@SessionScoped
public class NavigationBean extends ModelBean {
    
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(NavigationBean.class);
    
    // RichFaces Toolbar cannot be loaded using a overview iterator such as overviewTable, i.e. dynamically without
    // using the 'binding' attribute which means we have to create all sub-components
    // dynamically as well. Hence declare top-nav items statically. :(
    private TreeNodeAction dashboardAction;
    
    private TreeNodeAction overviewAction;
    private TreeNodeAction demoAction;
    private TreeNodeAction downloadAction;
    private TreeNodeAction documentationAction;
    private TreeNodeAction newsAction;
    private TreeNodeAction emailAction;
    private TreeNodeAction blogAction;
    private TreeNodeAction forumAction;
    private TreeNodeAction contactAction;
    
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
				downloadAction.setSelected(false);
				documentationAction.setSelected(false); 
				newsAction.setSelected(false); 
				emailAction.setSelected(false); 
				blogAction.setSelected(false); 
				demoAction.setSelected(false);
				forumAction.setSelected(false); 
				contactAction.setSelected(false); 
				administrationAction.setSelected(false);
			}

			@Override
			public void setSelection(TreeNodeAction selection) {
				clearSelection();
				selectedTopAction = selection;
				selectedTopAction.setSelected(true);				
			}

			@Override
			public TreeNodeAction getSelection() {
				return selectedTopAction;
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
	    demoAction = new TreeNodeAction(Action.topnav_demo, 
	    		topActionHandler, topSelectionModel);
	    downloadAction = new TreeNodeAction(Action.topnav_download, 
	    		topActionHandler, topSelectionModel);
	    documentationAction = new TreeNodeAction(Action.topnav_documentation, 
	    		topActionHandler, topSelectionModel);
	    newsAction = new TreeNodeAction(Action.topnav_news, 
	    		topActionHandler, topSelectionModel);
	    emailAction = new TreeNodeAction(Action.topnav_email, 
	    		topActionHandler, topSelectionModel);
	    blogAction = new TreeNodeAction(Action.topnav_blog, 
	    		topActionHandler, topSelectionModel);
	    forumAction = new TreeNodeAction(Action.topnav_forum, 
	    		topActionHandler, topSelectionModel);
	    contactAction = new TreeNodeAction(Action.topnav_contact, 
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

	public void setNewsSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.newsAction);
		else
			this.newsAction.setSelected(false);
	}
	
	public void setEmailSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.emailAction);
		else
			this.emailAction.setSelected(false);
	}
	
	public void setBlogSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.blogAction);
		else
			this.blogAction.setSelected(false);
	}
	
	public void setForumSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.forumAction);
		else
			this.forumAction.setSelected(false);
	}
	
	public void setContactSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.contactAction);
		else
			this.contactAction.setSelected(false);
	}
	
	public void setAdminitstrationSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.administrationAction);
		else
			this.blogAction.setSelected(false);
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

	public TreeNodeAction getNewsAction() {
		return newsAction;
	}

	public void setNewsAction(TreeNodeAction newsAction) {
		this.newsAction = newsAction;
	}
	
	public TreeNodeAction getEmailAction() {
		return emailAction;
	}

	public void setEmailAction(TreeNodeAction emailAction) {
		this.emailAction = emailAction;
	}
	
	public TreeNodeAction getBlogAction() {
		return blogAction;
	}

	public void setBlogAction(TreeNodeAction blogAction) {
		this.blogAction = blogAction;
	}
	
	public TreeNodeAction getForumAction() {
		return forumAction;
	}

	public void setForumAction(TreeNodeAction forumAction) {
		this.forumAction = forumAction;
	}
	
	public TreeNodeAction getContactAction() {
		return contactAction;
	}

	public void setContactAction(TreeNodeAction contactAction) {
		this.contactAction = contactAction;
	}
	
	public TreeNodeAction getDemoAction() {
		return demoAction;
	}

	public void setDemoAction(TreeNodeAction demoAction) {
		this.demoAction = demoAction;
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
