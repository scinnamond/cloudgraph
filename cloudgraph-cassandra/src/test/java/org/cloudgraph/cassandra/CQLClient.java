package org.cloudgraph.cassandra;

import java.util.Iterator;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

public class CQLClient {
	public static void main(String[] args) throws Exception {
		
        int concurrency = 50;
        final int maxRequestsPerConnection = 128;
        int maxConnections = concurrency / maxRequestsPerConnection + 1;
        String host = "u16551142.onlinehome-server.com";
		
        PoolingOptions pools = new PoolingOptions();
        pools.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, concurrency);
        pools.setCoreConnectionsPerHost(HostDistance.LOCAL, maxConnections);
        pools.setMaxConnectionsPerHost(HostDistance.LOCAL, maxConnections);
        pools.setCoreConnectionsPerHost(HostDistance.REMOTE, maxConnections);
        pools.setMaxConnectionsPerHost(HostDistance.REMOTE, maxConnections);
        
        
        // Create session to hosts
        Cluster cluster = 
        	new Cluster.Builder()
                       .addContactPoints(host)
                       .withPoolingOptions(pools)
                       .withSocketOptions(new SocketOptions().setTcpNoDelay(true))
                       .withLoadBalancingPolicy(new DCAwareRoundRobinPolicy())
                       .build();
        
        //cluster.getConfiguration().getProtocolOptions().setCompression(ProtocolOptions.Compression.SNAPPY);

        Session session = cluster.connect();

        Metadata metadata = cluster.getMetadata();
        System.out.println(String.format("Connected to cluster '%s' on %s.", metadata.getClusterName(), metadata.getAllHosts()));

        for (KeyspaceMetadata keyspaceMeta : metadata.getKeyspaces()) {
        	for (TableMetadata tableMeta : keyspaceMeta.getTables()) {
        		for (ColumnMetadata columnMeta : tableMeta.getColumns()) {
        			 
        		}
        		//tableMeta.getPartitionKey()
        		//tableMeta.getPrimaryKey()
        		//tableMeta.getOptions()
        	}
        }
        
        try {
            session.execute("DROP KEYSPACE cqlclient;");
        } catch (QueryValidationException e) { e.printStackTrace(); }

        try {
            session.execute("CREATE KEYSPACE cqlclient WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        } catch (QueryValidationException e) { e.printStackTrace(); }
        
        session.execute("USE cqlclient;");
        StringBuilder sb = new StringBuilder();
        try {
        	sb = new StringBuilder();
            sb.append("DROP TABLE user;");        
            session.execute(sb.toString());
        } catch (QueryValidationException e) { e.printStackTrace(); }
        
        try {
        	sb = new StringBuilder();
            sb.append("CREATE TABLE user (key bigint PRIMARY KEY, name text, age int);");        
            session.execute(sb.toString());
        } catch (QueryValidationException e) { e.printStackTrace(); }
        
        sb = new StringBuilder();
        sb.append("UPDATE user SET name='hank', age=34 WHERE key=1;");
        SimpleStatement statement = new SimpleStatement(sb.toString());
        ResultSet rs = session.execute(statement);
        
        sb = new StringBuilder();
        sb.append("INSERT INTO user (name, age, key) VALUES ('galye', 44, 2);");
        statement = new SimpleStatement(sb.toString());
        rs = session.execute(statement);
        
        sb = new StringBuilder();
        sb.append("SELECT * FROM user");
        statement = new SimpleStatement(sb.toString());
        rs = session.execute(statement);
       
        for (ColumnDefinitions.Definition def : rs.getColumnDefinitions()) {
        	System.out.println(def.getKeyspace() + ":" + def.getTable() + ":" + def.getType() + ":" + def.getName());
        }
        
        Iterator<Row> iter = rs.iterator();
        while (iter.hasNext()) {
        	Row row = iter.next();
        	 
        	
        	System.out.println(row);
        }
        
        
        System.exit(0);
        
	}
}
