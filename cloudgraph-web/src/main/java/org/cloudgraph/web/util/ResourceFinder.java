package org.cloudgraph.web.util;

import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.component.ChartResourceFinder;




public class ResourceFinder 
    implements WebConstants, ChartResourceFinder
{
    private static Log log =LogFactory.getLog(ResourceFinder.class);
    
    public String getStatusLabel(String status) {
        String result = status;
        try {
        	result = ResourceManager.instance().getString(RESOURCE_DASHBOARD_SATUS_PREFIX + status,
                    ResourceType.LABEL);        	
        } catch (MissingResourceException e) {
            log.warn(e.getMessage(), e);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        
        return result;
    }

	public String getLabel(String resourceId) {
		return getStatusLabel(resourceId);
	}    
}
