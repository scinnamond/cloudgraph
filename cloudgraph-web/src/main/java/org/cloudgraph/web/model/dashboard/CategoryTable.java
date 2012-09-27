package org.cloudgraph.web.model.dashboard;

import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;

import commonj.sdo.DataObject;

public class CategoryTable extends Table implements RowFactory {

	public CategoryTable(ComponentName name, ComponentShape shape,
			Class<?> sdoClass, String[] propertyNames, 
			Dashboard dashboard,
			Container homeContainer) {
		super(name, shape, sdoClass, propertyNames, 
				dashboard, homeContainer);
	}
	
	public Row createRow(DataObject dataObject, Object[] data) {
		return new CategoryRowAdapter(dataObject, data);
	}

}
