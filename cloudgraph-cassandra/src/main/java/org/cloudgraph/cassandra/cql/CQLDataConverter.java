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
package org.cloudgraph.cassandra.cql;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.helper.DataConverter;

import com.datastax.driver.core.Row;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class CQLDataConverter {
	private static Log log = LogFactory.getFactory().getInstance(
			CQLDataConverter.class);

	static public CQLDataConverter INSTANCE = initializeInstance();
	private Map<Integer, String> cqlTypeMap = new HashMap<Integer, String>();

	private CQLDataConverter() {

		for (com.datastax.driver.core.DataType.Name typeName : com.datastax.driver.core.DataType.Name.values()) {
			Integer value = (Integer) typeName.ordinal();
			cqlTypeMap.put(value, typeName.name());
		}
	}

	private static synchronized CQLDataConverter initializeInstance() {
		if (INSTANCE == null)
			INSTANCE = new CQLDataConverter();
		return INSTANCE;
	}

	public Object fromCQLDataType(Row row, int columnIndex,
			com.datastax.driver.core.DataType sourceType, PlasmaProperty targetProperty)   {

		Object result = convertFrom(row, columnIndex, sourceType, targetProperty);	 
		return result;
	}
	
	public int toCQLDataType(PlasmaProperty sourceProperty, Object value)
			  {

		int result = convertToCQLType(sourceProperty, value);;
		return result;
	}
	
	public Object toCQLDataValue(PlasmaProperty sourceProperty, Object value)
			  {

		Object result = convertToCQLValue(sourceProperty, value);
		
		return result;
	}
	
	public DataFlavor toCQLDataFlavor(PlasmaProperty sourceProperty)
	{
		if (!sourceProperty.getType().isDataType())
			throw new IllegalArgumentException("expected data type property, not " +
					sourceProperty.toString());
		PlasmaProperty dataProperty = sourceProperty;
    	return dataProperty.getDataFlavor();
	}

	private Object convertToCQLValue(Property property, Object value)
	{
		if (!property.getType().isDataType())
			throw new IllegalArgumentException("expected data type property, not " +
					property.toString());
		DataType dataType = DataType.valueOf(property.getType()
				.getName());
		Object result;
		switch (dataType) {
		case String:
		case URI:
		case Month:
		case MonthDay:
		case Day:
		case Time:
		case Year:
		case YearMonth:
		case YearMonthDay:
		case Duration:
			result = DataConverter.INSTANCE.toString(property
					.getType(), value);
			break;
		case Date:
			// Plasma SDO allows more precision than just month/day/year
			// in an SDO date datatype, and using java.sql.Date will truncate
			// here so use java.sql.Timestamp.
			Date date = DataConverter.INSTANCE.toDate(property
					.getType(), value);
			result = new java.sql.Timestamp(date.getTime());
			break;
		case DateTime:
			date = DataConverter.INSTANCE.toDate(property.getType(),
			    value);
			result = new java.sql.Timestamp(date.getTime());
			break;
		case Decimal:
			result = DataConverter.INSTANCE.toDecimal(property
					.getType(), value);
			break;
		case Bytes:
			result = DataConverter.INSTANCE.toBytes(property
					.getType(), value);
			break;
		case Byte:
			result = DataConverter.INSTANCE.toByte(
					property.getType(), value);
			break;
		case Boolean:
			result = DataConverter.INSTANCE.toBoolean(property
					.getType(), value);
			break;
		case Character:
			result = DataConverter.INSTANCE.toString(property
					.getType(), value);
			break;
		case Double:
			result = DataConverter.INSTANCE.toDouble(property
					.getType(), value);
			break;
		case Float:
			result = DataConverter.INSTANCE.toDouble(property
					.getType(), value);
			break;
		case Int:
			result = DataConverter.INSTANCE.toInt(property.getType(),
					value);
			break;
		case Integer:
			result = DataConverter.INSTANCE.toInteger(property
					.getType(), value);
			break;
		case Long:
			result = DataConverter.INSTANCE.toLong(
					property.getType(), value);
			break;
		case Short: // CQL has no short see http://www.datastax.com/documentation/cql/3.0/cql/cql_reference/cql_data_types_c.html
			if (value instanceof Integer)
				result = value;
			else
			    result = DataConverter.INSTANCE.toInt(
					property.getType(), value);
			break;
		case Strings:
			result = DataConverter.INSTANCE.toString(property
					.getType(), value);
			break;
		case Object:
		default:
			result = DataConverter.INSTANCE.toString(property
					.getType(), value);
			break;
		}

		return result;
	}
	
	private int convertToCQLType(Property property, Object value)
	{
		int result = -1;
		if (!property.getType().isDataType())
			throw new IllegalArgumentException("expected data type property, not " +
					property.toString());
		DataType dataType = DataType.valueOf(property.getType()
				.getName());
		switch (dataType) {
		case String:
		case URI:
		case Month:
		case MonthDay:
		case Day:
		case Time:
		case Year:
		case YearMonth:
		case YearMonthDay:
		case Duration:
		case Strings:
			result = com.datastax.driver.core.DataType.Name.VARCHAR.ordinal();
			break;
		case Date:
			// Plasma SDO allows more precision than just month/day/year
			// in an SDO date datatype, and using java.sql.Date will truncate
			// here so use java.sql.Timestamp.
			result = com.datastax.driver.core.DataType.Name.TIMESTAMP.ordinal();
			break;
		case DateTime:
			result = com.datastax.driver.core.DataType.Name.VARCHAR.ordinal();
			break;
		case Decimal:
			result = com.datastax.driver.core.DataType.Name.DECIMAL.ordinal();
			break;
		case Bytes:
			result = com.datastax.driver.core.DataType.Name.BLOB.ordinal();
			break;
		case Byte:
			//result = java.sql.Types.VARBINARY;
			result = com.datastax.driver.core.DataType.Name.INT.ordinal(); // no CQL single byte type
			break;
		case Boolean:
			result = com.datastax.driver.core.DataType.Name.BOOLEAN.ordinal();
			break;
		case Character:
			result = com.datastax.driver.core.DataType.Name.VARCHAR.ordinal(); // no CQL single character type
			break;
		case Double:
			result = com.datastax.driver.core.DataType.Name.DOUBLE.ordinal();
			break;
		case Float:
			result = com.datastax.driver.core.DataType.Name.FLOAT.ordinal();
			break;
		case Int:
			result = com.datastax.driver.core.DataType.Name.INT.ordinal();
			break;
		case Integer:
			result = com.datastax.driver.core.DataType.Name.VARINT.ordinal();
			break;
		case Long:
			result = com.datastax.driver.core.DataType.Name.COUNTER.ordinal(); // assuming counter does not have autoincrement, otherwise no long type (??)
			break;
		case Short:
			result = com.datastax.driver.core.DataType.Name.INT.ordinal(); // no CQL short type
			break;
		case Object:
		default:
			result = com.datastax.driver.core.DataType.Name.CUSTOM.ordinal();
			break;
		}
		return result;
	}
	
	private Object convertFrom(Row row, int columnIndex,
			com.datastax.driver.core.DataType sourceType, Property property)   {
		Object result = null;
		if (!property.getType().isDataType())
			throw new IllegalArgumentException("expected data type property, not " +
					property.toString());
		DataType targetDataType = DataType.valueOf(property.getType()
				.getName());
		switch (targetDataType) {
		case String:
		case URI:
		case Month:
		case MonthDay:
		case Day:
		case Time:
		case Year:
		case YearMonth:
		case YearMonthDay:
		case Duration:
			result = row.getString(columnIndex);
			break;
		case Date:
			java.util.Date ts = row.getDate(columnIndex);
			if (ts != null)
			    result = new java.util.Date(ts.getTime());
			break;
		case DateTime:
			ts = row.getDate(columnIndex);
			if (ts != null) {
				// format DateTime String for SDO
				java.util.Date date = new java.util.Date(ts.getTime());
				result = DataConverter.INSTANCE.getDateTimeFormat().format(date); 
			}
			break;
		case Decimal:
			result = row.getDecimal(columnIndex);
			break;
		case Bytes:
			break;
		case Byte:
			result = row.getBytes(columnIndex).array()[0];
			break;
		case Boolean:
			result = row.getBool(columnIndex);
			break;
		case Character:
			result = row.getInt(columnIndex);
			break;
		case Double:
			result = row.getDouble(columnIndex);
			break;
		case Float:
			result = row.getFloat(columnIndex);
			break;
		case Int:
			result = row.getInt(columnIndex);
			break;
		case Integer:
			result = new BigInteger(row.getString(columnIndex));
			break;
		case Long:
			result = row.getLong(columnIndex);
			break;
		case Short:
			int intValue = row.getInt(columnIndex);			
			result =  DataConverter.INSTANCE.fromInt(property.getType(), intValue);
			break;
		case Strings:
			String value = row.getString(columnIndex);
			if (value != null) {
				String[] values = value.split("\\s");
				List<String> list = new ArrayList<String>(values.length);
				for (int i = 0; i < values.length; i++)
					list.add(values[i]); // what no Java 5 sugar for this ??
				result = list;
			}
			break;
		case Object:
		default:
			//FIXME: getMap, getList etc... will likely be mapped to SDO Object data type
			result = row.getString(columnIndex);
			break;
		}
		return result;
	}

	public String toCQLString(Type sourceType, PlasmaProperty sourceProperty,
			Object value) {

		String result = null;
		DataFlavor flavor = sourceProperty.getDataFlavor();

		switch (flavor) {
		case integral:
		case real:
			result = value.toString();
			break;
		case string:
			result = "'" + value.toString() + "'";
			break;
		default:
			result = value.toString();
		}

		return result;
	}

	public String getCQLTypeName(int jdbcType) {

		return cqlTypeMap.get(jdbcType);
	}
}
