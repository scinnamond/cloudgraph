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
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableInputFormatBase;
import org.apache.hadoop.hbase.mapreduce.TableSplit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.StringUtils;

/**
 * A graph based input-specification for MapReduce jobs which splits an
 * underlying root table by region and by the scans resulting from a given 
 * <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>, then provides graph record
 * {@link GraphRecordReader readers} for each <a href=
 * "http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/mapreduce/TableSplit.html"
 * >split</a> which assemble and serve data graphs to client {@link GraphMapper
 * mapper} extensions.
 * <p>
 * Data graphs are assembled within a record {@link GraphRecordReader reader}
 * based on the detailed selection criteria within a given <a
 * href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>, and may
 * be passed to a {@link GraphRecordRecognizer recognizer} and potentially
 * screened from client {@link GraphMapper mappers} potentially illuminating
 * business logic dedicated to identifying specific records.
 * </p>
 * <p>
 * A graph recognizer is used only when query expressions are present which
 * reference properties not found in the row key model for a target <a
 * href="http://plasma-sdo.org/commonj/sdo/DataGraph.html">graph</a>.
 * </p>
 * 
 * @see org.apache.hadoop.hbase.mapreduce.TableSplit
 * @see GraphRecordReader
 * @see GraphRecordRecognizer
 * 
 * @author Scott Cinnamond
 * @since 0.5.8
 */
public class GraphInputFormat extends
		InputFormat<ImmutableBytesWritable, GraphWritable> implements
		Configurable {

	static final Log LOG = LogFactory.getLog(MultiTableInputFormatBase.class);

	/**
	 * Serialized query which encapsulates most necessary parameters for a scan
	 * including the input (root) table, the start and stop row, columns and
	 * column families.
	 */
	public static final String QUERY = "cloudgraph.hbase.mapreduce.query";

	/**
	 * Boolean property indicating whether a graph recognizer is necessary for
	 * the current query
	 */
	public static final String RECOGNIZER = "cloudgraph.hbase.mapreduce.recognizer";

	/** Internal Job parameter that specifies the scan list. */
	protected static final String SCANS = "cloudgraph.hbase.mapreduce.scans";

	/**
	 * Internal Job parameter that specifies root the input table as derived
	 * from a deserialized query
	 */
	protected static final String ROOT_TABLE = "cloudgraph.hbase.mapreduce.roottable";

	/** The timestamp used to filter columns with a specific timestamp. */
	protected static final String SCAN_TIMESTAMP = "cloudgraph.hbase.mapreduce.scan.timestamp";
	/**
	 * The starting timestamp used to filter columns with a specific range of
	 * versions.
	 */
	protected static final String SCAN_TIMERANGE_START = "cloudgraph.hbase.mapreduce.scan.timerange.start";
	/**
	 * The ending timestamp used to filter columns with a specific range of
	 * versions.
	 */
	protected static final String SCAN_TIMERANGE_END = "cloudgraph.hbase.mapreduce.scan.timerange.end";
	/** The maximum number of version to return. */
	protected static final String SCAN_MAXVERSIONS = "cloudgraph.hbase.mapreduce.scan.maxversions";

	/** Set to false to disable server-side caching of blocks for this scan. */
	public static final String SCAN_CACHEBLOCKS = "cloudgraph.hbase.mapreduce.scan.cacheblocks";
	/** The number of rows for caching that will be passed to scanners. */
	public static final String SCAN_CACHEDROWS = "cloudgraph.hbase.mapreduce.scan.cachedrows";
	
	
	/** The configuration. */
	private Configuration conf = null;

	/** The root table to scan. */
	private HTable table = null;

	/** Holds the set of scans used to define the input. */
	private List<Scan> scans;

	/** The reader scanning the table, can be a custom one. */
	private GraphRecordReader graphRecordReader = null;

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration configuration) {
		this.conf = configuration;
		String tableName = conf.get(ROOT_TABLE);
		try {
			table = new HTable(new Configuration(conf), tableName);
		} catch (Exception e) {
			LOG.error(StringUtils.stringifyException(e));
		}

		String[] rawScans = conf.getStrings(SCANS);
		if (rawScans.length <= 0) {
			throw new IllegalArgumentException(
					"There must be at least 1 scan configuration set to : "
							+ SCANS);
		}
		List<Scan> scans = new ArrayList<Scan>();

		for (int i = 0; i < rawScans.length; i++) {
			try {
				Scan scan = GraphMapReduceSetup.convertStringToScan(rawScans[i]);
				setConf(scan, configuration);
				scans.add(scan);
			} catch (IOException e) {
				throw new RuntimeException("Failed to convert Scan : "
						+ rawScans[i] + " to string", e);
			}
		}
		this.setScans(scans);
	}
	
	private void setConf(Scan scan, Configuration configuration) throws NumberFormatException, IOException
	{
		if (conf.get(SCAN_TIMESTAMP) != null) {
			scan.setTimeStamp(Long.parseLong(conf.get(SCAN_TIMESTAMP)));
		}

		if (conf.get(SCAN_TIMERANGE_START) != null
				&& conf.get(SCAN_TIMERANGE_END) != null) {
			scan.setTimeRange(
					Long.parseLong(conf.get(SCAN_TIMERANGE_START)),
					Long.parseLong(conf.get(SCAN_TIMERANGE_END)));
		}

		if (conf.get(SCAN_MAXVERSIONS) != null) {
			scan.setMaxVersions(Integer.parseInt(conf
					.get(SCAN_MAXVERSIONS)));
		}

		if (conf.get(SCAN_CACHEDROWS) != null) {
			scan.setCaching(Integer.parseInt(conf.get(SCAN_CACHEDROWS)));
		}

		// false by default, full table scans generate too much GC churn
		scan.setCacheBlocks((conf.getBoolean(SCAN_CACHEBLOCKS, false)));
	}

	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException,
			InterruptedException {
		if (scans.isEmpty()) {
			throw new IOException("No scans were provided.");
		}
		List<InputSplit> splits = new ArrayList<InputSplit>();

		for (Scan scan : scans) {
			byte[] tableName = scan
					.getAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME);
			if (tableName == null)
				throw new IOException("A scan object did not have a table name");
			HTable table = new HTable(context.getConfiguration(), tableName);
			Pair<byte[][], byte[][]> keys = table.getStartEndKeys();
			if (keys == null || keys.getFirst() == null
					|| keys.getFirst().length == 0) {
				throw new IOException(
						"Expecting at least one region for table : "
								+ Bytes.toString(tableName));
			}
			int count = 0;

			byte[] startRow = scan.getStartRow();
			byte[] stopRow = scan.getStopRow();

			for (int i = 0; i < keys.getFirst().length; i++) {
				if (!includeRegionInSplit(keys.getFirst()[i],
						keys.getSecond()[i])) {
					continue;
				}
				String regionLocation = table.getRegionLocation(
						keys.getFirst()[i], false).getHostname();

				// determine if the given start and stop keys fall into the
				// range
				if ((startRow.length == 0 || keys.getSecond()[i].length == 0 || Bytes
						.compareTo(startRow, keys.getSecond()[i]) < 0)
						&& (stopRow.length == 0 || Bytes.compareTo(stopRow,
								keys.getFirst()[i]) > 0)) {
					byte[] splitStart = startRow.length == 0
							|| Bytes.compareTo(keys.getFirst()[i], startRow) >= 0 ? keys
							.getFirst()[i] : startRow;
					byte[] splitStop = (stopRow.length == 0 || Bytes.compareTo(
							keys.getSecond()[i], stopRow) <= 0)
							&& keys.getSecond()[i].length > 0 ? keys
							.getSecond()[i] : stopRow;
					InputSplit split = new TableSplit(tableName, scan,
							splitStart, splitStop, regionLocation);
					splits.add(split);
					if (LOG.isDebugEnabled())
						LOG.debug("getSplits: split -> " + (count++) + " -> "
								+ split);
				}
			}
			table.close();
		}
		return splits;
	}

	@Override
	public RecordReader<ImmutableBytesWritable, GraphWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		if (table == null) {
			throw new IOException(
					"Cannot create a record reader because of a"
							+ " previous error. Please look at the previous logs lines from"
							+ " the task's full log for more details.");
		}
		TableSplit tSplit = (TableSplit) split;
		GraphRecordReader reader = this.graphRecordReader;
		// if no record reader was provided use default
		if (reader == null) {
			reader = new GraphRecordReader();
		}
		Scan sc = tSplit.getScan();
		sc.setStartRow(tSplit.getStartRow());
		sc.setStopRow(tSplit.getEndRow());
		reader.setScan(sc);
		reader.setHTable(table);
		try {
			reader.initialize(tSplit, context);
		} catch (InterruptedException e) {
			throw new InterruptedIOException(e.getMessage());
		}
		return reader;
	}

	/**
	 * Test if the given region is to be included in the InputSplit while
	 * splitting the regions of a table.
	 * <p>
	 * This optimization is effective when there is a specific reasoning to
	 * exclude an entire region from the M-R job, (and hence, not contributing
	 * to the InputSplit), given the start and end keys of the same. <br>
	 * Useful when we need to remember the last-processed top record and revisit
	 * the [last, current) interval for M-R processing, continuously. In
	 * addition to reducing InputSplits, reduces the load on the region server
	 * as well, due to the ordering of the keys. <br>
	 * <br>
	 * Note: It is possible that <code>endKey.length() == 0 </code> , for the
	 * last (recent) region. <br>
	 * Override this method, if you want to bulk exclude regions altogether from
	 * M-R. By default, no region is excluded( i.e. all regions are included).
	 * 
	 * @param startKey
	 *            Start key of the region
	 * @param endKey
	 *            End key of the region
	 * @return true, if this region needs to be included as part of the input
	 *         (default).
	 */
	protected boolean includeRegionInSplit(final byte[] startKey,
			final byte[] endKey) {
		return true;
	}

	/**
	 * Allows subclasses to get the list of {@link Scan} objects.
	 */
	protected List<Scan> getScans() {
		return this.scans;
	}

	/**
	 * Allows subclasses to set the list of {@link Scan} objects.
	 * 
	 * @param scans
	 *            The list of {@link Scan} used to define the input
	 */
	protected void setScans(List<Scan> scans) {
		this.scans = scans;
	}
}
