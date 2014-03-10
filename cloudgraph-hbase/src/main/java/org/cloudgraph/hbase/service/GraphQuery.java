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
package org.cloudgraph.hbase.service;

// java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.expr.Expr;
import org.cloudgraph.hbase.expr.ExprPrinter;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.HBaseFilterAssembler;
import org.cloudgraph.hbase.filter.InitialFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.PredicateRowFilterAssembler;
import org.cloudgraph.hbase.graph.FederatedGraphAssembler;
import org.cloudgraph.hbase.graph.FederatedGraphSliceAssembler;
import org.cloudgraph.hbase.graph.GraphRecognizerContext;
import org.cloudgraph.hbase.graph.GraphRecognizerSyntaxTreeAssembler;
import org.cloudgraph.hbase.graph.HBaseGraphAssembler;
import org.cloudgraph.hbase.io.FederatedGraphReader;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.scan.CompleteRowKey;
import org.cloudgraph.hbase.scan.FuzzyRowKey;
import org.cloudgraph.hbase.scan.FuzzyRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.PartialRowKey;
import org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.ScanCollector;
import org.cloudgraph.hbase.scan.ScanContext;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.state.GraphState;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.OrderBy;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.collector.Selection;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Variable;
import org.plasma.query.model.Where;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.QueryVisitor;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.QueryDispatcher;
import org.plasma.sdo.access.provider.common.DataGraphComparatorAssembler;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.xml.sax.SAXException;

import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

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
        	results = findResults(query, type, snapshotDate);
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
    	// FIXME: don't care about ordering, graph assembly 
    	// (if no graph recognizer), or potentially returning any
    	// columns whatsoever. 
    	PlasmaDataGraph[] graphs = find(query, 
        	type, new Timestamp(System.currentTimeMillis()));
        return graphs.length;
    }
    
    private PlasmaDataGraph[] findResults(Query query, PlasmaType type, Timestamp snapshotDate)
    {   
    	PlasmaDataGraph[] graphs = find(query, 
        		type, snapshotDate);
    	PlasmaDataGraph[] result = graphs;    	
        
        int last = graphs.length; 
        if (query.getStartRange() != null && query.getEndRange() != null) {
        	if (query.getStartRange() < last && query.getEndRange() < last) {
        		result = Arrays.copyOfRange(graphs, query.getStartRange(), 
            			query.getEndRange());
        	}
        	else if (query.getStartRange() < last && query.getEndRange() >= last) {
        		result = Arrays.copyOfRange(graphs, query.getStartRange(), 
            			last);
        		log.warn("query end range (" + query.getEndRange()
            	    + ") exceeds results size (" + graphs.length
            	    + ") - truncating results");
        	}
        	else {
        		log.warn("query range ("
        	        + query.getStartRange()+ ":" + query.getEndRange()
        	        + ") exceeds results size (" + graphs.length 
        	        + ") - clearing results");
        		result = new PlasmaDataGraph[0];
        	}
        }
        log.info("returning " + result.length + " results");
        
        return result;
    }  
    
    private PlasmaDataGraph[] find(Query query, PlasmaType type, Timestamp snapshotDate)
    {
        if (log.isDebugEnabled())
        	log(query);
        Where where = query.findWhereClause();
        SelectionCollector selectionCollector = null;
        if (where != null)
        	selectionCollector = new SelectionCollector(
                query.getSelectClause(), where, type);
        else
        	selectionCollector = new SelectionCollector(
                    query.getSelectClause(), type);
        selectionCollector.setOnlyDeclaredProperties(false);
        for (Type t : selectionCollector.getTypes()) 
        	collectRowKeyProperties(selectionCollector, (PlasmaType)t);        
        if (log.isDebugEnabled())
        	log.debug(selectionCollector.dumpInheritedProperties());
        
        FederatedGraphReader graphReader = new FederatedGraphReader(
        		type, selectionCollector.getTypes(),
        		this.context.getMarshallingContext());
        TableReader rootTableReader = graphReader.getRootTableReader();

        // Create and add a column filter for the initial
        // column set based on existence of path predicates
        // in the Select. 
        HBaseFilterAssembler columnFilterAssembler = 
        	createRootColumnFilterAssembler(type,
        	selectionCollector);
        Filter columnFilter = columnFilterAssembler.getFilter();

        // Create a graph assembler based on existence
        // of selection path predicates, need for federation, etc...
        HBaseGraphAssembler graphAssembler = createGraphAssembler(
        	type, graphReader, selectionCollector, snapshotDate);
        
        List<PartialRowKey> partialScans = new ArrayList<PartialRowKey>();
        List<FuzzyRowKey> fuzzyScans = new ArrayList<FuzzyRowKey>();
        List<CompleteRowKey> completeKeys = new ArrayList<CompleteRowKey>();
        Expr graphRecognizerRootExpr = null;
        if (where != null) {
	        GraphRecognizerSyntaxTreeAssembler recognizerAssembler = new GraphRecognizerSyntaxTreeAssembler(
	        		where, type);
	        graphRecognizerRootExpr = recognizerAssembler.getResult();
	        if (log.isDebugEnabled()) {
		        ExprPrinter printer = new ExprPrinter();
		        graphRecognizerRootExpr.accept(printer);
	            log.debug("Graph Recognizer: " + printer.toString());
	        }
	        ScanCollector scanCollector = new ScanCollector(type);
	        graphRecognizerRootExpr.accept(scanCollector);
	        partialScans = scanCollector.getPartialRowKeyScans();
	        fuzzyScans = scanCollector.getFuzzyRowKeyScans();
	        completeKeys = scanCollector.getCompleteRowKeys();
	        // in which case for a count this effects alot
	        if (!scanCollector.isQueryRequiresGraphRecognizer())
	        	graphRecognizerRootExpr = null;	        
        }        
        
        if (where == null || (partialScans.size() == 0 && fuzzyScans.size() == 0 && completeKeys.size() == 0)) {
    		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
    		scanAssembler.assemble();
    		byte[] startKey = scanAssembler.getStartKey();
    		if (startKey != null && startKey.length > 0) {
                log.warn("no root predicate present - using default graph partial "
                		+ "key scan - could result in very large results set");
        		partialScans.add(scanAssembler);
    		}
    		else
    	        log.warn("no root predicate present and no pre-defined row key fields found "
    	            + "configured for table / data-graph - using full table scan - " 
    	            + "could result in very large results set");
        }
        
        // use an ordered set so we get immediate sorting
        // and can therefore abort after max requested rows reached
        Set<PlasmaDataGraph> graphs = new HashSet<PlasmaDataGraph>();
        
        boolean hasOrdering = false;
        Comparator<PlasmaDataGraph> orderingComparator = null;
        OrderBy orderBy = query.findOrderByClause();
        if (orderBy != null) {
        	DataGraphComparatorAssembler orderingCompAssem = 
        		new DataGraphComparatorAssembler(
        			(org.plasma.query.model.OrderBy)orderBy, type);
        	orderingComparator = orderingCompAssem.getComparator();
        	hasOrdering = true;
        }
                
        // execute scans
        try {
        	long before = System.currentTimeMillis();
        	if (partialScans.size() > 0 || fuzzyScans.size() > 0 || completeKeys.size() > 0) {
	        	for (CompleteRowKey key : completeKeys) {
	        		if (canAbortScan(hasOrdering, query.getStartRange(), query.getEndRange(), graphs))
	        			break;
	        		List<PlasmaDataGraph> list = execute(key, 
		        		query.getStartRange(), query.getEndRange(),	
	        		    rootTableReader, columnFilter,
	        		    graphAssembler, 
	        		    graphRecognizerRootExpr);
	        		graphs.addAll(list);
	        	} // scan
	        	for (PartialRowKey scan : partialScans) {
	        		if (canAbortScan(hasOrdering, query.getStartRange(), query.getEndRange(), graphs))
	        			break;
	        		List<PlasmaDataGraph> list = execute(scan, 
		        		query.getStartRange(), query.getEndRange(),	
	        		    rootTableReader, columnFilter,
	        		    graphAssembler, 
	        		    graphRecognizerRootExpr);
	        		graphs.addAll(list);
	        	} // scan
	        	for (FuzzyRowKey scan : fuzzyScans) {
	        		if (canAbortScan(hasOrdering, query.getStartRange(), query.getEndRange(), graphs))
	        			break;
	        		List<PlasmaDataGraph> list = execute(scan,
	        			query.getStartRange(), query.getEndRange(),	
	        		    rootTableReader, columnFilter, 
	        		    graphAssembler, 
	        		    graphRecognizerRootExpr);
	        		graphs.addAll(list);
	        	} // scan
        	}
        	else {
    	        log.warn("no root predicate present and no pre-defined row key fields found "
        	            + "configured for table / data-graph - using full table scan - " 
        	            + "could result in very large results set");
	            FilterList rootFilter = new FilterList(
	        			FilterList.Operator.MUST_PASS_ALL);
	            rootFilter.addFilter(columnFilter);
	            Scan scan = new Scan();
	            scan.setFilter(rootFilter);        
	            List<PlasmaDataGraph> list = execute(scan, 
	      	  			rootTableReader, graphAssembler, 
	      	    		graphRecognizerRootExpr);
	    		graphs.addAll(list);
        	} 
        	
            long after = System.currentTimeMillis();
            log.info("initialized " + String.valueOf(graphs.size()) + " results ("
            	+ String.valueOf(after - before) + ")");
            PlasmaDataGraph[] array = new PlasmaDataGraph[graphs.size()]; 
            graphs.toArray(array);
            
            if (orderingComparator != null) {
            	Arrays.sort(array, orderingComparator);
            }
            
            return array;
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
    }  
    
    private boolean canAbortScan(boolean hasOrdering, Integer startRange, Integer endRange, 
    		Set<PlasmaDataGraph> result) {
    	if (!hasOrdering && startRange != null && endRange != null) {
    		if (result.size() >= endRange.intValue())
    			return true;
    	}
    	return false;
    }

    private List<PlasmaDataGraph> execute(PartialRowKey partialRowKey, 
    		Integer startRange, Integer endRange,
    		TableReader rootTableReader,
    		Filter columnFilter,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {    	
        Scan scan = createScan(partialRowKey, 
        		startRange, endRange, columnFilter);
  		return execute(scan,  
  			rootTableReader, graphAssembler, 
    		graphRecognizerRootExpr);
    }
    
    private Scan createScan(PartialRowKey partialRowKey, 
    		Integer startRange, Integer endRange,
    		Filter columnFilter) {
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        rootFilter.addFilter(columnFilter);
        Scan scan = new Scan();
        scan.setFilter(rootFilter);        
		scan.setStartRow(partialRowKey.getStartKey()); // inclusive
        scan.setStopRow(partialRowKey.getStopKey()); // exclusive
  		if (log.isDebugEnabled())
			log.debug("using partial row key scan: (" 
  		        + "start: '" + Bytes.toString(scan.getStartRow())
  		        + "' stop: '" + Bytes.toString(scan.getStopRow()) + "')");	
    	return scan;
    }
    
    private List<PlasmaDataGraph> execute(CompleteRowKey rowKey, 
    		Integer startRange, Integer endRange,
    		TableReader rootTableReader,
    		Filter columnFilter,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        rootFilter.addFilter(columnFilter);
    	Get get = new Get(rowKey.getKey());
    	get.setFilter(columnFilter);
  		if (log.isDebugEnabled())
			log.debug("using row key get: (" 
  		        + "row: '" + Bytes.toString(get.getRow()) + "'");	
  		return execute(get,  
  			rootTableReader, graphAssembler, 
    		graphRecognizerRootExpr);
   }
    
    private List<PlasmaDataGraph> execute(FuzzyRowKey fuzzyScan,
    		Integer startRange, Integer endRange,
    		TableReader rootTableReader,
    		Filter columnFilter,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {    	
        Scan scan = createScan(fuzzyScan,
        		startRange, endRange, columnFilter);
  		return execute(scan,   
  			rootTableReader, graphAssembler, 
    		graphRecognizerRootExpr);
    }
    
    private Scan createScan(FuzzyRowKey fuzzyScan,
    		Integer startRange, Integer endRange,
    		Filter columnFilter) throws IOException {
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        rootFilter.addFilter(columnFilter);
        Scan scan = new Scan();
        scan.setFilter(rootFilter); 
        if (startRange != null && endRange != null) {
        	int max = endRange.intValue() - startRange.intValue();
        	//scan.setMaxResultSize(max); // See 0.9.7
        }
        
        Filter fuzzyFilter = fuzzyScan.getFilter();
		rootFilter.addFilter(fuzzyFilter);
        if (log.isDebugEnabled() ) 
        	log.debug("using fuzzy scan: " 
                + FilterUtil.printFilterTree(fuzzyFilter));
		
		return scan;
    }
    
    private List<PlasmaDataGraph> execute(Get get, 
    		TableReader rootTableReader,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
    	
    	List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();    		
        if (log.isDebugEnabled() ) 
            log.debug("executing get...");
        
        if (log.isDebugEnabled() ) 
        	log.debug(FilterUtil.printFilterTree(get.getFilter()));
        Result resultRow = rootTableReader.getConnection().get(get);     
        if (resultRow == null || resultRow.isEmpty()) {
        	log.debug("no results from table "
                + rootTableReader.getTable().getName() + " for row '"
        		+ new String(get.getRow()) + "' - returning zero results graphs"); 
        	return result;
        }
    	if (log.isDebugEnabled()) {
	            log.debug(rootTableReader.getTable().getName() + ": " + new String(resultRow.getRow()));  
    		for (KeyValue keyValue : resultRow.list()) {
      	    	log.debug("\tkey: " 
      	    		+ new String(keyValue.getQualifier())
      	    	    + "\tvalue: " + new String(keyValue.getValue()));
      	    }
    	}
    	if (resultRow.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
    			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
    		return result; // ignore toumbstone roots
    	}
  	    graphAssembler.assemble(resultRow);            	
    	PlasmaDataGraph assembledGraph = graphAssembler.getDataGraph();
        graphAssembler.clear();
        if (graphRecognizerRootExpr != null) {
        	GraphRecognizerContext recognizerContext = new GraphRecognizerContext();
        	recognizerContext.setGraph(assembledGraph);
        	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
        		if (log.isDebugEnabled())
        			log.debug("recognizer excluded: " + Bytes.toString(
        					resultRow.getRow()));
        		if (log.isDebugEnabled())
        			log.debug(serializeGraph(assembledGraph));
        		
        		return result;
        	}
        }
    	result.add(assembledGraph);
        if (log.isDebugEnabled())
        	log.debug("assembled "+String.valueOf(result.size())
        			+" sub results");
       
        
        return result;
   }
    
    private List<PlasmaDataGraph> execute(Scan scan, 
    		TableReader rootTableReader,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
    	
    	List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();    		
        if (log.isDebugEnabled() ) 
            log.debug("executing scan...");
        
        if (log.isDebugEnabled() ) 
        	log.debug(FilterUtil.printFilterTree(scan.getFilter()));
        ResultScanner scanner = rootTableReader.getConnection().getScanner(scan);
        for (Result resultRow : scanner) {	     
        	if (log.isDebugEnabled()) {
      	        log.debug(rootTableReader.getTable().getName() + ": " + new String(resultRow.getRow()));              	  
          	    for (KeyValue keyValue : resultRow.list()) {
          	    	log.debug("\tkey: " 
          	    		+ new String(keyValue.getQualifier())
          	    	    + "\tvalue: " + new String(keyValue.getValue()));
          	    }
        	}
        	 
        	if (resultRow.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
        			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
        		continue; // ignore toumbstone roots
        	}
        	
      	    graphAssembler.assemble(resultRow);            	
        	PlasmaDataGraph assembledGraph = graphAssembler.getDataGraph();
            graphAssembler.clear();
        	
            if (graphRecognizerRootExpr != null) {
	        	GraphRecognizerContext recognizerContext = new GraphRecognizerContext();
	        	recognizerContext.setGraph(assembledGraph);
	        	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
	        		if (log.isDebugEnabled())
	        			log.debug("recognizer excluded: " + Bytes.toString(
	        					resultRow.getRow()));
	        		if (log.isDebugEnabled())
	        			log.debug(serializeGraph(assembledGraph));
	        		
	        		continue;
	        	}
            }
        	result.add(assembledGraph);
        }
        if (log.isDebugEnabled())
        	log.debug("assembled "+String.valueOf(result.size())
        			+" sub results");
        
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
    		SelectionCollector collector)
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
     * Recursively collects row key properties adding them to the 
     * given collector for the given type. Any additional properties
     * associated with types discovered during traversal of user defined
     * row key field paths are added recursively. 
     * @param collector the property collector
     * @param type the current type
     */
    private void collectRowKeyProperties(
    	SelectionCollector collector, PlasmaType type) {
	    CloudGraphConfig config = CloudGraphConfig.getInstance();    	
        DataGraphConfig graph = config.findDataGraph(type.getQualifiedName());
        if (graph != null) {	        
	        UserDefinedRowKeyFieldConfig[] fields = new UserDefinedRowKeyFieldConfig[graph.getUserDefinedRowKeyFields().size()];
	        graph.getUserDefinedRowKeyFields().toArray(fields);	        
		    for (UserDefinedRowKeyFieldConfig field : fields) { 
			    List<Type> types = collector.addProperty(graph.getRootType(), 
					field.getPropertyPath());
			    for (Type nextType : types)
			    	collectRowKeyProperties(collector, (PlasmaType)nextType);
		    }
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
    //FIXME generalize
    private HBaseGraphAssembler createGraphAssembler(
    		PlasmaType type,
    		FederatedReader graphReader,
    		Selection collector,
    		Timestamp snapshotDate)
    {
        HBaseGraphAssembler graphAssembler = null;
         
        if (collector.hasPredicates()) { 
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
    
    protected void log(Query query)
    {
    	String xml = "";
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			        new DefaultValidationEventHandler());
	        xml = binding.marshal(query);
		} catch (JAXBException e) {
			log.debug(e);
		} catch (SAXException e) {
			log.debug(e);
		}
        log.debug("query: " + xml);
    }

    protected String serializeGraph(commonj.sdo.DataGraph graph) throws IOException
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


