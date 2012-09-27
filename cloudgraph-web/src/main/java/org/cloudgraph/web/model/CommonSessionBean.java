package org.cloudgraph.web.model;

public class CommonSessionBean extends ModelBean {

    private boolean filterConfiguration = true;

    public String getAction() {
        return "";
    }
    
    public boolean isFilterConfiguration() {
        return filterConfiguration;
    }

    public void setFilterConfiguration(boolean filterConfiguration) {
        this.filterConfiguration = filterConfiguration;
    }
}
