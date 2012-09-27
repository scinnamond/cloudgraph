package org.cloudgraph.web.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.StringUtils;

/**
 */
public class ChartComponent extends UIData {

    private static Log log =LogFactory.getLog(ChartComponent.class);

    public static final String COMPONENT_TYPE = "org.cloudgraph.web.component.ChartComponent";

    public static final String COMPONENT_FAMILY = "org.cloudgraph.web.component";

    private Integer width;

    private Integer height;

    private Integer alpha;

    private Integer depth;

    private Integer startAngle;
    
    private Integer rows;

    private String title;

    private String type;

    private String background;

    private String foreground;

    private String xlabel;

    private String ylabel;

    private String orientation;

    private String colors;

    private Boolean is3d;
    
    private Boolean isLogarithmic;

    private Boolean legend;

    private Boolean antialias;

    private Boolean outline;

    private String styleClass;

    private String alt;

    private String imgTitle;

    private String onclick;

    private String ondblclick;

    private String onmousedown;

    private String onmouseup;

    private String onmouseover;

    private String onmousemove;

    private String onmouseout;

    private String onkeypress;

    private String onkeydown;

    private String onkeyup;

    private String output;

    private String usemap;

    private Object datasource;

    private Object tooltipgenerator = null;

    private Object urlgenerator = null;

    private int first = 0;

    private int rowCount = -1;

    private int rowIndex = -1;
    
    public ChartComponent() {
        super();
        setRendererType(null);
    }

    /**
     * Return the zero-relative row number of the first row to be displayed.
     * 
     * @see javax.faces.component.UIData#getFirst()
     */
    public int getFirst() {
        return first;
    }

    /**
     * Return the number of rows in the underlying data model.
     */
    public int getRowCount() {
    	
        if (this.rowCount < 0) {
			if (log.isDebugEnabled()) {
				log.debug("getRowCount(): " + this.getId());
			}        	
			//RuntimeException re = new RuntimeException("stack");
        	//log.error(re.getMessage(), re);
            ChartData chartData = new ChartData(this);
            ChartDataSource dataSource = (ChartDataSource) chartData.getDatasource();
            if (dataSource instanceof PagedDataSource)
                this.rowCount = ((PagedDataSource) dataSource).getRowCount();
            else
                this.rowCount = 0;
        }
        return this.rowCount;
    }

    /**
     * Return the data object representing the data for the currently selected
     * row index, if any.
     */
    public java.lang.Object getRowData() {
        return "data"; // FIXME: HACK
    }

    /**
     * Return the zero-relative index of the currently selected row.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Return the value of the UIData.
     */
    public java.lang.Object getValue() {
        return "data"; // FIXME: HACK
    }

    /**
     * Set the zero-relative row number of the first row to be displayed.
     */
    public void setFirst(int first) {
		if (log.isDebugEnabled()) {
			log.debug("setFirst: " + this + ": " + String.valueOf(first));
		}        
		//RuntimeException e = new RuntimeException();
        //e.printStackTrace();
        ChartDataSource ds = (ChartDataSource)this.getDatasource(); // use 'getter' so we pull from the tag value-binding
        if (ds instanceof PagedDataSource)
        {
        	PagedDataSource pds = (PagedDataSource)ds;
        	pds.setFirst(first);
        	pds.setRows(this.getRows()); // use 'getter' so we pull from the tag value-binding
        }
        this.first = first;
    }

    /**
     * Set the zero relative index of the current row, or -1 to indicate that no
     * row is currently selected, by implementing the following algorithm.
     */
    public void setRowIndex(int rowIndex) {
        //log.debug("setRowIndex: " + String.valueOf(rowIndex));
    	this.rowIndex = rowIndex;    	
    }

    /**
     * Set the value of the UIData.
     */
    public void setValue(java.lang.Object value) {
        //log.debug("setValue: " + String.valueOf(value));
    }
    
    private ChartManagerComponent findChartManager()
    {
    	UIComponent parent = this.getParent();
		if (parent == null)
			throw new RuntimeException("could not find chart manager component");
    	while (!(parent instanceof ChartManagerComponent))
    	{	
    		parent = parent.getParent();
    		if (parent == null)
    			throw new RuntimeException("could not find chart manager component");
    	}
    	return (ChartManagerComponent)parent;
    }

    public void encodeBegin(FacesContext context) 
        throws IOException 
    {
        try {
        	processEncodeBegin(context);
        }
        catch (Throwable t) {
        	log.error(t.getMessage(), t);
        }
    }
    
    private void processEncodeBegin(FacesContext context) throws IOException {
            	
		if (log.isDebugEnabled()) {
			log.debug("encodeBegin(): " + this);
		}    	
		ChartData chartData = new ChartData(this);
        ChartDataSource ds = (ChartDataSource) chartData.getDatasource();
        Dataset dataset = null;
        if (ds instanceof PagedDataSource)
        {   
        	PagedDataSource pds = (PagedDataSource)ds;
        	//pds.setFirst(this.getFirst()); // use 'getter' so we pull from the tag value-binding
        	pds.setRows(this.getRows()); // use 'getter' so we pull from the tag value-binding  
            //dataset = ((PagedDataSource) dataSource).getDataSet(getFirst(), 
            //    getFirst() + getRows());
        }
        //else
        
        dataset = ds.getDataSet();

        JFreeChart chart = null;
        if (dataset != null)
            chart = ChartUtils.createChartWithType(chartData, dataset);
        else
            chart = ChartUtils.createChartWithType(chartData);
        ChartUtils.setGeneralChartProperties(chart, chartData);
        
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);
        //log.debug("chart type: " + this.type);

        Object urlGenObject = this.getUrlgenerator();
        Plot plot = chart.getPlot();
        
        if (plot instanceof CategoryPlot) {
            CategoryPlot catplot = ((CategoryPlot) plot);
            //org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) catplot
            //    .getRangeAxis();
            //rangeAxis.setLowerBound(0D);
            // rangeAxis.setUpperBound(300);
            // rangeAxis.centerRange(25D);
            //rangeAxis.setRangeType(org.jfree.data.RangeType.POSITIVE);
            // rangeAxis.setAutoRangeStickyZero(true);
            
            PlotOrientation orientation = PlotOrientation.HORIZONTAL;
            String orientationProperty = this.getOrientation();
            if (orientationProperty != null && orientationProperty.length() > 0)
                orientation = ChartUtils.getPlotOrientation(orientationProperty);
            catplot.setOrientation(orientation);

            if (this.getIsLogarithmic()) {
                LogarithmicAxis rangeAxis = new LogarithmicAxis(this.getYlabel());
                rangeAxis.setStrictValuesFlag(false);
                rangeAxis.setAllowNegativesFlag(true);
                catplot.setRangeAxis(rangeAxis);
            }
            else {
                NumberAxis rangeAxis = new NumberAxis(this.getYlabel());
                rangeAxis.setAutoRangeIncludesZero(false);            
                catplot.setRangeAxis(rangeAxis);
            }
            
            //CategoryAxis domainAxis = catplot.getDomainAxis();
            //domainAxis.setCategoryLabelPositions(
            //		CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2)); 
            //domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
            //domainAxis.setCategoryMargin(0);
            

            if (urlGenObject != null) {
                CategoryURLGenerator urlGen = null;
                if (!(urlGenObject instanceof CategoryURLGenerator)) {
                    throw new IllegalArgumentException("expected instance of " + CategoryURLGenerator.class.getName()
                        + " for attribute 'urlgenerator' not '" + urlGenObject.getClass().getName()
                        + " - using default generator");
                }
                else
                    urlGen = (CategoryURLGenerator)urlGenObject;
                CategoryItemRenderer renderer = catplot.getRenderer();
                renderer.setBaseItemURLGenerator(urlGen);
            }
        }
        else if (plot instanceof XYPlot) {
        	XYPlot xyplot = ((XYPlot) plot);
            PlotOrientation orientation = PlotOrientation.HORIZONTAL;
            String orientationProperty = this.getOrientation();
            if (orientationProperty != null && orientationProperty.length() > 0)
                orientation = ChartUtils.getPlotOrientation(orientationProperty);
            xyplot.setOrientation(orientation);

            if (this.getIsLogarithmic()) {
                LogarithmicAxis rangeAxis = new LogarithmicAxis(this.getYlabel());
                rangeAxis.setStrictValuesFlag(false);
                rangeAxis.setAllowNegativesFlag(true);
                xyplot.setRangeAxis(rangeAxis);
            }
            else {
                NumberAxis rangeAxis = new NumberAxis(this.getYlabel());
                rangeAxis.setAutoRangeIncludesZero(false);            
                xyplot.setRangeAxis(rangeAxis);
            }           
            
            ValueAxis domainAxis = xyplot.getDomainAxis();
            domainAxis.setLabelAngle(90);
        }
        else if (plot instanceof PiePlot) {
            PiePlot pieplot = ((PiePlot) plot);
            if (urlGenObject != null) {
                PieURLGenerator urlGen = null;
                if (!(urlGenObject instanceof PieURLGenerator)) {
                    throw new IllegalArgumentException("expected instance of " + PieURLGenerator.class.getName()
                        + " for attribute 'urlgenerator' not '" + urlGenObject.getClass().getName()
                        + " - using default generator");
                }
                else
                    urlGen = (PieURLGenerator)urlGenObject;
                pieplot.setURLGenerator(urlGen);
            }
        }
        // chart.setBorderVisible(false);
        // chart.setBorderPaint(java.awt.Color.GREEN);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ChartRenderingInfo renderingInfo = writeChart(stream, chart, chartData);
        ChartImage imageData = new ChartImage(stream.toByteArray());
        // save it on session as this subsequent request sent as a result of
        // the 'img' tag rendering will not be a faces request.
        String clientId = getClientId(context);
        Map session = context.getExternalContext().getSessionMap();
        session.put(clientId, imageData);

        ResponseWriter writer = context.getResponseWriter();
        // MyFaces html.HtmlResponseWriterImpl will not write an 'img' tag with
        // content nested in it. So wew gotta bypass using write()
        writer.write("<img ");
        writer.write("id=\"" + getClientId(context) + "\" ");
        writer.write("width=\"" + String.valueOf(getWidth()) + "\" ");
        writer.write("height=\"" + String.valueOf(getHeight()) + "\" ");
        writer.write("width=\"" + String.valueOf(getWidth()) + "\" ");
        writer.write("height=\"" + String.valueOf(getHeight()) + "\" ");
        writer.write("src=\"" + ChartListener.CHART_REQUEST + ".faces?ts="
            + System.currentTimeMillis() + "&id=" + getClientId(context) + "\" ");

        if (this.getUsemap() != null && this.getUsemap().length() > 0) {
            ToolTipTagFragmentGenerator tooltipGen = new StandardToolTipTagFragmentGenerator();
            URLTagFragmentGenerator urlGen = null;

            // if we want URLS, just use the standard fragment generator
            // this basically just put's the url-generator args in a href
            // attribute with correct RUL encoding, etc..
            if (urlGenObject != null) { 
                urlGen = new StandardURLTagFragmentGenerator();
            }

            writer.write("usemap=\"#" + this.getUsemap() + "\"");
            ChartUtils.renderPassThruImgAttributes(writer, this);
            writer.write(">");
            
            //String map = ImageMapUtilities.getImageMap(this.usemap, renderingInfo, tooltipGen,
            //    urlGen);
            String map = getImageMap(this.getUsemap(), renderingInfo, tooltipGen, urlGen);
            writer.write(map);
            
            //renderingInfo.
            //ImageMapUtilities.
        } else {
            ChartUtils.renderPassThruImgAttributes(writer, this);
            writer.write(">");
        }

        writer.write("</img>"); // see html.HtmlResponseWriterImpl comment above

    }

    /**
     * Creates an image map element that complies with the XHTML 1.0
     * specification.
     *
     * @param name  the map name (<code>null</code> not permitted).
     * @param info  the chart rendering info (<code>null</code> not permitted).
     * @param toolTipTagFragmentGenerator  the tool tip generator.
     * @param urlTagFragmentGenerator  the url generator.
     *
     * @return The map tag.
     */
    private String getImageMap(String name, ChartRenderingInfo info,
            ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
            URLTagFragmentGenerator urlTagFragmentGenerator) {

        StringBuffer sb = new StringBuffer();
        sb.append("<map id=\"" + name + "\" name=\"" + name + "\">");
        sb.append(StringUtils.getLineSeparator());
        EntityCollection entities = info.getEntityCollection();
        if (entities != null) {
            int count = entities.getEntityCount();
            for (int i = count - 1; i >= 0; i--) {
                ChartEntity entity = entities.getEntity(i);
                if (entity.getToolTipText() != null 
                        || entity.getURLText() != null) {
                    String area = getImageMapAreaTag(entity,
                        toolTipTagFragmentGenerator, urlTagFragmentGenerator
                    );
                    if (area.length() > 0) {
                        sb.append(area);
                        sb.append(StringUtils.getLineSeparator());
                    }
                }
            }
        }
        sb.append("</map>");
        return sb.toString();    
    }

    /**
     * Returns an HTML image map tag for this entity.  The returned fragment
     * should be <code>XHTML 1.0</code> compliant.
     *
     * @param toolTipTagFragmentGenerator  the generator for tooltip fragment.
     * @param urlTagFragmentGenerator  the generator for the URL fragment.
     * 
     * @return The HTML tag.
     */
    private String getImageMapAreaTag(ChartEntity chartEntity,
            ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
            URLTagFragmentGenerator urlTagFragmentGenerator) {

        StringBuffer tag = new StringBuffer();
        boolean hasURL 
            = (chartEntity.getURLText() == null ? false : !chartEntity.getURLText().equals(""));
        boolean hasToolTip 
            = (chartEntity.getToolTipText() == null ? false : !chartEntity.getToolTipText().equals(""));
        if (hasURL || hasToolTip) {
            tag.append(
                "<area shape=\"" + chartEntity.getShapeType() + "\"" + " coords=\"" 
                    + chartEntity.getShapeCoords() + "\""
            );
            if (hasToolTip) {
                tag.append(toolTipTagFragmentGenerator.generateToolTipFragment(
                	chartEntity.getToolTipText()
                ));
            }
            
            // create a javascript array with params
            StringBuffer paramArrayBuf = new StringBuffer();
            paramArrayBuf.append("new Array(");
            
            String urlStr = chartEntity.getURLText();
            String paramStr = urlStr.substring(urlStr.indexOf("?")+1);
            String[] pairs = paramStr.split("&amp;");
            for (int i = 0; i < pairs.length; i++) {
            	String key = pairs[i].substring(0, pairs[i].indexOf("="));
            	String value = pairs[i].substring(pairs[i].indexOf("=")+1);
            	if (i > 0)
            		paramArrayBuf.append(", ");
            	paramArrayBuf.append("'");
            	paramArrayBuf.append(key);
            	paramArrayBuf.append("'");
            	paramArrayBuf.append(", ");
            	paramArrayBuf.append("'");
            	paramArrayBuf.append(value);
            	paramArrayBuf.append("'");
            }
            paramArrayBuf.append(")");
            
            tag.append(" href=\"javascript:void(0);\""); // don't call JQuery here causes JS/JQuery error related to event 
            tag.append(" onclick=\"javascript:handleChartOnClick('contextmenu1', event, " + paramArrayBuf.toString() + "); return false;\"");            
                
            // if there is a tool tip, we expect it to generate the title and
            // alt values, so we only add an empty alt if there is no tooltip
            if (!hasToolTip) {
                tag.append(" alt=\"\"");
            }
            tag.append(">");
            tag.append("</area>");
        }
        return tag.toString();
    }
    
    public void encodeEnd(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) {
			
				log.debug("encodeEnd(): " + this);
				// esponseWriter writer = context.getResponseWriter();
			}    	
    }

    private ChartRenderingInfo writeChart(OutputStream stream, JFreeChart chart, ChartData chartData)
        throws IOException {

        ChartRenderingInfo info = new ChartRenderingInfo();
        if (chartData.getOutput().equalsIgnoreCase("png"))
            ChartUtilities.writeChartAsPNG(stream, chart, chartData.getWidth(), chartData
                .getHeight(), info);
        else if (chartData.getOutput().equalsIgnoreCase("jpeg"))
            ChartUtilities.writeChartAsJPEG(stream, chart, chartData.getWidth(), chartData
                .getHeight(), info);

        stream.flush();
        stream.close();
        return info;
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * Alpha attribute for pie charts
     */
    public int getAlpha() {
        if (alpha != null)
            return alpha.intValue();

        ValueBinding vb = getValueBinding("alpha");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 100;
    }

    public void setAlpha(int alpha) {
        this.alpha = new Integer(alpha);
    }

    /**
     * Antialias attribute
     */
    public boolean getAntialias() {
        if (antialias != null)
            return antialias.booleanValue();

        ValueBinding vb = getValueBinding("antialias");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : false;
    }

    public void setAntialias(boolean antialias) {
        this.antialias = Boolean.valueOf(antialias);
    }

    /**
     * Background attribute
     */
    public String getBackground() {
        if (background != null)
            return background;

        ValueBinding vb = getValueBinding("background");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : "white";
    }

    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * Foreground attribute
     */
    public String getForeground() {
        if (foreground != null)
            return foreground;

        ValueBinding vb = getValueBinding("foreground");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : "white";
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    /**
     * 3D attribute
     */
    public boolean getIs3d() {
        if (is3d != null)
            return is3d.booleanValue();

        ValueBinding vb = getValueBinding("is3d");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : true;
    }

    public void setIs3d(boolean is3d) {
        this.is3d = Boolean.valueOf(is3d);
    }

    
    /**
     * isLogarithmic
     */
    public boolean getIsLogarithmic() {
        if (isLogarithmic != null)
            return isLogarithmic.booleanValue();

        ValueBinding vb = getValueBinding("isLogarithmic");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : true;
    }

    public void setIsLogarithmic(boolean isLogarithmic) {
        this.isLogarithmic = Boolean.valueOf(isLogarithmic);
    }
 
        
    
    public String getColors() {
        if (colors != null)
            return colors;

        ValueBinding vb = getValueBinding("colors");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        if (v != null) 
        	return v;
        else
        	return null;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    /**
     * Depth attribute for pie charts
     */
    public int getDepth() {
        if (depth != null)
            return depth.intValue();

        ValueBinding vb = getValueBinding("depth");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 15;
    }

    public void setDepth(int depth) {
        this.depth = new Integer(depth);
    }

    /**
     * Width attribute
     */
    public int getWidth() {
        if (width != null)
            return width.intValue();

        ValueBinding vb = getValueBinding("width");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 400;
    }

    public void setWidth(int width) {
        this.width = new Integer(width);
    }

    /**
     * Height attribute
     */
    public int getHeight() {
        if (height != null)
            return height.intValue();

        ValueBinding vb = getValueBinding("height");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 300;
    }

    public void setHeight(int height) {
        this.height = new Integer(height);
    }

    /**
     * Rows attribute
     */
    public int getRows() {
        if (rows != null)
            return rows.intValue();

        ValueBinding vb = getValueBinding("rows");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 5;
    }

    public void setRows(int rows) {
        this.rows = new Integer(rows);
    }
    
    /**
     * Legend attribute
     */
    public boolean getLegend() {
        if (legend != null)
            return legend.booleanValue();

        ValueBinding vb = getValueBinding("legend");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : true;
    }

    public void setLegend(boolean legend) {
        this.legend = Boolean.valueOf(legend);
    }

    /**
     * Orientation attribute
     */
    public String getOrientation() {
        if (orientation != null)
            return orientation;

        ValueBinding vb = getValueBinding("orientation");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : "vertical";
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    /**
     * Outline attribute
     */
    public boolean getOutline() {
        if (outline != null)
            return outline.booleanValue();

        ValueBinding vb = getValueBinding("outline");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : true;
    }

    public void setOutline(boolean outline) {
        this.outline = Boolean.valueOf(outline);
    }

    /**
     * Start Angle attribute for pie charts
     */
    public int getStartAngle() {
        if (startAngle != null)
            return startAngle.intValue();

        ValueBinding vb = getValueBinding("startAngle");
        Integer v = vb != null ? (Integer) vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : 0;
    }

    public void setStartAngle(int startAngle) {
        this.startAngle = new Integer(startAngle);
    }

    /**
     * Title attribute
     */
    public String getTitle() {
        if (title != null)
            return title;

        ValueBinding vb = getValueBinding("title");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Type attribute
     */
    public String getType() {
        if (type != null)
            return type;

        ValueBinding vb = getValueBinding("type");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * X-axis attribute
     */
    public String getXlabel() {
        if (xlabel != null)
            return xlabel;

        ValueBinding vb = getValueBinding("xlabel");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    /**
     * Y-axis attribute
     */
    public String getYlabel() {
        if (ylabel != null)
            return ylabel;

        ValueBinding vb = getValueBinding("ylabel");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    /**
     * StyleClass attribute
     */
    public String getStyleClass() {
        if (styleClass != null)
            return styleClass;

        ValueBinding vb = getValueBinding("styleClass");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Alt attribute
     */
    public String getAlt() {
        if (alt != null)
            return alt;

        ValueBinding vb = getValueBinding("alt");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    /**
     * ImgTitle attribute
     */
    public String getImgTitle() {
        if (imgTitle != null)
            return imgTitle;

        ValueBinding vb = getValueBinding("imgTitle");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setImgTitle(String imgTitle) {
        this.imgTitle = imgTitle;
    }

    /**
     * Onclick attribute
     */
    public String getOnclick() {
        if (onclick != null)
            return onclick;

        ValueBinding vb = getValueBinding("onclick");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    /**
     * Ondblclick attribute
     */
    public String getOndblclick() {
        if (ondblclick != null)
            return ondblclick;

        ValueBinding vb = getValueBinding("ondblclick");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOndblclick(String ondblclick) {
        this.ondblclick = ondblclick;
    }

    /**
     * Onkeydown attribute
     */
    public String getOnkeydown() {
        if (onkeydown != null)
            return onkeydown;

        ValueBinding vb = getValueBinding("onkeydown");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }

    /**
     * Onkeypress attribute
     */
    public String getOnkeypress() {
        if (onkeypress != null)
            return onkeypress;

        ValueBinding vb = getValueBinding("onkeypress");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnkeypress(String onkeypress) {
        this.onkeypress = onkeypress;
    }

    /**
     * Onkeyup attribute
     */
    public String getOnkeyup() {
        if (onkeyup != null)
            return onkeyup;

        ValueBinding vb = getValueBinding("onkeyup");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnkeyup(String onkeyup) {
        this.onkeyup = onkeyup;
    }

    /**
     * Onmousedown attribute
     */
    public String getOnmousedown() {
        if (onmousedown != null)
            return onmousedown;

        ValueBinding vb = getValueBinding("onmousedown");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnmousedown(String onmousedown) {
        this.onmousedown = onmousedown;
    }

    /**
     * Onmousemove attribute
     */
    public String getOnmousemove() {
        if (onmousemove != null)
            return onmousemove;

        ValueBinding vb = getValueBinding("onmousemove");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnmousemove(String onmousemove) {
        this.onmousemove = onmousemove;
    }

    /**
     * Onmouseout attribute
     */
    public String getOnmouseout() {
        if (onmouseout != null)
            return onmouseout;

        ValueBinding vb = getValueBinding("onmouseout");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnmouseout(String onmouseout) {
        this.onmouseout = onmouseout;
    }

    /**
     * Onmouseover attribute
     */
    public String getOnmouseover() {
        if (onmouseover != null)
            return onmouseover;

        ValueBinding vb = getValueBinding("onmouseover");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnmouseover(String onmouseover) {
        this.onmouseover = onmouseover;
    }

    /**
     * Onmouseup attribute
     */
    public String getOnmouseup() {
        if (onmouseup != null)
            return onmouseup;

        ValueBinding vb = getValueBinding("onmouseup");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setOnmouseup(String onmouseup) {
        this.onmouseup = onmouseup;
    }

    /**
     * Output attribute, default value is png
     */
    public String getOutput() {
        if (output != null)
            return output;

        ValueBinding vb = getValueBinding("output");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : "png";
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getUsemap() {
        if (usemap != null)
            return usemap;

        ValueBinding vb = getValueBinding("usemap");
        String v = vb != null ? (String) vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setUsemap(String usemap) {
        this.usemap = usemap;
    }

    /**
     * datasource attribute
     */
    public Object getDatasource() {
        if (datasource != null)
            return datasource;

        ValueBinding vb = getValueBinding("datasource");
        Object v = vb != null ? vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setDatasource(Object datasource) {
        this.datasource = datasource;
    }
    
    /**
     * tooltipgenerator attribute
     */
    public Object getTooltipgenerator() {
        if (tooltipgenerator != null)
            return tooltipgenerator;

        ValueBinding vb = getValueBinding("tooltipgenerator");
        Object v = vb != null ? vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setTooltipGgenerator(Object tooltipgenerator) {
        this.tooltipgenerator = tooltipgenerator;
    }

    /**
     * urlgenerator attribute
     */
    public Object getUrlgenerator() {
        if (urlgenerator != null)
            return urlgenerator;

        ValueBinding vb = getValueBinding("urlgenerator");
        Object v = vb != null ? vb.getValue(getFacesContext()) : null;
        return v != null ? v : null;
    }

    public void setUrlgenerator(Object urlgenerator) {
        this.urlgenerator = urlgenerator;
    }
    
    public Object saveState(FacesContext context) {
        Object values[] = new Object[39];
        values[0] = super.saveState(context);
        values[1] = width;       
        values[2] = height;      
        values[3] = alpha;       
        values[4] = depth;       
        values[5] = startAngle;  
        values[6] = title;       
        values[7] = type;        
        values[8] = background;  
        values[9] =  foreground;             
        values[10] = xlabel;                 
        values[11] = ylabel;                 
        values[12] = orientation;            
        values[13] = colors;                 
        values[14] = is3d;                   
        values[15] = legend;                 
        values[16] = antialias;              
        values[17] = outline;                
        values[18] = styleClass;             
        values[19] = alt;                    
        values[20] = imgTitle;               
        values[21] = onclick;                
        values[22] = ondblclick;             
        values[23] = onmousedown;            
        values[24] = onmouseup;              
        values[25] = onmouseover;            
        values[26] = onmousemove;            
        values[27] = onmouseout;             
        values[28] = onkeypress;             
        values[29] = onkeydown;              
        values[30] = onkeyup;                
        values[31] = output;                 
        values[32] = usemap;                 
        values[33] = tooltipgenerator;       
        values[34] = urlgenerator;           
        values[35] = rows;                   
        values[36] = datasource;                   
        values[37] = new Integer(first);                   
        values[38] = isLogarithmic;                   
        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);                                    
        this.width = (Integer)      values[1];   
        this.height = (Integer)     values[2];   
        this.alpha = (Integer)      values[3];   
        this.depth = (Integer)      values[4];   
        this.startAngle = (Integer) values[5];   
        this.title = (String)       values[6];   
        this.type = (String)        values[7];   
        this.background = (String)  values[8];   
        this.foreground = (String)  values[9];   
        this.xlabel = (String)      values[10];  
        this.ylabel = (String)      values[11];  
        this.orientation = (String) values[12];  
        this.colors = (String)      values[13];  
        this.is3d = (Boolean)       values[14];  
        this.legend = (Boolean)     values[15];  
        this.antialias = (Boolean)  values[16];  
        this.outline = (Boolean)    values[17];  
        this.styleClass = (String)  values[18];  
        this.alt = (String)         values[19];  
        this.imgTitle = (String)    values[20];  
        this.onclick = (String)     values[21];  
        this.ondblclick = (String)  values[22];  
        this.onmousedown = (String) values[23];  
        this.onmouseup = (String)   values[24];  
        this.onmouseover = (String) values[25];  
        this.onmousemove = (String) values[26];  
        this.onmouseout = (String)  values[27];  
        this.onkeypress = (String)  values[28];  
        this.onkeydown = (String)   values[29];  
        this.onkeyup = (String)     values[30];  
        this.output = (String)      values[31];  
        this.usemap = (String)      values[32];  
        this.tooltipgenerator =     values[33];  
        this.urlgenerator =         values[34];  
        this.rows = (Integer)       values[35];  
        this.datasource =     		values[36];  
        this.first = ((Integer) 		values[37]).intValue();  
        this.isLogarithmic = (Boolean)values[38];                   
    }                                 
}
