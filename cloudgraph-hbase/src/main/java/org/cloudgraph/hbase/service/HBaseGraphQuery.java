package org.cloudgraph.hbase.service;

// java imports
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.HBaseFilterAssembler;
import org.cloudgraph.hbase.filter.InitialFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateRowFilterAssembler;
import org.cloudgraph.hbase.key.CompositeRowKeyFactory;
import org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.ScanContext;
import org.cloudgraph.hbase.util.FilterUtil;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Variable;
import org.plasma.query.model.Where;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.QueryVisitor;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.QueryDispatcher;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaTypeHelper;

/**
 * Assembles and returns one or more {@link DataGraph data graphs} 
 * from HBase given a PlasmaQuery&#8482; based XPath or DSL query.
 * First an HBase row filter is assembled using 
 * {@link PredicateRowFilterAssembler} which uses query 
 * literal values and logic found in the 'where'
 * clause or predicate(s).     
 * <p>
 * Any "slice" of a graph or set of sub-graphs can be selected using the
 * PlasmaQuery&#8482; API by specifying paths through the graph. Paths may
 * include any number of predicates along the path. Based on this selection
 * criteria an {@link GraphFetchColumnFilterAssembler} is used to 
 * precisely restrict the HBase columns returned for each result row.      
 * </p>
 * <p>
 * Then for each resulting HBase row, a data graph 
 * {@link org.cloudgraph.hbase.service.HBaseGraphAssembler assembler} is used to reconstruct and return the original
 * graph structure from the resulting HBase row.
 * </p>
 * <p>
 * The PlasmaQuery&#8482; API provides a flexible mechanism to fully describe 
 * any arbitrary SDO results Data Graph, independent of any persistence 
 * framework or type of data store.  PlasmaQuery\u2122 supports XPath 
 * expressions as a free-text "surface language", parsed by the API 
 * implementation and used to construct an underlying query object 
 * model representation. As an alternative to free-text, PlasmaQuery\u2122 
 * contains a query Domain Specific Language (DSL) generator and 
 * API facilitating (IDE) code-completion, 100% compile-time checking 
 * and resulting in code with an almost "fluent" English appearance 
 * based on your business model.  
 * </p>
 * 
 * @see org.plasma.query.Query
 * @see PredicateRowFilterAssembler
 * @see DefaultGraphAssembler
 * @see GraphFetchColumnFilterAssembler
 * 
 */
public class HBaseGraphQuery 
    implements QueryDispatcher
{
    private static Log log = LogFactory.getLog(HBaseGraphQuery.class);

    public HBaseGraphQuery()
    {
    }

    public void close()
    {
    }

    public PlasmaDataGraph[] find(Query query, Timestamp snapshotDate)
    {
        return find(query, -1, snapshotDate);
    }

    public PlasmaDataGraph[] find(Query query, int requestMax, Timestamp snapshotDate)
    {
        From from = query.getFromClause();
        PlasmaType type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(
        		type.getQualifiedName());
    	HTableInterface con = HBaseConnectionManager.instance().getConnection(
    			tableConfig.getName());
    	 
        PlasmaDataGraph[] results = new PlasmaDataGraph[0];
        try {
            List<PlasmaDataGraph> queryResults = findResults(query, type, snapshotDate, tableConfig, con);
            
            if (log.isDebugEnabled() ){
                log.debug("assembling results");
            }
            results = new PlasmaDataGraph[queryResults.size()];
            queryResults.toArray(results);
        }
        finally {
    		try {
    			con.close();
    		} catch (IOException e) {
    			log.error(e.getMessage(), e);
    		}
        }
        
        return results;
    }

    /**
     * Returns a count of the given query. 
     * @param query the query
     * @return the query results size
     */
    public int count(Query query)
    {
        From from = query.getFromClause();
        PlasmaType type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(
        		type.getQualifiedName());
    	HTableInterface con = HBaseConnectionManager.instance().getConnection(
    			tableConfig.getName());
        int size = 0;
		try {
			size = this.countResults(query, type, tableConfig, con);
		}
		finally {
    		try {
    			con.close();
    		} catch (IOException e) {
    			log.error(e.getMessage(), e);
    		}
        }
		
        return size;
    }  
    
    private int countResults(Query query, PlasmaType type, TableConfig tableConfig, HTableInterface con)
    {
        int count = 0;
        Object[] params = new Object[0];

        Scan scan = new Scan();
        
    	scan.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
    		Bytes.toBytes(CloudGraphConstants.ROOT_UUID_COLUMN_NAME));
        
        try {
            // Create and add a scan start/stop range or row filter 
            Where where = query.findWhereClause();
            if (where != null)
            {
            	ScanContext scanContext = 
            			new ScanContext(type, where);
            	if (scanContext.canUsePartialKeyScan()) {
            		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
            		scanAssembler.assemble(scanContext.getLiterals());
                    scan.setStartRow(scanAssembler.getStartKey()); // inclusive
                    scan.setStopRow(scanAssembler.getStopKey()); // exclusive
              		if (log.isDebugEnabled())
            			log.debug("partial key scan: (" 
              		        + "start: " + Bytes.toString(scan.getStartRow())
              		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
            	}
            	else {
    	            PredicateRowFilterAssembler rowFilterAssembler = 
    	            	new PredicateRowFilterAssembler(type);
    	            rowFilterAssembler.assemble(where, type);
    	            scan.setFilter(rowFilterAssembler.getFilter());
            	}
            }
            else {
        		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
        		scanAssembler.assemble();
                scan.setStartRow(scanAssembler.getStartKey()); // inclusive
                scan.setStopRow(scanAssembler.getStopKey()); // exclusive
          		if (log.isDebugEnabled())
        			log.debug("default graph partial key scan: (" 
          		        + "start: " + Bytes.toString(scan.getStartRow())
          		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
            }

            if (query.getStartRange() != null && query.getEndRange() != null)
                log.warn("query range (start: "
                		+ query.getStartRange() + ", end: "
                		+ query.getEndRange() + ") ignored for count operation");
                
        	long before = System.currentTimeMillis();
            if (log.isDebugEnabled())  
                log.debug("executing count...");
        	ResultScanner scanner = con.getScanner(scan);
            for (Result result : scanner) {
            	count++;
            }       
            long after = System.currentTimeMillis();
            if (log.isDebugEnabled())  
                log.debug("returning count " + String.valueOf(count)
                		+ " (" + String.valueOf(after - before) + ")");
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}            	 
        finally {
        }
        return count;
    }
    
    private List<PlasmaDataGraph> findResults(Query query, PlasmaType type, Timestamp snapshotDate, TableConfig tableConfig, HTableInterface con)
    {
        Object[] params = new Object[0];
        List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();
        PropertySelectionCollector collector = new PropertySelectionCollector(
            query.getSelectClause(), type);
        collector.getResult(); // trigger the traversal

        Scan scan = new Scan();
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
        
        // get all columns for family
        //scan.addFamily(Bytes.toBytes(CloudGraphConstants.DATA_TABLE_FAMILY_1));
        
        // Create and add a column filter for the initial
        // column set based on existence of path predicates
        // in the Select. 
        HBaseFilterAssembler columnFilterAssembler = null;
        if (collector.getPredicateMap().size() > 0) {
            columnFilterAssembler = 
            	new InitialFetchColumnFilterAssembler(  
            			collector, type);
        }
        else {
            columnFilterAssembler = 
        		new GraphFetchColumnFilterAssembler(
        				collector, type);
        }
        rootFilter.addFilter(columnFilterAssembler.getFilter());

        // Create and add a scan start/stop range or row filter 
        Where where = query.findWhereClause();
        if (where != null)
        {
        	ScanContext scanContext = 
        			new ScanContext(type, where);
        	if (scanContext.canUsePartialKeyScan()) {
        		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
        		scanAssembler.assemble(scanContext.getLiterals());
                scan.setStartRow(scanAssembler.getStartKey()); // inclusive
                scan.setStopRow(scanAssembler.getStopKey()); // exclusive
          		if (log.isDebugEnabled())
        			log.debug("partial key scan: (" 
          		        + "start: " + Bytes.toString(scan.getStartRow())
          		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
        	}
        	else {
	            PredicateRowFilterAssembler rowFilterAssembler = 
	            	new PredicateRowFilterAssembler(type);
	            rowFilterAssembler.assemble(where, type);
	            rootFilter.addFilter(rowFilterAssembler.getFilter());
        	}
        }
        else {
    		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
    		scanAssembler.assemble();
            scan.setStartRow(scanAssembler.getStartKey()); // inclusive
            scan.setStopRow(scanAssembler.getStopKey()); // exclusive
      		if (log.isDebugEnabled())
    			log.debug("default graph partial key scan: (" 
      		        + "start: " + Bytes.toString(scan.getStartRow())
      		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
        }
        
        // Create a graph assembler based on existence
        // on selection path predicates
        HBaseGraphAssembler graphAssembler = null;
        if (collector.getPredicateMap().size() > 0) { 
        	graphAssembler = new GraphSliceAssembler(type, 
                collector, snapshotDate, tableConfig, con);
        }
        else {
        	graphAssembler = new DefaultGraphAssembler(type, 
                collector.getResult(), snapshotDate, tableConfig);
        }
        
        // Create a scan. For each result row, 
        // assemble a graph and return it
        try {
        	long before = System.currentTimeMillis();
            if (log.isDebugEnabled() ) 
                log.debug("executing scan...");
            
            if (log.isDebugEnabled() ) 
            	log.debug(FilterUtil.printFilterTree(rootFilter));
            ResultScanner scanner = con.getScanner(scan);
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
          	    graphAssembler.assemble(resultRow);
                result.add(graphAssembler.getDataGraph());
                graphAssembler.clear();
                count++;
            }      
            long after = System.currentTimeMillis();
            log.info("assembled " + String.valueOf(count) + " results ("
            	+ String.valueOf(after - before) + ")");
        }
        catch (IOException e) {
            throw new GraphServiceException(e);
        }
        catch (Throwable t) {
            throw new GraphServiceException(t);
        }
        finally {
        }
        return result;
    }
        
    public List getVariables(Where where)
    {
        final List<Variable> list = new ArrayList<Variable>(1);
        QueryVisitor visitor = new DefaultQueryVisitor() {
            public void start(Variable var) {
                list.add(var);
            }
        };
        where.accept(visitor);
        return list;
    }    
    

}


