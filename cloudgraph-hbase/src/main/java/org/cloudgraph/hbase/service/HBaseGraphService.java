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
package org.cloudgraph.hbase.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfigProp;
import org.cloudgraph.hbase.graph.GraphAssembler;
import org.cloudgraph.hbase.graph.ParallelGraphAssembler;
import org.cloudgraph.state.ConcurrentNonValidatingDataBinding;
import org.cloudgraph.state.PooledStateMarshallingContext;
import org.cloudgraph.state.SimpleStateMarshallingContext;
import org.cloudgraph.state.StateDataBindingFactory;
import org.cloudgraph.state.StateNonValidatingDataBinding;
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

import commonj.sdo.DataGraph;
import commonj.sdo.Type;

/**
 * Top level provider service implementing the
 * {@link org.plasma.sdo.access.DataAccessService DataAccessService } 
 * interface and delegating to {@link GraphQuery} for serving
 * data from HBase back to the client, and {@link GraphDispatcher} for
 * propagating changes to one or more data graphs back to HBase. 
 * <p>
 * CloudGraph&#8482; is based on the Service Data Objects (SDO) 2.1 specification 
 * and is designed as a suite of SDO Data Access Services (DAS) under the 
 * PlasmaSDO&#8482; Service Data Objects implementation. 
 * </p> 
 * <p>
 * Typical CRUD operations are provided across any full or partial 
 * data graph, and any "slice" of a graph or set of
 * sub-graph slices can be returned using 
 * common SDO query mechanisms including XPath and others.     
 * </p>
 * @see org.plasma.sdo.access.DataAccessService
 * @see GraphQuery
 * @see GraphDispatcher
 * @author Scott Cinnamond
 * @since 0.5
 */
public class HBaseGraphService implements PlasmaDataAccessService {
    
    private static Log log = LogFactory.getLog(HBaseGraphService.class);
    private ServiceContext context;
    
    public HBaseGraphService() {
    	try {
    		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    		config.setMaxTotal(40);
    		config.setMinIdle(40);       	
	    	this.context = new ServiceContext(new PooledStateMarshallingContext(
	    			config, new StateDataBindingFactory()));
		} catch (Exception e) {
			throw new GraphServiceException(e);
		}   	
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
        GraphQuery dispatcher = new GraphQuery(this.context);
        return dispatcher.count(query);
    }
    
    public int[] count(Query[] queries) {
        if (queries == null)
            throw new IllegalArgumentException("expected non-null 'queries' argument");
        int[] counts = new int[queries.length];
        for (int i = 0; i < queries.length; i++)
            counts[i] = count(queries[i]);
        return counts;
    }

    public DataGraph[] find(Query query) {
        if (query == null)
            throw new IllegalArgumentException("expected non-null 'query' argument");
        //validate(query);
        if (log.isDebugEnabled()) {
            log(query);
        }
        GraphQuery dispatcher = new GraphQuery(this.context);
        Timestamp snapshotDate = new Timestamp((new Date()).getTime());
        return dispatcher.find(query, snapshotDate);
    }

    public DataGraph[] find(Query query, int maxResults) {
        if (query == null)
            throw new IllegalArgumentException("expected non-null 'query' argument");
        validate(query);
        if (log.isDebugEnabled()) {
            log(query);
        }
        GraphQuery dispatcher = new GraphQuery(this.context);
        DataGraph[] results = dispatcher.find(query, -1, new Timestamp((new Date()).getTime()));
        return results;
    }

    public List<DataGraph[]> find(Query[] queries) {
        if (queries == null)
            throw new IllegalArgumentException("expected non-null 'queries' argument");
        GraphQuery dispatcher = new GraphQuery(this.context);
        List<DataGraph[]> list = new ArrayList<DataGraph[]>();
        Timestamp snapshotDate = new Timestamp((new Date()).getTime());
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

    public SnapshotMap commit(DataGraph dataGraph, String username) {
        if (dataGraph == null)
            throw new IllegalArgumentException("expected non-null 'dataGraph' argument");
        if (username == null)
            throw new IllegalArgumentException("expected non-null 'username' argument");
        if (username.trim().length() == 0)
            throw new IllegalArgumentException("unexpected zero length 'username' argument");
        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
        DataGraphDispatcher dispatcher = new GraphDispatcher(this.context,
        		snapshotMap, 
                username);
        try {
            dispatcher.commit(dataGraph);
            //con.commit();
            return snapshotMap;
        }
        catch (DataAccessException e) {
            if (log.isDebugEnabled())
                log.debug(e.getMessage(), e);
			//con.rollback();
            throw e;
        }
        catch (Throwable t) {
            if (log.isDebugEnabled())
                log.debug(t.getMessage(), t);
			//con.rollback();
            throw new DataAccessException(t);
        }
        finally {
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
        
        try {
            DataGraphDispatcher dispatcher = new GraphDispatcher(this.context,
            		snapshotMap,
                    username);
            try {
                dispatcher.commit(dataGraphs);        
            }
            finally {
                dispatcher.close();
            }
            return snapshotMap;
        }
        catch (DataAccessException e) {
			//con.rollback();
            throw e;
        }
        catch (Throwable t) {
			//con.rollback();
            throw new DataAccessException(t);
        }
        finally {
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
        log.debug("query: " + xml);
    }
}
