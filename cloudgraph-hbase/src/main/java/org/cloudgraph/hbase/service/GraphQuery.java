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
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.KeyFieldConfig;
import org.cloudgraph.config.TableConfig;
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
import org.cloudgraph.hbase.graph.SimpleGraphAssembler;
import org.cloudgraph.hbase.io.FederatedGraphReader;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.GraphTableReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.scan.FuzzyRowKeyScan;
import org.cloudgraph.hbase.scan.FuzzyRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.PartialRowKeyScan;
import org.cloudgraph.hbase.scan.PartialRowKeyScanAssembler;
import org.cloudgraph.hbase.scan.RowKeyScanAssembler;
import org.cloudgraph.hbase.scan.ScanCollector;
import org.cloudgraph.hbase.scan.ScanContext;
import org.cloudgraph.hbase.scan.ScanRecognizerSyntaxTreeAssembler;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.state.GraphState;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.model.From;
import org.plasma.query.model.Query;
import org.plasma.query.model.Variable;
import org.plasma.query.model.Where;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.QueryVisitor;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.QueryDispatcher;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import commonj.sdo.Type;

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
        
        if (log.isDebugEnabled())
        	log(query);
        Where where = query.findWhereClause();
        PropertySelectionCollector selectionCollector = new PropertySelectionCollector(
            query.getSelectClause(), type);
        selectionCollector.setOnlyDeclaredProperties(false);
        selectionCollector.getResult(); // trigger the traversal
        if (where != null)
        	selectionCollector.collect(where);
        for (Type t : selectionCollector.getTypes()) 
        	collectRowKeyProperties(selectionCollector, (PlasmaType)t);        
        //if (log.isDebugEnabled())
        //	log.debug(selectionCollector.dumpInheritedProperties());
        
        FederatedGraphReader graphReader = new FederatedGraphReader(
        		type, selectionCollector.getTypes(),
        		this.context.getMarshallingContext());
        TableReader rootTableReader = graphReader.getRootTableReader();

        // Create and add a column filter for the initial
        // column set based on existence of path predicates
        // in the Select. 
        //FilterList rootFilter = new FilterList(
    	//		FilterList.Operator.MUST_PASS_ALL);
        HBaseFilterAssembler columnFilterAssembler = 
        	createRootColumnFilterAssembler(type,
        	selectionCollector);
        Filter columnFilter = columnFilterAssembler.getFilter();
        //rootFilter.addFilter(columnFilterAssembler.getFilter());

        // Create a graph assembler based on existence
        // of selection path predicates, need for federation, etc...
        HBaseGraphAssembler graphAssembler = createGraphAssembler(
        	type, graphReader, selectionCollector, snapshotDate);
        
        List<PartialRowKeyScan> partialScans = new ArrayList<PartialRowKeyScan>();
        List<FuzzyRowKeyScan> fuzzyScans = new ArrayList<FuzzyRowKeyScan>();
        Expr graphRecognizerRootExpr = null;
        if (where != null) {
	        GraphRecognizerSyntaxTreeAssembler recognizerAssembler = new GraphRecognizerSyntaxTreeAssembler(
	        		where, type);
	        graphRecognizerRootExpr = recognizerAssembler.getResult();
	        ExprPrinter printer = new ExprPrinter();
	        graphRecognizerRootExpr.accept(printer);
	        if (log.isDebugEnabled())
	            log.debug("Graph Recognizer: " + printer.toString());
	        ScanCollector scanCollector = new ScanCollector(type);
	        graphRecognizerRootExpr.accept(scanCollector);
	        partialScans = scanCollector.getPartialRowKeyScans();
	        fuzzyScans = scanCollector.getFuzzyRowKeyScans();
        }        
        
        if (where == null || (partialScans.size() == 0 && fuzzyScans.size() == 0)) {
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
                
        // execute scans
        List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();
        try {
        	long before = System.currentTimeMillis();
        	int count = 0;
        	if (partialScans.size() > 0 || fuzzyScans.size() > 0) {
	        	for (PartialRowKeyScan scan : partialScans) {
	        		List<PlasmaDataGraph> list = execute(scan, 
	        		    rootTableReader, columnFilter,
	        		    graphAssembler, 
	        		    graphRecognizerRootExpr);
	        		result.addAll(list);
		            count += list.size();
	        	} // scan
	        	for (FuzzyRowKeyScan scan : fuzzyScans) {
	        		List<PlasmaDataGraph> list = execute(scan, 
	        		    rootTableReader, columnFilter, 
	        		    graphAssembler, 
	        		    graphRecognizerRootExpr);
	        		result.addAll(list);
		            count += list.size();
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
	    		result.addAll(list);
	            count += list.size();
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
    

    private List<PlasmaDataGraph> execute(PartialRowKeyScan partialScan, 
    		TableReader rootTableReader,
    		Filter columnFilter,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
    	
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        rootFilter.addFilter(columnFilter);
        Scan scan = new Scan();
        scan.setFilter(rootFilter);        
		scan.setStartRow(partialScan.getStartKey()); // inclusive
        scan.setStopRow(partialScan.getStopKey()); // exclusive
  		if (log.isDebugEnabled())
			log.debug("using partial row key scan: (" 
  		        + "start: '" + Bytes.toString(scan.getStartRow())
  		        + "' stop: '" + Bytes.toString(scan.getStopRow()) + "')");	
  		return execute(scan, 
  			rootTableReader, graphAssembler, 
    		graphRecognizerRootExpr);
    }
    
    private List<PlasmaDataGraph> execute(FuzzyRowKeyScan fuzzyScan, 
    		TableReader rootTableReader,
    		Filter columnFilter,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
    	
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        rootFilter.addFilter(columnFilter);
        Scan scan = new Scan();
        scan.setFilter(rootFilter); 
        Filter fuzzyFilter = fuzzyScan.getFilter();
		rootFilter.addFilter(fuzzyFilter);
        if (log.isDebugEnabled() ) 
        	log.debug("using fuzzy scan: " 
                + FilterUtil.printFilterTree(fuzzyFilter));
  		return execute(scan,   
  			rootTableReader, graphAssembler, 
    		graphRecognizerRootExpr);
    }
    
    private List<PlasmaDataGraph> execute(Scan scan, 
    		TableReader rootTableReader,
    		HBaseGraphAssembler graphAssembler, 
    		Expr graphRecognizerRootExpr) throws IOException {
    	
    	List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();    		
        if (log.isDebugEnabled() ) 
            log.debug("executing scan...");
        
        //if (log.isDebugEnabled() ) 
        //	log.debug(FilterUtil.printFilterTree(rootFilter));
        ResultScanner scanner = rootTableReader.getConnection().getScanner(scan);
        for (Result resultRow : scanner) {	     
        	if (log.isTraceEnabled()) {
      	        log.trace(rootTableReader.getTable().getName() + ": " + new String(resultRow.getRow()));              	  
          	    //for (KeyValue keyValue : resultRow.list()) {
          	    //	log.trace("\tkey: " 
          	    //		+ new String(keyValue.getQualifier())
          	    //	    + "\tvalue: " + new String(keyValue.getValue()));
          	    //}
        	}
        	 
        	if (resultRow.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
        			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
        		continue; // ignore toumbstone roots
        	}
        	
      	    graphAssembler.assemble(resultRow);            	
        	PlasmaDataGraph assembledGraph = graphAssembler.getDataGraph();
            graphAssembler.clear();
        	
        	// FIXME: need to determine cases where a graph recognizer is
        	// NOT needed based on completeness of partial or fuzzy scan(s)
            if (graphRecognizerRootExpr != null) {
	        	GraphRecognizerContext recognizerContext = new GraphRecognizerContext();
	        	recognizerContext.setGraph(assembledGraph);
	        	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
	        		if (log.isDebugEnabled())
	        			log.debug("recognizer excluded: " + Bytes.toString(
	        					resultRow.getRow()));
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
     * Recursively collects row key properties adding them to the 
     * given collector for the given type. Any additional properties
     * associated with types discovered during traversal of user defined
     * row key field paths are added recursively. 
     * @param collector the property collector
     * @param type the current type
     */
    private void collectRowKeyProperties(
    	PropertySelectionCollector collector, PlasmaType type) {
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
        		scanAssembler.assemble(scanContext.getPartialKeyScanLiterals());
                scan.setStartRow(scanAssembler.getStartKey()); // inclusive
                scan.setStopRow(scanAssembler.getStopKey()); // exclusive
          		if (log.isDebugEnabled())
        			log.debug("using partial row key scan: (" 
          		        + "start: '" + Bytes.toString(scan.getStartRow())
          		        + "' stop: '" + Bytes.toString(scan.getStopRow()) + "')");
        	}
        	else if (scanContext.canUseFuzzyKeyScan()) {
        		FuzzyRowKeyScanAssembler scanAssembler = new FuzzyRowKeyScanAssembler(type);
        		scanAssembler.assemble(scanContext.getFuzzyKeyScanLiterals());
        		rootFilter.addFilter(scanAssembler.getFilter());
        		if (log.isDebugEnabled())
        			log.debug("using fuzzy row key scan");
        	}
        	else {
	            PredicateRowFilterAssembler rowFilterAssembler = 
	            	new PredicateRowFilterAssembler(type);
	            rowFilterAssembler.assemble(where, type);
	            rootFilter.addFilter(rowFilterAssembler.getFilter());
        		log.warn("insufficient query parameters present for partial or fuzzy row "
        				+ "key scan - using row filter hierarchy - could result in very large results set");        		 
        	}
        }
        else {
    		PartialRowKeyScanAssembler scanAssembler = new PartialRowKeyScanAssembler(type);
    		scanAssembler.assemble();
    		byte[] startKey = scanAssembler.getStartKey();
    		if (startKey != null && startKey.length > 0) {
                scan.setStartRow(startKey); // inclusive
                scan.setStopRow(scanAssembler.getStopKey()); // exclusive
                log.warn("no root predicate present - using default graph partial key scan: (" 
      		            + "start: " + Bytes.toString(scan.getStartRow())
      		            + " stop: " + Bytes.toString(scan.getStopRow()) 
      		            + ") - could result in very large results set");
    		}
    		else
    	        log.warn("no root predicate present and no pre-defined row key fields found "
    	            + "configured for table / data-graph - using full table scan - " 
    	            + "could result in very large results set");
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

}


