package org.cloudgraph.hbase.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.key.GraphColumnKeyFactory;
import org.cloudgraph.common.service.GraphState;
import org.cloudgraph.hbase.key.CompositeColumnKeyFactory;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.query.model.Select;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.xml.sax.SAXException;

import commonj.sdo.Type;

/**
 * Creates an HBase column filter set based on the given criteria
 * which leverages the HBase <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/MultipleColumnPrefixFilter.html">MultipleColumnPrefixFilter</a>
 * to return only selected columns. The advantage of this strategy is that
 * a complete graph of any complexity may be returned in a single round trip. 
 *  
 * @see GraphColumnKeyFactory
 * @see InitialFetchColumnFilterAssembler
 */
public class BulkFetchColumnFilterAssembler extends FilterListAssembler 
    implements HBaseFilterAssembler
{
    private static Log log = LogFactory.getLog(BulkFetchColumnFilterAssembler.class);

	private GraphColumnKeyFactory columnKeyFac;
	private Map<String, byte[]> prefixMap = new HashMap<String, byte[]>();
    private PropertySelectionCollector collector;
	
	public BulkFetchColumnFilterAssembler( 
			PropertySelectionCollector collector,
			PlasmaType rootType) {
		
		super(rootType);
		this.collector = collector;
        this.columnKeyFac = new CompositeColumnKeyFactory(rootType);
		
    	this.rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ONE);

    	// add default filters for graph state info needed for all queries
        QualifierFilter rootUUIDFilter = new QualifierFilter(
        	CompareFilter.CompareOp.EQUAL,
        	new SubstringComparator(CloudGraphConstants.ROOT_UUID_COLUMN_NAME));   
        this.rootFilter.addFilter(rootUUIDFilter);
        QualifierFilter stateFilter = new QualifierFilter(
        	CompareFilter.CompareOp.EQUAL,
        	new SubstringComparator(GraphState.STATE_MAP_COLUMN_NAME));   
        this.rootFilter.addFilter(stateFilter);
    	
    	collect();
    	
    	byte[][] prefixes = new byte[this.prefixMap.size()][];
    	int i = 0;
    	for (byte[] prefix : this.prefixMap.values()) {
    		prefixes[i] = prefix; 
    		i++;
    	}
    	
        MultipleColumnPrefixFilter multiPrefixfilter = 
        	new MultipleColumnPrefixFilter(prefixes);
        
        this.rootFilter.addFilter(multiPrefixfilter);    	
	}
	
	public void clear() {
		super.clear();
		this.prefixMap.clear();
	}
	
	/**
	 * Collects and maps column prefixes used to create HBase filter(s)
	 * for column selection. 
	 * @param select the select clause
	 */
	private void collect() {
        Map<Type, List<String>> selectMap = this.collector.getResult();
        Iterator<Type> typeIter = selectMap.keySet().iterator();
        while (typeIter.hasNext()) {
        	PlasmaType type = (PlasmaType)typeIter.next();
        	List<String> names = selectMap.get(type);
            for (String name : names) {
    			PlasmaProperty prop = (PlasmaProperty)type.getProperty(name);
                byte[] colKey = this.columnKeyFac.createColumnKey(
                    type, prop);
                String colKeyStr = Bytes.toString(colKey);
                this.prefixMap.put(colKeyStr, colKey);
    		}        
        }
	}
		
    protected void log(Select root)
    {
    	String xml = "";
        PlasmaQueryDataBinding binding;
		try {
			binding = new PlasmaQueryDataBinding(
			        new DefaultValidationEventHandler());
	        xml = binding.marshal(root);
		} catch (JAXBException e) {
			log.debug(e);
		} catch (SAXException e) {
			log.debug(e);
		}
        log.debug("query: " + xml);
    }}
