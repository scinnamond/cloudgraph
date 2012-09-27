package org.cloudgraph.web.component;

public class ChartValue {

    private double value;
    private Comparable rowKey;
    private Comparable columnKey;
    public ChartValue(double value, Comparable rowKey, Comparable columnKey) {
        super();
        this.value = value;
        this.rowKey = rowKey;
        this.columnKey = columnKey;
    }
    public Comparable getColumnKey() {
        return columnKey;
    }
    public void setColumnKey(Comparable columnKey) {
        this.columnKey = columnKey;
    }
    public Comparable getRowKey() {
        return rowKey;
    }
    public void setRowKey(Comparable rowKey) {
        this.rowKey = rowKey;
    }
    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    
    
}
