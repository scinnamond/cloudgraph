package org.cloudgraph.web.model.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.tree.DynamicTreeNodeModel;
import org.cloudgraph.web.sdo.adapter.DataObjectAdapter;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Select;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.ListRowKey;

import commonj.sdo.DataGraph;

public class GraphEditBean extends ModelBean{

	private static Log log = LogFactory.getLog(GraphEditBean.class);
	private DataGraph selectedGraph;
	// accommodate treeState being queried even though tree not rendered
	// this initialization hack makes RichFaces tree find it's tree state and
	// be happy and not blow up even though the tree is not yet displayed 
	private GraphTreeBean graphTree = new GraphTreeBean();
	private UITree selectedGraphUITree;
	private DynamicTreeNodeModel selectedDataObjectNode;
	private ListRowKey selectedDataObjectListRowKey;
	private DataObjectAdapter selectedDataObject;
	private long selectedDataObjectSeqId;
	private DataGraph toDeleteDataObjectGraph;
	private String graphRootType;
	private String graphRootURI;
	
	private boolean reQuery = true;
	
	public GraphEditBean() {
		log.info("created GraphEditBean");
	}

	public String getTitle() {
		if (this.selectedGraph != null)
			return this.selectedGraph.getRootObject().getType().getName();
		else
			return "";
	}
	
	public long getSelectedDataObjectSeqId() {
		return selectedDataObjectSeqId;
	}

	public void setSelectedDataObjectSeqId(long selectedDataObjectSeqId) {
		this.selectedDataObjectSeqId = selectedDataObjectSeqId;
	}

	public String saveFromAjax() {
    	save();
    	return null; // maintains AJAX happyness
    }
	
	public GraphTreeBean getGraphTree() {
		
		String type = this.beanFinder.findDemoBean().getModelRootType();
		String uri = this.beanFinder.findDemoBean().getModelRootURI();
		if (type != null)
			if (this.graphRootType == null || !this.graphRootType.equals(type)) {
				this.reQuery = true;
				this.graphRootType = type;
			}
		if (uri != null)
			if (this.graphRootURI == null || !this.graphRootURI.equals(uri)) {
				this.reQuery = true;
				this.graphRootURI = uri;
			}
		
		String currTab = this.beanFinder.findDemoBean().getSelectedTab();
		if (this.reQuery == true && this.graphRootType != null &&
				this.graphRootURI != null && "tab_dataGraphs".equals(currTab)) {
			try {
				Select select = new Select(new String[] {
					"*",	
					"*/*",	
					"*/*/*",	
					"*/*/*/*",	
					"*/*/*/*/*",	
					"*/*/*/*/*/*"	
				});
				From from = new From(this.graphRootType, 
						this.graphRootURI);
				Query query = new Query(select, from);
				SDODataAccessClient service = new SDODataAccessClient(
		        		new HBasePojoDataAccessClient());
				DataGraph[] result = service.find(query);
	            this.graphTree = new GraphTreeBean(result);	
	            this.reQuery = false;
			}
			catch (Throwable t) {
				log.error(t.getMessage(), t);
			}
		}
        return this.graphTree;
	}
	
    public String save() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	if (log.isDebugEnabled())
                log.debug(((PlasmaDataObject)selectedGraph).dump());
		    SDODataAccessClient service = new SDODataAccessClient(
		    	new HBasePojoDataAccessClient());
		    if (this.toDeleteDataObjectGraph == null) {
		        service.commit(selectedGraph, 
		    	    beanFinder.findUserBean().getName());
		    }
		    else {
		    	service.commit(new DataGraph[] {selectedGraph, 
		    			toDeleteDataObjectGraph}, 
			    	    beanFinder.findUserBean().getName());
		    }	
            return AppActions.SAVE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        	this.toDeleteDataObjectGraph = null;        	
        }        
    }
    
    public String exit() {
    	try {
	    	this.selectedGraph.getChangeSummary().endLogging(); // wipe any changes 
	    	this.selectedGraph.getChangeSummary().beginLogging();
	    	this.selectedGraph = null;
        } catch (Throwable t) {
        } finally {
        	this.toDeleteDataObjectGraph = null;        	
        }
    	return null;
    }

    public String export() {

    	try {   		
    		//GraphExportAssembler visitor = 
    		//	new GraphExportAssembler();
    		        	
	    	//((PlasmaDataObject)this.selectedGraphGraph.getRootObject()).accept(visitor);
    	
    	    //GraphDataBinding binding = new GraphDataBinding(
    	    //		new DefaultValidationEventHandler());
    	        	    
    	    //this.exportXML = binding.marshal(visitor.getResult());
    	    log.info(this.exportXML);
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	
    	return AppActions.EXIT.value();
    }
    
    public String exportXML;
    public String getExportXML() {
    	return this.exportXML;
    }
    
    public boolean dataObjectSelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
    		HtmlTree tree = (HtmlTree)event.getSource();	
    		this.selectedGraphUITree = tree;
    		this.selectedDataObjectListRowKey = (ListRowKey)tree.getRowKey();
	        this.selectedDataObjectNode = (DynamicTreeNodeModel)tree.getTreeNode(this.selectedDataObjectListRowKey);
	        this.selectedDataObject = new DataObjectAdapter((commonj.sdo.DataObject)selectedDataObjectNode.getUserData());
	        this.toDeleteDataObjectGraph = null;
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return false;
    }
	
	public DataObjectAdapter getSelectedDataObject() {
		return selectedDataObject;
	}
    
	public DataGraph getSelectedGraph() {
		return selectedGraph;
	}
	
	public boolean getHasSelectedGraph() {
		return this.selectedGraph != null;
	}
	
	public void setSelectedGraph(DataGraph selected) {
		
        String name = selected.getRootObject().getType().getName();

        // query for the entire Graph
	    SDODataAccessClient service = new SDODataAccessClient();
	    Query query = null;
	    DataGraph[] results = service.find(query);
	    //DataGraph[] results = service.find(GraphQuery.createQuery(
	    //		name));
	    this.selectedGraph = (DataGraph)results[0].getRootObject();
	
	    this.selectedDataObject = null;
	    this.selectedDataObjectListRowKey = null;
	    this.selectedDataObjectNode = null;
	    this.toDeleteDataObjectGraph = null;		
	}

	
	
}
