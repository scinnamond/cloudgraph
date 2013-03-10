package org.cloudgraph.web.sdo.visitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyCategorizationAdapter;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.plasma.sdo.PlasmaDataGraphVisitor;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class PropertyCategorizationCollector extends CategorizationCollector 
    implements PlasmaDataGraphVisitor 
{
    private static Log log = LogFactory.getLog(PropertyCategorizationCollector.class);
	
	
	public PropertyCategorizationCollector(Type type, ReferenceDataCache cache) {
		super(type, cache);
	}
	
	protected CategorizationAdapter newAdapter(DataObject dataObject, 
			Category cat) {
		PropertyCategorizationAdapter adapter = 
    		new PropertyCategorizationAdapter((PropertyCategorization)dataObject, cat);
		return adapter;
	}
	
	protected Category getCategory(DataObject target) {
		PropertyCategorization pcat = (PropertyCategorization)target;
		if (pcat.getCategorization() == null || 
				pcat.getCategorization().getCategory() == null) {
			log.warn("ignoring incomplete categorization");
			return null;
		}
		return pcat.getCategorization().getCategory();
	}


}
