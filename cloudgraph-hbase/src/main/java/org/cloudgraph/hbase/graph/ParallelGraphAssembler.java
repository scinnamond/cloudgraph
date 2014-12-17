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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.concurrent.GraphMetricVisitor;
import org.cloudgraph.common.concurrent.SubgraphTask;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.OperationException;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.Selection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreDataObject;

import commonj.sdo.Property;

/**
 * Constructs a data graph in parallel starting with a given root SDO type based
 * on a given <a target="#"
 * href="http://plasma-sdo.org/org/plasma/query/collector/Selection.html"
 * >"selection graph"</a>, where processing proceeds as a breadth-first
 * traversal and tasks/threads are dynamically added based on availability
 * within a shared <a href=
 * "https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html"
 * >thread pool</a>.
 * <p>
 * While the result graph may be of any arbitrary size or depth, because the
 * traversal is breadth-first, many tasks are typically spawned at the "base" of
 * the graph, exhausting the available pool threads. Each subgraph task can
 * spawn further sub tasks based on thread availability, but typically this
 * means each task will traverse and process a healthy segment of the total
 * graph. Since the actual size or depth of the result graph is not known until
 * discovered on traversal, a fixed number of parallel tasks cannot be initially
 * created, but must be dynamically spawned during graph discovery.
 * <p>
 * The assembly is triggered by calling the
 * {@link ParallelGraphAssembler#assemble(Result resultRow)} method which
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
 * @see org.cloudgraph.hbase.key.StatefullColumnKeyFactory
 * @see ParallelSubgraphTask
 * 
 * @author Scott Cinnamond
 * @since 0.6.2
 */
public class ParallelGraphAssembler extends DistributedAssembler {
	private static Log log = LogFactory.getLog(ParallelGraphAssembler.class);

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
	 */
	public ParallelGraphAssembler(PlasmaType rootType, Selection selection,
			DistributedReader distributedReader, Timestamp snapshotDate) {
		super(rootType, selection, distributedReader, snapshotDate);
	}

	/**
	 * Creates a single
	 */
	@Override
	protected void assemble(PlasmaDataObject target, PlasmaDataObject source,
			PlasmaProperty sourceProperty, RowReader rowReader, int level)
			throws IOException {

		ParallelSubgraphTask task = new ParallelSubgraphTask(target,
				this.selection, this.snapshotDate, this.distributedReader,
				source, sourceProperty, rowReader, level, 0);
		task.assemble();
	}
}
