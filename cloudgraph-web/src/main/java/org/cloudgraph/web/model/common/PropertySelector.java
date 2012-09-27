package org.cloudgraph.web.model.common;

import org.cloudgraph.web.sdo.meta.Property;

public interface PropertySelector {
    public boolean isSelected(Property property);
}
