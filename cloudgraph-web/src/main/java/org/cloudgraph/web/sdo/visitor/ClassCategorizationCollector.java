package org.cloudgraph.web.sdo.visitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.sdo.adapter.CategorizationAdapter;
import org.cloudgraph.web.sdo.adapter.ClassCategorizationAdapter;
import org.plasma.sdo.PlasmaDataGraphVisitor;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.ClassCategorization;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class ClassCategorizationCollector extends CategorizationCollector 
    implements PlasmaDataGraphVisitor 
{
    private static Log log = LogFactory.getLog(ClassCategorizationCollector.class);
	
	
	public ClassCategorizationCollector(Type type, ReferenceDataCache cache) {
		super(type, cache);
	}
	
	protected CategorizationAdapter newAdapter(DataObject dataObject, 
			Category cat) {
		ClassCategorizationAdapter adapter = 
    		new ClassCategorizationAdapter((ClassCategorization)dataObject, cat);
		return adapter;
	}
	
	protected Category getCategory(DataObject target) {
		ClassCategorization pcat = (ClassCategorization)target;
		if (pcat.getCategorization() == null || 
				pcat.getCategorization().getCategory() == null) {
			log.warn("ignoring incomplete categorization");
			return null;
		}
		return pcat.getCategorization().getCategory();
	}


}
