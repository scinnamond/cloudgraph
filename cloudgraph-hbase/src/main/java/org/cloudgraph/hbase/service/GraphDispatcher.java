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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.hbase.io.DistributedGraphWriter;
import org.cloudgraph.hbase.io.DistributedWriter;
import org.cloudgraph.hbase.io.RowWriter;
import org.cloudgraph.hbase.io.TableWriter;
import org.cloudgraph.hbase.io.TableWriterCollector;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.cloudgraph.store.service.CreatedCommitComparator;
import org.cloudgraph.store.service.DuplicateRowException;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaSetting;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphDispatcher;
import org.plasma.sdo.access.RequiredPropertyException;
import org.plasma.sdo.access.provider.common.DeletedObjectCollector;
import org.plasma.sdo.access.provider.common.ModifiedObjectCollector;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.core.NullValue;
import org.plasma.sdo.core.SnapshotMap;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.profile.ConcurrencyType;
import org.plasma.sdo.profile.ConcurrentDataFlavor;
import org.plasma.sdo.profile.KeyType;

import sorts.InsertionSort;
import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * Propagates changes to a {@link commonj.sdo.DataGraph data graph} including
 * any number of creates (inserts), modifications (updates) and deletes
 * across one or more HBase table rows. 
 * <p>
 * For new (created) data graphs, a row key {org.cloudgraph.hbase.key.HBaseRowKeyFactory factory} 
 * is used to create a new composite HBase row key. The row key generation is
 * driven by a configured CloudGraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}.
 * A minimal set of {@link org.cloudgraph.state.GraphState state} information is 
 * persisted with each new data graph.     
 * </p>
 * <p>
 * For data graphs with any other combination of changes, e.g. 
 * data object modifications, deletes, etc... an existing HBase
 * row key is fetched using an HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html" target="#">Get</a> 
 * operation.
 * </p>
 * @see org.cloudgraph.hbase.io.DistributedWriter
 * @see org.cloudgraph.store.key.GraphRowKeyFactory
 * @see org.cloudgraph.store.key.GraphColumnKeyFactory
 * @see org.cloudgraph.state.GraphState
 * 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphDispatcher extends MutationCollector
    implements DataGraphDispatcher 
{
    private static Log log = LogFactory.getLog(GraphDispatcher.class);
        
    
	public GraphDispatcher(ServiceContext context, SnapshotMap snapshotMap,
			String username) {
		super(context, snapshotMap, username);
	}

	public void close()
    {
    }
     
    /**
     * Propagates changes to the given <a href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html" target="#">data graph</a> including
     * any number of creates (inserts), modifications (updates) and deletes
     * to a single or multiple HBase tables and table rows. 
     * @return a map of internally managed concurrency property values and data 
     * store generated keys.
     * @throws DuplicateRowException if for a new data graph, the generated row key
     * already exists in the HBase table configured . 
     */
    public SnapshotMap commit(DataGraph dataGraph) {
        
    	try {
    		Map<TableWriter, List<Row>> mutations = collectChanges(dataGraph);
    		Iterator<TableWriter> iter = mutations.keySet().iterator();
    		while (iter.hasNext()) {
    			TableWriter tableWriter = iter.next();
    			List<Row> tableMutations = mutations.get(tableWriter);
    			if (log.isDebugEnabled())
    				log.debug("commiting "+tableMutations.size()+" mutations to table: " + tableWriter.getTable().getName());
    			tableWriter.getConnection().batch(tableMutations);
    			tableWriter.getConnection().flushCommits();
    		}
    		
            return snapshotMap;
        }                                                         
        catch(IOException e) {                         
            throw new DataAccessException(e);                         
        }                                                         
        catch(InterruptedException e) {                         
            throw new DataAccessException(e);                         
        }        
        catch(IllegalAccessException e) {                         
            throw new DataAccessException(e);                         
        }               
    }
 
    /**
     * Propagates changes to the given array of <a href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html" target="#">data graphs</a> including
     * any number of creates (inserts), modifications (updates) and deletes
     * to a single or multiple HBase tables and table rows. The given graphs may be heterogeneous, with
     * different root data objects any 'shape' or depth.  
     * @return a map of internally managed concurrency property values and data 
     * store generated keys.
     * @throws DuplicateRowException if for a new data graph, the generated row key
     * already exists in the HBase table configured . 
     */
    public SnapshotMap commit(DataGraph[] dataGraphs) {
        
    	try {
    		Map<TableWriter, List<Row>> mutations = collectChanges(dataGraphs);
    		Iterator<TableWriter> iter = mutations.keySet().iterator();
    		while (iter.hasNext()) {
    			TableWriter tableWriter = iter.next();
    			List<Row> tableMutations = mutations.get(tableWriter);
    			if (log.isDebugEnabled())
    				log.debug("commiting "+tableMutations.size()+" mutations to table: " + tableWriter.getTable().getName());
    			tableWriter.getConnection().batch(tableMutations);
    			tableWriter.getConnection().flushCommits();
    		}
            return snapshotMap;
        }                                                         
        catch(IOException e) {                         
            throw new DataAccessException(e);                         
        }                                                         
        catch(InterruptedException e) {                         
            throw new DataAccessException(e);                         
        }        
        catch(IllegalAccessException e) {                         
            throw new DataAccessException(e);                         
        }               
    }    
    
}
