package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.filter.PredicateRowFilterAssembler;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.ScanContext;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;

import commonj.sdo.Type;

/**
 * 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class FederatedGraphSliceAssembler extends FederatedAssembler {

    private static Log log = LogFactory.getLog(FederatedGraphSliceAssembler.class);
	private Map<Type, List<String>> propertyMap;
	private Map<commonj.sdo.Property, Where> predicateMap; 
	private int scanCount;
	private SliceSupport sliceSupport = new SliceSupport();
	private Charset charset;

	public FederatedGraphSliceAssembler(PlasmaType rootType,
			PropertySelectionCollector collector,
			FederatedReader federatedReader, 
			Timestamp snapshotDate) {
		super(rootType, collector, federatedReader, snapshotDate);
		this.propertyMap = this.collector.getResult();
		this.predicateMap = this.collector.getPredicateMap();
		this.charset = Charset.forName( CoreConstants.UTF8_ENCODING );
	}

	@Override
	protected void assemble(PlasmaDataObject target, 
			PlasmaDataObject source, PlasmaProperty sourceProperty, 
			RowReader rowReader, int level) throws IOException
    {		 
		CoreNode targetDataNode = (CoreNode)target;
        
        if (log.isDebugEnabled())
			log.debug("assembling: ("
				+ targetDataNode.getUUIDAsString() + ") "
					+ target.getType().getURI() + "#" 
					+ target.getType().getName());
		
		List<String> names = this.propertyMap.get(target.getType());
		if (log.isDebugEnabled())
			log.debug(target.getType().getName() + " names: " + names.toString());
		
		assembleData(target, names, rowReader);
		
		
		TableReader tableReader = rowReader.getTableReader();
		TableConfig tableConfig = tableReader.getTable();

		// reference props
		for (String name : names) {
			PlasmaProperty prop = (PlasmaProperty)target.getType().getProperty(name);
			if (prop.getType().isDataType())
				continue;
			
			byte[] keyValue = getColumnValue(target, prop, 
				tableConfig, rowReader);
			if (keyValue == null)
				continue;
			
			Edge[] edges = rowReader.getGraphState().parseEdges(prop.getType(), 
				keyValue);
			
			PlasmaType childType = (PlasmaType)prop.getType();
			
			// NOTE: can we have predicates on singular props? 
			Where where = this.predicateMap.get(prop);
			
			// if target type is not bound to a specific table/root,
			// derive a child row reader context from its level
			TableReader externalTableReader = this.federatedReader.getTableReader(childType.getQualifiedName());
			if (externalTableReader == null) { 								
				RowReader childRowReader = getRowReader(level);
				Map<Long, Long> sequences = null;
				if (prop.isMany() && where != null) {
			    	sequences = this.sliceSupport.fetchSequences((PlasmaType)prop.getType(), 
			    			where, rowReader);
					List<String> childPropertyNames = this.propertyMap.get(prop.getType());
					this.sliceSupport.loadBySequenceList(sequences.values(), 
						childPropertyNames,
			    		childType, childRowReader);
				}
				else {
				    List<String> childPropertyNames = this.propertyMap.get(prop.getType());
				    this.sliceSupport.load(childPropertyNames,
			    			childType, childRowReader);
				}			
				
	        	assembleEdges(target, prop, edges, sequences, rowReader, 
	        		childRowReader.getTableReader(), 
	        		childRowReader, level);			
	        }
			else 
			{
				if (log.isDebugEnabled())
					if (!tableConfig.getName().equals(externalTableReader.getTable().getName()))
					    log.debug("switching row context from table: '"
						    + tableConfig.getName() + "' to table: '"
						    + externalTableReader.getTable().getName() + "'");
				Map<String, Result> resultRows = null;
				if (prop.isMany() && where != null) {
				    resultRows = this.fetch(childType, where, externalTableReader);
				}
				assembleExternalEdges(target, prop, edges, rowReader,	
					resultRows, externalTableReader, level);
			}			
		}
    }
	
	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, Map<Long, Long> sequences, RowReader rowReader, 
			TableReader childTableReader, RowReader childRowReader,
			int level) throws IOException 
	{
		for (Edge edge : edges) {	
			RowReader existingChildRowReader = childTableReader.getRowReader(edge.getUuid());
        	if (existingChildRowReader != null)
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
        	if (sequences != null && sequences.get(edge.getId()) == null)
				continue; // screen out edges
			
        	// create a child object
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
            childRowReader.addDataObject(child);
            
			assembleEdge(target, prop, edge, 
			        child, childRowReader, level);
		}
	}
	
	
	// Target is a different row, within this table or another.
	// Since we are assembling a graph, each edge requires
	// a new row reader. 
	// each edge is a new root in the target table
	// so need a new row reader for each
	private void assembleExternalEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, RowReader rowReader, Map<String, Result> resultRows,
			TableReader childTableReader, int level) throws IOException 
	{
		RowReader childRowReader = null;
		for (Edge edge : edges) {	
			RowReader existingChildRowReader = childTableReader.getRowReader(edge.getUuid());
        	if (existingChildRowReader != null)
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getDataObject(edge.getUuid());
        		link(existingChild, target, prop);
        		continue; 
        	}
			byte[] childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid());
			String childRowKeyStr = new String(childRowKey, this.charset);
			if (resultRows != null && resultRows.get(childRowKeyStr) == null)
				continue; //not found in predicate
			
			PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop);
			CoreNode childDataNode = (CoreNode)child;
			childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
				UUID.fromString(edge.getUuid()));
			
			// create a row reader for every external edge
			Result childResult = fetchGraph(childRowKey, childTableReader, child);
			childRowReader = childTableReader.createRowReader(
				child, childResult);
			
			assembleEdge(target, prop, edge, 
		        child, childRowReader, level);
		}
	}
	
	private Map<String, Result> fetch(PlasmaType contextType,
			Where where, TableReader tableReader) throws IOException
	{
		Map<String, Result> results = new HashMap<String, Result>();
		Charset charset = tableReader.getTable().getCharset();
        Scan scan = new Scan();
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
    	ScanContext scanContext = 
    			new ScanContext(contextType, where);
    	if (scanContext.canUsePartialKeyScan()) {
    		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(contextType);
    		scanAssembler.assemble(scanContext.getLiterals());
            scan.setStartRow(scanAssembler.getStartKey()); // inclusive
            scan.setStopRow(scanAssembler.getStopKey()); // exclusive
      		if (log.isDebugEnabled())
    			log.debug("partial key scan: (" 
      		        + "start: " + Bytes.toString(scan.getStartRow())
      		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
    	}
    	else {
    		log.warn("could not create partial row-key scan due to wildcards, non-contiguous row-key field values and/or other factors in the path predicate query - could cause full table scan");
            PredicateRowFilterAssembler rowFilterAssembler = 
            	new PredicateRowFilterAssembler(contextType);
            rowFilterAssembler.assemble(where, contextType);
            rootFilter.addFilter(rowFilterAssembler.getFilter());
    	}
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("executing scan...");
        
        if (log.isDebugEnabled() ) 
        	log.debug(FilterUtil.printFilterTree(rootFilter));
        ResultScanner scanner = tableReader.getConnection().getScanner(scan);
    	int count = 0;
        for (Result resultRow : scanner) {
        	if (log.isTraceEnabled()) {
      	        log.trace("row: " + new String(resultRow.getRow()));              	  
          	    for (KeyValue keyValue : resultRow.list()) {
          	    	log.trace("\tkey: " 
          	    		+ new String(keyValue.getQualifier())
          	    	    + "\tvalue: " + new String(keyValue.getValue()));
          	    }
        	}
        	String rowKey = new String(resultRow.getRow(), charset);
        	results.put(rowKey, resultRow);
            count++;
        }      
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled())
        	log.debug("assembled " + String.valueOf(count) + " results ("
        	    + String.valueOf(after - before) + ")");
		return results;
	}
}
