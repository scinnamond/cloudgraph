package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * An XML oriented input format based on <code>FileInputFormat</code>
 * 
 * @see org.cloudgraph.hbase.mapreduce.GraphXmlRecordReader
 * @see org.cloudgraph.hbase.mapreduce.GraphWritable
 * 
 * @author Scott Cinnamond
 * @since 0.5.8
 */
public class GraphXmlInputFormat extends FileInputFormat<LongWritable, GraphWritable> implements
    Configurable {
	
	public static final String ROOT_ELEM_NAMESPACE_URI = "cloudgraph.hbase.mapreduce.root.namespace.uri";
	public static final String ROOT_ELEM_NAMESPACE_PREFIX = "cloudgraph.hbase.mapreduce.root.namespace.prefix";
	
	/** The configuration. */
	private Configuration conf = null;

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration configuration) {
		this.conf = configuration;
	}
		
    @Override
	public RecordReader<LongWritable, GraphWritable> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException,
			InterruptedException {
    	GraphXmlRecordReader reader = new GraphXmlRecordReader();
		try {
			reader.initialize(split, context);
		} catch (InterruptedException e) {
			throw new InterruptedIOException(e.getMessage());
		}
		
		return reader;
	}

}
