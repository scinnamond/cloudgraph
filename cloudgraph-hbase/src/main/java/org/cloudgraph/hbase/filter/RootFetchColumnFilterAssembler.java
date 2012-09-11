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


public class RootFetchColumnFilterAssembler extends FilterListAssembler 
    implements HBaseFilterAssembler
{
    private static Log log = LogFactory.getLog(RootFetchColumnFilterAssembler.class);

	private GraphColumnKeyFactory columnKeyFac;
	private Map<String, byte[]> prefixMap = new HashMap<String, byte[]>();
    private PropertySelectionCollector collector;

	@SuppressWarnings("unused")
	private RootFetchColumnFilterAssembler() {}
	
	public RootFetchColumnFilterAssembler(Select select,
			PropertySelectionCollector collector,
			PlasmaType rootType) {
		this.collector = collector;
		this.rootType = rootType;
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
    	
    	if (log.isDebugEnabled())
    		this.log(select);
    	
    	collect(select);
    	
    	byte[][] prefixes = new byte[prefixMap.size()][];
    	int i = 0;
    	for (byte[] prefix : prefixMap.values()) {
    		prefixes[i] = prefix; 
    		i++;
    	}
    	
        MultipleColumnPrefixFilter multiPrefixfilter = 
        	new MultipleColumnPrefixFilter(prefixes);
        
        this.rootFilter.addFilter(multiPrefixfilter);    	
	}
	
	/**
	 * Collects and maps column prefixes used to create HBase filter(s)
	 * for column selection. 
	 * @param select the select clause
	 */
	private void collect(Select select) {
        Map<Type, List<String>> selectMap = this.collector.getResult();
        Iterator<Type> typeIter = selectMap.keySet().iterator();
        while (typeIter.hasNext()) {
        	PlasmaType type = (PlasmaType)typeIter.next();
        	if (!(rootType.getURI().equals(type.getURI()) &&
        		rootType.getName().equals(type.getName())))
        		continue;
        	List<String> names = selectMap.get(type);
            for (String name : names) {
    			PlasmaProperty prop = (PlasmaProperty)type.getProperty(name);
    	       	if (log.isDebugEnabled())
    	    		log.debug("collected " + type.getURI() + "#"
    	    				+ type.getName() + "." + prop.getName());        
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
