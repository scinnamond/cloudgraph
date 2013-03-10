package org.cloudgraph.web.model.data;

// java imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.plasma.query.Query;


/**
 */
public class InstanceBrowserQueueBean extends InstanceQueueBean {
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(InstanceBrowserQueueBean.class);

    private Long classId = new Long(-1);     
    

    public InstanceBrowserQueueBean() {
    }
    
    public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}   
    
	public Query getQuery() {
		return InstanceSpecificationQuery.createQueueQueryByClassId(
				this.classId);    	
    }
 }
