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
package org.cloudgraph.recognizer;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.query.Wildcard;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperatorValues;
import org.plasma.query.model.WildcardOperatorValues;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.helper.DataConverter;

import commonj.sdo.DataObject;

/**
 * Delegate for graph recognizer expression classes. This class is not
 * thread safe and should not be shared across multiple expression class
 * instances.  
 * @author Scott Cinnamond
 * @since 0.5.3
 */
public class GraphRecognizerSupport {
    private static Log log = LogFactory.getLog(GraphRecognizerSupport.class);
	@SuppressWarnings("rawtypes")
	private NumberComparator numberComparator = 
 			new NumberComparator();
	private DataConverter dataConverter = DataConverter.INSTANCE;
	/** cached wildcard pattern */
	private Pattern wildcardLiteralPattern;
	
	/**
	 * Collects and returns data values at the endpoint of the
	 * given path, traversing objects along the
	 * the given traversal path if exists.
	 * @param targetObject the current target
	 * @param property the query property
	 * @param path the query property path
	 * @param pathIndex the current path element index
	 * @param values the collection of result values
	 */
	public void collect(DataObject targetObject,
			Property property, Path path, int pathIndex,
			List<Object> values)
	{
        PlasmaType targetType = (PlasmaType)targetObject.getType(); 
        if (path != null && pathIndex < path.getPathNodes().size()) {
        	AbstractPathElement pathElem = path.getPathNodes().get(pathIndex).getPathElement();
            if (pathElem instanceof WildcardPathElement)
                throw new GraphServiceException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
            String elem = ((PathElement)pathElem).getValue();
            PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem); 
            if (targetObject.isSet(prop)) {
                if (prop.isMany()) {
            	    @SuppressWarnings("unchecked")
					List<DataObject> list = targetObject.getList(prop);
            	    for (DataObject next : list)
            	    	collect(next, property, path, pathIndex + 1, values);
                }
                else {
                    DataObject next = targetObject.getDataObject(prop);                	
        	    	collect(next, property, path, pathIndex + 1, values);
                }
            }
        }
        else {
            PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
            if (!endpointProp.getType().isDataType())
                throw new GraphServiceException("expected datatype property for, "
                	+ endpointProp);
            if (targetObject.isSet(endpointProp)) {
	            if (endpointProp.isMany()) {
	        	    @SuppressWarnings("unchecked")
					List<Object> list = targetObject.getList(endpointProp);
	        	    for (Object value : list)
	        	    	values.add(value);
	            }
	            else {
	        	    Object value = targetObject.get(endpointProp);  
	        	    values.add(value);
	            }
            }
        }
	}	
	
	/**
	 * Returns the SDO property endpoint for the given
	 * query property traversal path
	 * @param property the query property
	 * @param rootType the graph root type
	 * @return the SDO property endpoint
	 */
	public PlasmaProperty getEndpoint(Property property, PlasmaType rootType)
	{
        Path path = property.getPath();
        PlasmaType targetType = rootType;                
        if (path != null)
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new GraphServiceException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);                
                targetType = (PlasmaType)prop.getType(); // traverse                
            }
        	
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
	    return endpointProp;
	}
	
	/**
	 * Determines the property datatype and evaluates the
	 * given property value against the given literal and
	 * relational operator.
	 * @param property the SDO property
	 * @param propertyValue the property data value
	 * @param operator the relational operator
	 * @param literal the query literal
	 * @return whether the data property value evaluates true
	 * against given literal and
	 * relational operator
	 */
	public boolean evaluate(PlasmaProperty property, 
			Object propertyValue,
			RelationalOperatorValues operator,
			String literal)
	{
		DataType dataType = DataType.valueOf(property.getType().getName());
        boolean result = true;
		
		switch (property.getDataFlavor()) {
		case integral:
		case real:
			Number propertyNumberValue = (Number)propertyValue;
			Number literalNumberValue = (Number)this.dataConverter.convert(property.getType(), literal);
			result = evaluate(propertyNumberValue, 
				operator,
				literalNumberValue); 
		    break;
		case string:
			String propertyStringValue = (String)propertyValue;
			String literalStringValue = (String)this.dataConverter.convert(property.getType(), literal);
			result = evaluate(propertyStringValue, 
				operator,
				literalStringValue); 
		    break;
		case temporal:
			switch (dataType) {
			case Date:
				Date propertyDateValue = (Date)propertyValue;
				Date literalDateValue = (Date)this.dataConverter.convert(property.getType(), literal);
				result = evaluate(propertyDateValue, 
					operator,
					literalDateValue); 
				break;	
			default:	
				propertyStringValue = (String)propertyValue;
				literalStringValue = (String)this.dataConverter.convert(property.getType(), literal);
				result = evaluate(propertyStringValue, 
					operator,
					literalStringValue); 
				break;
			}
			break;
		case other:
			throw new GraphServiceException("data flavor '"
		        + property.getDataFlavor() 
		        + "' not supported for relational operator '"
		        + operator + "'");
		}
		return result;
	}

	
	/**
	 * Determines the property datatype and evaluates the
	 * given property value against the given literal and
	 * wildcard operator.
	 * @param property the SDO property
	 * @param propertyValue the property data value
	 * @param operator the wildcard operator
	 * @param literal the query literal
	 * @return whether the data property value evaluates true
	 * against given literal and
	 * wildcard operator
	 */
	public boolean evaluate(PlasmaProperty property, 
			Object propertyValue,
			WildcardOperatorValues operator,
			String literal)
	{
		DataType dataType = DataType.valueOf(property.getType().getName());
        boolean result = true;
		
		switch (property.getDataFlavor()) {
		case string:
			String propertyStringValue = (String)propertyValue;
			propertyStringValue = propertyStringValue.trim(); // as trailing newlines confuse regexp greatly
			String literalStringValue = (String)this.dataConverter.convert(property.getType(), literal);
			result = evaluate(propertyStringValue, 
				operator,
				literalStringValue); 
		    break;
		case integral:
		case real:
		case temporal:
		case other:
			throw new GraphServiceException("data flavor '"
			        + property.getDataFlavor() 
			        + "' not supported for wildcard operator '"
			        + operator + "'");
		}
		return result;
	}
	
	private boolean evaluate(Date propertyValue,
			RelationalOperatorValues operator,
			Date literalValue)
	{
	    int comp = propertyValue.compareTo( 
			literalValue);
		return evaluate(operator, comp);
	}
	
	private boolean evaluate(Number propertyValue,
		RelationalOperatorValues operator,
		Number literalValue)
	{
		@SuppressWarnings("unchecked")
		int comp = this.numberComparator.compare(propertyValue, 
				literalValue);
		return evaluate(operator, comp);
	}

	private boolean evaluate(String propertyValue,
		RelationalOperatorValues operator,
		String literalValue) 
	{
		try {
		int comp = propertyValue.compareTo( 
			literalValue);
		return evaluate(operator, comp);
		}
		catch (NullPointerException e) {
			throw e;
		}
	}

	private boolean evaluate(String propertyValue,
		WildcardOperatorValues operator,
		String literalValue) 
	{
		if (this.wildcardLiteralPattern == null) {
			String pattern = wildcardToRegex(literalValue);
			this.wildcardLiteralPattern = Pattern.compile(pattern);
		}
		Matcher matcher = this.wildcardLiteralPattern.matcher(propertyValue);
		return matcher.matches();
	}
	
    private String wildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
	
	private boolean evaluate(
		RelationalOperatorValues operator,
		int comp)
	{
		switch (operator) {
		case EQUALS: 
			return comp == 0;
		case NOT_EQUALS:
			return comp != 0;
		case GREATER_THAN:
			return comp > 0;
		case GREATER_THAN_EQUALS:
			return comp >= 0;
		case LESS_THAN:
			return comp < 0;
		case LESS_THAN_EQUALS:
			return comp <= 0;
		default:
			throw new GraphServiceException("unknown relational operator, " 
		        + operator);
		}		
	}
	
	@SuppressWarnings("rawtypes")
	class NumberComparator<T extends Number & Comparable> implements Comparator<T> {

	    @SuppressWarnings("unchecked")
		public int compare( T a, T b ) throws ClassCastException {
	        return a.compareTo( b );
	    }
	}
}
