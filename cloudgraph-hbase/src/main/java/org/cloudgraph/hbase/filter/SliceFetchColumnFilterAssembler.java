package org.cloudgraph.hbase.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.filter.CloudGraphFilterException;
import org.cloudgraph.common.key.CloudGraphColumnKeyFactory;
import org.cloudgraph.common.service.CloudGraphState;
import org.cloudgraph.hbase.key.HBaseCompositeColumnKeyFactory;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Literal;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.Select;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.xml.sax.SAXException;


public class SliceFetchColumnFilterAssembler extends FilterHierarchyAssembler 
    implements HBaseColumnFilterAssembler
{
    private static Log log = LogFactory.getLog(SliceFetchColumnFilterAssembler.class);

	private CloudGraphColumnKeyFactory columnKeyFac;
	private Map<String, byte[]> prefixMap = new HashMap<String, byte[]>();

	@SuppressWarnings("unused")
	private SliceFetchColumnFilterAssembler() {}
	
	public SliceFetchColumnFilterAssembler(Select select,
			PlasmaType rootType) {
		this.rootType = rootType;
        this.columnKeyFac = new HBaseCompositeColumnKeyFactory(rootType);
		
    	this.rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ONE);
    	this.filterStack.push(this.rootFilter);

    	// add default filters for graph state info needed for all queries
        QualifierFilter rootUUIDFilter = new QualifierFilter(
        	CompareFilter.CompareOp.EQUAL,
        	new SubstringComparator(CloudGraphConstants.ROOT_UUID_COLUMN_NAME));   
        this.rootFilter.addFilter(rootUUIDFilter);
        QualifierFilter stateFilter = new QualifierFilter(
        	CompareFilter.CompareOp.EQUAL,
        	new SubstringComparator(CloudGraphState.STATE_MAP_COLUMN_NAME));   
        this.rootFilter.addFilter(stateFilter);
    	
    	if (log.isDebugEnabled())
    		this.log(select);
    	
    	traverse(select);
    	
    	byte[][] prefixes = new byte[prefixMap.size()][];
    	int i = 0;
    	for (byte[] prefix : prefixMap.values()) {
    		prefixes[i] = prefix; 
    		i++;
    	}
    	
        MultipleColumnPrefixFilter multiPrefixfilter = 
        	new MultipleColumnPrefixFilter(prefixes);
        
        this.filterStack.peek().addFilter(multiPrefixfilter);    	
	}
	
	
	{
		FilterList list = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
		
        QualifierFilter qualFilter = new QualifierFilter(
            	CompareFilter.CompareOp.EQUAL,
            	new BinaryPrefixComparator("COL_PREFIX".getBytes())); 
        list.addFilter(qualFilter);
        ValueFilter valueFilter = new ValueFilter(
            	CompareFilter.CompareOp.EQUAL,
            	new BinaryComparator("val".getBytes()));
        list.addFilter(valueFilter);
		
    	this.filterStack.push(list);
	}
		
	private void traverse(Select select) 
	{
    	if (log.isDebugEnabled())
    		log.debug("begin traverse");
        select.accept(this); // traverse
    	if (log.isDebugEnabled())
    		log.debug("end traverse");
	}
	
    public void start(Property property)
    {                
    	//if (log.isDebugEnabled())
    	//	log.debug("visit property: " + property.getName());
        org.plasma.query.model.FunctionValues function = property.getFunction();
        if (function != null)
            throw new CloudGraphFilterException("aggregate functions only supported in subqueries not primary queries");
          
        Path path = property.getPath();

        PlasmaType targetType = (PlasmaType)rootType;
                
        if (path != null)
        {

            String pathKey = "";
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new DataAccessException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);
                
                byte[] pathColKey = this.columnKeyFac.createColumnKey(
                	targetType, prop);
                String pathColKeyStr = Bytes.toString(pathColKey);
                prefixMap.put(pathColKeyStr, pathColKey);
                targetType = (PlasmaType)prop.getType(); // traverse
                
                pathKey += "/" + elem;
            }
        }
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        
        byte[] endpointKey = this.columnKeyFac.createColumnKey(
            targetType, endpointProp);
        String endpointKeyStr = Bytes.toString(endpointKey);
        prefixMap.put(endpointKeyStr, endpointKey);
        
        super.start(property);
    } 
    
	public void start(Literal literal) {
		super.start(literal);
	}

	public void start(NullLiteral nullLiteral) {
		//filter.append(paramName);
		//params.add(nullLiteral);
		super.start(nullLiteral);
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
