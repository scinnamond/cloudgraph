package org.cloudgraph.web.model.administration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.cache.TaxonomyQuery;
import org.cloudgraph.web.model.tree.DynamicTreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.sdo.adapter.CategoryAdapter;
import org.cloudgraph.web.sdo.adapter.DataObjectAdapter;
import org.cloudgraph.web.sdo.finder.CategoryFinder;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.ListRowKey;

import org.cloudgraph.web.sdo.categorization.Taxonomy;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class TaxonomyEditBean extends ModelBean{

	private static Log log = LogFactory.getLog(TaxonomyEditBean.class);
	private TreeNodeModel taxonomies;
	private Taxonomy selectedTaxonomy;
	// accommodate treeState being queried even though tree not rendered
	// this initialization hack makes RichFaces tree find it's tree state and
	// be happy and not blow up even though the tree is not yet displayed 
	private TaxonomyTreeBean selectedTaxonomyTree = new TaxonomyTreeBean();
	private UITree selectedTaxonomyUITree;
	private DynamicTreeNodeModel selectedCategoryNode;
	private ListRowKey selectedCategoryListRowKey;
	private CategoryAdapter selectedCategory;
	private long selectedCategorySeqId;
	private DataGraph toDeleteCategoryGraph;
	
	public TaxonomyEditBean() {
		log.info("created TaxonomyEditBean");
	}

	public String getTitle() {
		if (this.selectedTaxonomy != null)
			return "Taxonomy: " 
			    + this.selectedTaxonomy.getCategory().getName()
		        + " Version: " + this.selectedTaxonomy.getVersion();
		else
			return "";
	}
	
	public long getSelectedCategorySeqId() {
		return selectedCategorySeqId;
	}

	public void setSelectedCategorySeqId(long selectedCategorySeqId) {
		this.selectedCategorySeqId = selectedCategorySeqId;
	}

	public String saveFromAjax() {
    	save();
    	return null; // maintains AJAX happyness
    }
    
    public String save() {
    	BeanFinder beanFinder = new BeanFinder();
        ErrorHandlerBean errorHandler = beanFinder.findErrorHandlerBean();
        try {
        	if (log.isDebugEnabled())
                log.debug(((PlasmaDataObject)selectedTaxonomy).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
		    if (this.toDeleteCategoryGraph == null) {
		        service.commit(selectedTaxonomy.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
		    }
		    else {
		    	service.commit(new DataGraph[] {selectedTaxonomy.getDataGraph(), 
		    			toDeleteCategoryGraph}, 
			    	    beanFinder.findUserBean().getName());
		    }	
            return AppActions.SAVE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        	this.toDeleteCategoryGraph = null;        	
        }
        
    }
    
    public String exit() {
    	try {
	    	this.selectedTaxonomy.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
	    	this.selectedTaxonomy.getDataGraph().getChangeSummary().beginLogging();
	    	this.selectedTaxonomy = null;
        } catch (Throwable t) {
        } finally {
        	this.toDeleteCategoryGraph = null;        	
        }
    	return null;
    }

    public String export() {

    	try {   		
    		//TaxonomyExportAssembler visitor = 
    		//	new TaxonomyExportAssembler();
    		        	
	    	//((PlasmaDataObject)this.selectedTaxonomyGraph.getRootObject()).accept(visitor);
    	
    	    //TaxonomyDataBinding binding = new TaxonomyDataBinding(
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

    public String addChild() {
    	try {
    		if (this.selectedCategory == null || this.selectedCategorySeqId != this.selectedCategory.getSeqId()) {
    		    log.warn("user must first select using left-click");
    			return null;
    		}    
    		
	    	CategoryFinder finder = new CategoryFinder(this.selectedCategorySeqId);
	    	((PlasmaDataObject)this.selectedTaxonomy).accept(finder); 
	    	
	    	// automatically links new child
	    	CategoryAdapter child = new CategoryAdapter(finder.getResult().createChild());  
	    	child.setName("new child category");
	    	this.selectedCategory = child;
	    	// reflect new child in UI component tree
	    	this.selectedTaxonomyTree.addChildrenNodes(this.selectedCategoryNode);	
	    	this.selectedCategoryNode.setSelected(false);
	    	this.selectedCategoryNode.setLeaf(false);
	    	this.selectedTaxonomyTree.getTreeState().expandNode(this.selectedTaxonomyUITree, 
	    			this.selectedCategoryListRowKey);
	    		    		   
	    	Object id = this.selectedCategoryNode.getNodes().keySet().iterator().next();
	    	DynamicTreeNodeModel uiChild = (DynamicTreeNodeModel)this.selectedCategoryNode.getChild(id);
	    	uiChild.setLeaf(true);
	    	uiChild.setSelected(true);
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	
    	return null; // maintains AJAX happiness
    }
    
    public String addSibling() {
        try {
    		if (this.selectedCategory == null || this.selectedCategorySeqId != this.selectedCategory.getSeqId()) {
    		    log.warn("user must first select using left-click");
    			return null;
    		}    
	    	CategoryFinder finder = new CategoryFinder(this.selectedCategorySeqId);
	    	((PlasmaDataObject)this.selectedTaxonomy).accept(finder);
	    	this.selectedCategory = new CategoryAdapter(finder.getResult());
        
        
	    	// automatically links new child
	    	CategoryAdapter sibling = new CategoryAdapter(finder.getResult().getParent().createChild());  
	    	sibling.setName("new sibling category");
	    	this.selectedCategory = sibling;
	    	// reflect new child in UI component tree
	    	this.selectedTaxonomyTree.addChildrenNodes((DynamicTreeNodeModel)this.selectedCategoryNode.getParent());	
	    	this.selectedCategoryNode.setSelected(false);
        }
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return null; // maintains AJAX happiness
    }
    
    public String confirmDelete() {
    	try {
    		if (this.selectedCategory == null || this.selectedCategorySeqId != this.selectedCategory.getSeqId()) {
    		    log.warn("user must first select using left-click");
    			return null;
    		}    
	    	CategoryFinder finder = new CategoryFinder(this.selectedCategorySeqId);
	    	((PlasmaDataObject)this.selectedTaxonomy).accept(finder);

	    	// Query for the graph of all sub-categories and objects
	    	// linked one hop away. Use this graph to inform
	    	// the user of all the linked objects (if any) which
	    	// will be deleted along with the selected cat
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(TaxonomyQuery.createDeleteConfirmQuery(
		    		finder.getResult().getSeqId()));
		    this.toDeleteCategoryGraph = results[0];
		    // In the above wildcard query we get the parent
		    // as a contained object. Unset it here, as we don't
		    // want to delete it. 
		    org.cloudgraph.web.sdo.categorization.Category rootCat = (org.cloudgraph.web.sdo.categorization.Category)this.toDeleteCategoryGraph.getRootObject();
		    rootCat.unsetParent();
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	
    	return null; // maintains AJAX happiness    	
    }
    
    public List<DataObjectAdapter> getDeletedCatagoryGraphAsList() {
    	
    	final List<DataObjectAdapter> result = new ArrayList<DataObjectAdapter>();
    	if (this.toDeleteCategoryGraph != null) {
        	PlasmaDataGraphVisitor visitor = new PlasmaDataGraphVisitor() {
    			public void visit(DataObject target, DataObject source,
    					String sourceKey, int level) {
    				if (source != null) // ignore the root
    				    result.add(new DataObjectAdapter(target));
    			}
        	};
    	    ((PlasmaDataObject)this.toDeleteCategoryGraph.getRootObject()).accept(visitor);
    	}
    	return result;
    }
    
    public String delete() {
    	try {
    		if (this.toDeleteCategoryGraph == null) {
    		    log.warn("user must first select using left-click, then confirm deletion");
    			return null;
    		}    
    	
    		// Delete the root and all contained DataObjects
    		this.toDeleteCategoryGraph.getRootObject().delete();
    		
    		// just remove the selected cat from it's parent
    		this.selectedCategory.getDelegate().getParent().removeChild(
    				this.selectedCategory.getDelegate());
	        
	    	// reflect new child in UI component tree
	    	DynamicTreeNodeModel parentCategoryNode = (DynamicTreeNodeModel)this.selectedCategoryNode.getParent();
	    	this.selectedTaxonomyTree.addChildrenNodes(parentCategoryNode);	
	    	this.selectedCategoryNode.setSelected(false);
	    	parentCategoryNode.setSelected(true);
	    	this.selectedTaxonomyTree.getTreeState().collapseNode(this.selectedTaxonomyUITree, 
	    			this.selectedCategoryListRowKey);
	    	    	
	    	this.selectedCategory = null;
	    	this.selectedCategoryNode =null;
	    	this.selectedCategoryListRowKey =null;
	    	this.selectedCategorySeqId = -1;
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	
    	return null; // maintains AJAX happiness
    }
    
    public String cancelDelete() {
    	this.selectedCategory = null;
    	this.selectedCategoryNode =null;
    	this.selectedCategoryListRowKey =null;
    	this.selectedCategorySeqId = -1;
    	this.toDeleteCategoryGraph = null;
    	return null; // maintains AJAX happiness
    }
    
    public boolean categorySelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
    		HtmlTree tree = (HtmlTree)event.getSource();	
    		this.selectedTaxonomyUITree = tree;
    		this.selectedCategoryListRowKey = (ListRowKey)tree.getRowKey();
	        this.selectedCategoryNode = (DynamicTreeNodeModel)tree.getTreeNode(this.selectedCategoryListRowKey);
	        this.selectedCategory = new CategoryAdapter((org.cloudgraph.web.sdo.categorization.Category)selectedCategoryNode.getUserData());
	        this.toDeleteCategoryGraph = null;
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return false;
    }
	
	public CategoryAdapter getSelectedCategory() {
		return selectedCategory;
	}
    
	public TaxonomyTreeBean getSelectedTaxonomyTree() {
		return selectedTaxonomyTree;
	}

	public List<Taxonomy> getTaxonomies() {
		return this.beanFinder.findReferenceDataCache().getTaxonomies();
	}	
	
	public Taxonomy getSelectedTaxonomy() {
		return selectedTaxonomy;
	}
	
	public boolean getHasSelectedTaxonomy() {
		return this.selectedTaxonomy != null;
	}
	
	public void setSelectedTaxonomy(Taxonomy selected) {
		
        String name = selected.getCategory().getName();

        // query for the entire taxonomy
	    SDODataAccessClient service = new SDODataAccessClient();
	    DataGraph[] results = service.find(TaxonomyQuery.createQuery(
	    		name));
	    this.selectedTaxonomy = (Taxonomy)results[0].getRootObject();
        this.selectedTaxonomyTree = new TaxonomyTreeBean(this.selectedTaxonomy);
	
	    this.selectedCategory = null;
	    this.selectedCategoryListRowKey = null;
	    this.selectedCategoryNode = null;
	    this.toDeleteCategoryGraph = null;		
	}
}
