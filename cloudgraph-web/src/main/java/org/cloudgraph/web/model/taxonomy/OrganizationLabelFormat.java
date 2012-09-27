package org.cloudgraph.web.model.taxonomy;

import org.cloudgraph.web.sdo.core.Organization;

public interface OrganizationLabelFormat {
    public String getLabel(Organization organization);
}
