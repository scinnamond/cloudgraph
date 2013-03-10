package org.cloudgraph.web.model.administration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.cache.TaxonomyMapQuery;
import org.cloudgraph.web.model.taxonomy.DefaultDynamicTaxonomyTreeBean;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.sdo.adapter.CategoryLinkAdapter;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.CategoryLink;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.cloudgraph.web.sdo.categorization.TaxonomyMap;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.ListRowKey;

import commonj.sdo.DataGraph;

public class TaxonomyMapEditBean extends ModelBean{

	private static Log log = LogFactory.getLog(TaxonomyMapEditBean.class);
	private TreeNodeModel maps;
	private TaxonomyMap selectedTaxonomyMap;
	private CategoryLinkAdapter selectedCategoryLink;
	// this initialization hack makes RichFaces tree find it's tree state and
	// be happy and not blow up even though the tree is not yet displayed 
	private DefaultDynamicTaxonomyTreeBean leftTaxonomyTree = new DefaultDynamicTaxonomyTreeBean();
	private DefaultDynamicTaxonomyTreeBean rightTaxonomyTree = new DefaultDynamicTaxonomyTreeBean();
	
	public TaxonomyMapEditBean() {
		log.info("created TaxonomyMapEditBean");
	}
	
	public String getTitle() {
		if (this.selectedTaxonomyMap != null)
			return this.selectedTaxonomyMap.getName();
		else
			return "";
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
                log.debug(((PlasmaDataObject)getSelectedTaxonomyMap()).dump());
		    SDODataAccessClient service = new SDODataAccessClient();
	    	service.commit(selectedTaxonomyMap.getDataGraph(), 
		    	    beanFinder.findUserBean().getName());
            return AppActions.SAVE.value();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            errorHandler.setError(t);
            errorHandler.setRecoverable(false);
            return AppActions.ERRORHANDLER.value();
        } finally {
        }       
    }

    public String add() {
    	CategoryLink link = getSelectedTaxonomyMap().createCategoryLink();
    	this.selectedCategoryLink = new CategoryLinkAdapter(link);
    	return null; // maintains AJAX happyness
    }
    
    public String exit() {
    	selectedTaxonomyMap.getDataGraph().getChangeSummary().endLogging(); // wipe any changes 
    	selectedTaxonomyMap.getDataGraph().getChangeSummary().beginLogging();
    	return null;
    }

    public String export() {

    	try {   		
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
    
	public List<TaxonomyMap> getTaxonomyMaps() {

	    return this.beanFinder.findReferenceDataCache().getTaxonomyMaps();
	}

	public boolean getHasSelectedTaxonomyMap() {
		return getSelectedTaxonomyMap() != null;
	}
	
	public TaxonomyMap getSelectedTaxonomyMap() {
		return this.selectedTaxonomyMap;
	}
	
	public void setSelectedTaxonomyMap(TaxonomyMap selected) {
        try {			
	        log.info("setSelectedTaxonomyMap: " + selected.getName());
	
	        // query for the entire taxonomy map graph
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(TaxonomyMapQuery.createQuery(
		    		selected.getSeqId()));
		    this.selectedTaxonomyMap = (TaxonomyMap)results[0].getRootObject(); 
		    
		    
		    Taxonomy left = this.beanFinder.findReferenceDataCache().getTaxonomy(
		    		this.selectedTaxonomyMap.getLeft().getCategory().getName());
		    Taxonomy right = this.beanFinder.findReferenceDataCache().getTaxonomy(
		    		this.selectedTaxonomyMap.getRight().getCategory().getName());
		    this.leftTaxonomyTree = new DefaultDynamicTaxonomyTreeBean(left);
		    this.rightTaxonomyTree = new DefaultDynamicTaxonomyTreeBean(right);
			
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
	}
	
	public List<CategoryLinkAdapter> getMapElements() {
		List<CategoryLinkAdapter> result = new ArrayList<CategoryLinkAdapter>();
		TaxonomyMap map = getSelectedTaxonomyMap();
		if (map != null && map.getCategoryLink() != null)
		    for (CategoryLink link : map.getCategoryLink())
			    result.add(new CategoryLinkAdapter(link));	
		return result;
	}

	public CategoryLinkAdapter getSelectedCategoryLink() {
		return selectedCategoryLink;
	}

	public void setSelectedCategoryLink(CategoryLinkAdapter selectedCategoryLink) {
		this.selectedCategoryLink = selectedCategoryLink;
	}

	public DefaultDynamicTaxonomyTreeBean getLeftTaxonomyTree() {
		return leftTaxonomyTree;
	}

	public DefaultDynamicTaxonomyTreeBean getRightTaxonomyTree() {
		return rightTaxonomyTree;
	}

    public boolean leftCategorySelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	        Category cat = (Category)selectedNode.getUserData();
	        selectedCategoryLink.setLeft(cat);
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return false;
    }   

    public boolean rightCategorySelectListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel selectedNode = (TreeNodeModel)tree.getTreeNode(rowKey);
	        Category cat = (Category)selectedNode.getUserData();
	        selectedCategoryLink.setRight(cat);
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return false;
    }   
    
}
