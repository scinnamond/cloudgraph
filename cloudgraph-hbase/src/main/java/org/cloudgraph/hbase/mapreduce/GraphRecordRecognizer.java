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
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.ScannerCallable;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.metrics.util.MetricsTimeVaryingLong;
import org.apache.hadoop.util.StringUtils;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.expr.Expr;
import org.cloudgraph.hbase.expr.ExprPrinter;
import org.cloudgraph.hbase.graph.FederatedGraphAssembler;
import org.cloudgraph.hbase.graph.FederatedGraphSliceAssembler;
import org.cloudgraph.hbase.graph.GraphRecognizerContext;
import org.cloudgraph.hbase.graph.GraphRecognizerSyntaxTreeAssembler;
import org.cloudgraph.hbase.graph.HBaseGraphAssembler;
import org.cloudgraph.hbase.io.FederatedGraphReader;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.StateMarshallingContext;
import org.cloudgraph.state.StatelNonValidatinglDataBinding;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.collector.Selection;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import commonj.sdo.Type;

/**
 * Iterates over HBase root table data for the current <code>TableSplit</code>, assembling data graphs based on the detailed selection criteria within a 
 * given <a href="http://plasma-sdo.org/org/plasma/query/Query.html">query</a>. Partially or fully
 * assembled data graphs may be passed to a {@link GraphRecordRecognizer} and potentially screened from
 * client {@link GraphMapper} extensions potentially illuminating business logic dedicated to identifying
 * specific records.   
 * <p>
 * 
 * </p>
 */
public class GraphRecordRecognizer {
	public static final String LOG_PER_ROW_COUNT = "hbase.mapreduce.log.scanner.rowcount";

	static final Log log = LogFactory.getLog(GraphRecordRecognizer.class);

	/** Name of mapreduce counter group for CloudGraph */
	private static final String CLOUDGRAPH_COUNTER_GROUP_NAME = "CloudGraph Counters";
	
	/** 
	 * MapReduce counter which stores the number of data graphs successfully 
	 * recognized by the current recognizer. Will remain zero if no recognizer
	 * is required for the current query */
	private static final String CLOUDGRAPH_COUNTER_NAME_NUM_RECOGNIZED_GRAPHS = "NUM_RECOGNIZED_GRAPHS";
	
	/** 
	 * MapReduce counter which stores the number of data graphs not recognized by the 
	 * current recognizer. Will remain zero if no recognizer
	 * is required for the current query */
	private static final String CLOUDGRAPH_COUNTER_NAME_NUM_UNRECOGNIZED_GRAPHS = "NUM_UNRECOGNIZED_GRAPHS";
	
	/** MapReduce counter which stores the total number of graph nodes assembled */
	private static final String CLOUDGRAPH_COUNTER_NAME_NUM_GRAPH_NODES_ASSEMBLED = "NUM_GRAPH_NODES_ASSEMBLED";
	
	/** MapReduce counter which stores the total time in milliseconds taken for graph assembly */
	private static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_ASSEMBLY_TIME = "MILLIS_GRAPH_ASSEMBLY";	

	/** MapReduce counter which stores the total time in milliseconds taken for graph recognition */
	private static final String CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_RECOG_TIME = "MILLIS_GRAPH_RECOGNITION";	
	
	/** name of mapreduce counter group for HBase */
	private static final String HBASE_COUNTER_GROUP_NAME = "HBase Counters";
	private ResultScanner scanner = null;
	private Scan scan = null;
	private Scan currentScan = null;
	private HTable htable = null;
	private byte[] lastSuccessfulRow = null;
	private ImmutableBytesWritable key = null;
	private GraphWritable value = null;
	private TaskAttemptContext context = null;
	private Method getCounter = null;
	private long numRestarts = 0;
	private long timestamp;
	private int rowcount;
	private boolean logScannerActivity = false;
	private int logPerRowCount = 100;
	private Expr graphRecognizerRootExpr;
	private HBaseGraphAssembler graphAssembler;
	private GraphRecognizerContext recognizerContext;
	private TableReader rootTableReader;
	
	private long numRecognizedGraphs = 0;
	
	private long numUnrecognizedGraphs = 0;
	
	private long totalGraphNodesAssembled = 0;
	private long totalGraphAssemblyTime = 0;
	private long totalGrapRecognitionTime = 0;

	/**
	 * Restart from survivable exceptions by creating a new scanner.
	 * 
	 * @param firstRow
	 *            The first row to start at.
	 * @throws IOException
	 *             When restarting fails.
	 */
	public void restart(byte[] firstRow) throws IOException {
		currentScan = new Scan(scan);
		currentScan.setStartRow(firstRow);
		currentScan.setAttribute(Scan.SCAN_ATTRIBUTES_METRICS_ENABLE,
				Bytes.toBytes(Boolean.TRUE));
		this.scanner = this.htable.getScanner(currentScan);
		if (logScannerActivity) {
			log.info("Current scan=" + currentScan.toString());
			timestamp = System.currentTimeMillis();
			rowcount = 0;
		}
	}

	/**
	 * In new mapreduce APIs, TaskAttemptContext has two getCounter methods
	 * Check if getCounter(String, String) method is available.
	 * 
	 * @return The getCounter method or null if not available.
	 * @throws IOException
	 */
	private Method retrieveGetCounterWithStringsParams(
			TaskAttemptContext context) throws IOException {
		Method m = null;
		try {
			m = context.getClass().getMethod("getCounter",
					new Class[] { String.class, String.class });
		} catch (SecurityException e) {
			throw new IOException("Failed test for getCounter", e);
		} catch (NoSuchMethodException e) {
			// Ignore
		}
		return m;
	}

	/**
	 * Sets the HBase table.
	 * 
	 * @param htable
	 *            The {@link HTable} to scan.
	 */
	public void setHTable(HTable htable) {
		Configuration conf = htable.getConfiguration();
		logScannerActivity = conf.getBoolean(
				ScannerCallable.LOG_SCANNER_ACTIVITY, false);
		logPerRowCount = conf.getInt(LOG_PER_ROW_COUNT, 100);
		this.htable = htable;
	}

	/**
	 * Sets the scan defining the actual details like columns etc.
	 * 
	 * @param scan
	 *            The scan to set.
	 */
	public void setScan(Scan scan) {
		this.scan = scan;
	}

	/**
	 * 
	 * Not done in constructor to allow for extension.
	 * 
	 * @throws IOException
	 *             , InterruptedException
	 */
	public void initialize(InputSplit inputsplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		if (context != null) {
			this.context = context;
			getCounter = retrieveGetCounterWithStringsParams(context);

			String queryXml = context.getConfiguration().get(
					GraphInputFormat.QUERY);
			Query query = unmarshal(queryXml);

			PlasmaType type = getRootType(query);
	        Where where = query.findWhereClause();
	        SelectionCollector selectionCollector = null;
	        if (where != null)
	        	selectionCollector = new SelectionCollector(
	                query.getSelectClause(), where, type);
	        else
	        	selectionCollector = new SelectionCollector(
	                    query.getSelectClause(), type);
	        selectionCollector.setOnlyDeclaredProperties(false);
	        // FIXME generalize
	        for (Type t : selectionCollector.getTypes()) 
	        	collectRowKeyProperties(selectionCollector, (PlasmaType)t);        

			StateMarshallingContext marshallingContext = null;
			try {
				marshallingContext = new StateMarshallingContext(
						new StatelNonValidatinglDataBinding());
			} catch (JAXBException e) {
				throw new GraphServiceException(e);
			} catch (SAXException e) {
				throw new GraphServiceException(e);
			}

			FederatedGraphReader graphReader = new FederatedGraphReader(
					type, selectionCollector.getTypes(), marshallingContext);
			this.rootTableReader = graphReader.getRootTableReader();

			this.graphAssembler = createGraphAssembler(type, graphReader,
					selectionCollector,
					new Timestamp(System.currentTimeMillis()));

			boolean needsRecognizer = context.getConfiguration().getBoolean(
					GraphInputFormat.RECOGNIZER, false);
			if (needsRecognizer) {
				GraphRecognizerSyntaxTreeAssembler recognizerAssembler = 
					new GraphRecognizerSyntaxTreeAssembler(
					where, type);
				this.graphRecognizerRootExpr = recognizerAssembler.getResult();
				this.recognizerContext = new GraphRecognizerContext();
				if (log.isDebugEnabled()) {
				    ExprPrinter printer = new ExprPrinter();
				    graphRecognizerRootExpr.accept(printer);
				    log.debug("Graph Recognizer: " + printer.toString());
				}
			}

		}
		restart(scan.getStartRow());
	}

	/**
	 * Closes the split.
	 */
	public void close() {
		this.scanner.close();
	}

	/**
	 * Returns the current key.
	 * 
	 * @return The current key.
	 * @throws IOException
	 * @throws InterruptedException
	 *             When the job is aborted.
	 */
	public ImmutableBytesWritable getCurrentKey() throws IOException,
			InterruptedException {
		return key;
	}

	/**
	 * Returns the current value.
	 * 
	 * @return The current value.
	 * @throws IOException
	 *             When the value is faulty.
	 * @throws InterruptedException
	 *             When the job is aborted.
	 */
	public GraphWritable getCurrentValue() throws IOException,
			InterruptedException {
		return value;
	}

	/**
	 * Positions the record reader to the next recognized record, scanning forward past
	 * any unrecognized records, not passing these records to <code>Mapper</code> clients. 
	 * @return <code>true</code> if there was another record.
	 * @throws IOException
	 *             When reading the record failed.
	 * @throws InterruptedException
	 *             When the job was aborted.
	 */
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (key == null)
			key = new ImmutableBytesWritable();
		Result resultRow = null;
		try {
			try {
				while ((resultRow = this.scanner.next()) != null) {
					PlasmaDataGraph graph = recognize(resultRow);
					if (graph != null) {
						 this.value = new GraphWritable(graph);
						 break;
					}
				}
				
				if (logScannerActivity) {
					rowcount++;
					if (rowcount >= logPerRowCount) {
						long now = System.currentTimeMillis();
						log.info("Mapper took " + (now - timestamp)
								+ "ms to process " + rowcount + " rows");
						timestamp = now;
						rowcount = 0;
					}
				}
			} catch (IOException e) {
				// try to handle all IOExceptions by restarting
				// the scanner, if the second call fails, it will be rethrown
				log.info("recovered from " + StringUtils.stringifyException(e));
				if (lastSuccessfulRow == null) {
					log.warn("We are restarting the first next() invocation,"
							+ " if your mapper has restarted a few other times like this"
							+ " then you should consider killing this job and investigate"
							+ " why it's taking so long.");
				}
				if (lastSuccessfulRow == null) {
					restart(scan.getStartRow());
				} else {
					restart(lastSuccessfulRow);
					scanner.next(); // skip presumed already mapped row
				}
				while ((resultRow = this.scanner.next()) != null) {
					PlasmaDataGraph graph = recognize(resultRow);
					if (graph != null) {
						 this.value = new GraphWritable(graph);
						 break;
					}
				}
				this.numRestarts++;
			}
			if (resultRow != null && resultRow.size() > 0) {
				key.set(resultRow.getRow());
				lastSuccessfulRow = key.get();
				return true;
			}

			updateCounters();
			return false;
		} catch (IOException ioe) {
			if (logScannerActivity) {
				long now = System.currentTimeMillis();
				log.info("Mapper took " + (now - timestamp) + "ms to process "
						+ rowcount + " rows");
				log.info(ioe);
				String lastRow = lastSuccessfulRow == null ? "null" : Bytes
						.toStringBinary(lastSuccessfulRow);
				log.info("lastSuccessfulRow=" + lastRow);
			}
			throw ioe;
		}
	}

	private boolean requiresRecognizer() {
		return this.graphRecognizerRootExpr != null;
	}
	
	/**
	 * Assembles a graph for the given row, then if the current query context requires
	 * a graph recognizer, invokes the recognizer returning the graph it it is recognized
	 * or null if not. 
	 * @param resultRow the row
	 * @return the recognized graph or null if the assembled graph is not recognized
	 */
	private PlasmaDataGraph recognize(Result resultRow) {
    	if (resultRow.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
    			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
    		return null; // ignore toumbstone roots
    	}
		PlasmaDataGraph graph = assemble(resultRow);
		if (requiresRecognizer()) {
			long before = System.currentTimeMillis();
			this.recognizerContext.setGraph(graph);
			if (this.graphRecognizerRootExpr.evaluate(recognizerContext)) {
				this.numRecognizedGraphs++;
			} else {
				this.numUnrecognizedGraphs++;
				graph = null;  
			}
			long after = System.currentTimeMillis();
			this.totalGrapRecognitionTime += (after - before);
		}
			
		return graph;
	}	
	
	 
	/**
	 * Assembles a data graph from the given result row, capturing various
	 * graph metrics and counters. 
	 * @param resultRow the row
	 * @return the assembled data graph
	 */
	private PlasmaDataGraph assemble(Result resultRow) {
		this.graphAssembler.clear();
		this.graphAssembler.assemble(resultRow);
		PlasmaDataGraph result = graphAssembler.getDataGraph();
		CoreDataObject root = (CoreDataObject)result.getRootObject();
		Long time = (Long)root.getValue(CloudGraphConstants.GRAPH_ASSEMBLY_TIME);
		if (time != null)
		    this.totalGraphAssemblyTime += time.longValue();
		Long nodeCount = (Long)root.getValue(CloudGraphConstants.GRAPH_NODE_COUNT);
		if (nodeCount != null)
			this.totalGraphNodesAssembled += nodeCount.longValue();			
		return graphAssembler.getDataGraph();
	}

	/**
	 * Updates various job counters.
	 * If we are running on the new version of mapreduce, RecordReader has access to
	 * counters thus can update counters based on scanMetrics. If we are running on
	 * old version of mapreduce, it won't be able to get access to counters and
	 * TableRecorderReader can't update counter values.
	 * 
	 * @throws IOException
	 */
	private void updateCounters() throws IOException {
		// we can get access to counters only if hbase uses new mapreduce APIs
		if (this.getCounter == null) {
			return;
		}
		updateHBaseCounters();
		try {
			if (this.numRecognizedGraphs > 0)
			    ((Counter) this.getCounter.invoke(context,
					CLOUDGRAPH_COUNTER_GROUP_NAME, CLOUDGRAPH_COUNTER_NAME_NUM_RECOGNIZED_GRAPHS))
					.increment(this.numRecognizedGraphs);
			if (this.numUnrecognizedGraphs > 0)
			    ((Counter) this.getCounter.invoke(context,
					CLOUDGRAPH_COUNTER_GROUP_NAME, CLOUDGRAPH_COUNTER_NAME_NUM_UNRECOGNIZED_GRAPHS))
					.increment(this.numUnrecognizedGraphs);
			
		    ((Counter) this.getCounter.invoke(context,
				CLOUDGRAPH_COUNTER_GROUP_NAME, CLOUDGRAPH_COUNTER_NAME_NUM_GRAPH_NODES_ASSEMBLED))
				.increment(this.totalGraphNodesAssembled);
		    
		    ((Counter) this.getCounter.invoke(context,
				CLOUDGRAPH_COUNTER_GROUP_NAME, CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_ASSEMBLY_TIME))
				.increment(this.totalGraphAssemblyTime);		    

		    ((Counter) this.getCounter.invoke(context,
				CLOUDGRAPH_COUNTER_GROUP_NAME, CLOUDGRAPH_COUNTER_NAME_TOT_GRAPH_RECOG_TIME))
				.increment(this.totalGrapRecognitionTime);		    
		    
		} catch (Exception e) {
			log.debug("can't update counter."
					+ StringUtils.stringifyException(e));
		}
	}
	
	/**
	 * Increments various HBase specific counters, mostly taken from
	 * <code>ScanMetrics</code>. 
	 * @throws IOException
	 */
	private void updateHBaseCounters() throws IOException {
		byte[] serializedMetrics = currentScan
				.getAttribute(Scan.SCAN_ATTRIBUTES_METRICS_DATA);
		if (serializedMetrics == null || serializedMetrics.length == 0) {
			return;
		}

		DataInputBuffer in = new DataInputBuffer();
		in.reset(serializedMetrics, 0, serializedMetrics.length);
		ScanMetrics scanMetrics = new ScanMetrics();
		scanMetrics.readFields(in);
		MetricsTimeVaryingLong[] mlvs = scanMetrics
				.getMetricsTimeVaryingLongArray();

		try {
			for (MetricsTimeVaryingLong mlv : mlvs) {
				Counter ct = (Counter) this.getCounter.invoke(context,
						HBASE_COUNTER_GROUP_NAME, mlv.getName());
				ct.increment(mlv.getCurrentIntervalValue());
			}
			((Counter) this.getCounter.invoke(context,
					HBASE_COUNTER_GROUP_NAME, "NUM_SCANNER_RESTARTS"))
					.increment(numRestarts);
		} catch (Exception e) {
			log.debug("can't update counter."
					+ StringUtils.stringifyException(e));
		}
	}

	/**
	 * The current progress of the record reader through its data.
	 * 
	 * @return A number between 0.0 and 1.0, the fraction of the data read.
	 */
	public float getProgress() {
		// Depends on the total number of tuples
		return 0;
	}

	private static void collectRowKeyProperties(SelectionCollector collector,
			PlasmaType type) {
		CloudGraphConfig config = CloudGraphConfig.getInstance();
		DataGraphConfig graph = config.findDataGraph(type.getQualifiedName());
		if (graph != null) {
			UserDefinedRowKeyFieldConfig[] fields = new UserDefinedRowKeyFieldConfig[graph
					.getUserDefinedRowKeyFields().size()];
			graph.getUserDefinedRowKeyFields().toArray(fields);
			for (UserDefinedRowKeyFieldConfig field : fields) {
				List<Type> types = collector.addProperty(graph.getRootType(),
						field.getPropertyPath());
				for (Type nextType : types)
					collectRowKeyProperties(collector, (PlasmaType) nextType);
			}
		}
	}

	private static PlasmaType getRootType(Query query) {
		From from = query.getFromClause();
		if (from.getEntity() == null)
			throw new GraphServiceException(
					"given query has no root type and/or URI");
		if (from.getEntity().getName() == null
				|| from.getEntity().getNamespaceURI() == null)
			throw new GraphServiceException(
					"given query has no root type and/or URI");
		PlasmaType type = (PlasmaType) PlasmaTypeHelper.INSTANCE.getType(from
				.getEntity().getNamespaceURI(), from.getEntity().getName());
		return type;
	}

	private static HBaseGraphAssembler createGraphAssembler(PlasmaType type,
			FederatedReader graphReader, Selection collector,
			Timestamp snapshotDate) {
		HBaseGraphAssembler graphAssembler = null;

		if (collector.hasPredicates()) {
			graphAssembler = new FederatedGraphSliceAssembler(type, collector,
					graphReader, snapshotDate);
		} else {
			graphAssembler = new FederatedGraphAssembler(type, collector,
					graphReader, snapshotDate);
		}

		return graphAssembler;
	}

	private static Query unmarshal(String xmlQuery) {
		try {
			PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
					new DefaultValidationEventHandler());
			return (Query) binding.unmarshal(xmlQuery);
		} catch (JAXBException e1) {
			throw new GraphServiceException(e1);
		} catch (SAXException e1) {
			throw new GraphServiceException(e1);
		}
	}

}
