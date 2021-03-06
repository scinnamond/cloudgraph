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
package org.cloudgraph.config;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.ValueConstraint;
import org.plasma.sdo.helper.DataConverter;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * Encapsulates logic related to access of a configured
 * user-defined row key field. 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class UserDefinedRowKeyFieldConfig extends KeyFieldConfig {
    private static Log log = LogFactory.getLog(UserDefinedRowKeyFieldConfig.class);
	
    private DataGraphConfig dataGraph;
    private UserDefinedField userDefinedField;
    /** The simple property path with any XPath traversal elements removed */
    private String propertyPath;
    private PlasmaProperty endpointProperty;
    private int maxLength;
        
	public UserDefinedRowKeyFieldConfig(DataGraphConfig dataGraph, 
			UserDefinedField userDefinedField,
			int sequenceNum, int totalFields) {
		super(userDefinedField, sequenceNum, totalFields);
		this.dataGraph = dataGraph;
		this.userDefinedField = userDefinedField;
		
		try {
			construct(this.userDefinedField.getPath());
		}
		catch (IllegalArgumentException e) {
			throw new CloudGraphConfigurationException(e);
		}
		finally {			
		}
	}
	
	/**
	 * 
	 * Note; not using SDO XPath here as this operates
	 * on data not metadata and at this point we have only
	 * metadata to work with. This seems to surface the need
	 * for XPath processing for SDO types.
	 * @param xpath
	 */
	private void construct(String xpath) {
		Type contextType = this.getDataGraph().getRootType();
		StringBuilder buf = new StringBuilder();
		String[] tokens = xpath.split("/");
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0)
				buf.append("/");
			String token = tokens[i];
			int right = token.indexOf("[");
			if (right >= 0) // remove predicate - were just after the path 
				token = token.substring(0, right);	
			int attr = token.indexOf("@");
			if (attr == 0)
				token = token.substring(1);
			PlasmaProperty prop = (PlasmaProperty)contextType.getProperty(token);
			if (!prop.getType().isDataType()) 				
				contextType = prop.getType(); // traverse
			else
				this.endpointProperty = prop;
			buf.append(prop.getName());
		}
		
		this.propertyPath = buf.toString();	
		
		DataFlavor flavor = this.endpointProperty.getDataFlavor();		
		DataType dataType = DataType.valueOf(
				this.endpointProperty.getType().getName());
		switch (flavor) {
		case string:
			this.maxLength = getStringTypeMaxLength(this.endpointProperty, dataType, flavor);
			break;
		case integral:
			this.maxLength = getIntegralTypeMaxLength(this.endpointProperty, 
			    dataType, flavor);
			break;
		case real:
			this.maxLength = getRealTypeMaxLength(this.endpointProperty, dataType, flavor);
			break;
		case temporal:
			this.maxLength = getTemporalTypeMaxLength(this.endpointProperty, dataType, flavor);
			break;
		case other:
        	throw new IllegalArgumentException("data flavor '"
        			+ flavor + "' not supported for row key fields");	
		}
	}
	
	private int getRealTypeMaxLength(PlasmaProperty prop, 
			DataType dataType, DataFlavor flavor) {
		if (prop.getValueConstraint() == null)
			throw new MissingRequiredConstraintException("expected value contraint for property, "
				+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and real properties used in row key fields must be annotated with a value constraint and the total-digits set");
		ValueConstraint constraint = prop.getValueConstraint();
		if (constraint.getTotalDigits() == null || constraint.getTotalDigits().length() == 0)
			throw new MissingRequiredConstraintException("expected value contraint total-digits for real property, "
				+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and real properties used in row key fields must be annotated with a value constraint with the total-digits set");
		int totalDigits = Integer.parseInt(constraint.getTotalDigits());
		if (totalDigits == 0)
			throw new MissingRequiredConstraintException("expected value contraint with non-zero total-digits for real property, "
					+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and real properties used in row key fields must be annotated with a value constraint with the total-digits set");
		return Integer.parseInt(constraint.getTotalDigits());
	}
	
	private int getStringTypeMaxLength(PlasmaProperty prop, 
			DataType dataType, DataFlavor flavor) {
		if (prop.getValueConstraint() == null)
			throw new MissingRequiredConstraintException("expected value contraint for string property, "
				+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and string properties used in row key fields must be annotated with a value constraint and the max-length set");
		ValueConstraint constraint = prop.getValueConstraint();
		if (constraint.getMaxLength() == null || constraint.getMaxLength().length() == 0)
			throw new MissingRequiredConstraintException("expected value contraint max-length for string property, "
					+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and string properties used in row key fields must be annotated with a value constraint with the max-length set");
		int maxLength = Integer.parseInt(constraint.getMaxLength());
		if (maxLength == 0)
			throw new MissingRequiredConstraintException("expected value contraint with non-zero max-length for string property, "
					+ prop.getContainingType().toString() + "." + prop.getName() + " - row key fields are fixed length and string properties used in row key fields must be annotated with a value constraint with the max-length set");
			
		return Integer.parseInt(constraint.getMaxLength());
	}
	
	private int getIntegralTypeMaxLength(PlasmaProperty prop, 
			DataType dataType, DataFlavor flavor) {
		int result = 0;
		
		if (prop.getValueConstraint() != null && prop.getValueConstraint().getTotalDigits() != null && prop.getValueConstraint().getTotalDigits().length() > 0) {
			result = Integer.parseInt(prop.getValueConstraint().getTotalDigits());
			if (result == 0) {
				log.warn("expected value contraint with non-zero total-digits for integral property, "
						+ prop.getContainingType().toString() + "." + prop.getName() 
						+ " - row key fields are fixed length and integral properties used in row key fields must be annotated with a "
						+ "value constraint with the total-digits set - ignoring, using default");
				result = getDefaultIntegralTypeMaxLength(prop, dataType, flavor);
			}
		}
		else { // default it and warn
			result = getDefaultIntegralTypeMaxLength(prop, dataType, flavor);
		}
		return result;
	}
	
	private int getDefaultIntegralTypeMaxLength(PlasmaProperty prop, 
			DataType dataType, DataFlavor flavor)
	{
		int result;
		switch (dataType) {
        case Short: 
        	log.warn("expected value contraint total-digits for integral property, "
				+ prop.getContainingType().toString() + "." + prop.getName() 
				+ " - row key fields are fixed length and integral properties used in "
				+ "row key fields should be annotated with a value constraint with the total-digits set"
				+ " - defaulting to max size for datatype, " + dataType 
				+ ".");
        	result = String.valueOf(Short.MAX_VALUE).length();
			break;
        case Int:  
        	log.warn("expected value contraint total-digits for integral property, "
				+ prop.getContainingType().toString() + "." + prop.getName() 
				+ " - row key fields are fixed length and integral properties used in "
				+ "row key fields should be annotated with a value constraint with the total-digits set"
				+ " - defaulting to max size for datatype, " + dataType 
				+ ".");
        	result = String.valueOf(Integer.MAX_VALUE).length();
			break;
        case Long:  
        	log.warn("expected value contraint total-digits for integral property, "
				+ prop.getContainingType().toString() + "." + prop.getName() 
				+ " - row key fields are fixed length and integral properties used in "
				+ "row key fields should be annotated with a value constraint with the total-digits set"
				+ " - defaulting to max size for datatype, " + dataType 
				+ ".");
        	result = String.valueOf(Long.MAX_VALUE).length();
			break;
        default:
        	log.warn("cannot default max-length for datatype, "
        		+ dataType);
			throw new MissingRequiredConstraintException("expected value contraint total-digits for integral "+dataType+" property, "
					+ prop.getContainingType().toString() + "." + prop.getName() 
					+ " - row key fields are fixed length and integral properties used in row key fields must be annotated with a value constraint with the total-digits set");
		}
		return result;
	}
	
	private int getTemporalTypeMaxLength(PlasmaProperty prop, 
			DataType dataType, DataFlavor flavor) {
		switch (dataType) {
        case Date:  
        	return DataConverter.FORMAT_PATTERN_DATE.length();
        case DateTime: 
        	return DataConverter.FORMAT_PATTERN_DATETIME.length();
        case Day:      
        	return DataConverter.FORMAT_PATTERN_DAY.length();
        case Month:    
        	return DataConverter.FORMAT_PATTERN_MONTH.length();
        case MonthDay: 
        	return DataConverter.FORMAT_PATTERN_MONTHDAY.length();
        case Year:     
        	return DataConverter.FORMAT_PATTERN_YEAR.length();
        case YearMonth:
        	return DataConverter.FORMAT_PATTERN_YEARMONTH.length();
        case YearMonthDay:
        	return DataConverter.FORMAT_PATTERN_YEARMONTHDAY.length();
        case Time:     
        	return DataConverter.FORMAT_PATTERN_TIME.length();
        case Duration: 
        default:
        	throw new IllegalArgumentException("temporal datatype '"
        			+ dataType + "' not supported for row key fields");	
		}
	}
	
    public boolean equals(Object obj) {
    	UserDefinedRowKeyFieldConfig other = (UserDefinedRowKeyFieldConfig)obj;
	    return (this.sequenceNum == other.sequenceNum);
    }
    
	public int getSequenceNum() {
		return sequenceNum;
	}

	public DataGraphConfig getDataGraph() {
		return dataGraph;
	}

	public UserDefinedField getUserToken() {
		return userDefinedField;
	}

	public String getPathExpression() {
		return this.userDefinedField.getPath();
	}
	
	public boolean isHash() {
		return this.userDefinedField.isHash();
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public PlasmaProperty getEndpointProperty() {
		return endpointProperty;
	}

	/**
	 * Returns a token value from the given Data Graph
	 * @param dataGraph the data graph 
	 * @return the token value
	 */
	public byte[] getKeyBytes(
			commonj.sdo.DataGraph dataGraph) {
		return this.getKeyBytes(dataGraph.getRootObject());
	}
	/**
	 * Returns a user defined key value from the 
	 * given data object.
	 * @param dataObject the root data object 
	 * @return the token value
	 * @throws UnresolvedPathExpressionException if the 
	 * configured XPath expression resolves to a null value
	 */
	public byte[] getKeyBytes(
		DataObject dataObject) 
	{
		return getKey(dataObject).getBytes(this.charset);
	}
	
	public String getKey(
			commonj.sdo.DataGraph dataGraph) {
		return this.getKey(dataGraph.getRootObject());
	}

	/**
	 * Returns a user defined key value from the 
	 * given data object.
	 * @param dataObject the root data object 
	 * @return the token value
	 * @throws UnresolvedPathExpressionException if the 
	 * configured XPath expression resolves to a null value
	 */
	public String getKey(
		DataObject dataObject) 
	{
		// FIXME: do we want to invoke a converter here?
		// FIXME: do we want to transform this value somehow?
		String result = dataObject.getString(
			this.getPathExpression());
		if (result == null)
			throw new UnresolvedPathExpressionException(
				"the configured XPath expression '" 
				+ this.getPathExpression() + "'"
				+ " for graph root type '" + dataGraph.getRootType().getName() + "'"
				+ " within table '" + dataGraph.getTable().getName() + "'"
				+ " resolved to a null value - "
				+ "use an XPath expressions which terminate with a mandatory property, "
				+ "and return mandatory properties for data graph root types");			
		
		return result;
	}

	/**
	 * Returns the maximum length allowed for this
	 * key field. 
	 * @return the maximum length allowed for this
	 * key field.
	 */
	@Override
	public int getMaxLength() {
		return maxLength;
	}
	
	@Override
	public DataFlavor getDataFlavor() {
		return ((PlasmaProperty)this.endpointProperty).getDataFlavor();
	}
	
	
}
