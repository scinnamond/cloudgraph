package org.cloudgraph.cassandra.connect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.config.DataAccessProvider;
import org.plasma.config.DataAccessProviderName;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.Property;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

public class CassandraConnectionManager {
	private static final Log log = LogFactory.getLog(CassandraConnectionManager.class);
	
	/** single or space separated list of contact hosts or IP's */
	public static final String CONNECTION_HOSTS = "org.plasma.sdo.access.provider.cassandra.ContactHosts";
	/** maximum connections per host */
	public static final String CONNECTION_MAX_REQUESTS = "org.plasma.sdo.access.provider.cassandra.ConnectionMaxRequests";
	/** the maximum simultaneous requests per connection */
	public static final String CONNECTION_CONCURRENCY = "org.plasma.sdo.access.provider.cassandra.ConnectionConcurrency";

    private static CassandraConnectionManager instance;
    private Cluster cluster;

	private CassandraConnectionManager() {		
        int concurrency = 50;
        int maxRequestsPerConnection = 128;
        int maxConnections;

        DataAccessProvider cassandraDas = PlasmaConfig.getInstance().getDataAccessProvider(
        		DataAccessProviderName.CASSANDRA);
        
        Property prop = PlasmaConfig.getInstance().findProviderProperty(cassandraDas, CONNECTION_MAX_REQUESTS);		
        if (prop != null) {
        	maxRequestsPerConnection = Integer.valueOf(prop.getValue());
        }
        prop = PlasmaConfig.getInstance().findProviderProperty(cassandraDas, CONNECTION_CONCURRENCY);		
        if (prop != null) {
        	concurrency = Integer.valueOf(prop.getValue());
        }        
        maxConnections = concurrency / maxRequestsPerConnection + 1;

        String host = null;
        prop = PlasmaConfig.getInstance().findProviderProperty(cassandraDas, CONNECTION_HOSTS);		
        if (prop != null) {
        	host = prop.getValue();
        	if (host == null || host.trim().length() == 0)
        	    throw new GraphServiceException("expected property '" + CONNECTION_HOSTS + "' for data access provider: " + 
        			cassandraDas.getName().name());
        }
        else
        	throw new GraphServiceException("expected property '" + CONNECTION_HOSTS + "' for data access provider: " + 
        			cassandraDas.getName().name());
        
        PoolingOptions pools = new PoolingOptions();
        pools.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, concurrency);
        pools.setCoreConnectionsPerHost(HostDistance.LOCAL, maxConnections);
        pools.setMaxConnectionsPerHost(HostDistance.LOCAL, maxConnections);
        pools.setCoreConnectionsPerHost(HostDistance.REMOTE, maxConnections);
        pools.setMaxConnectionsPerHost(HostDistance.REMOTE, maxConnections);
        
        this.cluster =  new Cluster.Builder()
                       .addContactPoints(host)
                       .withPoolingOptions(pools)
                       .withSocketOptions(new SocketOptions().setTcpNoDelay(true))
                       .withLoadBalancingPolicy(new DCAwareRoundRobinPolicy())
                       .build();
		
	}
	
	public Session getSession() {
		return cluster.connect();
	}

	public static CassandraConnectionManager instance()
    {
        if (instance == null)
            initInstance();  
        return instance;     
    }

    private static synchronized void initInstance()
    {
        if (instance == null)
            instance = new CassandraConnectionManager();
    }
 	
}
