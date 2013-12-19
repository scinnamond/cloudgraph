package org.cloudgraph.web.model.documentation;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.common.InstanceBean;

@ManagedBean(name="ChapterBean")
@RequestScoped
public class ChapterBean extends InstanceBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ChapterBean.class);	
	
	private String url = "overview/Section-Overview.htm";
	private int width = 700;
	private int height = 1000;
		
	public ChapterBean() {
		super("Chapter");
		log.debug("created ChapterBean");
	}
	
	public String view() {
       return null;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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


}
