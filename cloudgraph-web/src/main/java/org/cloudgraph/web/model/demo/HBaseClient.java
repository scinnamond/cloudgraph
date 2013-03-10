package org.cloudgraph.web.model.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.connect.HBaseConnectionManager;
import org.cloudgraph.state.GraphState;

public class HBaseClient {

	private static final Log log = LogFactory.getLog(HBaseClient.class);
	
	public Map<String, Map<String, String>> get(String modelRootType, 
			String modelRootURI) throws IOException {
		
		QName name = new QName(modelRootURI, modelRootType);
		TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(name);
		return get(tableConfig);
	}
	
	public Map<String, Map<String, String>> get(String tableName) throws IOException {
		TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(tableName);
		return get(tableConfig);
	}	
	
	public Map<String, Map<String, String>> get(TableConfig tableConfig) throws IOException {
		
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		
		HTableInterface table = HBaseConnectionManager.instance().getConnection(
				tableConfig.getTable().getName());
        Scan scan = new Scan();
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        scan.setFilter(rootFilter);
        QualifierFilter rootUUIDFilter = new QualifierFilter(
            	CompareFilter.CompareOp.NOT_EQUAL,
            	new SubstringComparator(GraphState.ROOT_UUID_COLUMN_NAME));   
        rootFilter.addFilter(rootUUIDFilter);
        QualifierFilter stateFilter = new QualifierFilter(
            	CompareFilter.CompareOp.NOT_EQUAL,
            	new SubstringComparator(GraphState.STATE_COLUMN_NAME));   
        rootFilter.addFilter(stateFilter);
        
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("executing scan...");
        ResultScanner scanner = table.getScanner(scan);
    	int count = 0;
        for (Result resultRow : scanner) {
        	Map<String, String> row = new HashMap<String, String>();
        	result.put(Bytes.toString(resultRow.getRow()), row);
      	    for (KeyValue keyValue : resultRow.list()) {
      	    	row.put(Bytes.toString(keyValue.getQualifier()), 
      	    			Bytes.toString(keyValue.getValue()));
      	    }
            count++;
        }      
        long after = System.currentTimeMillis();
        log.info("assembled " + String.valueOf(count) + " results ("
        	+ String.valueOf(after - before) + ")");
        
        return result;
	}
}
