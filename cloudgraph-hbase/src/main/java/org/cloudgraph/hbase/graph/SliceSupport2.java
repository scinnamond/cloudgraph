package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.cloudgraph.hbase.filter.BinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateColumnFilterAssembler;
import org.cloudgraph.hbase.filter.StatefullBinaryPrefixColumnFilterAssembler;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.util.FilterUtil;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;

/**
 * Delegates fetch operations for graph slice assemblers and
 * other clients. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class SliceSupport2 {
    private static Log log = LogFactory.getLog(SliceSupport2.class);
	
	/**
	 * Creates a column qualifier/value filter hierarchy based on the given path
	 * predicate for a single row specified by the given row key, then returns
	 * the column qualifier sequence numbers which represent the subset
	 * of total graph edges as restricted by the predicate.   
	 * @param contextType the current type
	 * @param where the predicate
	 * @param rowKey the row key
	 * @return a collection of sequence ids
	 * @throws IOException 
	 * @see PredicateColumnFilterAssembler
	 */
	public Map<Long, Long> fetchSequences(PlasmaType contextType,
			Where where, RowReader rowReader) throws IOException {

        Get get = new Get(rowReader.getRowKey());
        PredicateColumnFilterAssembler columnFilterAssembler = 
        	new PredicateColumnFilterAssembler(rowReader.getGraphState(), 
        			(PlasmaType)rowReader.getRootType());
        columnFilterAssembler.assemble(where, contextType);
        Filter filter = columnFilterAssembler.getFilter();
        get.setFilter(filter);

        PlasmaType rootType = (PlasmaType)rowReader.getRootType();
		DataGraphConfig graphConfig = CloudGraphConfig.getInstance().getDataGraph(
				rootType.getQualifiedName());
        
        return fetchSequences(get, rowReader.getTableReader(), graphConfig);
	}

	/**
	 * Runs the given get and parses the column qualifier sequence
	 * number suffixes from the returned columns.  
	 * @param get the row get
	 * @return the sequence numbers.
	 * @throws IOException 
	 */
	public Map<Long, Long> fetchSequences(Get get, TableReader tableReader, DataGraphConfig graphConfig) throws IOException
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
    	
		Map<Long, Long> seqMap = new HashMap<Long, Long>();
  	    if (!result.isEmpty())
			for (KeyValue keyValue : result.list()) {
	  	    	// FIXME: no parsing here !!
	            String qual = Bytes.toString(keyValue.getQualifier());
	  	    	if (log.isDebugEnabled()) 
	  	    	    log.debug("\tkey: " + qual
	  	    	        + "\tvalue: " + Bytes.toString(keyValue.getValue()));
	  	        String[] sections = qual.split(graphConfig.getColumnKeySectionDelimiter());
	  	        Long seq = Long.valueOf(sections[1]);
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

	public void load(List<String> propertyNames,
			PlasmaType contextType, RowReader rowReader) throws IOException
	{
        Get get = new Get(rowReader.getRowKey());
		
        PlasmaType rootType = (PlasmaType)rowReader.getRootType();        
        BinaryPrefixColumnFilterAssembler columnFilterAssembler = 
        	new BinaryPrefixColumnFilterAssembler(rootType);
        columnFilterAssembler.assemble(propertyNames,
        		contextType);
        Filter filter = columnFilterAssembler.getFilter();
        get.setFilter(filter);
        
        load(get, rowReader);
	}
	
	public void loadBySequenceList(Collection<Long> sequences, List<String> propertyNames,
			PlasmaType contextType, RowReader rowReader) throws IOException
	{
        Scan scan = new Scan();
        scan.setStartRow(rowReader.getRowKey());
        scan.setStopRow(rowReader.getRowKey());
        PlasmaType rootType = (PlasmaType)rowReader.getRootType();        
        
        StatefullBinaryPrefixColumnFilterAssembler columnFilterAssembler = 
        	new StatefullBinaryPrefixColumnFilterAssembler( 
        		rowReader.getGraphState(), rootType);
        columnFilterAssembler.assemble(propertyNames, sequences, contextType);
        Filter filter = columnFilterAssembler.getFilter();
        scan.setFilter(filter);
        load(scan, rowReader);
	}

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
