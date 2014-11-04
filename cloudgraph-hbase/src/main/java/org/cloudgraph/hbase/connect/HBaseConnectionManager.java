/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
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
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.cloudgraph.state.StateException;


/**
 * Manages HBase table pool and table interface access.
 * @see CloudGraphContext
 * @see TableConfig
 * @author Scott Cinnamond
 * @since 0.5
 */
public class HBaseConnectionManager {
	
	public static final String CONNECTION_POOL_MIN_SIZE = "org.plasma.sdo.access.provider.hbase.ConnectionPoolMinSize";
	public static final String CONNECTION_POOL_MAX_SIZE = "org.plasma.sdo.access.provider.hbase.ConnectionPoolMaxSize";

	private static final Log log = LogFactory.getLog(HBaseConnectionManager.class);

    private static HBaseConnectionManager instance;
	private static HTablePool _pool = null;

	private HBaseConnectionManager() {		

		
		Configuration config = CloudGraphContext.instance().getConfig();
		
	    int poolMinSize = Integer.valueOf(config.get(CONNECTION_POOL_MIN_SIZE));
	    int poolMaxSize = Integer.valueOf(config.get(CONNECTION_POOL_MAX_SIZE));	    

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
				throw new StateException(e1);
			}
		}
		catch (Throwable t) {
			if (t.getCause() instanceof TableNotFoundException) 
			{
				createTable(tableName);
				try {
					result = getPooledTable(tableName);
				} catch (TableNotFoundException e1) {
					throw new StateException(e1);
				}
			}
			else
			    throw new StateException(t);
		}
		if (log.isDebugEnabled())
		    if (result.isAutoFlush())
			    log.debug("table " + tableName + " set to auto-flush");
	    return result;
	}
	
	private HTableInterface getPooledTable(String tableName) 
	    throws TableNotFoundException
	{
		HTableInterface result = _pool.getTable(tableName);
        return result;
	}
	
	private void createTable(String tableName) {

		HBaseAdmin hbase = null;
    	try {
    		hbase = new HBaseAdmin(CloudGraphContext.instance().getConfig());
	    	
    		TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(tableName);
    		HTableDescriptor tableDesc = new HTableDescriptor(tableConfig.getName());
	    	HColumnDescriptor fam1 = new HColumnDescriptor(tableConfig.getDataColumnFamilyName().getBytes());
	    	 
	    	//HColumnDescriptor fam2 = new HColumnDescriptor(CloudGraphConstants.DATA_TABLE_FAMILY_2.getBytes());
	    	//fam2.setBloomFilterType(bt);
	    	tableDesc.addFamily(fam1);
	    	//tableDesc.addFamily(fam2);
			hbase.createTable(tableDesc);
		} catch (MasterNotRunningException e1) {
			throw new StateException(e1);
		} catch (ZooKeeperConnectionException e1) {
			throw new StateException(e1);
		} catch (IOException e) {
			throw new StateException(e);
		} finally {
			if (hbase != null)
				try {
					hbase.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
		}
    }

}
