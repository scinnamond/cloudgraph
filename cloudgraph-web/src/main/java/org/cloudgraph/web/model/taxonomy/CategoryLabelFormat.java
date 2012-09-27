package org.cloudgraph.web.model.taxonomy;

import org.cloudgraph.web.sdo.categorization.Category;

public interface CategoryLabelFormat {
    public String getLabel(Category category);
}
