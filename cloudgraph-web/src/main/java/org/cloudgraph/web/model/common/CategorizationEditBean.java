package org.cloudgraph.web.model.common;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.model.taxonomy.CategoryLabelFormat;
import org.cloudgraph.web.model.taxonomy.DynamicTaxonomyTreeBean;
import org.cloudgraph.web.model.taxonomy.TaxonomyTreeNodeType;
import org.cloudgraph.web.model.tree.TreeNodeModel;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.TaxonomyAdapter;
import org.cloudgraph.web.sdo.categorization.Category;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.ListRowKey;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;

public class CategorizationEditBean extends DynamicTaxonomyTreeBean {
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(CategorizationEditBean.class);

	protected DataObject target;
	protected TaxonomyAdapter taxonomy;
	protected CategorizationAdapter categorization;
	protected String saveActionReRender;
    protected Category selectedCategory;
        
    public CategorizationEditBean() {
		int foo = 0;
		foo++;
    }
    
    public DataObject getTarget() {
		return target;
	}

	public void setTarget(DataObject target) {
		this.target = target;
	}

	public TaxonomyAdapter getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(TaxonomyAdapter taxonomy) {
		this.taxonomy = taxonomy;
		TreeNodeTypeMap treeMap = new MyTreeNodeTypeMap();
		this.setTypeMap(treeMap);
		this.initTree(this.taxonomy.getTaxonomy());
		this.setLabelFormat(new CategoryLabelFormat() {
			public String getLabel(Category category) {
	        	String label = "(" + category.getId() + ") "
        	        + category.getName();
	        	return label;
			}				
		});
		
		this.selectedCategory = null; // force user to select
	}

	public String getSaveActionReRender() {
		return saveActionReRender;
	}

	public void setSaveActionReRender(String saveActionReRender) {
		this.saveActionReRender = saveActionReRender;
	}

	public CategorizationAdapter getCategorization() {
		return categorization;
	}

	public void setCategorization(CategorizationAdapter categorization) {
		this.categorization = categorization;
	}

	public String create() {
    	try {
    		// Initially create the adapter without
    		// the DataObject it is adapting to accommodate
    		// user cancel and close of modal dialog
    		// Only build the DataObject on demand during
    		// appropriate JSF lifecycle phase    		
    		this.categorization = 
    			CategorizationAdapter.newAdapter(this.target); 			
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return null;
    }
    
    public String edit() {
    	try {
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return null;
    }
    
    public String save() {
        try {
		    SDODataAccessClient service = new SDODataAccessClient();		    
		    service.commit(categorization.getDataGraph(), 
		    	beanFinder.findUserBean().getName());           
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }       	
    	return null;
    }
    
    public String cancel() {
    	try {
    		this.categorization = null;
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return null;
    }
    
    public String delete() {
    	try {
    		ChangeSummary changeSummary = categorization.getDataGraph().getChangeSummary();
    		if (changeSummary.isCreated(categorization.getRoot()))
    			((PlasmaDataObject)categorization.getRoot()).remove();
    		else {
    			// don't delete referenced category
    			categorization.getCategorization().unsetCategory();
    			
    			categorization.getRoot().delete(); 
    		}
    		    		
		    SDODataAccessClient service = new SDODataAccessClient();		    
		    service.commit(categorization.getRoot().getDataGraph(), 
		    	beanFinder.findUserBean().getName());           
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return null;
    } 

    public boolean categorySelectedListener(org.richfaces.event.NodeSelectedEvent event) {
    	try {
	    	HtmlTree tree = (HtmlTree)event.getSource();
	    	
	        ListRowKey rowKey = (ListRowKey)tree.getRowKey();
	        TreeNodeModel model = (TreeNodeModel)tree.getTreeNode(rowKey);
	        if (model.getUserData() instanceof Category) {
	        	this.selectedCategory = (Category)model.getUserData();
	        	Category copy = (Category)PlasmaCopyHelper.INSTANCE.copyShallow(this.selectedCategory);
	        	this.categorization.setCategory(copy);
	        }
	        else
	        	log.error("expected instance of Category not, " + 
	        			model.getUserData().getClass().getName());
    	}
    	catch (Throwable t) {
    		log.error(t.getMessage(), t);
    	}
    	return false;
    }
    
    public boolean getCategorySelected() {
    	return this.selectedCategory != null;
    }
 
    public String getSelectedCategoryName() {
    	if (this.selectedCategory != null)
    	    return this.selectedCategory.getName();
    	else
    		return WebConstants.DEFAULT_SELECTION;
    }

    public void validateSelectedCategoryName(FacesContext facesContext,
            UIComponent component, Object value) {

    	if (value == null || 
    		((String)value).trim().length() == 0 || 
    		WebConstants.DEFAULT_SELECTION.equals(value)) 
    	{
            String msg = "Please select a category";
            throw new ValidatorException(new FacesMessage(msg, msg));
    	}
	}    
    
	class MyTreeNodeTypeMap implements TreeNodeTypeMap {

		public String getTreeNodeType(int level) {
			switch (level)
			{
			default: return TaxonomyTreeNodeType.level_any.name(); 
			}
		}		
	}

}
