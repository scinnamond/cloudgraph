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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.hbase.filter.BinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.HBaseFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateColumnFilterAssembler;
import org.cloudgraph.hbase.filter.StatefullBinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.io.DistributedGraphReader;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableOperation;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.query.expr.Expr;
import org.cloudgraph.query.expr.ExprPrinter;
import org.cloudgraph.recognizer.GraphRecognizerContext;
import org.cloudgraph.recognizer.GraphRecognizerSyntaxTreeAssembler;
import org.cloudgraph.state.GraphState.Edge;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.query.collector.Selection;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.Property;
import commonj.sdo.helper.XMLDocument;

/**
 * Delegate class for various graph slice fetch and edge post processing
 * operations. Supports graph assemblers and
 * other clients. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
class GraphSliceSupport {
    private static Log log = LogFactory.getLog(GraphSliceSupport.class);
    private Selection selection;
    private Timestamp snapshotDate;
	private Charset charset;
    
    @SuppressWarnings("unused")
	private GraphSliceSupport() {}
    public GraphSliceSupport(Selection selection, Timestamp snapshotDate) {
    	this.selection = selection;
    	this.snapshotDate = snapshotDate;
		this.charset = Charset.forName( CoreConstants.UTF8_ENCODING );
    }
	
	/**
	 * Creates and executes a "sub-graph" filter based on the given state-edges and path predicate
	 * and then excludes appropriate results based on a {@link GraphRecognizerSyntaxTreeAssembler binary syntax tree} assembled 
	 * from the same path predicate. Each sub-graph must first be assembled to do any evaluation, but
	 * a single syntax tree instance evaluates every sub-graph 
	 * (potentially thousands/millions) resulting
	 * from the given edge collection. The graph {@link Selection selection criteria} is based not on the 
	 * primary graph selection but only on the properties found in the given path predicate, so the
	 * assembly is only/exactly as extensive as required by the predicate.  
	 * Any sub-graphs assembled may themselves be "distributed" graphs.   
	 *  
	 * @param contextType the current type
	 * @param edges the state edge set
	 * @param where the path predicate
	 * @param rowReader the row reader
	 * @param tableReader the table reader
	 * @return the results filtered results
	 * @throws IOException
	 * @see GraphRecognizerSyntaxTreeAssembler
	 * @see SelectionCollector
	 * @see Selection
	 */
	public Map<String, Result> filter(  
			PlasmaType contextType, Edge[] edges, 
			Where where, RowReader rowReader, TableReader tableReader) throws IOException
	{
		Map<String, Result> results = new HashMap<String, Result>();
		     	
        SelectionCollector selectionCollector = new SelectionCollector(
               where, contextType);

        // create a new reader as the existing one may have cached data objects already
        // linked to the parent graph. Cannot link to sub-graph as well.
        DistributedReader existingReader = (DistributedReader)tableReader.getFederatedOperation();
        DistributedGraphReader sliceGraphReader = new DistributedGraphReader(
        		contextType, selectionCollector.getTypes(),
        		existingReader.getMarshallingContext());

        HBaseGraphAssembler graphAssembler = new GraphAssembler(contextType,
        		selectionCollector, sliceGraphReader, 
       			snapshotDate);
   	
        GraphRecognizerSyntaxTreeAssembler recognizerAssembler = new GraphRecognizerSyntaxTreeAssembler(
        		where, contextType);
        Expr graphRecognizerRootExpr = recognizerAssembler.getResult();
        if (log.isDebugEnabled()) {
            ExprPrinter printer = new ExprPrinter();
            graphRecognizerRootExpr.accept(printer);
            log.debug("Graph Recognizer: " + printer.toString());
        }
        
        // column filter
        HBaseFilterAssembler columnFilterAssembler = 
     		new GraphFetchColumnFilterAssembler(
     				this.selection, contextType);
        Filter columnFilter = columnFilterAssembler.getFilter();       
        
        List<Get> gets = new ArrayList<Get>();
		for (Edge edge : edges) {	
			byte[] childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid()); // use local edge UUID
			Get get = new Get(childRowKey);
			get.setFilter(columnFilter);
			gets.add(get);		
		}
		DataGraphConfig graphConfig = CloudGraphConfig.getInstance().getDataGraph(
				contextType.getQualifiedName());
        Result[] rows = this.fetchResult(gets, tableReader, 
    			graphConfig);
        
    	GraphRecognizerContext recognizerContext = new GraphRecognizerContext();
        int rowIndex = 0;
        for (Result resultRow : rows) {
        	if (resultRow == null || resultRow.isEmpty()) {
        		Get get = gets.get(rowIndex);
        		String rowStr = new String(get.getRow(), charset);
        		if (resultRow == null)
        		    throw new IllegalStateException("got null result row for '" + rowStr + "' for mulit-get operation - indicates failure with retries");
        		else
        		    throw new IllegalStateException("got no result for row for '" + rowStr + "' for mulit-get operation - indicates row noes not exist");
        	}
        	
      	    graphAssembler.assemble(resultRow);            	
        	PlasmaDataGraph assembledGraph = graphAssembler.getDataGraph();
            graphAssembler.clear();
        	
        	recognizerContext.setGraph(assembledGraph);
        	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
        		if (log.isDebugEnabled())
        			log.debug("recognizer excluded: " + Bytes.toString(
        					resultRow.getRow()));
        		if (log.isDebugEnabled())
        			log.debug(serializeGraph(assembledGraph));
        		
        		continue;
        	}

        	String rowKey = new String(resultRow.getRow(), charset);
        	results.put(rowKey, resultRow);
        	rowIndex++;
        }
		
		return results;
	}
		
    /**
	 * Creates a column qualifier/value filter hierarchy based on the given path
	 * predicate for a single row specified by the given row key, then returns
	 * the column qualifier sequence numbers which represent the subset
	 * of total graph edges as restricted by the predicate.   
	 * @param contextType the type of the edge property
	 * @param where the predicate
	 * @param rowKey the row key
	 * @return a collection of sequence ids
	 * @throws IOException 
	 * @see PredicateColumnFilterAssembler
	 */
	public Map<Integer, Integer> fetchSequences(PlasmaType contextType,
			Where where, RowReader rowReader) throws IOException {

        PlasmaType rootType = (PlasmaType)rowReader.getRootType();
		DataGraphConfig graphConfig = CloudGraphConfig.getInstance().getDataGraph(
				rootType.getQualifiedName());
        Get get = new Get(rowReader.getRowKey());
        PredicateColumnFilterAssembler columnFilterAssembler = 
        	new PredicateColumnFilterAssembler(rowReader.getGraphState(), 
        			rootType);
        columnFilterAssembler.assemble(where, contextType);
        Filter filter = columnFilterAssembler.getFilter();
        get.setFilter(filter);

		Result result = fetchResult(get, rowReader.getTableReader(), graphConfig);
		Map<Integer, Map<String, KeyValue>> buckets = buketizeResult(result, graphConfig);
		
		// assemble a recognizer once for
		// all results. Then only evaluate each result.
		EdgeRecognizerSyntaxTreeAssembler assembler = new EdgeRecognizerSyntaxTreeAssembler(where, 
			graphConfig, contextType, rootType);	
		Expr recogniser = assembler.getResult();
		
		Map<Integer, Integer> sequences = new HashMap<Integer, Integer>();
		EdgeRecognizerContext context = new EdgeRecognizerContext();
		
		for (Integer seq : buckets.keySet())
		{
			Map<String, KeyValue> seqMap = buckets.get(seq);
			context.setSequence(seq);
			context.setKeyMap(seqMap);			
			if (recogniser.evaluate(context))
				sequences.put(seq, seq);
		}
        return sequences;
	}
	
	/**
	 * Runs the given get and returns the result.  
	 * @param get the row get
	 * @return the result.
	 * @throws IOException 
	 */
	public Result fetchResult(Get get, TableOperation tableOperation, 
			DataGraphConfig graphConfig) throws IOException
	{
        if (log.isDebugEnabled() )
			try {
				log.debug("get filter: " + FilterUtil.printFilterTree(get.getFilter()));
			} catch (IOException e1) {
			}
        
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("executing get...");
        
        Result result = tableOperation.getConnection().get(get);
        if (result == null) // Note: may not have any key-values
        	throw new GraphServiceException("expected result from table "
                + tableOperation.getTable().getName() + " for row '"
        		+ new String(get.getRow()) + "'");
    	
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("returned 1 results ("
        	    + String.valueOf(after - before) + ")");
        
        return result;
	}
	
	/**
	 * Runs the given get and returns the result.  
	 * @param gets the list of row get operations
	 * @return the result.
	 * @throws IOException 
	 */
	public Result[] fetchResult(List<Get> gets, TableOperation tableOperation, 
			DataGraphConfig graphConfig) throws IOException
	{
        if (log.isDebugEnabled() )
			try {
				log.debug("get filter: " + FilterUtil.printFilterTree(gets.get(0).getFilter()));
			} catch (IOException e1) {
			}
        
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("executing "+gets.size()+" gets...");
        
        Result[] result = tableOperation.getConnection().get(gets);
    	
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("returned "+result.length+" results ("
        	    + String.valueOf(after - before) + ")");
        
        return result;
	}
	
	public Map<Integer, Map<String, KeyValue>> buketizeResult(Result result, DataGraphConfig graphConfig) {
		Map<Integer, Map<String, KeyValue>> resultMap = new HashMap<Integer, Map<String, KeyValue>>();
  	    
		if (!result.isEmpty())
			for (KeyValue keyValue : result.list()) {
	  	    	// FIXME: no parsing here !!
	            String qual = Bytes.toString(keyValue.getQualifier());
	  	    	if (log.isDebugEnabled()) 
	  	    	    log.debug("\tkey: " + qual
	  	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
	  	        String[] sections = qual.split(graphConfig.getColumnKeySectionDelimiter());
	  	        Integer seq = Integer.valueOf(sections[1]);
	  	        Map<String, KeyValue> subMap = resultMap.get(seq);
	  	        if (subMap == null) {
	  	        	subMap = new HashMap<String, KeyValue>();
	  	        	resultMap.put(seq, subMap);
	  	        }
	  	        subMap.put(qual, keyValue);	  	         
	  	    }
		
		return resultMap;
	}

	/**
	 * Runs the given get and parses the column qualifier sequence
	 * number suffixes from the returned columns.  
	 * @param get the row get
	 * @return the sequence numbers.
	 * @throws IOException 
	 */
	public Map<Integer, Integer> fetchSequences(Get get, TableReader tableReader, DataGraphConfig graphConfig) throws IOException
	{
        if (log.isDebugEnabled() )
			try {
				log.debug("get filter: " + FilterUtil.printFilterTree(get.getFilter()));
			} catch (IOException e1) {
			}
        
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("executing get...");
        
        Result result = tableReader.getConnection().get(get);
        if (result == null) // Note: may not have any key-values
        	throw new GraphServiceException("expected result from table "
                + tableReader.getTable().getName() + " for row '"
        		+ new String(get.getRow()) + "'");
    	
		Map<Integer, Integer> seqMap = new HashMap<Integer, Integer>();
  	    if (!result.isEmpty())
			for (KeyValue keyValue : result.list()) {
	  	    	// FIXME: no parsing here !!
	            String qual = Bytes.toString(keyValue.getQualifier());
	  	    	if (log.isDebugEnabled()) 
	  	    	    log.debug("\tkey: " + qual
	  	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
	  	        String[] sections = qual.split(graphConfig.getColumnKeySectionDelimiter());
	  	        Integer seq = Integer.valueOf(sections[1]);
	  	        seqMap.put(seq, seq);
	  	    }
        
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("returned 1 results ("
        	    + String.valueOf(after - before) + ")");
        return seqMap;
	}
	
	/**
	 * Runs the given scan and parses the column qualifier sequence
	 * number suffixes from the returned columns.  
	 * @param scan the row scan
	 * @return the sequence numbers.
	 */
	public Map<Long, Long> fetchSequences(Scan scan, TableReader tableReader, DataGraphConfig graphConfig)
	{
        if (log.isDebugEnabled() )
			try {
				log.debug("scan filter: " + FilterUtil.printFilterTree(scan.getFilter()));
			} catch (IOException e1) {
			}
        
        if (log.isDebugEnabled())
        	log.debug("executing scan...");
        ResultScanner scanner = null;
		try {
			scanner = tableReader.getConnection().getScanner(scan);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}				 
				
		Map<Long, Long> result = new HashMap<Long, Long>();
        for (Result row : scanner) {
        	if (log.isDebugEnabled()) 
      	        log.debug("row: " + new String(row.getRow()));              	  
      	    for (KeyValue keyValue : row.list()) {
                if (log.isDebugEnabled())
                	log.debug("returned " + row.size() + " columns");
      	    	// FIXME: no parsing here !!
                String qual = Bytes.toString(keyValue.getQualifier());
      	    	if (log.isDebugEnabled()) 
      	    	    log.debug("\tkey: " + qual
      	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
      	        String[] sections = qual.split(graphConfig.getColumnKeySectionDelimiter());
      	        Long seq = Long.valueOf(sections[1]);
      	    	result.put(seq, seq);
      	    }
        }
        return result;
	}

	/**
	 * Loads the columns resulting from a {@link BinaryPrefixColumnFilterAssembler} based on the 
	 * given properties into the given row reader.
	 * @param properties the properties to fetch
	 * @param contextType the current type
	 * @param rowReader the row reader
	 * @throws IOException
	 * @see BinaryPrefixColumnFilterAssembler
	 */
	public void load(Set<Property> properties,  
			PlasmaType contextType, RowReader rowReader) throws IOException
	{
        Get get = new Get(rowReader.getRowKey());
		
        PlasmaType rootType = (PlasmaType)rowReader.getRootType();        
        BinaryPrefixColumnFilterAssembler columnFilterAssembler = 
        	new BinaryPrefixColumnFilterAssembler(rootType);
        columnFilterAssembler.assemble(properties,
        		contextType);
        Filter filter = columnFilterAssembler.getFilter();
        get.setFilter(filter);
        
        load(get, rowReader);
	}
	
	/**
	 * Loads the columns resulting from a {@link StatefullBinaryPrefixColumnFilterAssembler} based on the 
	 * given properties and the given state sequences into the given row reader.
	 * @param sequences the sequences
	 * @param properties the properties to fetch
	 * @param contextType the current type
	 * @param rowReader the row reader
	 * @throws IOException
	 */
	public void loadBySequenceList(Collection<Integer> sequences, Set<Property> properties,
			PlasmaType contextType, RowReader rowReader) throws IOException
	{
        Scan scan = new Scan();
        scan.setStartRow(rowReader.getRowKey());
        scan.setStopRow(rowReader.getRowKey());
        PlasmaType rootType = (PlasmaType)rowReader.getRootType();        
        
        StatefullBinaryPrefixColumnFilterAssembler columnFilterAssembler = 
        	new StatefullBinaryPrefixColumnFilterAssembler( 
        		rowReader.getGraphState(), rootType);
        columnFilterAssembler.assemble(properties, sequences, contextType);
        Filter filter = columnFilterAssembler.getFilter();
        scan.setFilter(filter);
        load(scan, rowReader);
	}

	/**
	 * Loads columns returned with the given get and its column filter into the existing row reader. 
	 * @param get the Get operations
	 * @param rowReader the existing row reader 
	 * @throws IOException
	 */
	public void load(Get get, RowReader rowReader) throws IOException
	{        
        if (log.isDebugEnabled() )
			try {
				log.debug("get filter: " 
			    + FilterUtil.printFilterTree(get.getFilter()));
			} catch (IOException e1) {
			}		
        
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled())
        	log.debug("executing get...");
        
        Result result = rowReader.getTableReader().getConnection().get(get);
        if (result == null) // do expect a result since a Get oper, but might have no columns
        	throw new GraphServiceException("expected result from table "
                + rowReader.getTableReader().getTable().getName() + " for row '"
        		+ new String(get.getRow()) + "'");
    	if (!result.isEmpty())
	  	    for (KeyValue keyValue : result.list()) {
	  	    	rowReader.getRow().addColumn(keyValue);
	  	    	if (log.isDebugEnabled()) {
	      	    	String qual = Bytes.toString(keyValue.getQualifier());
	  	    	    log.debug("\tkey: " + qual
	  	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
	  	    	}
	  	    }
    	
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("returned 1 results ("
        	    + String.valueOf(after - before) + ")");
	}		
	
	public void load(Scan scan, RowReader rowReader)
	{        
        if (log.isDebugEnabled() )
			try {
				log.debug("scan filter: " 
			    + FilterUtil.printFilterTree(scan.getFilter()));
			} catch (IOException e1) {
			}		
        
        if (log.isDebugEnabled())
        	log.debug("executing scan...");
        ResultScanner scanner = null;
		try {
			scanner = rowReader.getTableReader().getConnection().getScanner(scan);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}
        for (Result row : scanner) {
        	if (log.isDebugEnabled()) 
      	        log.debug("row: " + new String(row.getRow()));              	  
            if (log.isDebugEnabled())
            	log.debug("returned " + row.size() + " columns");
      	    for (KeyValue keyValue : row.list()) {
      	    	rowReader.getRow().addColumn(keyValue);
      	    	if (log.isDebugEnabled()) {
          	    	String qual = Bytes.toString(keyValue.getQualifier());
      	    	    log.debug("\tkey: " + qual
      	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
      	    	}
      	    }
        }		
	}	
	
    private String serializeGraph(commonj.sdo.DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("debug");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }

}
