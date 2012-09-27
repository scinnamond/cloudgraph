package org.cloudgraph.web.model.cache;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Work with ReferenceDataCache to cache and then expire and evict cached data.
 * ReferenceDataCache tells me when it caches something and I record it. A TimerTask
 * Thread wakes up every once in awhile and checks for items cached beyond their
 * expiration date. If expired, I tell ReferenceDataCache to evict it.
 * 
 * @author lmbates
 *
 */
public class ReferenceDataCacheMonitor extends TimerTask
{
	
    private static Log log = LogFactory.getLog(ReferenceDataCacheMonitor.class);
    
    // These should probably go in a properties file.
    private static final long sleepInterval = 60 * 1000 * 1;  //wake up every 1 minute
    private static final long cacheTTL = 60 * 1000 * 10;      //expire and evict every 10 minutes
    
    private ReferenceDataCache referenceDataCache = null;
    
    private Map<String, Long> cacheTimeMap = new HashMap<String, Long>();


    /**
     * Start the Thread that wakes up every once in awhile and checks for cached items
     * that have expired.
     * @param referenceDataCache - the ReferenceDataCache cache owner who receives expire/evict notices. 
     */
    public ReferenceDataCacheMonitor(ReferenceDataCache referenceDataCache)
	{
    	
    	super();
    	this.referenceDataCache = referenceDataCache;
    	Timer timer = new Timer("ReferenceDataCache", true);
    	timer.scheduleAtFixedRate(this, sleepInterval, sleepInterval);
		
	} // ReferenceDataCacheMonitor
    
    
    /**
     * Record the time that an item was cached by the ReferenceDataCache cache owner.
     * @param objName - the lookup name of the cached item.
     */
    public void monitor(String objName)
    {
    	
    	log.debug("Monitor Cache Object " + objName);
    	synchronized(this)
    	{
    		cacheTimeMap.put(objName, new Long(System.currentTimeMillis()));
    	}
    	
    } // monitor
	

    /**
     * Thread that wakes up every once in awhile and checks for cached items that have expired.
     * If so, notify the ReferenceDataCache cache owner with expire/evict notices.
     */
	public void run()
	{
		
		log.debug("Wake Up!!!");
		
    	synchronized(this)
    	{
    		long timeNow = System.currentTimeMillis();
    		Iterator<String> itr = cacheTimeMap.keySet().iterator();
    		
    		while (itr.hasNext())
    		{
    			String key = (String) itr.next();
    			long timeCached = cacheTimeMap.get(key).longValue();
    			
    			if (timeNow - timeCached > cacheTTL)
    			{
    				log.debug("Expire " + key);
    				referenceDataCache.expire(key);
    				itr.remove();
    			}
    		}
    	}
		
	} // run
	
} // class ReferenceDataCacheMonitor
