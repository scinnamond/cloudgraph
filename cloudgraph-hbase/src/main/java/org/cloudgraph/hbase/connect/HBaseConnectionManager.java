package org.cloudgraph.hbase.connect;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.cloudgraph.CloudGraphConstants;
import org.cloudgraph.connect.CloudGraphContext;
import org.cloudgraph.connect.ConnectonException;


/**
 */
public class HBaseConnectionManager {

	private static final Log log = LogFactory.getLog(HBaseConnectionManager.class);

    private static HBaseConnectionManager instance;
	private static HTablePool _pool = null;

	private HBaseConnectionManager() {		

		
		Configuration config = CloudGraphContext.instance().getConfig();
		
	    int poolMinSize = Integer.valueOf(config.get("org.plasma.sdo.access.provider.hbase.ConnectionPoolMinSize"));
	    int poolMaxSize = Integer.valueOf(config.get("org.plasma.sdo.access.provider.hbase.ConnectionPoolMaxSize"));	    

		if (log.isDebugEnabled())
		    log.debug("trying to connect to database...");
		try {
			_pool = new HTablePool(config, poolMaxSize);
			

			log.debug("Connection attempt to database succeeded.");
		} catch (Exception e) {
			log.error("Error when attempting to connect to DB ", e);
		}
		finally {
		}
	}

	public static HBaseConnectionManager instance()
    {
        if (instance == null)
            initInstance(); // double-checked locking pattern 
        return instance;     
    }

    private static synchronized void initInstance()
    {
        if (instance == null)
            instance = new HBaseConnectionManager();
    }
 
	protected void finalize() {
		log.debug("Finalizing ConnectionManager");
		try {
			super.finalize();
		} catch (Throwable ex) {
			log.error("ConnectionManager finalize failed to disconnect: ", ex);
		}
	}

	public HTableInterface getConnection(String tableName) 
	{	
		HTableInterface result = null;
		try {
		    result = getPooledTable(tableName);
	    }
		catch (TableNotFoundException e) {
			createTable(tableName);
			try {
				result = getPooledTable(tableName);
			} catch (TableNotFoundException e1) {
				throw new ConnectonException(e1);
			}
		}
		catch (Throwable t) {
			if (t.getCause() instanceof TableNotFoundException) 
			{
				createTable(tableName);
				try {
					result = getPooledTable(tableName);
				} catch (TableNotFoundException e1) {
					throw new ConnectonException(e1);
				}
			}
			else
			    throw new ConnectonException(t);
		}
		if (result.isAutoFlush())
			log.warn("table " + tableName + " set to auto-flush");
	    return result;
	}
	
	private HTableInterface getPooledTable(String tableName) 
	    throws TableNotFoundException
	{
		HTableInterface result = _pool.getTable(tableName);
        return result;
	}
	
	private void createTable(String tableName) {

    	try {
    		HBaseAdmin hbase = new HBaseAdmin(CloudGraphContext.instance().getConfig());
	    	HTableDescriptor tableDesc = new HTableDescriptor(tableName);
	    	HColumnDescriptor fam1 = new HColumnDescriptor(CloudGraphConstants.DATA_TABLE_FAMILY_1.getBytes());
	    	//HColumnDescriptor fam2 = new HColumnDescriptor(HGraphConstants.DATA_TABLE_FAMILY_2.getBytes());
	    	//fam2.setBloomFilterType(bt);
	    	tableDesc.addFamily(fam1);
	    	//tableDesc.addFamily(fam2);
			hbase.createTable(tableDesc);
		} catch (MasterNotRunningException e1) {
			throw new ConnectonException(e1);
		} catch (ZooKeeperConnectionException e1) {
			throw new ConnectonException(e1);
		} catch (IOException e) {
			throw new ConnectonException(e);
		} 	
    }

}
