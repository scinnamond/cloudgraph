package org.cloudgraph.web.model.news;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;
import org.cloudgraph.web.model.common.InstanceBean;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;

@ManagedBean(name="NewsBean")
@RequestScoped
public class NewsBean extends InstanceBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(NewsBean.class);	
	
	public NewsBean() {
		super("NewsItem");
		log.debug("created NewsBean");
	}

	public InstanceSpecificationAdapter getNewsItem() {
		return this.getInstance();		
	}
	
	public String getTitle() {
		return (String)this.getInstance().getValues().get("Title");
	}

	public String getDate() {
		return (String)this.getInstance().getValues().get("EventDate");
	}


}
