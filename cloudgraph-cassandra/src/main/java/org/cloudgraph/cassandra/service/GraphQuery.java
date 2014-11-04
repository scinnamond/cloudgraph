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
package org.cloudgraph.cassandra.service;

// java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.filter.FilterAssembler;
import org.cloudgraph.cassandra.filter.OrderingDeclarationAssembler;
import org.cloudgraph.query.expr.Expr;
import org.cloudgraph.query.expr.ExprPrinter;
import org.cloudgraph.recognizer.GraphRecognizerContext;
import org.cloudgraph.recognizer.GraphRecognizerSyntaxTreeAssembler;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.From;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Query;
import org.plasma.query.model.QueryConstants;
import org.plasma.query.model.Select;
import org.plasma.query.model.Variable;
import org.plasma.query.model.Where;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.query.visitor.QueryVisitor;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.MaxResultsExceededException;
import org.plasma.sdo.access.QueryDispatcher;
import org.plasma.sdo.access.provider.common.DataObjectHashKeyAssembler;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

import commonj.sdo.DataGraph;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;


public class GraphQuery extends CQLSupport 
    implements QueryDispatcher
{
    private static Log log = LogFactory.getLog(GraphQuery.class);
    /* The rownum alias used for pagination. Use upper case as we screen this column from final results and e.g. Oracle
     * returns it as upper case in the results-set metadata.
     */
    private static final String ROWNUM_ALIAS = "RNMX";  
    /* The alias for intermediate results table */
    private static final String PAGE_ALIAS = "TX";
    
    private Session con;

    @SuppressWarnings("unused")
	private GraphQuery() {}
    public GraphQuery(Session con)
    {
    	this.con = con;
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
        
        SelectionCollector collector = new SelectionCollector(
            	query.getSelectClause(), null, query.findOrderByClause(), type); 
        collector.setOnlySingularProperties(false);
        collector.setOnlyDeclaredProperties(false); // collect from superclasses
        List<List<PropertyPair>> queryResults = findResults(query, collector, type, con);
        
        GraphAssembler assembler = new GraphAssembler(type, 
            collector, snapshotDate, con);
        
        Expr graphRecognizerRootExpr = null;
        Where where = query.getWhereClause();
        if (where != null)
        {
	        GraphRecognizerSyntaxTreeAssembler recognizerAssembler = new GraphRecognizerSyntaxTreeAssembler(
	        		where, type);
	        graphRecognizerRootExpr = recognizerAssembler.getResult();
	        if (log.isDebugEnabled()) {
		        ExprPrinter printer = new ExprPrinter();
		        graphRecognizerRootExpr.accept(printer);
	            log.debug("Graph Recognizer: " + printer.toString());
	        }
        }          

        PlasmaDataGraph[] results = null;
        try {
            if (!query.getSelectClause().hasDistinctProperties())
                results = assembleResults(queryResults, 
                        requestMax, assembler, graphRecognizerRootExpr);
            else
                results = trimResults(queryResults, 
                        requestMax, assembler, query.getSelectClause(), type);
        }
        catch (Exception e) {
        	throw new CassandraServiceException(e);
        }
        
        return results;
    }

    /**
     * Returns a count of the given query. This does NOT return any results but causes
     * a "count(*)" to be issued.
     * @param query the Query Object Model (QOM) query
     * @return the query results size
     */
    public int count(Query query)
    {
        From from = query.getFromClause();
        PlasmaType type = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(from.getEntity().getNamespaceURI(), 
        		from.getEntity().getName());
        int size = this.countResults(con, query, type);
        return size;
    }

  
    /**
     * Iterate over the given results collection up to the given limit, assembling a data-object
     * graph for each result. In the simplest case, only a single root data-object
     * is assembled.
     * @param collection - the results collection
     * @param requestMax - the requested max or limit of items required in the results. If results exceed 
     * the given maximum, then they are truncated. If no maximum
     * value is given (e.g. -1) then a default max is enforced.
     * @param assembler - the value object assembler
     * @throws MaxResultsExceededException when no request maximum is given and the default 
     * max is exceeded.
     */
    private PlasmaDataGraph[] assembleResults(List<List<PropertyPair>> collection, 
            int requestMax, GraphAssembler assembler, Expr graphRecognizerRootExpr)  
    {
    	long before = System.currentTimeMillis();
        int unrecognizedResults = 0;
        GraphRecognizerContext recognizerContext = null;
        ArrayList<PlasmaDataGraph> list = new ArrayList<PlasmaDataGraph>(20);
        Iterator<List<PropertyPair>> iter = collection.iterator();
        for (int i = 1; iter.hasNext(); i++)
        {
        	List<PropertyPair> pairs = iter.next();
        	
            if (requestMax <= 0) 
            {
                if (i > QueryConstants.MAX_RESULTS) // note: this is cheezy but checking the collection size is expensive            
                    throw new MaxResultsExceededException(i, QueryConstants.MAX_RESULTS);
            }
            else if (i > requestMax)
            {
                if (log.isDebugEnabled() ){
                    log.debug("truncating results at " + String.valueOf(requestMax));
                }
                break;
            }
            assembler.assemble(pairs);
            PlasmaDataGraph graph = assembler.getDataGraph();
            boolean recognized = true;
            if (graphRecognizerRootExpr != null) {
            	if (recognizerContext == null)
            		recognizerContext = new GraphRecognizerContext();
            	recognizerContext.setGraph(graph);
        		if (log.isDebugEnabled())
        			log.debug("evaluating: " + graph);
            	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
            		if (log.isDebugEnabled())
            			log.debug("recognizer excluded: " + graph);
            		if (log.isDebugEnabled())
						try {
							log.debug(serializeGraph(graph));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}   
            		unrecognizedResults++;
            		recognized = false;
            	}
            }

            if (recognized)
                list.add(graph);
            
            assembler.clear();
        }
        PlasmaDataGraph[] results = new PlasmaDataGraph[list.size()];
        list.toArray(results);
        if (log.isDebugEnabled() ){
        	long after = System.currentTimeMillis();
            log.debug("assembled " + String.valueOf(results.length) 
            		+ " results (" + String.valueOf(after-before) + ")");
        }
        return results;
    }

    /**
     * Iterates over the given results collection pruning value-object results after assembly based
     * on any "distinct" property(ies) in the results object graph. Results must be pruned/trimmed
     * after assembly because the assembly invokes POM traversals/queries where the "distinct" propertiy(ies)
     * are realized. For each result, a hash key is assembled to determine it's uniqueness based on a search
     * of the assembled value-object graph for "distinct" properties as defined in the given Query select
     * clause.
     * @param collection - the results collection
     * @param requestMax - the requested max or limit of items required in the results. If results exceed 
     * the given maximum, then they are truncated. If no maximum
     * value is given (e.g. -1) then a default max is enforced.
     * @param assembler - the value object assembler
     * @param select the Query select clause
     * @param type the candidate Type definition
     * @throws SQLException 
     * @throws MaxResultsExceededException when no request maximum is given and the default max is exceeded.
     */
    private PlasmaDataGraph[] trimResults(List<List<PropertyPair>> collection, 
            int requestMax, GraphAssembler assembler, Select select, Type type)  
    {
        DataObjectHashKeyAssembler hashKeyAssembler =
            new DataObjectHashKeyAssembler(select, type);
        // FIXME rely on non-ordered map to retain initial ordering and get
        // rid of list collection.
        Map<String, PlasmaDataObject> distinctMap = new HashMap<String, PlasmaDataObject>(20);
        List<PlasmaDataObject> distinctList = new ArrayList<PlasmaDataObject>(20);
        Iterator<List<PropertyPair>> iter = collection.iterator();

        int i;
        for (i = 1; iter.hasNext(); i++)
        {
            if (requestMax <= 0 && i > QueryConstants.MAX_RESULTS) // note: this is cheezy but checking the collection size is expensive
                throw new MaxResultsExceededException(i, QueryConstants.MAX_RESULTS);
            List<PropertyPair> pairs = iter.next();
            assembler.assemble(pairs);
            PlasmaDataGraph dataGraph = assembler.getDataGraph();
            assembler.clear();
            String key = hashKeyAssembler.getHashKey((PlasmaDataObject)dataGraph.getRootObject());
            if (distinctMap.get(key) == null)
            {
                if (requestMax <= 0)  
                {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                    if (i > QueryConstants.MAX_RESULTS) // note: this is cheezy but checking the collection size is expensive                                                                                                                                                                                                                                                                                                                                                                                                                         
                        throw new MaxResultsExceededException(distinctList.size(), QueryConstants.MAX_RESULTS);
                }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
                else if (i > requestMax)
                {
                    if (log.isDebugEnabled() ){
                        log.debug("truncating results at " + String.valueOf(requestMax));                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                    }
                    break;          
                }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                distinctMap.put(key, (PlasmaDataObject)dataGraph.getRootObject());
                distinctList.add((PlasmaDataObject)dataGraph.getRootObject());
            }
        }
        PlasmaDataGraph[] trimmedResults = new PlasmaDataGraph[distinctList.size()];
        for (i = 0; i < trimmedResults.length; i++)
            trimmedResults[i] = (PlasmaDataGraph) distinctList.get(i).getDataGraph();
        
        if (log.isDebugEnabled() ){
            log.debug("assembled " + String.valueOf(trimmedResults.length) + " results out of " + String.valueOf(i));
        }
        return trimmedResults;
    }
 
    private int countResults(Session con, Query query, PlasmaType type)
    {
        int result = 0;
        Object[] params = new Object[0];

        StringBuilder sqlQuery = new StringBuilder();
        
        // construct a filter adding to alias map
        FilterAssembler filterAssembler = null;
        Where where = query.findWhereClause();
        if (where != null)
        {
            filterAssembler = new FilterAssembler(where, type);
            params = filterAssembler.getParams();               
            if (log.isDebugEnabled() ){
                log.debug("filter: " + filterAssembler.getFilter());
            }
        }  
        
        sqlQuery.append("SELECT COUNT(*)");

        // construct a FROM clause from alias map
        sqlQuery.append(" FROM ");        	        	
		sqlQuery.append(getQualifiedPhysicalName(type));
    	
        if (filterAssembler != null)
        {
            sqlQuery.append(" ");
            sqlQuery.append(filterAssembler.getFilter());
        }
        
    	if(query.getStartRange() != null && query.getEndRange() != null)
            log.warn("query range (start: "
            		+ query.getStartRange() + ", end: "
            		+ query.getEndRange() + ") ignored for count operation");
        
    	SimpleStatement statement = null;
    	ResultSet rs = null; 
        
        try {
               
            // set params 
            // note params are pre-converted
            // to string in filter assembly
            if (filterAssembler != null) {
	            params = filterAssembler.getParams();
	            statement = new SimpleStatement(sqlQuery.toString(), params);
            }
            else
            	statement = new SimpleStatement(sqlQuery.toString());
            
            if (log.isDebugEnabled() ){
                if (params == null || params.length == 0) {
                    log.debug("executing: "+ sqlQuery.toString());                	
                }
                else
                {
                    StringBuilder paramBuf = new StringBuilder();
                	paramBuf.append(" [");
                    for (int p = 0; p < params.length; p++)
                    {
                        if (p > 0)
                        	paramBuf.append(", ");
                        paramBuf.append(String.valueOf(params[p]));
                    }
                    paramBuf.append("]");
                    log.debug("executing: "+ sqlQuery.toString() 
                    		+ " " + paramBuf.toString());
                }
            } 
            
            rs = con.execute(statement);
            result = rs.one().getInt(1);
        }
        catch (Throwable t) {
            StringBuffer buf = this.generateErrorDetail(t, sqlQuery.toString(), 
                    filterAssembler);
            log.error(buf.toString());
            throw new DataAccessException(t);
        }
        finally {
        }
        return result;
    }
    
    private List<List<PropertyPair>> findResults(Query query, SelectionCollector collector,
    		PlasmaType type, Session con)
    {
        Object[] params = new Object[0];
        CQLDataConverter converter = CQLDataConverter.INSTANCE;

        // construct a filter adding to alias map
        FilterAssembler filterAssembler = null;
        
        Where where = query.findWhereClause();
        
        if (where != null)
        {
            filterAssembler = new FilterAssembler(where, type);
        }  
         
        OrderingDeclarationAssembler orderingDeclAssembler = null;
        OrderBy orderby = query.findOrderByClause();
        if (orderby != null) 
            orderingDeclAssembler = new OrderingDeclarationAssembler(orderby, type);
                
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT ");  
        
        int i = 0;
        Set<Property> props = collector.getProperties(type);
        for (Property prop : props) {
			if (prop.isMany() && !prop.getType().isDataType())
				continue;			
			if (i > 0)
        		sqlQuery.append(", ");
        	sqlQuery.append(((PlasmaProperty)prop).getPhysicalName());
        	i++;
		}        
        
        // construct a FROM clause  
        sqlQuery.append(" FROM ");        	        	
		sqlQuery.append(getQualifiedPhysicalName(type));
    	
    	// append WHERE filter
    	if (filterAssembler != null) {
            sqlQuery.append(" ");
            sqlQuery.append(filterAssembler.getFilter());
    	}
    	
        if (orderingDeclAssembler != null) {
            sqlQuery.append(" ");
            sqlQuery.append(orderingDeclAssembler.getOrderingDeclaration());
        }
        
		if (query.getStartRange() != null && query.getEndRange() != null) {
			long offset = query.getStartRange() - 1; // inclusive
			if (offset < 0)
				offset = 0;
			long rowcount = query.getEndRange() - offset;
			sqlQuery.append(" LIMIT "); // e.g. LIMIT offset,numrows
			sqlQuery.append(String.valueOf(offset));
			sqlQuery.append(",");
			sqlQuery.append(String.valueOf(rowcount));
		}
               
        List<List<PropertyPair>> rows = new ArrayList<List<PropertyPair>>();
        SimpleStatement statement = null;
        ResultSet rs = null; 
        try {
            //statement.setFetchSize(32);
            //log.debug("setting fetch size 32");
            
            // set params 
            // FIXME: params are pre-converted
            // to string in filter assembly
            int paramCount = 0;
            if (filterAssembler != null) {
	            params = filterAssembler.getParams();
	            statement = new SimpleStatement(sqlQuery.toString(), params);
            }
            else
            	statement = new SimpleStatement(sqlQuery.toString());
            
            // execute
            long before = System.currentTimeMillis();
            rs = con.execute(statement);           
            long after = System.currentTimeMillis();
            
            if (log.isDebugEnabled() ){
                if (params == null || params.length == 0) {
                    log.debug("executed: "+ sqlQuery.toString() + " (" + String.valueOf(after-before) + ")");                	
                }
                else
                {
                    StringBuilder paramBuf = new StringBuilder();
                	paramBuf.append(" [");
                    for (int p = 0; p < params.length; p++)
                    {
                        if (p > 0)
                        	paramBuf.append(", ");
                        paramBuf.append(String.valueOf(params[p]));
                    }
                    paramBuf.append("]");
                    log.debug("executed: "+ sqlQuery.toString() 
                    		+ " " + paramBuf.toString() + " (" + String.valueOf(after-before) + ")");
                }
            } 
           
            // read results
            before = System.currentTimeMillis();
            int numresults = 0; 
            ColumnDefinitions rsMeta = rs.getColumnDefinitions();	           
            int numcols = rsMeta.size();
            Iterator<Row> iter = rs.iterator();
            List<PropertyPair> row = null;
            PropertyPair pair = null;
            while(iter.hasNext()) {
            	Row dataRow = iter.next();
            	row = new ArrayList<PropertyPair>();
            	rows.add(row);                
            	int column = 0;
            	for(ColumnDefinitions.Definition def : rs.getColumnDefinitions()) {
            		String columnName = def.getName();
            		DataType columnType = def.getType();
            		
            		PlasmaProperty prop = (PlasmaProperty)type.getProperty(columnName);
            		PlasmaProperty valueProp = prop;
            		while (!valueProp.getType().isDataType()) {
            			valueProp = this.getOppositePriKeyProperty(valueProp);
            		}
            		
            		Object value = converter.fromCQLDataType(dataRow, 
            				column, columnType, valueProp);
            		if (value != null) {
            		    pair = new PropertyPair(prop, value);
            		    pair.setColumn(i);
            		    if (!valueProp.equals(prop))
            		    	pair.setValueProp(valueProp);
            		    row.add(pair);
            		}
            		column++;
                }
            	numresults++;
            }
            after = System.currentTimeMillis();
            if (log.isDebugEnabled()) 
                log.debug("read "+ numresults + " results (" + String.valueOf(after-before) + ")");                	
        }
        catch (Throwable t) {
            StringBuffer buf = this.generateErrorDetail(t,   
            		sqlQuery.toString(), 
                    filterAssembler);
            log.error(buf.toString());
            throw new DataAccessException(t);
        }
        finally {
        }
        return rows;
    }
    
    private StringBuffer generateErrorDetail(Throwable t, String queryString, 
            FilterAssembler filterAssembler)
    {
        StringBuffer buf = new StringBuffer(2048);
        buf.append("QUERY FAILED: ");
        buf.append(t.getMessage());
        buf.append(" \n");
        if (queryString != null) {
            buf.append("queryString: ");
            buf.append(queryString);
            buf.append(" \n");
        }
        if (filterAssembler != null) {
            if (filterAssembler.hasImportDeclarations())
            {
                buf.append("import decl: ");
                buf.append(filterAssembler.getImportDeclarations());
                buf.append(" \n");
            }
            Object[] params = filterAssembler.getParams();
            if (params != null)
            {
                buf.append("parameters: [");
                for (int i = 0; i < params.length; i++)
                {
                    if (i > 0)
                        buf.append(", ");
                    buf.append(String.valueOf(params[i]));
                }
                buf.append("]");
                buf.append(" \n");
            }
            if (filterAssembler.hasParameterDeclarations())
            {
                buf.append("param decl: ");
                buf.append(filterAssembler.getParameterDeclarations());
                buf.append(" \n");
            }
            if (filterAssembler.hasVariableDeclarations())
            {
                buf.append("variable decl: ");
                buf.append(filterAssembler.getVariableDeclarations());
                buf.append(" \n");
            }
        }
        return buf;
    }    
    
    public List<Variable> getVariables(Where where)
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

	public void close() {
		// TODO Auto-generated method stub
		
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


