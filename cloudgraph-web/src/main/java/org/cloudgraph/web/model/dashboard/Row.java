package org.cloudgraph.web.model.dashboard;

import commonj.sdo.DataObject;

public interface Row {
    public Object[] getData();
    public DataObject getDataObject();
    public String getType();
}
