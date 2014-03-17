package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;

import commonj.sdo.DataGraph;

/**
 * Supports tracking and commit of modifications to arbitrary data graphs at various stages 
 * of a <code>Job</code>.
 * @author Scott Cinnamond
 * @since 0.5.8
 * 
 * @see GraphWritable
 * @see GraphMapper
 * @see GraphReducer
 */
public interface GraphMutator {
	public void commit(DataGraph graph, JobContext context) throws IOException;
}
