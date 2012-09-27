package org.cloudgraph.web.model.dashboard;

import commonj.sdo.DataObject;

public interface RowFactory {

	public Row createRow(DataObject dataObject, Object[] data);
}
