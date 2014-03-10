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

/**
 * Supplies fully realized data {@link GraphWritable graphs} as the input value to MapReduce <code>Mapper</code> 
 * client subclasses, the input key being an HBase row key <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/io/ImmutableBytesWritable.html">bytes</a>. 
 * 
 * Any <code>Mapper</code> clients wishing to modify input graphs and commit changes within the
 * map phase should extend {@link GraphMutatorMapper} which 
 * supports detection of changes to the input data graph, and propagation of table mutations
 * to the underlying HBase table(s).  
 * 
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
 * @see org.cloudgraph.hbase.mapreduce.GraphMutatorMapper
 * @see org.cloudgraph.hbase.mapreduce.GraphRecordReader
 * @see org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup
 * 
 * @author Scott Cinnamond
 * @since 0.5.8
 */
public abstract class GraphMapper<KEYOUT, VALUEOUT>
extends Mapper<ImmutableBytesWritable, GraphWritable, KEYOUT, VALUEOUT> {
	
    private static Log log = LogFactory.getLog(GraphMapper.class);
	
	public GraphMapper() {
	}
	
	@Override
	public void map(ImmutableBytesWritable row, GraphWritable graph,
			Context context) throws IOException {
        //
	}
}
