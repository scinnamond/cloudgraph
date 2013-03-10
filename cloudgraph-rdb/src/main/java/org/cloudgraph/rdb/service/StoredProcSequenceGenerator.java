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
package org.cloudgraph.rdb.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.rdb.connect.RDBConnectionManager;
import org.plasma.config.DataAccessProviderName;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.SequenceConfiguration;
import org.plasma.sdo.Alias;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.SequenceGenerator;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class StoredProcSequenceGenerator 
   implements SequenceGenerator 
{
    private static Log log = LogFactory.getLog(StoredProcSequenceGenerator.class);
	private Connection conn;
	
    public StoredProcSequenceGenerator() {       
    }

    public Long get(DataObject dataObject) {
        return getSeqNum(getSeqName(dataObject.getType()));
    }   
    
    private Long getSeqNum(String seqName)
    {
        CallableStatement cstmt1 = null;
        try { 
            if (conn == null)                                          
            	initialize();
            
            cstmt1 =(CallableStatement)                                
                    conn.prepareCall ("{ call GET_SQNC_NMBR (?, ?) }");  
            cstmt1.registerOutParameter (2, Types.NUMERIC);            
            cstmt1.setString (1, seqName);
            cstmt1.execute (); 
            long id = cstmt1.getLong (2);
            return new Long(id);    
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }        
        finally {
            if (cstmt1 != null)
                try {
                    cstmt1.close();    
                }  
                catch (Throwable t2) {
                }           
        }       
    }
    
    private String getSeqName(Type type)
    {
		SequenceConfiguration config = PlasmaConfig.getInstance().getDataAccessProvider(DataAccessProviderName.JDBC).getSequenceConfiguration();

        Alias alias = ((PlasmaType)type).getAlias();
        if (alias == null)
        	throw new RuntimeException("type has no alias, " 
        			+ type.getURI() + "#" + type.getName());
	    StringBuilder buf = new StringBuilder();
		if (config.getPrefix() != null && config.getPrefix().trim().length() > 0) {
	        buf.append(config.getPrefix());
	    }
	    buf.append(alias.getPhysicalName());
	    if (config.getSuffix() != null && config.getSuffix().trim().length() > 0) {
	        buf.append(config.getSuffix());
	    }
        
        return buf.toString();
    }

	@Override
	public void initialize() {
		try {
			this.conn = RDBConnectionManager.instance().getConnection();
		} catch (SQLException e2) {
            throw new DataAccessException(e2);
		}
	}

	@Override
	public void close() {
		try {
			if (this.conn != null) {
			    this.conn.close(); // return to pool
			    this.conn = null;
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
	}
}
