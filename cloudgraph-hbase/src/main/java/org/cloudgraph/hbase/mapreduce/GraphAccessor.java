package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;
import org.plasma.query.Query;

import commonj.sdo.DataGraph;

/**
 * Supports access to arbitrary data graphs at various stages of a <code>Job</code> as supplied
 * based on the given <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>.
 * @author Scott Cinnamond
 * @since 0.5.8
 * 
 * @see GraphWritable
 * @see GraphMapper
 * @see GraphReducer
 */
public interface GraphAccessor {
	public DataGraph[] find(Query query, JobContext context) throws IOException;
}
