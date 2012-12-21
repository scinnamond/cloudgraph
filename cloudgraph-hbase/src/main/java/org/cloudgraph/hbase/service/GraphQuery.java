package org.cloudgraph.hbase.service;

// java imports
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.HBaseFilterAssembler;
import org.cloudgraph.hbase.filter.InitialFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateRowFilterAssembler;
import org.cloudgraph.hbase.graph.FederatedGraphAssembler;
import org.cloudgraph.hbase.graph.FederatedGraphSliceAssembler;
import org.cloudgraph.hbase.graph.HBaseGraphAssembler;
import org.cloudgraph.hbase.graph.SimpleGraphAssembler;
import org.cloudgraph.hbase.io.FederatedGraphReader;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.GraphTableReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.ScanContext;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.state.GraphState;
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
 * {@link org.cloudgraph.hbase.graph.HBaseGraphAssembler assembler} is used to reconstruct and return the original
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
 * @see SimpleGraphAssembler
 * @see GraphFetchColumnFilterAssembler
 * 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphQuery 
    implements QueryDispatcher
{
    private static Log log = LogFactory.getLog(GraphQuery.class);
    private ServiceContext context;

    public GraphQuery(ServiceContext context)
    {
    	this.context = context;
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
        if (from.getEntity() == null)
        	throw new IllegalArgumentException("given query has no root type and/or URI");
        if (from.getEntity().getName() == null || from.getEntity().getNamespaceURI() == null)
        	throw new IllegalArgumentException("given query has no root type and/or URI");
        PlasmaType type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        PlasmaDataGraph[] results = new PlasmaDataGraph[0];
        try {
            List<PlasmaDataGraph> queryResults = findResults(query, type, snapshotDate);
            
            if (log.isDebugEnabled() ){
                log.debug("assembling results");
            }
            results = new PlasmaDataGraph[queryResults.size()];
            queryResults.toArray(results);
        }
        finally {
        	
        	//FIXME: close connections
    		//try {
    		//	con.close();
    		//} catch (IOException e) {
    		//	log.error(e.getMessage(), e);
    		//}
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
        int size = 0;
		try {
			size = this.countResults(query, type);
		}
		finally {
        }
		
        return size;
    }  
    
    private int countResults(Query query, PlasmaType type)
    {
        int count = 0;
        Object[] params = new Object[0];
        
        TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(
        		type.getQualifiedName());
        TableReader tableReader = new GraphTableReader(tableConfig,
        		null); // FIXME: 
        
        Scan scan = new Scan();
        
    	scan.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
    		Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
        
        // Create and add a scan start/stop range or row filter 
        Where where = query.findWhereClause();
        setupScanContext(scan, rootFilter, where, type);

        try {
            if (query.getStartRange() != null && query.getEndRange() != null)
                log.warn("query range (start: "
                		+ query.getStartRange() + ", end: "
                		+ query.getEndRange() + ") ignored for count operation");
                
        	long before = System.currentTimeMillis();
            if (log.isDebugEnabled())  
                log.debug("executing count...");
        	ResultScanner scanner = tableReader.getConnection().getScanner(scan);
            for (Result result : scanner) {
            	if (result.containsColumn(tableReader.getTable().getDataColumnFamilyNameBytes(), 
            			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
            		continue; // ignore toumbstone roots
            	}
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
        	try {
				tableReader.getConnection().close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
        }
        return count;
    }
    
    private List<PlasmaDataGraph> findResults(Query query, PlasmaType type, Timestamp snapshotDate)
    {
        Object[] params = new Object[0];
        List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();
        PropertySelectionCollector collector = new PropertySelectionCollector(
            query.getSelectClause(), type);
        collector.setOnlyDeclaredProperties(false);
        collector.getResult(); // trigger the traversal
        if (log.isDebugEnabled())
        	log.debug(collector.dumpInheritedProperties());
        FederatedGraphReader graphReader = new FederatedGraphReader(
        		type, collector.getTypes(),
        		this.context.getMarshallingContext());
        TableReader rootTableReader = graphReader.getRootTableReader();

        Scan scan = new Scan();
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
        
        // Create and add a column filter for the initial
        // column set based on existence of path predicates
        // in the Select. 
        HBaseFilterAssembler columnFilterAssembler = createRootColumnFilterAssembler(type,
        	collector);
        rootFilter.addFilter(columnFilterAssembler.getFilter());

        // Create and add a scan start/stop range or row filter 
        Where where = query.findWhereClause();
        setupScanContext(scan, rootFilter, where, type);
        
        // Create a graph assembler based on existence
        // of selection path predicates, need for federation, etc...
        HBaseGraphAssembler graphAssembler = createGraphAssembler(
        	type, graphReader, collector, snapshotDate);
        
        // Create a scan. For each result row, 
        // assemble a graph and return it
        try {
        	long before = System.currentTimeMillis();
            if (log.isDebugEnabled() ) 
                log.debug("executing scan...");
            
            if (log.isDebugEnabled() ) 
            	log.debug(FilterUtil.printFilterTree(rootFilter));
            ResultScanner scanner = rootTableReader.getConnection().getScanner(scan);
        	int count = 0;
            for (Result resultRow : scanner) {
            	if (log.isTraceEnabled()) {
          	        log.trace(rootTableReader.getTable().getName() + ": " + new String(resultRow.getRow()));              	  
              	    for (KeyValue keyValue : resultRow.list()) {
              	    	log.trace("\tkey: " 
              	    		+ new String(keyValue.getQualifier())
              	    	    + "\tvalue: " + new String(keyValue.getValue()));
              	    }
            	}
            	if (resultRow.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
            			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
            		continue; // ignore toumbstone roots
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
        	for (TableReader reader : graphReader.getTableReaders()) {
        		if (reader.hasConnection()) {
        			try {
						reader.getConnection().close();
					} catch (IOException e) {
						log.error(e.getMessage());
					}
        		}
        	}
        }
        return result;
    }
    
    /**
     * Create and add a column filter for the initial
     * column set based on existence of path predicates
     * in the Select. If no path predicates exist in the selection, 
     * we know the entire selection graph can be fetched in one round trip, so
     * {@link GraphFetchColumnFilterAssembler} is create. Otherwise
     * a (@link InitialFetchColumnFilterAssembler) is used to populate
     * the root(s), and then subsequent fetches are used as directed within the
     * graph assembler facilitated by the path predicates. 
     *  
     * @param type the root type
     * @param collector the selection collector
     * @return the new filter assembler
     * 
     * @see GraphFetchColumnFilterAssembler
     * @see InitialFetchColumnFilterAssembler
     */
    private HBaseFilterAssembler createRootColumnFilterAssembler(PlasmaType type,
    		PropertySelectionCollector collector)
    {
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
        return columnFilterAssembler;
    }
    
    /**
     * Determines whether a partial row-key scan is possible given the
     * current {@link ScanContext}, and is so {@link PartialRowKeyScanAssembler assembles}  
     * a partial row-key scan. Otherwise a row filter hierarchy is 
     * {@link PredicateRowFilterAssembler assembled} and added to the given 
     * root filter. 
     * 
     * @param scan the HBase scan
     * @param rootFilter the root row/column filter (list)
     * @param where the root predicate
     * @param type the root type
     * 
     * @see org.cloudgraph.hbase.scan.ScanContext
     * @see org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler
     */
    private void setupScanContext(Scan scan, FilterList rootFilter, 
    		Where where, PlasmaType type)
    {
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
            //FIXME: partial key scans failing here - The Type name where it is the final field
	        // in the key has incorrect stop-key value. E.g. both start and stop are the same
	        /*    
    		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
    		scanAssembler.assemble();
            scan.setStartRow(scanAssembler.getStartKey()); // inclusive
            scan.setStopRow(scanAssembler.getStopKey()); // exclusive
      		if (log.isDebugEnabled())
    			log.debug("default graph partial key scan: (" 
      		        + "start: " + Bytes.toString(scan.getStartRow())
      		        + " stop: " + Bytes.toString(scan.getStopRow()) + ")");
      		*/        
        }    	
    }
       
    /**
     * Create a specific graph assembler based on the existence
     * of selection path predicates found in the given collector. 
     * Since the reader hierarchy is initialized based entirely on metadata
     * found in the selection graph, whether federation exists across the
     * persisted graph cannot be determined up front. Federation must be
     * discovered dynamically during assembly. Therefore on all cases
     * graph assemblers capable of handling a federated graph are used
     * on all cases.   
     * 
     * @param type the root type
     * @param graphReader the graph reader
     * @param collector the selection collector
     * @param snapshotDate the query snapshot date
     * @return the graph assembler
     */
    private HBaseGraphAssembler createGraphAssembler(
    		PlasmaType type,
    		FederatedReader graphReader,
    		PropertySelectionCollector collector,
    		Timestamp snapshotDate)
    {
        HBaseGraphAssembler graphAssembler = null;
        
        if (collector.getPredicateMap().size() > 0) { 
        	graphAssembler = new FederatedGraphSliceAssembler(type,
            		collector, graphReader, snapshotDate);
        }
        else {
        	graphAssembler = new FederatedGraphAssembler(type,
            		collector, graphReader, snapshotDate);
        }
	        
    	return graphAssembler;
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


