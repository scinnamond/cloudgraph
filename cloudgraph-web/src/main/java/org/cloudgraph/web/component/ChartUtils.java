package org.cloudgraph.web.component;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 */
public class ChartUtils {

    private static Log log =LogFactory.getLog(ChartUtils.class);
    
	private static String passthruImgAttributes[] = {
		"alt",
		"styleClass",
		"onclick",
		"ondblclick",
		"onmousedown",
		"onmouseup",
		"onmouseover",
		"onmousemove",
		"onmouseout",
		"onkeypress",
		"onkeydown",
		"onkeyup",
		"usemap",
    };
	
    // MyFaces html.HtmlResponseWriterImpl will not write an 'img' tag with
    // content nested in it. So wew gotta bypass using write()
	public static void renderPassThruImgAttributes(ResponseWriter writer, UIComponent component) throws IOException{
		for(int i = 0 ; i < passthruImgAttributes.length ; i++) {
			Object value = component.getAttributes().get(passthruImgAttributes[i]);
			if(value != null) {
				writer.write(passthruImgAttributes[i] + "=\"" + value + "\"");
			}
		}
		//title attribute overlaps with the chart title so renamed to imgTitle to define img tag's title  
		if(component.getAttributes().get("imgTitle") != null) 
			writer.write("title=\"" + component.getAttributes().get("imgTitle") + "\"");
	}

	public static PlotOrientation getPlotOrientation(String orientation) {
		if (orientation.equalsIgnoreCase("horizontal")) {
			return PlotOrientation.HORIZONTAL;
		} else if (orientation.equalsIgnoreCase("vertical")){
			return PlotOrientation.VERTICAL;
		} else {
			throw new RuntimeException("Unsupported plot orientation:" + orientation);
		}
	}
	
	public static Color getColor(String color) {
		// HTML colors (#FFFFFF format)
		if (color.startsWith("#")) {
			return new Color(Integer.parseInt(color.substring(1), 16));
		} else {
			// Colors by name
			if (color.equalsIgnoreCase("black"))
				return Color.black;
			if (color.equalsIgnoreCase("grey"))
				return Color.gray;
			if (color.equalsIgnoreCase("yellow"))
				return Color.yellow;
			if (color.equalsIgnoreCase("green"))
				return Color.green;
			if (color.equalsIgnoreCase("blue"))
				return Color.blue;
			if (color.equalsIgnoreCase("red"))
				return Color.red;
			if (color.equalsIgnoreCase("orange"))
				return Color.orange;
			if (color.equalsIgnoreCase("cyan"))
				return Color.cyan;
			if (color.equalsIgnoreCase("magenta"))
				return Color.magenta;
			if (color.equalsIgnoreCase("darkgray"))
				return Color.darkGray;
			if (color.equalsIgnoreCase("lightgray"))
				return Color.lightGray;
			if (color.equalsIgnoreCase("pink"))
				return Color.pink;
			if (color.equalsIgnoreCase("white"))
				return Color.white;
			
			throw new RuntimeException("Unsupported chart color:" + color);
		}
	}
	
	public static String resolveContentType(String output) {
		if(output.equalsIgnoreCase("png"))
			return "img/png";
		else if(output.equalsIgnoreCase("jpeg"))
			return "img/jpeg";
		else
			throw new RuntimeException("Unsupported output format:" + output);
	}
	
	//	Creates the chart with the given chart data
	public static JFreeChart createChartWithType(ChartData chartData) {
		JFreeChart chart = null;
		Object datasource = chartData.getDatasource();
		if (datasource instanceof PieDataset) {
			chart = createChartWithPieDataSet(chartData, (PieDataset)datasource);
		} else if (datasource instanceof CategoryDataset) {
			chart = createChartWithCategoryDataSet(chartData, (CategoryDataset)datasource);
		} else if (datasource instanceof XYDataset) {
			chart = createChartWithXYDataSet(chartData, (XYDataset)datasource);
		} else {
			throw new RuntimeException("Unsupported dataset type, " + datasource.getClass().getName());
		}
		return chart;
	}

    //  Creates the chart with the given chart data
    public static JFreeChart createChartWithType(ChartData chartData, Dataset dataset) {
        JFreeChart chart = null;
        if (dataset instanceof PieDataset) {
            chart = createChartWithPieDataSet(chartData, (PieDataset)dataset);
        } else if (dataset instanceof CategoryDataset) {
            chart = createChartWithCategoryDataSet(chartData, (CategoryDataset)dataset);
        } else if (dataset instanceof XYDataset) {
            chart = createChartWithXYDataSet(chartData, (XYDataset)dataset);
        } else {
            throw new RuntimeException("Unsupported dataset type, " + dataset.getClass().getName());
        }
        return chart;
    }
    
	public static void setGeneralChartProperties(JFreeChart chart, ChartData chartData) {
		chart.setBackgroundPaint(ChartUtils.getColor(chartData.getBackground()));
		chart.getPlot().setBackgroundPaint(ChartUtils.getColor(chartData.getForeground()));
		chart.setTitle(chartData.getTitle());
		chart.setAntiAlias(chartData.isAntialias());
		TextTitle textTitle = new TextTitle();
		textTitle.setText(chartData.getTitle());
		Font font = new Font("Verdana", Font.BOLD, 14); // FIXME: mo' tag attributes ? 
		textTitle.setFont(font);
		chart.setTitle(textTitle);

		// Alpha transparency (100% means opaque)
		if (chartData.getAlpha() < 100) {
			chart.getPlot().setForegroundAlpha((float) chartData.getAlpha() / 100);
		}
	}

	public static JFreeChart createChartWithCategoryDataSet(ChartData chartData, CategoryDataset dataset) {
		JFreeChart chart = null;
		PlotOrientation plotOrientation = ChartUtils.getPlotOrientation(chartData.getOrientation());
		String type = chartData.getType();
		String xAxis = chartData.getXlabel();
		String yAxis = chartData.getYlabel();
		boolean is3d = chartData.isChart3d();
		boolean legend = chartData.isLegend();

		if (type.equalsIgnoreCase("bar")) {
			if (is3d == true) {
				chart = ChartFactory.createBarChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, true);
			} else {
				chart = ChartFactory.createBarChart("", xAxis, yAxis, dataset, plotOrientation, legend, true, true);
			}
			setBarOutline(chart, chartData);
		} else if (type.equalsIgnoreCase("stackedbar")) {
			if (is3d == true) {
				chart = ChartFactory.createStackedBarChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			} else {
				chart = ChartFactory.createStackedBarChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			}
			setBarOutline(chart, chartData);
		} else if (type.equalsIgnoreCase("line")) {
			if (is3d == true)
				chart = ChartFactory.createLineChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			else
				chart = ChartFactory.createLineChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("area")) {
			chart = ChartFactory.createAreaChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("stackedarea")) {
			chart = ChartFactory.createStackedAreaChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("waterfall")) {
			chart = ChartFactory.createWaterfallChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("gantt")) {
			chart = ChartFactory.createGanttChart("", xAxis, yAxis,(IntervalCategoryDataset) dataset, legend, true, false);
		}
		setCategorySeriesColors(chart, chartData);
		return chart;
	}

	public static JFreeChart createChartWithPieDataSet(ChartData chartData, PieDataset dataset) {
		String type = chartData.getType();
		boolean legend = chartData.isLegend();
		JFreeChart chart = null;

		if (type.equalsIgnoreCase("pie")) {
			if (chartData.isChart3d()) {
				chart = ChartFactory.createPieChart3D("", dataset, legend,true, false);
				PiePlot3D plot = (PiePlot3D) chart.getPlot();
				plot.setDepthFactor((float) chartData.getDepth() / 100);
			} else {
				chart = ChartFactory.createPieChart("", dataset, legend, true,false);
			}
		} else if (type.equalsIgnoreCase("ring")) {
			chart = ChartFactory.createRingChart("", dataset, legend, true,false);
		}
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle((float) chartData.getStartAngle());
		setPieSectionColors(chart, chartData);
		
		return chart;
	}

	public static JFreeChart createChartWithXYDataSet(ChartData chartData, XYDataset dataset) {
		String type = chartData.getType();
		String xAxis = chartData.getXlabel();
		String yAxis = chartData.getYlabel();
		boolean legend = chartData.isLegend();

		JFreeChart chart = null;
		PlotOrientation plotOrientation = ChartUtils.getPlotOrientation(chartData.getOrientation());

		if (type.equalsIgnoreCase("bar")) {
			if (!(dataset instanceof IntervalXYDataset))
				throw new RuntimeException("expected instance of IntervalXYDataset");
			chart = ChartFactory.createXYBarChart("", xAxis, false, yAxis, (IntervalXYDataset)dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("timeseries")) {
			chart = ChartFactory.createTimeSeriesChart("", xAxis, yAxis,dataset, legend, true, false);
		} else if (type.equalsIgnoreCase("xyline")) {
			chart = ChartFactory.createXYLineChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("polar")) {
			chart = ChartFactory.createPolarChart("", dataset, legend, true,false);
		} else if (type.equalsIgnoreCase("scatter")) {
			chart = ChartFactory.createScatterPlot("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xyarea")) {
			chart = ChartFactory.createXYAreaChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xysteparea")) {
			chart = ChartFactory.createXYStepAreaChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xystep")) {
			chart = ChartFactory.createXYStepChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("bubble")) {
			chart = ChartFactory.createBubbleChart("", xAxis, yAxis,(XYZDataset) dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("candlestick")) {
			chart = ChartFactory.createCandlestickChart("", xAxis, yAxis,(OHLCDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("boxandwhisker")) {
			chart = ChartFactory.createBoxAndWhiskerChart("", xAxis, yAxis,(BoxAndWhiskerXYDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("highlow")) {
			chart = ChartFactory.createHighLowChart("", xAxis, yAxis,(OHLCDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("histogram")) {
			chart = ChartFactory.createHistogram("", xAxis, yAxis,(IntervalXYDataset) dataset, plotOrientation, legend, true,false);
		//} else if (type.equalsIgnoreCase("signal")) {
		//	chart = ChartFactory.createSignalChart("", xAxis, yAxis,(SignalsDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("wind")) {
			chart = ChartFactory.createWindPlot("", xAxis, yAxis,(WindDataset) dataset, legend, true, false);
		}
		setXYSeriesColors(chart, chartData);
		return chart;
	}
	
	/**
	 * Series coloring
	 * Plot has no getRenderer so two methods for each plot type(categoryplot and xyplot)
	 */
	public static void setCategorySeriesColors(JFreeChart chart, ChartData chartData) {
        //KeyedValues2D dataValues = (KeyedValues2D)chartData.getDatasource();
		if(chart.getPlot() instanceof CategoryPlot) {
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			if (chartData.getColors() != null) {
                ChartDataSource dataSource = (ChartDataSource)chartData.getDatasource();
                List seriesKeys = ((CategoryDataset)dataSource.getDataSet()).getRowKeys();
                
				String[] colors = chartData.getColors().split(",");
				for (int i = 0; i < colors.length; i++) {
                    // if it's a color mapping of a series name to a color
                    int idx = colors[i].indexOf(":");
                    if (idx >= 0)
                    {
                        String seriesKey = colors[i].substring(0, idx).trim();
                        String color = colors[i].substring(idx+1).trim();
                        // find the series
                        for (int j = 0; j < seriesKeys.size(); j++)
                            if (seriesKeys.get(j).equals(seriesKey))
                                 plot.getRenderer().setSeriesPaint(j, ChartUtils.getColor(color));        
                    }
                    else // it's a static color set which assumes the number of colors matches number of series
					    plot.getRenderer().setSeriesPaint(i, ChartUtils.getColor(colors[i].trim()));
				}
			}
		}
	}
	
	public static void setXYSeriesColors(JFreeChart chart, ChartData chartData) {
		if(chart.getPlot() instanceof XYPlot && chartData.getColors() != null) {
				XYPlot plot = (XYPlot) chart.getPlot();
				String[] colors = chartData.getColors().split(",");
				for (int i = 0; i < colors.length; i++) {
				plot.getRenderer().setSeriesPaint(i, ChartUtils.getColor(colors[i].trim()));
			}
		}
	}
	
	public static void setPieSectionColors(JFreeChart chart, ChartData chartData) {
		if(chart.getPlot() instanceof PiePlot && chartData.getColors() != null) {
			String[] colors = chartData.getColors().split(",");
            ChartDataSource dataSource = (ChartDataSource)chartData.getDatasource();
            PieDataset dataSet = (PieDataset)dataSource.getDataSet();
			for (int i = 0; i < colors.length; i++) {
                // if it's a color mapping of a series name to a color
                int idx = colors[i].indexOf(":");
                if (idx >= 0)
                {
                    String key = colors[i].substring(0, idx).trim();
                    String color = colors[i].substring(idx+1).trim();
                    // find the series
                    int keyIndex = dataSet.getIndex(key);
                    if (keyIndex >= 0)
                        ((PiePlot)chart.getPlot()).setSectionPaint(keyIndex, ChartUtils.getColor(color));
                }
                else // it's a static color set which assumes the number of colors matches number of series                
				    ((PiePlot)chart.getPlot()).setSectionPaint(i, ChartUtils.getColor(colors[i].trim()));
			}
		}
	}
	
	/**
	 * Sets the outline of the bars
	 */
	public static void setBarOutline(JFreeChart chart, ChartData chartData) {
		CategoryPlot plot = (CategoryPlot) chart.getPlot();			
		BarRenderer barrenderer = (BarRenderer) plot.getRenderer();
		barrenderer.setDrawBarOutline(chartData.isOutline());
	}
}
