package org.cloudgraph.web.model.dashboard;

// java imports
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.AppMessageUtils;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.config.web.AppParamName;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.config.web.PropertyName;
import org.cloudgraph.web.datasource.DataSourceManager;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.util.BeanFinder;
import org.cloudgraph.web.util.ResourceCache;
import org.cloudgraph.web.util.ResourceFinder;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.DropEvent;
import org.richfaces.event.DropListener;
import org.richfaces.model.ListRowKey;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.sdo.personalization.Setting;

/**
 * 
 */
public class DashboardBean extends ModelBean 
    implements Dashboard, WebConstants, DropListener
{

	private static Log log = LogFactory.getLog(DashboardBean.class);

	private ResourceFinder resourceFinder = new ResourceFinder();

	private DataSourceManager dataSourceManager = new DataSourceManager();

	private UserBean user;
	private String selectedApplication = null; // "VIPR"
	private String selectedJobCode = null; //"IDP4FF";
	private Integer selectedFiscalYear;
	private String selectedCostCategoryName = "Expenses";
	private ElementType type = ElementType.PAGE;
	private ComponentName name = ComponentName.PAGE___DASHBOARD;
	private Map<ComponentName, Component> componentMap = new HashMap<ComponentName, Component>();
	
	private int[] numWidgetsDropped = null;
	
	private Container availableChartsContainer;
	public Container getAvailableChartsContainer() {
		return this.availableChartsContainer;
	}	
	private Container availableAlertsContainer;
	public Container getAvailableAlertsContainer() {
		return this.availableAlertsContainer;
	}	
	private Container availableEventsContainer;
	public Container getAvailableEventsContainer() {
		return this.availableEventsContainer;
	}	
	private Container availableTablesContainer;
	public Container getAvailableTablesContainer() {
		return this.availableTablesContainer;
	}	
	private Container availableLayoutsContainer;
	public Container getAvailableLayoutsContainer() {
		return this.availableLayoutsContainer;
	}	

	
	protected SimpleDateFormat seriesFormat = new SimpleDateFormat(
			DATE_FORMAT_CHART);

	public DashboardBean() {
		log.debug("DashboardBean()");
		try {
			user = beanFinder.findUserBean();
			AppMessageUtils.setMessageBundle(user.getBundleName());
			// if (log.isDebugEnabled()) {
			// log.debug("checking role for user '" + user.getName() + "'");
			// }
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		
		try {

			this.availableLayoutsContainer = new Container(ComponentName.CONTAINER___AVAILABLE___LAYOUTS, 
					ComponentShape.TALL, 999, this, null);
			
			AbstractTableLayout twoColumnTableLayout = new TwoColumnTableLayout(
					ComponentName.LAYOUT___TWO___COLUMN___TABLE,
					this, this.availableLayoutsContainer);
			twoColumnTableLayout.setCaption("Two Column Table");
			twoColumnTableLayout.setTitle("Two Column Table");
			twoColumnTableLayout.setDescription("A two column table layout with header and footer for wide widgets");			
			this.componentMap.put(twoColumnTableLayout.getComponentName(), twoColumnTableLayout);
			
			
			AbstractTableLayout threeColumnTableLayout = new ThreeColumnTableLayout(
					ComponentName.LAYOUT___THREE___COLUMN___TABLE,
					this, this.availableLayoutsContainer);
			threeColumnTableLayout.setCaption("Three Column Table");
			threeColumnTableLayout.setTitle("Three Column Table");
			threeColumnTableLayout.setDescription("A three column table layout with header and footer for wide widgets");
			this.componentMap.put(threeColumnTableLayout.getComponentName(), threeColumnTableLayout);

		
			availableChartsContainer = new Container(ComponentName.CONTAINER___AVAILABLE___CHARTS, 
					ComponentShape.TALL, 230, this, null);
			availableAlertsContainer = new Container(ComponentName.CONTAINER___AVAILABLE___ALERTS, 
					ComponentShape.TALL, 230, this, null);
			availableEventsContainer = new Container(ComponentName.CONTAINER___AVAILABLE___EVENTS, 
					ComponentShape.TALL, 230, this, null);
			availableTablesContainer = new Container(ComponentName.CONTAINER___AVAILABLE___TABLES, 
					ComponentShape.TALL, 230, this, null);
			
		
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		
		numWidgetsDropped = new int[5];
		for (int i = 0; i < numWidgetsDropped.length; i++) numWidgetsDropped[i] = 0;
	}
	
	public ComponentName getComponentName() {
		return this.name;
	}
	
	
	public ElementType getType() {
		return type;
	}

	public Component getComponent(ComponentName name) {
	    return this.componentMap.get(name);	
	}
	
	public String clearData() {
		getLayout().clearData();		
		
		return null;
	}
	
	public void clearDataAction(javax.faces.event.ActionEvent e) {
		clearData();
	}
	
	public String getRedirect() {
		HttpServletResponse response = (HttpServletResponse) FacesContext
				.getCurrentInstance().getExternalContext().getResponse();
		String url = getDashboardUrl();
		log.info("redirecting to: " + url);
		this.forward(url, response);
		return "HERE";
	}

	public String getDashboard() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();
		ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY);
		javax.faces.application.Application app = appFactory.getApplication();
		// app.getNavigationHandler().
		// app.getNavigationHandler().handleNavigation(context, arg1, arg2)
		return "";
	}

	public String getDashboardUrl() {
		return this.URL_DASHBOARD;
	}

	public String getAction() {
		// the left nav selection is mapped to a dashboard page
		// in faces-config.xml
		// return
		// this.beanFinder.findLeftNavBean().getSelectedAction().toString();
		return "";
	}

	public ResourceCache getResourceCache() {
		return ResourceCache.instance();
	}

	public String getTitle() {
		// LeftNavBean leftNav = this.beanFinder.findLeftNavBean();
		// String title =
		// resourceFinder.getDashboardTitle(leftNav.getSelectedAction());
		// title = appendRoleInfoLabel(title);
		return "Application Inventory Dashboard";
	}


	public String getSelectedCostCategoryName() {
		return selectedCostCategoryName;
	}

	public void setSelectedCostCategoryName(String selectedCostCategoryName) {
		this.selectedCostCategoryName = selectedCostCategoryName;
	}

	public String getSelectedApplication() {
		return selectedApplication;
	}

	public void setSelectedApplication(String app) {
		selectedApplication = app;
	}

	public void selectedApplicationChange(ActionEvent e) {
		log.info("changed app: " + e.getSource());
		//if (appFundingByFiscalYearDataSource != null) {
		//	appFundingByFiscalYearDataSource.purgeCurrentDataSet();
		//}
	}

	public void selectedApplicationChange(ValueChangeEvent e) {
		log.info("changed app: " + e.getSource());
		//if (appFundingByFiscalYearDataSource != null) {
		//	appFundingByFiscalYearDataSource.purgeCurrentDataSet();
		//}
	}


	public String getSelectedJobCode() {
		return selectedJobCode;
	}

	public void setSelectedJobCode(String code) {
		selectedJobCode = code;
	}

	public void selectedJobCodeChange(ActionEvent e) {
		log.info("changed code: " + e.getSource());
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String code = params.get("jobcode");
    	log.info("code: " + code);
    	if (code != null && code.length() > 0 && !selectedJobCode.equals(code))
    	{	
    	    selectedJobCode = code;
		    //if (jobCodeFundingByFiscalYearDataSource != null) {
			//    jobCodeFundingByFiscalYearDataSource.purgeCurrentDataSet();
		    //}
    	}
	}

	public void selectedJobCodeChange(ValueChangeEvent e) {
		log.info("changed code: " + e.getSource());
		//if (jobCodeFundingByFiscalYearDataSource != null) {
		//	jobCodeFundingByFiscalYearDataSource.purgeCurrentDataSet();
		//}
	}
	
	public void costCategorySelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	        Category cat = (Category)selectedNode.getUserData();
	        this.selectedCostCategoryName = cat.getName();
	        //this.investmentCostCategoryDataSource = null;
	        //this.investmentCostPieDataSource = null;
	        
	        //Action selectedCat = Action.valueOf(selectedNode.getAction());
	        log.info("costCategorySelectListener: " + this.selectedCostCategoryName);	
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    }	

	public Integer getSelectedFiscalYear() {
	    return this.selectedFiscalYear;	
	}
	
	public void fiscalYearSelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	        
	        selectedFiscalYear = Integer.valueOf(selectedNode.getName());
	        //this.investmentCostCategoryDataSource = null;
	        //this.investmentCostPieDataSource = null;
	        
	        //Action selectedCat = Action.valueOf(selectedNode.getAction());
	        log.info("fiscalYearSelectListener: " + selectedFiscalYear);	
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    }	

	public void applicationSelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	        
	        this.selectedApplication = selectedNode.getName();
	        //this.investmentCostCategoryDataSource = null;
	        //this.investmentCostPieDataSource = null;
	        
	        //Action selectedCat = Action.valueOf(selectedNode.getAction());
	        log.info("applicationSelectListener: " + selectedApplication);	
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    }	
	
	public String getAppFundingByFiscalYearTitle() {
		return "Application Funding by Fiscal Year (" + selectedApplication
				+ ")";
	}

	
	public String getJobCodeFundingByFiscalYearTitle() {
		return "Job Code Funding by Fiscal Year (" + selectedJobCode + ")";
	}


	public CategoryURLGenerator getInvestmentCostCategoryURLGenerator() {
		CategoryURLGenerator gen = new StandardCategoryURLGenerator(
				URL_PROJECT_FUNDING_FROM_DASHBOARD, 
				AppParamName.SUBCAT.value(), 
				AppParamName.CAT.value()) 
		{
			public String generateURL(CategoryDataset dataset, int series,
					int category) {
				String result = super.generateURL(dataset, series, category);
				if (selectedApplication != null && selectedApplication.length() > 0)
				    result += "&amp;" + AppParamName.APP.toString() + "="
						+ selectedApplication;
				if (selectedFiscalYear != null && selectedFiscalYear.intValue() > 0)
				    result += "&amp;" + AppParamName.YEAR.toString() + "="
						+ selectedFiscalYear.toString();
				return result;
			}
		};

		return gen;
	}

	public StandardPieURLGenerator getInvestmentCostPieURLGenerator() {

		StandardPieURLGenerator gen = new StandardPieURLGenerator(
				URL_PROJECT_FUNDING_FROM_DASHBOARD,
				AppParamName.SUBCAT.value()) 
	    {			
			public String generateURL(PieDataset dataset, int series,
					int category) {	
				String result = super.generateURL(dataset, series, category);
				if (selectedApplication != null && selectedApplication.length() > 0)
				    result += "&amp;" + AppParamName.APP.toString() + "="
						+ selectedApplication;
				if (selectedFiscalYear != null && selectedFiscalYear.intValue() > 0)
				    result += "&amp;" + AppParamName.YEAR.toString() + "="
						+ selectedFiscalYear.toString();
				
				return result;
			}
		};

		return gen;
	}	


	public CategoryURLGenerator getAppFundingByFiscalYearURLGenerator() {
		// CategoryURLGenerator gen = new StandardCategoryURLGenerator(
		// URL_PROJECT_FUNDING_FROM_DASHBOARD,
		// AppParamName.NAME.value(),
		// AppParamName.YEAR.value());

		CategoryURLGenerator gen = new StandardCategoryURLGenerator(
				URL_PROJECT_FUNDING_FROM_DASHBOARD, AppParamName.NAME
						.toString(), AppParamName.YEAR.toString()) {
			public String generateURL(CategoryDataset dataset, int series,
					int category) {
				String result = super.generateURL(dataset, series, category);
				result += "&amp;" + AppParamName.APP.toString() + "="
						+ selectedApplication;
				return result;
			}
		};

		return gen;
	}


	
	public CategoryURLGenerator getJobCodeFundingCategoryURLGenerator() {
		CategoryURLGenerator gen = new StandardCategoryURLGenerator(
				URL_PROJECT_FUNDING_FROM_DASHBOARD, 
				    AppParamName.CODE.value(), 
					AppParamName.YEAR.value()) {
			public String generateURL(CategoryDataset dataset, int series,
					int category) {
				String result = super.generateURL(dataset, series, category);
				result += "&amp;" + "foo" + "=" + "bar";
				return result;
			}
		};
		return gen;
	}

	public PieURLGenerator getJobCodeFundingPieURLGenerator() {
		PieURLGenerator gen = new StandardPieURLGenerator(
				addContext(URL_PROJECT_FUNDING_FROM_DASHBOARD),
				AppParamName.CODE.value());
		return gen;
	}


	public PieURLGenerator getAppFundingTotalsURLGenerator() {
		PieURLGenerator gen = new StandardPieURLGenerator(
				addContext(URL_PROJECT_FUNDING_FROM_DASHBOARD),
				AppParamName.NAME.value());
		return gen;
	}

	private String addContext(String url) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();
		String path = request.getContextPath();
		if (path.indexOf("dashboard") >= 0) // to root
			path += "..";
		return path + "/" + url;
	}

	public String navigate() {
		log.debug("navigate");
		return "foo";
	}

	public void actionListener(ActionEvent event) {
		log.debug("actionListener: " + event.getSource());
	}

	public SimpleDateFormat getSeriesFormat() {
		return seriesFormat;
	}

	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	public String getResourceContext() {
		return "aplsDashboard";
	}
	
	public Layout getLayout() {
    	ComponentName layoutName = ComponentName.LAYOUT___TWO___COLUMN___TABLE;
		Setting layoutNameSetting = user.findComponentSetting(this.name,  
	        PropertyName.LAYOUT___NAME);
        if (layoutNameSetting != null) {
        	try {
        		String layoutNameValue = layoutNameSetting.getValue();
        	    layoutName = ComponentName.fromValue(layoutNameValue);
        	}
        	catch (IllegalArgumentException e) {
        	    // may be obsolete layout name	
        	}
        }
        for (Component layoutComp : this.availableLayoutsContainer.getComponents()) {
        	if (layoutComp.getComponentName().ordinal() == layoutName.ordinal())
        		return (Layout)layoutComp; 
        }
        
        throw new IllegalStateException("could not get dashboard layout");
	}

	public void setLayout(Layout layout) {
		try {
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.LAYOUT___NAME, 
	        	layout.getComponentName()); 	
	        user.commitProfile();
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
	

	public void processDrop(DropEvent de)
	{
		
		String dragType = de.getDragType();
		String dragValue = de.getDragValue().toString();
		log.info("getDragType: " + dragType);
		log.info("getDragValue: " + dragValue);
		
        FacesContext context = FacesContext.getCurrentInstance();
		String dragSourceId = context.getExternalContext().getRequestParameterMap().get("dragSourceId");
		String dropTargetId = context.getExternalContext().getRequestParameterMap().get("dropTargetId");
		log.info("dragSourceId: " + dragSourceId);
		log.info("dropTargetId: " + dropTargetId);
		
		int dropIdx = -1;
		if ("CHART_WIDGET".equals(dragType))
		{
			dropIdx = 0;
			log.info("Dropped " + dragType);
		}
		else
		if ("TABLE_WIDGET".equals(dragType))
		{
			dropIdx = 1;
			log.info("Dropped " + dragType);
		}
		else
		if ("ALERT_WIDGET".equals(dragType))
		{
			dropIdx = 2;
			log.info("Dropped " + dragType);
		}
		else
		if ("EVENT_WIDGET".equals(dragType))
		{
			dropIdx = 3;
			log.info("Dropped " + dragType);
		}
		else
		if ("LAYOUT_WIDGET".equals(dragType))
		{
			dropIdx = 4;
			log.info("Dropped " + dragType);
		}
		
		numWidgetsDropped[dropIdx]++;
		
	} // processDrop
	
	
	public String resetWidgetDropZone()
	{
		
		for (int i = 0; i < numWidgetsDropped.length; i++) numWidgetsDropped[i] = 0;
		return null;
		
	} // resetWidgetDropZone
	
	
	public int getChartDropCount() {return numWidgetsDropped[0];}
	public int getTableDropCount() {return numWidgetsDropped[1];}
	public int getAlertDropCount() {return numWidgetsDropped[2];}
	public int getEventDropCount() {return numWidgetsDropped[3];}
	public int getLayoutDropCount() {return numWidgetsDropped[4];}
	  

}
