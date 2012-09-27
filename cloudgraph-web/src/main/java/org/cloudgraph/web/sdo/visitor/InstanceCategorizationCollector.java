package org.cloudgraph.web.sdo.visitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.ClassCategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.InstanceCategorizationAdapter;
import org.plasma.sdo.PlasmaDataGraphVisitor;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.ClassCategorization;
import org.cloudgraph.web.sdo.core.InstanceCategorization;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class InstanceCategorizationCollector extends CategorizationCollector 
    implements PlasmaDataGraphVisitor 
{
    private static Log log = LogFactory.getLog(InstanceCategorizationCollector.class);
	
	
	public InstanceCategorizationCollector(Type type, ReferenceDataCache cache) {
		super(type, cache);
	}
	
	protected CategorizationAdapter newAdapter(DataObject dataObject, 
			Category cat) {
		InstanceCategorizationAdapter adapter = 
    		new InstanceCategorizationAdapter((InstanceCategorization)dataObject, cat);
		return adapter;
	}
	
	protected Category getCategory(DataObject target) {
		InstanceCategorization pcat = (InstanceCategorization)target;
		if (pcat.getCategorization() == null || 
				pcat.getCategorization().getCategory() == null) {
			log.warn("ignoring incomplete categorization");
			return null;
		}
		return pcat.getCategorization().getCategory();
	}


}
