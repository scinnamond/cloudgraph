package org.cloudgraph.hbase.service;

// java imports
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.CloudGraphServiceException;
import org.cloudgraph.common.service.DispatcherSupport;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.hbase.filter.EagerFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.DefaultHBaseRowFilterAssembler;
import org.cloudgraph.hbase.key.HBaseCompositeRowKeyFactory;
import org.plasma.query.collector.PropertyCollector;
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

import commonj.sdo.Type;

/**
 * Assembles and returns one or more {@link DataGraph data graphs} 
 * from HBase given a PlasmaQuery\u2122 based XPath or DSL query.
 * First an HBase row filter is assembled using 
 * {@link DefaultHBaseRowFilterAssembler} which uses query 
 * literal values and logic found in the 'where'
 * clause or predicate(s).     
 * <p>
 * Any "slice" of a graph or set of sub-graphs can be selected using the
 * PlasmaQuery\u2122 API by specifying paths through the graph. Paths may
 * include any number of predicates along the path. Based on this selection
 * criteria an {@link EagerFetchColumnFilterAssembler} is used to 
 * precisely restrict the HBase columns returned for each result row.      
 * </p>
 * <p>
 * Then for each resulting HBase row, a data graph 
 * {@CloudgraphAssembler assembler} is used to reconstruct and return the original
 * graph structure from the resulting HBase row.
 * </p>
 * <p>
 * The PlasmaQuery\u2122 API provides a flexible mechanism to fully describe 
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
 * @see DefaultHBaseRowFilterAssembler
 * @see HBaseGraphAssembler
 * @see EagerFetchColumnFilterAssembler
 * 
 */
public class HBaseGraphQuery extends DispatcherSupport 
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
        String htable = CloudGraphConfig.getInstance().getHTableName(
        		type.getQualifiedName());
    	HTableInterface con = HBaseConnectionManager.instance().getConnection(htable);
        
        PlasmaDataGraph[] results = new PlasmaDataGraph[0];
        try {
            List<PlasmaDataGraph> queryResults = findResults(con, query, type, snapshotDate);
            
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
        String htable = CloudGraphConfig.getInstance().getHTableName(
        		type.getQualifiedName());
    	HTableInterface con = HBaseConnectionManager.instance().getConnection(htable);
        int size = 0;
		try {
			size = this.countResults(con, query, type);
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
    
    private int countResults(HTableInterface con, Query query, PlasmaType type)
    {
        int count = 0;
        Object[] params = new Object[0];

        Scan scan = new Scan();
        
    	scan.addColumn(Bytes.toBytes(CloudGraphConstants.DATA_TABLE_FAMILY_1), 
    		Bytes.toBytes(CloudGraphConstants.ROOT_UUID_COLUMN_NAME));
        
        try {
            Where where = query.findWhereClause();
            if (where != null)
            {
                DefaultHBaseRowFilterAssembler rowFilterAssembler = 
                	new DefaultHBaseRowFilterAssembler(where, type);

                scan.setFilter(rowFilterAssembler.getFilter());
            }
            else {
                HBaseCompositeRowKeyFactory rowKeyGen = new HBaseCompositeRowKeyFactory(type);
                
                String rowKey = rowKeyGen.createRowKey(type);
        		if (log.isDebugEnabled())
        			log.debug("row-id: " + rowKey);

                scan.setStartRow(Bytes.toBytes(rowKey));
                //shorter string lexicographically precedes the longer string, so
                //add max unicode char
                String endRowKey = rowKey + Character.MAX_VALUE;
                scan.setStopRow(Bytes.toBytes(endRowKey));
            }
            if (query.getStartRange() != null && query.getEndRange() != null)
                log.warn("query range (start: "
                		+ query.getStartRange() + ", end: "
                		+ query.getEndRange() + ") ignored for count operation");
                
            if (log.isDebugEnabled() ){
                log.debug("executing count...");
            }            
        	ResultScanner scanner = con.getScanner(scan);
            for (Result result : scanner) {
            	count++;
            	/*
            	if (log.isDebugEnabled()) {
              	    log.debug("row: " + new String(result.getRow()));
              	    for (KeyValue keyValue : result.list()) {
              	    	log.debug("\tkey: " 
              	    		+ new String(keyValue.getQualifier())
              	    	    + "\tvalue: " + new String(keyValue.getValue()));
              	    }
            	}
            	*/
            }       
		} catch (IOException e) {
			throw new CloudGraphServiceException(e);
		}            	 
        finally {
        }
        return count;
    }
    
    private List<PlasmaDataGraph> findResults(HTableInterface con, Query query, PlasmaType type, Timestamp snapshotDate)
    {
        Object[] params = new Object[0];
        List<PlasmaDataGraph> result = new ArrayList<PlasmaDataGraph>();

        Scan scan = new Scan();
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
        
        // add UUID map col always
    	//scan.addColumn(Bytes.toBytes(CloudgraphConstants.DATA_TABLE_FAMILY_1), 
    	//	Bytes.toBytes(CloudgraphState.STATE_MAP_COLUMN_NAME));
    	//scan.addColumn(Bytes.toBytes(CloudgraphConstants.DATA_TABLE_FAMILY_1), 
        //		Bytes.toBytes(CloudgraphConstants.ROOT_UUID_COLUMN_NAME));
        
        // get all columns for family
        scan.addFamily(Bytes.toBytes(CloudGraphConstants.DATA_TABLE_FAMILY_1));
        
        EagerFetchColumnFilterAssembler columnFilterAssembler = 
    		new EagerFetchColumnFilterAssembler(query.getSelectClause(), 
    				type);
        rootFilter.addFilter(columnFilterAssembler.getFilter());

        Where where = query.findWhereClause();
        if (where != null)
        {
            DefaultHBaseRowFilterAssembler rowFilterAssembler = 
            	new DefaultHBaseRowFilterAssembler(where, type);
     
            rootFilter.addFilter(rowFilterAssembler.getFilter());
        } 
        else {
            HBaseCompositeRowKeyFactory rowKeyGen = new HBaseCompositeRowKeyFactory(type);
            
            byte[] rowKey = rowKeyGen.createRowKeyBytes(type);
    		if (log.isDebugEnabled())
    			log.debug("row-id: " + rowKey);

            scan.setStartRow(rowKey);
            //shorter string lexicographically precedes the longer string, so
            //add max unicode char
            byte[] maxCharBytes = Bytes.toBytes(Character.MAX_VALUE);
            byte[] stopRowKey = new byte[rowKey.length + maxCharBytes.length];
            System.arraycopy(rowKey, 0, stopRowKey, 0, rowKey.length);
            System.arraycopy(maxCharBytes, 0, stopRowKey, rowKey.length, maxCharBytes.length);
            scan.setStopRow(stopRowKey);
            
            //scan.setTimeRange(minStamp, maxStamp)
        }
        
        try {
            PropertyCollector collector = new PropertyCollector(
                	query.getSelectClause(), type);
            Map<Type, List<String>> selectMap = collector.getResult();
            HBaseGraphAssembler assembler = new HBaseGraphAssembler(type, 
                	selectMap, snapshotDate);
            if (log.isDebugEnabled() ) 
                log.debug("executing scan...");
            
            if (log.isDebugEnabled() ) 
            	log.debug(printFilterTree(rootFilter));
            ResultScanner scanner = con.getScanner(scan);
        	int count = 0;
            for (Result resultRow : scanner) {
            	if (log.isDebugEnabled()) {
          	        log.debug("row: " + new String(resultRow.getRow()));              	  
              	    for (KeyValue keyValue : resultRow.list()) {
              	    	log.debug("\tkey: " 
              	    		+ new String(keyValue.getQualifier())
              	    	    + "\tvalue: " + new String(keyValue.getValue()));
              	    }
            	}
          	    assembler.assemble(resultRow);
                result.add(assembler.getDataGraph());
                assembler.clear();
                count++;
            }       
            log.info("assembled " + String.valueOf(count) + " results");
        }
        catch (IOException e) {
            throw new CloudGraphServiceException(e);
        }
        catch (Throwable t) {
            throw new CloudGraphServiceException(t);
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
    
    
    private String printFilterTree(Filter filter) throws IOException {
    	StringBuilder buf = new StringBuilder();
    	printFilterTree(filter, buf, 0);
        return buf.toString();
    }
    
    private void printFilterTree(Filter filter, StringBuilder buf, int level) throws IOException {
        if (filter instanceof FilterList) {
        	buf.append("\n");
        	for (int i = 0; i < level; i++)
        		buf.append("\t");
        	FilterList list = (FilterList)filter;
        	buf.append("[LIST:");
        	buf.append(list.getOperator().name());
        	for (Filter child : list.getFilters()) {
        		printFilterTree(child, buf, level + 1);
        	}
        	buf.append("\n");
        	for (int i = 0; i < level; i++)
        		buf.append("\t");
        	buf.append("]");
        }
        else {
        	buf.append("\n");
        	for (int i = 0; i < level; i++)
        		buf.append("\t");
        	if (filter instanceof CompareFilter) {
        		CompareFilter compFilter = (CompareFilter)filter;
        	    buf.append(compFilter.getClass().getSimpleName() + ": ");
        	    buf.append(compFilter.getOperator().name());
        	    buf.append(" ");
        	    buf.append(new String(compFilter.getComparator().getValue()));
        	}
        	else {
        	    buf.append(filter.getClass().getSimpleName()+": ");
        	}
        }
    }

}


