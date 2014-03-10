package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;

import commonj.sdo.DataGraph;

public interface GraphMutator {
	public void commit(DataGraph graph, JobContext context) throws IOException;
}
