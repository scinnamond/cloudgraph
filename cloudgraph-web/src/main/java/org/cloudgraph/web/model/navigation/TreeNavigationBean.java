package org.cloudgraph.web.model.navigation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.cloudgraph.web.model.Action;
import org.cloudgraph.web.model.OperationName;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.ListRowKey;


public class TreeNavigationBean extends NavigationBean {
    
    private Action selectedLeftAction;
    private Action selectedTopAction;
	private TreeNodeModel selectedNode;
    private static Log log = LogFactory.getLog(TreeNavigationBean.class);
    
	private TreeNodeModel budgetYears;
	private TreeNodeModel costFiscalYears;
	
	private boolean edit = false;

	public TreeNavigationBean()
	{
	    init();	
	}
	
	public Action getSelectedAction() {
		return selectedLeftAction;
	}

	public void setSelectedAction(Action selectedAction) {
		this.selectedLeftAction = selectedAction;
	}
	
	public String nullAction()
	{
		return null;
	}
	
	public String getAction()
	{
		return this.selectedLeftAction.toString();
	}

    public TreeNodeModel getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNodeModel selectedNode) {
		this.selectedNode = selectedNode;
	}
	

	private void init()
    {
    	int id = 22;
    	   	        
        // could query the FY table but, let's avoid it
        id = 500;
        costFiscalYears = new TreeNodeModel(id++);
        
        for (int i = 2009; i <= 2016; i++) {       
            TreeNodeModel year = new TreeNodeModel(id++);
            year.setName(String.valueOf(i));
            year.setLabel(String.valueOf(i));
            year.setUserData(new Integer(i));
            year.setTooltip("Fiscal year " + String.valueOf(i));
            year.setType(LeftNavigationTreeNodeType.fiscal_year.name());
            costFiscalYears.addNode(year);
        }

        budgetYears = new TreeNodeModel(id++);
        
        for (int i = 2012; i <= 2014; i++) {       
            TreeNodeModel year = new TreeNodeModel(id++);
            year.setName(String.valueOf(i));
            year.setLabel(String.valueOf(i));
            year.setUserData(new Integer(i));
            year.setTooltip("Budget year " + String.valueOf(i));
            year.setType(LeftNavigationTreeNodeType.fiscal_year.name());
            budgetYears.addNode(year);
        }
        
    }
	
    public void setData(Object data)
    {
    }

    public Object getBudgetYears()
    {
    	return this.budgetYears;
    } 

    public Object getCostFiscalYears()
    {
    	return this.costFiscalYears;
    } 
 

    public void nodeSelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	    	selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	    	selectedLeftAction = Action.valueOf(selectedNode.getAction());
	    	
	        log.info("nodeSelectListener: " + selectedLeftAction.toString());	
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    }
    
    public OperationName getSelectedOperation()
    {
	    OperationName operation = null;
	    if (this.getSelectedNode() != null)
	        operation = this.getSelectedNode().getOperation();
        return operation;	
    }
    
    public boolean getOperationSelected()
    {
    	return getSelectedOperation() != null;
    }
    
    public String getSelectedOperationLabel()
    {
    	OperationName operation = getSelectedOperation();
    	if (operation != null)
    	    return ResourceManager.instance().getString(operation.toString(), ResourceType.LABEL);
    	else
    		return "";
    }
    
    public String editActionForOperation()
    {
    	OperationName operation = getSelectedOperation();
    	if (operation != null)
    	    return Action.valueOf(operation.toString()).toString();
    	else
    		return null;
    }
 
	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	
    
}


/*
public String getSelectedView()
{
    ApplicationFactory appFactory =                                                                     
        (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);               
    Application app = appFactory.getApplication();  
    //NavigationHandler handler = app.getNavigationHandler();
    //com.sun.faces.application.NavigationHandlerImpl handler = 
    //	(com.sun.faces.application.NavigationHandlerImpl)app.getNavigationHandler();
    //org.richfaces.ui.application.StateNavigationHandler handler = 
    //	(org.richfaces.ui.application.StateNavigationHandler)app.getNavigationHandler();
    // this default bugger provides no access at all
    // FIXME; Need a custom navigation handler for sure it seems
            
    String result = "/Dashboard.jsp";
    
    try {
    	if (selectedTopAction == null)
    		result = "/Dashboard.jsp";
    	else if (selectedTopAction.ordinal() == Action.dashboard.ordinal())
    		result =  "/Dashboard.jsp";
    	else if (selectedTopAction.ordinal() == Action.request.ordinal())
    	{	
    		if (!this.isEdit())
    			result =  "/RequestList.jsp";
    		else
    		{
    	    if (selectedLeftAction.ordinal() == Action.calculator_add_student.ordinal())
    	    	result =  "/calculator/AddStudent.jsp";
    	    else if (selectedLeftAction.ordinal() == Action.calculator_calculate.ordinal())
    	    	result =  "/calculator/Calculate.jsp";
    	    else if (selectedLeftAction.ordinal() == Action.calculator_returnatask.ordinal())
    	    	result =  "/calculator/ReturnATask.jsp";
    	    else if (selectedLeftAction.ordinal() == Action.calculator_submitcalculation.ordinal())
    	    	result =  "/calculator/SubmitCalculation.jsp";
    		}    
    	}
    	else if (selectedTopAction.ordinal() == Action.roles.ordinal())
    		result =  "/Roles.jsp";
	}
	catch (Throwable t) {
		log.error(t.getMessage(), t);
	}
	
	log.info("to: " + result);
	return result;		
}
*/
