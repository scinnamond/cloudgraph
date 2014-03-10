package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;
import org.plasma.query.Query;

import commonj.sdo.DataGraph;

public interface GraphAccessor {
	public DataGraph[] find(Query query, JobContext context) throws IOException;
}
