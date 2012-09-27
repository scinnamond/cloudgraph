package org.cloudgraph.web.component;

import org.jfree.data.general.Dataset;

public interface PagedDataSource extends ChartDataSource{
    public int getRowCount();
    public void setFirst(int first);
    public void setRows(int rows);
    public Dataset getDataSet(int startIndex, int endIndex);
}
