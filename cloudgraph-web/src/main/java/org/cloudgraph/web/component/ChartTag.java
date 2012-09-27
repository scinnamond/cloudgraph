package org.cloudgraph.web.component;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * Tag class for the component
 */

public class ChartTag extends UIComponentTag {
	
	private String title = null;

	private String type = null;

	private String width = null;

	private String height = null;

	private String rows = null;
	
	private String background = null;
	
	private String foreground = null;

	private String xlabel = null;

	private String ylabel = null;

	private String orientation = null;

	private String is3d = null;

	private String isLogarithmic = null;
	
	private String antialias = null;

	private String outline = null;

	private String legend = null;

	private String alpha = null;

	private String depth = null;

	private String startAngle = null;

	private String colors = null;

	private String styleClass = null;
	
	private String alt = null;	
	
	private String imgTitle = null;
	
	private String onclick = null;
	
	private String ondblclick = null;
	
	private String onmousedown = null;
	
	private String onmouseup = null;
	
	private String onmouseover = null;
	
	private String onmousemove = null;
	
	private String onmouseout = null;
	
	private String onkeypress = null;
	
	private String onkeydown = null;
	
	private String onkeyup = null;
	
	private String output = null;
	
	private String usemap = null;

    private String datasource = null;

    private String tooltipgenerator = null;

    private String urlgenerator = null;
    
    
	public void release() {
		super.release();
		title = null;
		type = null;
		width = null;
		height = null;
		rows = null;
		background = null;
		foreground = null;
		xlabel = null;
		ylabel = null;
		orientation = null;
		is3d = null;
		isLogarithmic = null;
		antialias = null;
		outline = null;
		legend = null;
		alpha = null;
		depth = null;
		startAngle = null;
		colors = null;
		styleClass = null;
		alt = null;
		imgTitle = null;
		onclick = null;
		ondblclick = null;
		onmousedown = null;
		onmouseup = null;
		onmouseover = null;
		onmousemove = null;
		onmouseout = null;
		onkeypress = null;
		onkeydown = null;
		onkeyup = null;
		output = null;
		usemap = null;
        datasource = null;
        tooltipgenerator = null;
        urlgenerator = null;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);

		if (title != null) {
			if (isValueReference(title)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(title);
				component.setValueBinding("title", vb);
			} else {
				component.getAttributes().put("title", title);
			}
		}

		if (type != null) {
			if (isValueReference(type)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(type);
				component.setValueBinding("type", vb);
			} else {
				component.getAttributes().put("type", type);
			}
		}

		if (width != null) {
			if (isValueReference(width)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(width);
				component.setValueBinding("width", vb);
			} else {
				component.getAttributes().put("width", Integer.valueOf(width));
			}
		}

		if (height != null) {
			if (isValueReference(height)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(height);
				component.setValueBinding("height", vb);
			} else {
				component.getAttributes().put("height", Integer.valueOf(height));
			}
		}

		if (rows != null) {
			if (isValueReference(rows)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(rows);
				component.setValueBinding("rows", vb);
			} else {
				component.getAttributes().put("rows", Integer.valueOf(rows));
			}
		}
		
		if (background != null) {
			if (isValueReference(background)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(background);
				component.setValueBinding("background", vb);
			} else {
				component.getAttributes().put("background", background);
			}
		}
		
		if (foreground != null) {
			if (isValueReference(foreground)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(foreground);
				component.setValueBinding("foreground", vb);
			} else {
				component.getAttributes().put("foreground", foreground);
			}
		}

		if (xlabel != null) {
			if (isValueReference(xlabel)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(xlabel);
				component.setValueBinding("xlabel", vb);
			} else {
				component.getAttributes().put("xlabel", xlabel);
			}
		}

		if (ylabel != null) {
			if (isValueReference(ylabel)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(ylabel);
				component.setValueBinding("ylabel", vb);
			} else {
				component.getAttributes().put("ylabel", ylabel);
			}
		}

		if (orientation != null) {
			if (isValueReference(orientation)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(orientation);
				component.setValueBinding("orientation", vb);
			} else {
				component.getAttributes().put("orientation", orientation);
			}
		}

		if (is3d != null) {
			if (isValueReference(is3d)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(is3d);
				component.setValueBinding("is3d", vb);
			} else {
				component.getAttributes().put("is3d", Boolean.valueOf(is3d));
			}
		}

		if (isLogarithmic != null) {
			if (isValueReference(isLogarithmic)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(isLogarithmic);
				component.setValueBinding("isLogarithmic", vb);
			} else {
				component.getAttributes().put("isLogarithmic", Boolean.valueOf(isLogarithmic));
			}
		}
		
		if (antialias != null) {
			if (isValueReference(antialias)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(antialias);
				component.setValueBinding("antialias", vb);
			} else {
				component.getAttributes().put("antialias", Boolean.valueOf(antialias));
			}
		}

		if (outline != null) {
			if (isValueReference(outline)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(outline);
				component.setValueBinding("outline", vb);
			} else {
				component.getAttributes().put("outline", Boolean.valueOf(outline));
			}
		}

		if (legend != null) {
			if (isValueReference(legend)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(legend);
				component.setValueBinding("legend", vb);
			} else {
				component.getAttributes().put("legend", Boolean.valueOf(legend));
			}
		}

		if (alpha != null) {
			if (isValueReference(alpha)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(alpha);
				component.setValueBinding("alpha", vb);
			} else {
				component.getAttributes().put("alpha", Integer.valueOf(alpha));
			}
		}

		if (depth != null) {
			if (isValueReference(depth)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(depth);
				component.setValueBinding("depth", vb);
			} else {
				component.getAttributes().put("depth", Integer.valueOf(depth));
			}
		}

		if (startAngle != null) {
			if (isValueReference(startAngle)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(startAngle);
				component.setValueBinding("startAngle", vb);
			} else {
				component.getAttributes().put("startAngle", Integer.valueOf(startAngle));
			}
		}

		if (colors != null) {
			if (isValueReference(colors)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(colors);
				component.setValueBinding("colors", vb);
			} else {
				component.getAttributes().put("colors", colors);
			}
		}

		if (styleClass != null) {
			if (isValueReference(styleClass)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(styleClass);
				component.setValueBinding("styleClass", vb);
			} else {
				component.getAttributes().put("styleClass", styleClass);
			}
		}
		
		if (alt != null) {
			if (isValueReference(alt)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(alt);
				component.setValueBinding("alt", vb);
			} else {
				component.getAttributes().put("alt", alt);
			}
		}
		
		if (imgTitle != null) {
			if (isValueReference(imgTitle)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(imgTitle);
				component.setValueBinding("imgTitle", vb);
			} else {
				component.getAttributes().put("imgTitle", imgTitle);
			}
		}
		
		if (onclick != null) {
			if (isValueReference(onclick)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onclick);
				component.setValueBinding("onclick", vb);
			} else {
				component.getAttributes().put("onclick", onclick);
			}
		}
		
		if (ondblclick != null) {
			if (isValueReference(ondblclick)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(ondblclick);
				component.setValueBinding("ondblclick", vb);
			} else {
				component.getAttributes().put("ondblclick", ondblclick);
			}
		}
		
		if (onmousedown != null) {
			if (isValueReference(onmousedown)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onmousedown);
				component.setValueBinding("onmousedown", vb);
			} else {
				component.getAttributes().put("onmousedown", onmousedown);
			}
		}
		
		if (onmouseup != null) {
			if (isValueReference(onmouseup)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onmouseup);
				component.setValueBinding("onmouseup", vb);
			} else {
				component.getAttributes().put("onmouseup", onmouseup);
			}
		}
		
		if (onmouseover != null) {
			if (isValueReference(onmouseover)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onmouseover);
				component.setValueBinding("onmouseover", vb);
			} else {
				component.getAttributes().put("onmouseover", onmouseover);
			}
		}
		
		if (onmousemove != null) {
			if (isValueReference(onmousemove)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onmousemove);
				component.setValueBinding("onmousemove", vb);
			} else {
				component.getAttributes().put("onmousemove", onmousemove);
			}
		}
		
		if (onmouseout != null) {
			if (isValueReference(onmouseout)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onmouseout);
				component.setValueBinding("onmouseout", vb);
			} else {
				component.getAttributes().put("onmouseout", onmouseout);
			}
		}
		
		if (onkeypress != null) {
			if (isValueReference(onkeypress)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onkeypress);
				component.setValueBinding("onkeypress", vb);
			} else {
				component.getAttributes().put("onkeypress", onkeypress);
			}
		}
		
		if (onkeydown != null) {
			if (isValueReference(onkeydown)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onkeydown);
				component.setValueBinding("onkeydown", vb);
			} else {
				component.getAttributes().put("onkeydown", onkeydown);
			}
		}
		
		if (onkeyup != null) {
			if (isValueReference(onkeyup)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(onkeyup);
				component.setValueBinding("onkeyup", vb);
			} else {
				component.getAttributes().put("onkeyup", onkeyup);
			}
		}
		
		if (output != null) {
			if (isValueReference(output)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(output);
				component.setValueBinding("output", vb);
			} else {
				component.getAttributes().put("output", output);
			}
		}
		
		if (usemap != null) {
			if (isValueReference(usemap)) {
				ValueBinding vb = getFacesContext().getApplication()
						.createValueBinding(usemap);
				component.setValueBinding("usemap", vb);
			} else {
				component.getAttributes().put("usemap", usemap);
			}
		}

        if (datasource != null) {
            if (isValueReference(datasource)) {
                ValueBinding vb = getFacesContext().getApplication()
                        .createValueBinding(datasource);
                component.setValueBinding("datasource", vb);
            } else {
                component.getAttributes().put("datasource", datasource);
            }
        }
        
        if (tooltipgenerator != null) {
            if (isValueReference(tooltipgenerator)) {
                ValueBinding vb = getFacesContext().getApplication()
                        .createValueBinding(tooltipgenerator);
                component.setValueBinding("tooltipgenerator", vb);
            } else {
                component.getAttributes().put("tooltipgenerator", tooltipgenerator);
            }
        }

        if (urlgenerator != null) {
            if (isValueReference(urlgenerator)) {
                ValueBinding vb = getFacesContext().getApplication()
                        .createValueBinding(urlgenerator);
                component.setValueBinding("urlgenerator", vb);
            } else {
                component.getAttributes().put("urlgenerator", urlgenerator);
            }
        }
                
    }

	public String getComponentType() {
		return ChartComponent.COMPONENT_TYPE;
	}

	public String getRendererType() {
		return null;
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

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
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

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getIs3d() {
		return is3d;
	}

	public void setIs3d(String is3d) {
		this.is3d = is3d;
	}
	
	public String getIsLogarithmic() {
		return isLogarithmic;
	}

	public void setIsLogarithmic(String isLogarithmic) {
		this.isLogarithmic = isLogarithmic;
	}	
	
	public String getAntialias() {
		return antialias;
	}

	public void setAntialias(String antialias) {
		this.antialias = antialias;
	}

	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	public String getDepth() {
		return depth;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public String getOutline() {
		return outline;
	}

	public void setOutline(String outline) {
		this.outline = outline;
	}

	public String getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(String startAngle) {
		this.startAngle = startAngle;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
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

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getImgTitle() {
		return imgTitle;
	}

	public void setImgTitle(String imgTitle) {
		this.imgTitle = imgTitle;
	}

	public String getOnclick() {
		return onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public String getOndblclick() {
		return ondblclick;
	}

	public void setOndblclick(String ondblclick) {
		this.ondblclick = ondblclick;
	}

	public String getOnkeydown() {
		return onkeydown;
	}

	public void setOnkeydown(String onkeydown) {
		this.onkeydown = onkeydown;
	}

	public String getOnkeypress() {
		return onkeypress;
	}

	public void setOnkeypress(String onkeypress) {
		this.onkeypress = onkeypress;
	}

	public String getOnkeyup() {
		return onkeyup;
	}

	public void setOnkeyup(String onkeyup) {
		this.onkeyup = onkeyup;
	}

	public String getOnmousedown() {
		return onmousedown;
	}

	public void setOnmousedown(String onmousedown) {
		this.onmousedown = onmousedown;
	}

	public String getOnmousemove() {
		return onmousemove;
	}

	public void setOnmousemove(String onmousemove) {
		this.onmousemove = onmousemove;
	}

	public String getOnmouseout() {
		return onmouseout;
	}

	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}

	public String getOnmouseover() {
		return onmouseover;
	}

	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}

	public String getOnmouseup() {
		return onmouseup;
	}

	public void setOnmouseup(String onmouseup) {
		this.onmouseup = onmouseup;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getUsemap() {
		return usemap;
	}

	public void setUsemap(String usemap) {
		this.usemap = usemap;
	}

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
	
    public String getTooltipgenerator() {
        return tooltipgenerator;
    }

    public void setTooltipgenerator(String tooltipgenerator) {
        this.tooltipgenerator = tooltipgenerator;
    }

    public String getUrlgenerator() {
        return urlgenerator;
    }

    public void setUrlgenerator(String urlgenerator) {
        this.urlgenerator = urlgenerator;
    }

	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

}