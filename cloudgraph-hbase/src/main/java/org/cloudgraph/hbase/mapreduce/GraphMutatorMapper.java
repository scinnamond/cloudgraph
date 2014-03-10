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
package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.hbase.io.FederatedWriter;
import org.cloudgraph.hbase.io.TableWriter;
import org.cloudgraph.hbase.service.GraphDispatcher;
import org.cloudgraph.hbase.service.MutationCollector;
import org.cloudgraph.hbase.service.ServiceContext;
import org.cloudgraph.state.StateMarshallingContext;
import org.cloudgraph.state.StatelNonValidatinglDataBinding;
import org.plasma.sdo.core.SnapshotMap;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;


/**
 * Supports detection of changes to the input data graph, and propagation of table mutations
 * to the underlying HBase table(s). For <code>Mapper</code> clients wishing to modify input graphs and 
 * commit changes within the map phase. See the below code sample based on the Wikipedia domain model
 * which adds a page link to each input graph.  
 *<p>
 *<pre>
 *public class PageLinkAdder extends GraphMutatorMapper<ImmutableBytesWritable, DataGraph> {
 *    @Override
 *    public void map(ImmutableBytesWritable row, GraphWritable graph, Context context) throws IOException {
 *    
 *        // track changes
 *        graph.getDataGraph().getChangeSummary().beginLogging();
 *    
 *        Page page = (Page)graph.getDataGraph().getRootObject();
 *        Categorylinks link = page.createCategorylinks();
 *        link.setClTo("Some Category Page");
 *        link.setClTimestamp((new Date()).toString());
 *
 *        // commit above changes
 *        super.commit(row, graph, context);
 *    }
 *}
 *</pre>
 *</p>
 * <p>
 * Data graphs of any size of complexity may be supplied to MapReduce jobs including graphs where the underlying
 * domain model contains instances of multiple inheritance. The set of data graphs is provided to
 * a MapReduce job using a <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>, typically 
 * supplied using {@link GraphMapReduceSetup}.    
 * </p>
 * <p>
 * Data graphs are assembled within a {@link GraphRecordReader} based on the detailed selection criteria within a given <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>, and
 * may be passed to a {@link GraphRecordRecognizer} and potentially screened from
 * client {@link GraphMapper} extensions potentially illuminating business logic dedicated to identifying
 * specific records.   
 * </p>
 * 
 * @param <KEYOUT> the output key type
 * @param <VALUEOUT> the output value type
 * 
 * @see org.cloudgraph.hbase.mapreduce.GraphWritable
 * @see org.cloudgraph.hbase.mapreduce.GraphRecordReader
 * @see org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup
 */
public class GraphMutatorMapper<KEYOUT, VALUEOUT>
    extends GraphMapper<KEYOUT, VALUEOUT> implements GraphMutator {
	
    private static Log log = LogFactory.getLog(GraphMutatorMapper.class);
    private ServiceContext context;
	
	public GraphMutatorMapper() {
    	try {
			StateMarshallingContext marshallingContext = new StateMarshallingContext(
					new StatelNonValidatinglDataBinding());
	    	this.context = new ServiceContext(marshallingContext);
		} catch (JAXBException e) {
			throw new GraphServiceException(e);
		} catch (SAXException e) {
			throw new GraphServiceException(e);
		}    	
	}	
	 
	@Override
	public void commit(DataGraph graph,
			JobContext jobContext) throws IOException {

        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
		MutationCollector collector = new MutationCollector(this.context,
				snapshotMap, jobContext.getJobName());

		Map<TableWriter, List<Row>> mutations = new HashMap<TableWriter, List<Row>>();
		try {
			mutations = collector.collectChanges(graph);
		} catch (IllegalAccessException e) {
			throw new GraphServiceException(e);
		}
		Iterator<TableWriter> iter = mutations.keySet().iterator();
		while (iter.hasNext()) {
			TableWriter tableWriter = iter.next();
			List<Row> tableMutations = mutations.get(tableWriter);
			//if (log.isDebugEnabled())
				log.info("commiting "+tableMutations.size()+" mutations to table: " + tableWriter.getTable().getName());
			try {
				tableWriter.getConnection().batch(tableMutations);
			} catch (InterruptedException e) {
				log.info(e.getMessage(), e);
			}
			tableWriter.getConnection().flushCommits();
			/*
			for (Row mutation : tableMutations)
				try {
					context.write(row, (Mutation)mutation);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    */
		}
	}
}
