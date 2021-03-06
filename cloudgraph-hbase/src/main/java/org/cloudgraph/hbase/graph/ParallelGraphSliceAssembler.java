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
package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.common.concurrent.ConfigProps;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.recognizer.GraphRecognizerSyntaxTreeAssembler;
import org.plasma.query.collector.Selection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

/**
 * Assembles a data graph in parallel where one or more collections may be "sliced" 
 * based on path predicates within the <a target="#"
 * href="http://plasma-sdo.org/org/plasma/query/collector/Selection.html"
 * >"selection graph"</a>. 
 * <p>
 * Edges within a collection are "recognized" as members of a slice based on
 * a binary expression syntax tree assembled from the path
 * predicate describing the slice. While a path predicate may be quite complex resulting in
 * any number of logical, relational or wildcard binary expressions, a single 
 * slice syntax tree is used to evaluate any number of edges within a 
 * collection. Where edge opposite graph nodes are found within the current row, an 
 * edge recognizer is used, but where edge opposite graph nodes are found "outside" the 
 * current row, a graph recognizer is used.   
 * </p>
 * <p>
 * Parallel processing proceeds as a breadth-first
 * traversal and subgraph tasks are dynamically added based on thread availability
 * within a shared <a href=
 * "https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html"
 * >thread pool</a>.
 * </p>
 * <p>
 * While the result graph may be of any arbitrary size or depth, because the
 * traversal is breadth-first, many tasks are typically spawned at the "base" of
 * the graph, exhausting the available pool threads. Each subgraph task can
 * spawn further sub tasks based on thread availability, but typically this
 * means each task will traverse and process a healthy segment of the total
 * graph. Since the actual size or depth of the result graph is not known until
 * discovered on traversal, a fixed number of parallel tasks cannot be initially
 * created, but must be dynamically spawned during graph discovery.
 * </p>
 * The assembly is triggered by calling the
 * {@link ParallelGraphSliceAssembler#assemble(Result resultRow)} method which
 * recursively reads HBase keys and values re-constituting the data graph. The
 * assembly traversal is driven by HBase column values representing the original
 * edges or containment structure of the graph.
 * </p>
 * <p>
 * Since every column key in HBase must be unique, and a data graph may contain
 * any number of nodes, a column key factory is used both to persist as well as
 * re-constitute a graph. A minimal amount of "state" information is therefore
 * stored with each graph which maps user readable sequence numbers (which are
 * used in column keys) to UUID values. The nodes of the resulting data graph
 * are re-created with the original UUID values.
 * </p>
 * 
 * @see EdgeRecognizerSyntaxTreeAssembler
 * @see EdgeRecognizerContext
 * @see GraphRecognizerSyntaxTreeAssembler
 * @see GraphRecognizerContext
 * @see GraphSliceSupport
 * @see org.cloudgraph.hbase.key.StatefullColumnKeyFactory
 * @see ParallelSliceSubgraphTask
 * 
 * @author Scott Cinnamond
 * @since 0.6.2
 */
public class ParallelGraphSliceAssembler extends DistributedAssembler {
	/**
	 * Thread pool shared by all tasks created by this assembler.  
	 */
    private ThreadPoolExecutor executorService;	
    private ConfigProps config;

	/**
	 * Constructor.
	 * 
	 * @param rootType
	 *            the SDO root type for the result data graph
	 * @param selection
	 *            selected SDO properties. Properties are mapped by selected
	 *            types required in the result graph.
	 * @param snapshotDate
	 *            the query snapshot date which is populated into every data
	 *            object in the result data graph.
	 * @param distributedReader the distributed reader
	 * @param minPoolSize the minimum or core size of the underlying thread pool used for
	 * all tasks executed under this assembler           
	 * @param maxPoolSize the maximum size of the underlying thread pool used for
	 * all tasks executed under this assembler           
	 */
	public ParallelGraphSliceAssembler(PlasmaType rootType, Selection selection,
			DistributedReader distributedReader, Timestamp snapshotDate,
			ConfigProps config) {
		super(rootType, selection, distributedReader, snapshotDate);
		
		this.executorService = new ThreadPoolExecutor(
				config.getMinThreadPoolSize(), config.getMaxThreadPoolSize(),
	            0L, TimeUnit.MILLISECONDS,
	            new LinkedBlockingQueue<Runnable>(),
	            new ThreadPoolExecutor.CallerRunsPolicy());
		this.config = config;
	}

	/**
	 * Creates a single task and begins the traversal from root in the
	 * current thread. 
	 */ 
	@Override
	protected void assemble(PlasmaDataObject target, PlasmaDataObject source,
			PlasmaProperty sourceProperty, RowReader rowReader, int level)
			throws IOException {

		ParallelSliceSubgraphTask task = new ParallelSliceSubgraphTask(target,
				this.selection, this.snapshotDate, this.distributedReader,
				source, sourceProperty, rowReader, level, 0, this.executorService,
				this.config);
		task.assemble(); // in current thread. 
	}
}
