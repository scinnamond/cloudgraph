package org.cloudgraph.web.component;

import java.io.Serializable;

/**
 * Holds the chart information to be rendered
 */
public class ChartData implements Serializable {

	private ChartDataSource datasource;

	private int width;

	private int height;

	private int alpha;

	private int depth;

	private int startAngle;

	private int rows;
	
	private String title;

	private String type;

	private String background;
	
	private String foreground;

	private String xlabel;

	private String ylabel;

	private String orientation;

	private String colors;

	private boolean chart3d;

	private boolean legend;

	private boolean antialias;

	private boolean outline;
	
	private String output;

    private Object tooltipgenerator;

    private Object urlgenerator;
    
	public ChartData() {}

	public ChartData(ChartComponent chart) {
		this.datasource = (ChartDataSource)chart.getDatasource();
		this.width = chart.getWidth();
		this.height = chart.getHeight();
		this.rows = chart.getRows();
		this.alpha = chart.getAlpha();
		this.depth = chart.getDepth();
		this.startAngle = chart.getStartAngle();
		this.title = chart.getTitle();
		this.type = chart.getType();
		this.background = chart.getBackground();
		this.foreground = chart.getForeground();
		this.xlabel = chart.getXlabel();
		this.ylabel = chart.getYlabel();
		this.orientation = chart.getOrientation();
		this.colors = chart.getColors();
		this.chart3d = chart.getIs3d();
		this.legend = chart.getLegend();
		this.antialias = chart.getAntialias();
		this.outline = chart.getOutline();
		this.output = chart.getOutput();
        this.tooltipgenerator = chart.getTooltipgenerator();
        this.urlgenerator = chart.getUrlgenerator();
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public boolean isAntialias() {
		return antialias;
	}

	public void setAntialias(boolean antialias) {
		this.antialias = antialias;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}
	
	public String getForeground() {
		return foreground;
	}

	public void setForeground(String foreground) {
		this.foreground = foreground;
	}

	public boolean isChart3d() {
		return chart3d;
	}

	public void setChart3d(boolean chart3d) {
		this.chart3d = chart3d;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	public Object getDatasource() {
		return datasource;
	}

	public void setDatasource(ChartDataSource datasource) {
		this.datasource = datasource;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isLegend() {
		return legend;
	}

	public void setLegend(boolean legend) {
		this.legend = legend;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean isOutline() {
		return outline;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}

	public int getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(int startAngle) {
		this.startAngle = startAngle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getXlabel() {
		return xlabel;
	}

	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}

	public String getYlabel() {
		return ylabel;
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

    public Object getTooltipgenerator() {
        return tooltipgenerator;
    }

    public void setTooltipgenerator(Object tooltipgenerator) {
        this.tooltipgenerator = tooltipgenerator;
    }

    public Object getUrlgenerator() {
        return urlgenerator;
    }

    public void setUrlgenerator(Object urlgenerator) {
        this.urlgenerator = urlgenerator;
    }

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
}