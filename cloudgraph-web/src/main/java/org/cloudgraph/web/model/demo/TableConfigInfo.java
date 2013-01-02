package org.cloudgraph.web.model.demo;

import org.cloudgraph.config.TableConfig;

public class TableConfigInfo {

	private TableConfig config;
	private String xml;
	
	public TableConfigInfo(TableConfig config, String xml) {
		super();
		this.config = config;
		this.xml = xml;
	}
	public TableConfig getConfig() {
		return config;
	}
	public void setConfig(TableConfig config) {
		this.config = config;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	
	
}
