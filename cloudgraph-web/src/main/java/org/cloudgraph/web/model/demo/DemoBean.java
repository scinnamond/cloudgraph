package org.cloudgraph.web.model.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;

public class DemoBean extends ModelBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(DemoBean.class);	
	
	private String defaultUrl = "overview/Section-Overview.htm";
	
	private String modelDisplayName;
	private String modelDescription;
	private String modelUrl;
	private String javaDocUrl;
	private String modelRootURI;
	private String modelRootType;
	
	private int width = 700;
	private int height = 1000;
	
	private String selectedTab;
		
	public DemoBean() {
		log.debug("created");
	}
		
	public boolean getHasModel() {
		return this.modelUrl != null && 
				!this.modelUrl.equals(this.defaultUrl);
	}
	
	public String getDefaultUrl() {
		return defaultUrl;
	}

	public String view() {
       return null;
    }

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getModelDisplayName() {
		return modelDisplayName;
	}

	public void setModelDisplayName(String modelDisplayName) {
		this.modelDisplayName = modelDisplayName;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}

	public String getModelUrl() {
		return modelUrl;
	}

	public void setModelUrl(String modelUrl) {
		this.modelUrl = modelUrl;
	}

	public String getJavaDocUrl() {
		return javaDocUrl;
	}

	public void setJavaDocUrl(String javaDocUrl) {
		this.javaDocUrl = javaDocUrl;
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public String getModelRootURI() {
		return modelRootURI;
	}

	public void setModelRootURI(String modelRootURI) {
		this.modelRootURI = modelRootURI;
	}

	public String getModelRootType() {
		return modelRootType;
	}

	public void setModelRootType(String modelRootType) {
		this.modelRootType = modelRootType;
	}
	
}
