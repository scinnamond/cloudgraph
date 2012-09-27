package org.cloudgraph.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.config.ApplicationConfig;
import org.cloudgraph.web.config.web.ChartDef;
import org.cloudgraph.web.config.web.StatusDef;


public class ResourceCache implements WebConstants
{
    private static Log log =LogFactory.getLog(
        ResourceCache.class); 


    static private ResourceCache instance = null; 
    
    private Map statusLabelMap = new HashMap();

    private ResourceCache()
    {
        // pull common resources out once only
        try {

            ChartDef[] chartDefs = ApplicationConfig.getInstance().getAllChartDefs();
            for (int i = 0; i < chartDefs.length; i++)
            {
            	for (StatusDef statusDef : chartDefs[i].getStatusDef())
            	{	
            		String label = ResourceManager.instance().getString(
            			RESOURCE_DASHBOARD_SATUS + "_" + statusDef.getName().toString(), 
            				ResourceType.LABEL);
            		statusLabelMap.put(label, statusDef.getName());
            	}
            }

        } catch (MissingResourceException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static ResourceCache instance()
    {
        if (instance == null)
            initializeInstance();
        return instance;
    }  

    private static synchronized void initializeInstance()
    {
        if (instance == null)            
            instance = new ResourceCache();
    }
    
}