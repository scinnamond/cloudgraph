package org.cloudgraph.hbase.mapreduce;

public interface Counters {
	/** Name of mapreduce counter group for CloudGraph */
	public static final String CLOUDGRAPH_COUNTER_GROUP_NAME = "CloudGraph Counters";
	
	/** 
	 * MapReduce counter which stores the number of data graphs successfully 
	 * recognized by the current recognizer. Will remain zero if no recognizer
	 * is required for the current query */
	public static final String CLOUDGRAPH_COUNTER_NAME_NUM_RECOGNIZED_GRAPHS = "NUM_RECOGNIZED_GRAPHS";
	
	/** 
	 * MapReduce counter which stores the number of data graphs not recognized by the 
	 * current recognizer. Will remain zero if no recognizer
	 * is required for the current query */
	public static final String CLOUDGRAPH_COUNTER_NAME_NUM_UNRECOGNIZED_GRAPHS = "NUM_UNRECOGNIZED_GRAPHS";
	
	/** MapReduce counter which stores the total number of graph nodes assembled */
	public static final String CLOUDGRAPH_COUNTER_NAME_NUM_GRAPH_NODES_ASSEMBLED = "NUM_GRAPH_NODES_ASSEMBLED";
	
	/** MapReduce counter which stores the total time in milliseconds taken for graph assembly. Note: this
	 * time counter is summed across all tasks on all hosts so could exceed the total time taken for
	 * the job, as the tasks of course run in parallel.  */
	public static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_ASSEMBLY_TIME = "MILLIS_GRAPH_ASSEMBLY";	

	/** MapReduce counter which stores the total time in milliseconds taken for graph recognition */
	public static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_RECOG_TIME = "MILLIS_GRAPH_RECOGNITION";	

	/** MapReduce counter which stores the total time in milliseconds taken for graph XMl unmarshalling. Note: this
	 * time counter is summed across all tasks on all hosts so could exceed the total time taken for
	 * the job, as the tasks of course run in parallel. */
	public static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_XML_UNMARSHAL_TIME = "MILLIS_GRAPH_XML_UNMARSHAL";
	
	/** MapReduce counter which stores the total time in milliseconds taken for graph XMl marshalling. Note: this
	 * time counter is summed across all tasks on all hosts so could exceed the total time taken for
	 * the job, as the tasks of course run in parallel. */
	public static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_XML_MARSHAL_TIME = "MILLIS_GRAPH_XML_MARSHAL";
	
    /** HBase Counter Group */
	public static final String HBASE_COUNTER_GROUP_NAME = "HBase Counters";
	
	/** Scan restarts counter for HBase */
	public static final String HBASE_COUNTER_NAME_NUM_SCANNER_RESTARTS = "NUM_SCANNER_RESTARTS";

}
