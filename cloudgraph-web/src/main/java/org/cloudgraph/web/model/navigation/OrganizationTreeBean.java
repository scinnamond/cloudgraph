package org.cloudgraph.web.model.navigation;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.taxonomy.OrganizationLabelFormat;
import org.cloudgraph.web.model.taxonomy.TaxonomyTreeNodeType;
import org.cloudgraph.web.model.tree.TreeNodeTypeMap;

import org.cloudgraph.web.sdo.core.Organization;

public class OrganizationTreeBean extends org.cloudgraph.web.model.taxonomy.DynamicOrganizationTreeBean {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(OrganizationTreeBean.class);
	
	public OrganizationTreeBean() {
		try {						
			List<Organization> model = beanFinder.findReferenceDataCache().getDeputyAreas();
			super.typeMap = new OrganizationTreeNodeTypeMap();
			OrganizationLabelFormat labelFormat = new OrganizationLabelFormat() {
				public String getLabel(Organization organization) {
					return organization.getCode();
				}				
			};
			this.setLabelFormat(labelFormat);
			
			initTree(model);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}
	
	class OrganizationTreeNodeTypeMap implements TreeNodeTypeMap {

		/**
		 */
		public String getTreeNodeType(int level) {
			switch (level)
			{
			default: return TaxonomyTreeNodeType.level_any.name(); 
			}
		}		
	}
	
 }


