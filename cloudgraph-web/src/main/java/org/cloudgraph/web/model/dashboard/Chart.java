package org.cloudgraph.web.model.dashboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.component.ChartDataSource;
import org.cloudgraph.web.component.ChartOrientation;
import org.cloudgraph.web.component.ChartType;
import org.cloudgraph.web.config.web.AppParamName;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.config.web.PropertyName;
import org.cloudgraph.web.datasource.AbstractDataSource;
import org.cloudgraph.web.model.profile.UserBean;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.cloudgraph.web.sdo.personalization.Setting;
import org.cloudgraph.web.util.BeanFinder;

public class Chart extends AbstractComponent {
	private static Log log = LogFactory.getLog(Chart.class);

	private Map<ChartType, ChartDataSource> dataSourceMap = new HashMap<ChartType, ChartDataSource>();
	private Map<ChartType, Object> urlGeneratorMap = new HashMap<ChartType, Object>();
	
    private String background; 
    private String ylabel;
    private ChartOrientation orientation;
    private String usemap;
    private int width;
    private int height;
    private int expandedWidth;
    private int expandedHeight;
	
	public Chart(ComponentName name, ComponentShape shape,  
			Dashboard dashboard,
			Container homeContainer) {
		super(name, shape, ElementType.CHART, dashboard, homeContainer);		
	}
	
	public void clear()
	{
		for (Iterator<ChartDataSource> it = dataSourceMap.values().iterator(); it.hasNext();) {
			ChartDataSource value = it.next();
			((AbstractDataSource)value).purgeCurrentDataSet();
	    }
	}
	
	public String toBarChart() {
        try {		
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.CHART___TYPE, 
	        	ChartType.bar.name());	
	        user.commitProfile();
        }
        catch (Throwable t) {
	        log.error(t.getMessage(), t);
        }
        return null;
	}

	public String toPieChart() {
		try {
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.CHART___TYPE, ChartType.pie.name());		
	        user.commitProfile();		
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return null;
	}
	
	public String setIs3D() {
        try {		
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.THREE___DIMENSIONAL, 
	        	"true");	
	        user.commitProfile();
        }
        catch (Throwable t) {
	        log.error(t.getMessage(), t);
        }
        return null;
	}

	public String unsetIs3D() {
		try {
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.THREE___DIMENSIONAL, "false");		
	        user.commitProfile();		
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return null;
	}

	public String setIsLogarithmic() {
        try {		
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.LOGARITHMIC, 
	        	"true");	
	        user.commitProfile();
        }
        catch (Throwable t) {
	        log.error(t.getMessage(), t);
        }
        return null;
	}

	public String unsetIsLogarithmic() {
		try {
			BeanFinder finder = new BeanFinder();
			UserBean user = finder.findUserBean();
	        user.updateProfileSetting(this.name, this.type, 
	        	PropertyName.LOGARITHMIC, "false");		
	        user.commitProfile();		
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return null;
	}	

	public ChartDataSource getDataSource() {
try {
		handleCategoryChange();		
		return dataSourceMap.get(this.getChartTypeEnum());
}
catch (Throwable t) {
	log.error(t.getMessage(), t);
	return null;
}
	}

	public void addDataSource(ChartType chartType, ChartDataSource dataSource) {
		dataSourceMap.put(chartType, dataSource);
	}

	@SuppressWarnings("unchecked")
	private void handleCategoryChange() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		//String cat = (String)params.get(AppParamName.CAT.value());
		String subcat = (String)params.get(AppParamName.SUBCAT.value());
		String drilldownString = (String)params.get(AppParamName.DRILLDOWN.value());
		String drillupString = (String)params.get(AppParamName.DRILLUP.value());
		boolean drilldown = (new Boolean(drilldownString)).booleanValue();
		boolean drillup = (new Boolean(drillupString)).booleanValue();
		
		if (drilldown && subcat != null) {
			if (!subcat.equals(this.getCategoryName())) {
				log.info("drill down from: " + this.getCategoryName()
						+ " to " + subcat);
				this.categories.push(subcat);
				this.clear();
			}
		}
		else if (drillup && this.categories.size() > 1) {
			String oldCat = this.getCategoryName();
		    this.categories.pop();
			log.info("drill up from: " + oldCat
					+ " to " + this.getCategoryName());
			this.clear();
		}
	}
	
	public Object getUrlGenerator() {
try {
		return this.urlGeneratorMap.get(this.getChartTypeEnum());	
}
catch (Throwable t) {
	log.error(t.getMessage(), t);
	return null;
}
	}
	
	public String getColorMap() {
		return this.getDataSource().getColorMap();
	}

	
	public String getChartType() {
		return getChartTypeEnum().name();
	}
	
	private ChartType getChartTypeEnum() {
		ChartType result = ChartType.bar;
		
		BeanFinder finder = new BeanFinder();
		UserBean user = finder.findUserBean();
		Setting chartTypeSetting = user.findComponentSetting(this.name,  
	        	PropertyName.CHART___TYPE);
		if (chartTypeSetting != null) {
			String typeValue = chartTypeSetting.getValue();
			result = ChartType.valueOf(typeValue);
		}
		else {
			Setting dashboardChartTypeSetting = user.findComponentSetting(
					dashboard.getComponentName(),  
		        	PropertyName.CHART___TYPE);
			if (dashboardChartTypeSetting != null) {
				String typeValue = dashboardChartTypeSetting.getValue();
				result = ChartType.valueOf(typeValue);
			}
		}
		return result;
	}
	
	public boolean getIsThreeDimensional() {
		boolean result = true;
		BeanFinder finder = new BeanFinder();
		UserBean user = finder.findUserBean();
		Setting chartSetting = user.findComponentSetting(this.name,  
	        	PropertyName.THREE___DIMENSIONAL);
		if (chartSetting != null) {
			String value = chartSetting.getValue();
			result = Boolean.valueOf(value);
		}
		else {
			Setting dashboardChartSetting = user.findComponentSetting(
				dashboard.getComponentName(),  
		        PropertyName.THREE___DIMENSIONAL);
			if (dashboardChartSetting != null) {
				String value = dashboardChartSetting.getValue();
				result = Boolean.valueOf(value);
			}
		}
		return result;
	}
	
	public boolean getIsLogarithmic() {
		boolean result = false;
		BeanFinder finder = new BeanFinder();
		UserBean user = finder.findUserBean();
		Setting chartSetting = user.findComponentSetting(this.name,  
	        	PropertyName.LOGARITHMIC);
		if (chartSetting != null) {
			String value = chartSetting.getValue();
			result = Boolean.valueOf(value);
		}
		else {
			Setting dashboardChartSetting = user.findComponentSetting(
				dashboard.getComponentName(),  
		        PropertyName.LOGARITHMIC);
			if (dashboardChartSetting != null) {
				String value = dashboardChartSetting.getValue();
				result = Boolean.valueOf(value);
			}
		}
		return result;
	}
	
	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getYlabel() {
		return ylabel;
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

	public int getWidth() {
		if (!this.isExpanded())
		    return width;
		else
			return expandedWidth;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		if (!this.isExpanded())
		    return height;
		else
			return expandedHeight;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getExpandedWidth() {
		return expandedWidth;
	}

	public void setExpandedWidth(int expandedWidth) {
		this.expandedWidth = expandedWidth;
	}

	public int getExpandedHeight() {
		return expandedHeight;
	}

	public void setExpandedHeight(int expandedHeight) {
		this.expandedHeight = expandedHeight;
	}

	public String getOrientation() {
		return orientation.name();
	}

	public void setOrientation(String orientation) {
		this.orientation = ChartOrientation.valueOf(orientation);
	}

	public void setOrientation(ChartOrientation orientation) {
		this.orientation = orientation;
	}

	public String getUsemap() {
		return usemap;
	}

	public void setUsemap(String usemap) {
		this.usemap = usemap;
	}

	public void addUrlGenerator(ChartType chartType, 
			Object urlGenerator) {
		this.urlGeneratorMap.put(chartType, urlGenerator);
	}


}
