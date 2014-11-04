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
package org.cloudgraph.cassandra.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;
import org.cloudgraph.cassandra.connect.CassandraConnectionManager;
import org.cloudgraph.mapreduce.GraphService;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.QueryValidator;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphDispatcher;
import org.plasma.sdo.access.PlasmaDataAccessService;
import org.plasma.sdo.core.SnapshotMap;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import com.datastax.driver.core.Session;

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

public class CassandraGraphService implements PlasmaDataAccessService, GraphService {
    
    private static Log log = LogFactory.getLog(CassandraGraphService.class);

    public CassandraGraphService() {
    }
 
    public void initialize() {}
    public void close() {}
    
    public int count(Query query) {
        if (query == null)
            throw new IllegalArgumentException("expected non-null 'query' argument");
        validate(query);
        if (log.isDebugEnabled()) {
            log(query);
        }
        Session con = null;
		try {
			if (log.isDebugEnabled())
				log.debug("getting connection");
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
        GraphQuery dispatcher = new GraphQuery(con);
        try {
            return dispatcher.count(query);
        }
        finally {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
    }
    
    public int[] count(Query[] queries) {
        if (queries == null)
            throw new IllegalArgumentException("expected non-null 'queries' argument");
        Session con = null;
		try {
			if (log.isDebugEnabled())
				log.debug("getting connection");
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
        GraphQuery dispatcher = new GraphQuery(con);
        int[] counts = new int[queries.length];
        try {
            for (int i = 0; i < queries.length; i++)
                counts[i] = dispatcher.count(queries[i]);
            return counts;
        }
        finally {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    public DataGraph[] find(Query query) {
        return find(query, -1);
    }

    public DataGraph[] find(Query query, int maxResults) {
        if (query == null)
            throw new IllegalArgumentException("expected non-null 'query' argument");
        validate(query);
        if (log.isDebugEnabled()) {
            log(query);
        }
        Session con = null;
		try {
			if (log.isDebugEnabled())
				log.debug("getting connection");
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
        GraphQuery dispatcher = new GraphQuery(con);
        try {
        	DataGraph[] results = null;
         
        	if (maxResults > 0)  
                results = dispatcher.find(query, maxResults, new Timestamp((new Date()).getTime()));
            else
            	results = dispatcher.find(query, new Timestamp((new Date()).getTime()));
            return results;
        }
        finally {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    public List<DataGraph[]> find(Query[] queries) {
        if (queries == null)
            throw new IllegalArgumentException("expected non-null 'queries' argument");
        Session con = null;
		try {
			if (log.isDebugEnabled())
				log.debug("getting connection");
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
        GraphQuery dispatcher = new GraphQuery(con);
        List<DataGraph[]> list = new ArrayList<DataGraph[]>();
        Timestamp snapshotDate = new Timestamp((new Date()).getTime());
        try {
	        for (int i = 0; i < queries.length; i++)
	        {
	            validate(queries[i]);
	            if (log.isDebugEnabled()) {
	                log(queries[i]);
	            }
	            DataGraph[] results = dispatcher.find(queries[i], snapshotDate);
	            list.add(results);
	        }
	        return list;
        }
        finally {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    public SnapshotMap commit(DataGraph dataGraph, String username) {
        if (dataGraph == null)
            throw new IllegalArgumentException("expected non-null 'dataGraph' argument");
        if (username == null)
            throw new IllegalArgumentException("expected non-null 'username' argument");
        if (username.trim().length() == 0)
            throw new IllegalArgumentException("unexpected zero length 'username' argument");
        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
		if (log.isDebugEnabled())
			log.debug("getting connection");
        Session con = null;
		try {
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
		DataGraphDispatcher dispatcher = 
        	new GraphDispatcher(snapshotMap, 
                username, con);
        try {
            dispatcher.commit(dataGraph);
            return snapshotMap;
        }
        catch (DataAccessException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	 
        	try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
            dispatcher.close();
        }
    }

    public SnapshotMap commit(DataGraph[] dataGraphs, String username) {
        if (dataGraphs == null)
            throw new IllegalArgumentException("expected non-null 'dataGraphs' argument");
        if (username == null)
            throw new IllegalArgumentException("expected non-null 'username' argument");
        if (username.trim().length() == 0)
            throw new IllegalArgumentException("unexpected zero length 'username' argument");
        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
        Session con = null;
		try {
			if (log.isDebugEnabled())
				log.debug("getting connection");
			con = CassandraConnectionManager.instance().getSession();
		} catch (Exception e2) {
            try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
            throw new DataAccessException(e2);
		}
		DataGraphDispatcher dispatcher = 
        	new GraphDispatcher(snapshotMap, 
                username, con);
       
        try {
            dispatcher.commit(dataGraphs);        
            return snapshotMap;
        }
        catch (DataAccessException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	try {
    			if (log.isDebugEnabled())
    				log.debug("closing connection");
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
            dispatcher.close();
        }
    }

    private void validate(Query query)
    {
        From from = (From)query.getFromClause();
        Type type = PlasmaTypeHelper.INSTANCE.getType(
        		from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        log.debug("validating query");
        new QueryValidator((Query)query, type);
    }
 
    private void log(Query query)
    {
    	String xml = "";
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			        new DefaultValidationEventHandler());
	        xml = binding.marshal(query);
		} catch (JAXBException e) {
		} catch (SAXException e) {
		}
        //log.debug("query: " + xml);
    }

	@Override
	public DataGraph[] find(org.plasma.query.Query query, JobContext context)
			throws IOException {
		 
		return this.find(query.getModel());
	}

	@Override
	public void commit(DataGraph graph, JobContext context) throws IOException {
		this.commit(graph, context.getJobName());
	}
}
