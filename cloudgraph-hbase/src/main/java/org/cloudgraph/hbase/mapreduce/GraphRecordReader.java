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

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.cloudgraph.mapreduce.GraphWritable;

/**
 * Iterates over HBase root table data for the current <code>TableSplit</code>, assembling data graphs based on the detailed selection criteria within a 
 * given <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>. Partially or fully
 * assembled data graphs may be passed to a {@link GraphRecordRecognizer} and potentially screened from
 * client {@link GraphMapper} extensions potentially illuminating business logic dedicated to identifying
 * specific records.   
 */
public class GraphRecordReader extends
		RecordReader<ImmutableBytesWritable, GraphWritable> {

	private GraphRecordRecognizer recordReaderImpl = new GraphRecordRecognizer();

	/**
	 * Restart from survivable exceptions by creating a new scanner.
	 * 
	 * @param firstRow
	 *            The first row to start at.
	 * @throws IOException
	 *             When restarting fails.
	 */
	public void restart(byte[] firstRow) throws IOException {
		this.recordReaderImpl.restart(firstRow);
	}

	/**
	 * Sets the HBase table.
	 * 
	 * @param htable
	 *            The {@link HTable} to scan.
	 */
	public void setHTable(HTable htable) {
		this.recordReaderImpl.setHTable(htable);
	}

	/**
	 * Sets the scan defining the actual details like columns etc.
	 * 
	 * @param scan
	 *            The scan to set.
	 */
	public void setScan(Scan scan) {
		this.recordReaderImpl.setScan(scan);
	}

	/**
	 * Closes the split.
	 * 
	 * @see org.apache.hadoop.mapreduce.RecordReader#close()
	 */
	@Override
	public void close() {
		this.recordReaderImpl.close();
	}

	/**
	 * Returns the current key.
	 * 
	 * @return The current key.
	 * @throws IOException
	 * @throws InterruptedException
	 *             When the job is aborted.
	 * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()
	 */
	@Override
	public ImmutableBytesWritable getCurrentKey() throws IOException,
			InterruptedException {
		return this.recordReaderImpl.getCurrentKey();
	}

	/**
	 * Returns the current value.
	 * 
	 * @return The current value.
	 * @throws IOException
	 *             When the value is faulty.
	 * @throws InterruptedException
	 *             When the job is aborted.
	 * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()
	 */
	@Override
	public GraphWritable getCurrentValue() throws IOException,
			InterruptedException {
		return this.recordReaderImpl.getCurrentValue();
	}

	/**
	 * Initializes the reader.
	 * 
	 * @param inputsplit
	 *            The split to work with.
	 * @param context
	 *            The current task context.
	 * @throws IOException
	 *             When setting up the reader fails.
	 * @throws InterruptedException
	 *             When the job is aborted.
	 * @see org.apache.hadoop.mapreduce.RecordReader#initialize(org.apache.hadoop.mapreduce.InputSplit,
	 *      org.apache.hadoop.mapreduce.TaskAttemptContext)
	 */
	@Override
	public void initialize(InputSplit inputsplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.recordReaderImpl.initialize(inputsplit, context);
	}

	/**
	 * Positions the record reader to the next record.
	 * 
	 * @return <code>true</code> if there was another record.
	 * @throws IOException
	 *             When reading the record failed.
	 * @throws InterruptedException
	 *             When the job was aborted.
	 * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()
	 */
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		return this.recordReaderImpl.nextKeyValue();
	}

	/**
	 * The current progress of the record reader through its data.
	 * 
	 * @return A number between 0.0 and 1.0, the fraction of the data read.
	 * @see org.apache.hadoop.mapreduce.RecordReader#getProgress()
	 */
	@Override
	public float getProgress() {
		return this.recordReaderImpl.getProgress();
	}
}
