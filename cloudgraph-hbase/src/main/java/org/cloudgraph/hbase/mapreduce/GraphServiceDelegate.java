package org.cloudgraph.hbase.mapreduce;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.mapreduce.JobContext;
import org.cloudgraph.hbase.io.TableWriter;
import org.cloudgraph.hbase.service.GraphQuery;
import org.cloudgraph.hbase.service.MutationCollector;
import org.cloudgraph.hbase.service.ServiceContext;
import org.cloudgraph.mapreduce.GraphService;
import org.cloudgraph.state.SimpleStateMarshallingContext;
import org.cloudgraph.state.StateMarshalingContext;
import org.cloudgraph.state.StateNonValidatingDataBinding;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.core.SnapshotMap;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class GraphServiceDelegate implements GraphService {
    private static Log log = LogFactory.getLog(GraphServiceDelegate.class);
    private ServiceContext context;
	
	public GraphServiceDelegate() {
    	try {
			StateMarshalingContext marshallingContext = new SimpleStateMarshallingContext(
					new StateNonValidatingDataBinding());
	    	this.context = new ServiceContext(marshallingContext);
		} catch (JAXBException e) {
			throw new GraphServiceException(e);
		} catch (SAXException e) {
			throw new GraphServiceException(e);
		}    	
	}

	@Override
	public void commit(DataGraph graph,
			JobContext jobContext) throws IOException {

		String jobName = this.getClass().getSimpleName();
		if (jobContext != null)
			jobName = jobContext.getJobName();
        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
		MutationCollector collector = new MutationCollector(this.context,
				snapshotMap, jobName);

		Map<TableWriter, List<Row>> mutations = new HashMap<TableWriter, List<Row>>();
		try {
			mutations = collector.collectChanges(graph);
		} catch (IllegalAccessException e) {
			throw new GraphServiceException(e);
		}
		Iterator<TableWriter> iter = mutations.keySet().iterator();
		while (iter.hasNext()) {
			TableWriter tableWriter = iter.next();
			List<Row> tableMutations = mutations.get(tableWriter);
			if (log.isDebugEnabled())
				log.info("commiting "+tableMutations.size()+" mutations to table: " + tableWriter.getTable().getName());
			try {
				tableWriter.getConnection().batch(tableMutations);
			} catch (InterruptedException e) {
				throw new GraphServiceException(e);
			}
			tableWriter.getConnection().flushCommits();
		}
        List<DataObject> changedObjects = graph.getChangeSummary().getChangedDataObjects();
        for (DataObject dataObject : changedObjects)
            if (!graph.getChangeSummary().isDeleted(dataObject))
                ((PlasmaNode)dataObject).getDataObject().reset(snapshotMap, jobName);
        graph.getChangeSummary().endLogging();
        graph.getChangeSummary().beginLogging();		
	}

	@Override
	public DataGraph[] find(Query query, JobContext jobContext) throws IOException {
		GraphQuery dispatcher = new GraphQuery(context);
		Timestamp timestamp = new Timestamp((new Date()).getTime());
		DataGraph[] results = dispatcher.find(query.getModel(), timestamp);
		for (DataGraph graph : results)
			graph.getChangeSummary().beginLogging();
		return results;
	}

	@Override
	public void commit(DataGraph[] graphs, JobContext jobContext)
			throws IOException {
		String jobName = this.getClass().getSimpleName();
		if (jobContext != null)
			jobName = jobContext.getJobName();
		Map<TableWriter, List<Row>> mutations = new HashMap<TableWriter, List<Row>>();
		for (DataGraph graph : graphs) {
	        SnapshotMap snapshotMap = new SnapshotMap(new Timestamp((new Date()).getTime()));
			MutationCollector collector = new MutationCollector(this.context,
					snapshotMap, jobName);
	
			try {
				mutations = collector.collectChanges(graph);
			} catch (IllegalAccessException e) {
				throw new GraphServiceException(e);
			}
	        List<DataObject> changedObjects = graph.getChangeSummary().getChangedDataObjects();
	        for (DataObject dataObject : changedObjects)
	            if (!graph.getChangeSummary().isDeleted(dataObject))
	                ((PlasmaNode)dataObject).getDataObject().reset(snapshotMap, jobName);
	        graph.getChangeSummary().endLogging();
	        graph.getChangeSummary().beginLogging();	
		}
		Iterator<TableWriter> iter = mutations.keySet().iterator();
		while (iter.hasNext()) {
			TableWriter tableWriter = iter.next();
			List<Row> tableMutations = mutations.get(tableWriter);
			if (log.isDebugEnabled())
				log.info("commiting "+tableMutations.size()+" mutations to table: " + tableWriter.getTable().getName());
			try {
				tableWriter.getConnection().batch(tableMutations);
			} catch (InterruptedException e) {
				throw new GraphServiceException(e);
			}
			tableWriter.getConnection().flushCommits();
		}
	}

}
