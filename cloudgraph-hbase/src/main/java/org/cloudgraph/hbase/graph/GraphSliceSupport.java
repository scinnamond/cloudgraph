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
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.hbase.expr.Expr;
import org.cloudgraph.hbase.filter.BinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateColumnFilterAssembler;
import org.cloudgraph.hbase.filter.StatefullBinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableOperation;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.util.FilterUtil;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.Property;

/**
 * Delegate class for various graph slice fetch and edge post processing
 * operations. Supports graph assemblers and
 * other clients. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class GraphSliceSupport {
    private static Log log = LogFactory.getLog(GraphSliceSupport.class);
	
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
		
		// assemble a recogniser once for
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
}
