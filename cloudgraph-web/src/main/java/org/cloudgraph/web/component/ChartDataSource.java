package org.cloudgraph.web.component;

import org.jfree.data.general.Dataset;

/**
 * Marker interface for chart data sources. 
 * @author scott
 */
public interface ChartDataSource {
    public Dataset getDataSet();
    public String getColorMap();
}
