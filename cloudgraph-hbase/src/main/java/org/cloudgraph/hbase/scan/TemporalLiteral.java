package org.cloudgraph.hbase.scan;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.cloudgraph.hbase.service.HBaseDataConverter;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaType;

/**
 * A temporal data "flavor" specific literal class used to abstract 
 * the complexities involved in assembling the various 
 * segments and fields of composite (scan start/stop) row keys 
 * under various relational and logical operator and
 * various configurable composite key-field hashing, formatting, padding 
 * and other features.
 * 
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.service.HBaseDataConverter
 */
public class TemporalLiteral extends ScanLiteral {

    private static Log log = LogFactory.getLog(TemporalLiteral.class);
    private final int INCREMENT = 1;
    private final int DATE_INCREMENT = 1000; // SDO Date data type resolution is seconds
    private final int DATE_TIME_INCREMENT = 1; // SDO Datetime data type resolution is seconds

	public TemporalLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			LogicalOperator logicalOperator, UserDefinedFieldConfig fieldConfig) {
		super(literal, rootType, relationalOperator, 
				logicalOperator, fieldConfig);
	}

	protected int getIncrement(DataType dataType) {
		switch (dataType) {
		case Date:
			return DATE_INCREMENT;
		case DateTime:
			return DATE_TIME_INCREMENT;
		default:	
			return INCREMENT;
		}
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getEqualsStartBytes() {
		byte[] startBytes = null;
		// FIXME: convert to native type as we create literals
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		if (fieldConfig.isHash()) {
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, value);
			int startHashValue = hash.hash(startBytes);
			String startHashValueStr = String.valueOf(startHashValue);
			startBytes = startHashValueStr.getBytes(this.charset);
		}
		else {
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, value);
		}
		return startBytes;
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getEqualsStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
        DataType dataType = DataType.valueOf(property.getType().getName());
		// As per SDO 2.1 spec every temporal data type can be
		// converted to a date, not a long however.
		// So get to a date then a long, then manipulate/increment the
		// value and convert back...
		Date dateValue = this.dataConverter.toDate(property.getType(), value);
		Long longValue = dateValue.getTime(); 
		
		//Note: the partial scan stop row bytes are exclusive 
		// which is why we are incrementing below. 
		
		if (this.fieldConfig.isHash()) {
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, value);
			int startHashValue = hash.hash(stopBytes);
			
			// Note only increment/decrement the hash value
			// for hashed fields
			Long stopValue = Long.valueOf(startHashValue + HASH_INCREMENT);
			String stopValueStr = String.valueOf(stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			Long stopLongValue = longValue + getIncrement(dataType);
			Date stopDate = new Date(stopLongValue);
			// back to whatever its native type is
			Object stopValue = this.dataConverter.convert(property.getType(), stopDate);
			// re format the string under its native type
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}	

	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
        DataType dataType = DataType.valueOf(property.getType().getName());
		// As per SDO 2.1 spec every temporal data type can be
		// converted to a date, not a long however.
		// So get to a date then a long, then manipulate/increment the
		// value and convert back...
		Date dateValue = this.dataConverter.toDate(property.getType(), value);
		Long longValue = dateValue.getTime(); 
		
		if (this.fieldConfig.isHash()) {
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, value);
			int startHashValue = hash.hash(startBytes);
			
			// Note only increment/decrement the hash value
			// for hashed fields
			Long startValue = Long.valueOf(startHashValue + HASH_INCREMENT);
			String startValueStr = String.valueOf(startValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			Long startLongValue = longValue + getIncrement(dataType);
			Date startDate = new Date(startLongValue);
			// back to whatever its native type is
			Object startValue = this.dataConverter.convert(property.getType(), startDate);
			// re format the string under its native type
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			
			startBytes = startValueStr.getBytes(this.charset);
		}
		return startBytes;
	}
	
	/**
	 * The "greater than" relational operator does not
	 * effect the stop bytes for an HBase partial row-key scan
	 * and this method therefore returns an empty
	 * byte array or "no-op". 
	 * @return an empty
	 * byte array or "no-op". 
	 */
	protected byte[] getGreaterThanStopBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanEqualStartBytes() {
	    return this.getEqualsStartBytes();
	}
	
	/**
	 * The "greater than equals" relational operator does not
	 * effect the stop bytes for an HBase partial row-key scan
	 * and this method therefore returns an empty
	 * byte array or "no-op". 
	 * @return an empty
	 * byte array or "no-op". 
	 */
	protected byte[] getGreaterThanEqualStopBytes() {
	    return new byte[0];
	}
	
	/**
	 * The "less than" relational operator does not
	 * effect the start bytes for an HBase partial row-key scan
	 * and this method therefore returns an empty
	 * byte array or "no-op". 
	 * @return an empty
	 * byte array or "no-op". 
	 */
	protected byte[] getLessThanStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		// As per SDO 2.1 spec every temporal data type can be
		// converted to a date, not a long however.
		// So get to a date then a long, then manipulate/increment the
		// value and convert back...
		Date dateValue = this.dataConverter.toDate(property.getType(), value);
		Long longValue = dateValue.getTime(); 
		
		//Note: the partial scan stop row bytes are exclusive 
		// which is why we are incrementing below. 
		
		if (this.fieldConfig.isHash()) {
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, value);
			int startHashValue = hash.hash(stopBytes);
			Long stopValue = Long.valueOf(startHashValue);
			String stopValueStr = String.valueOf(stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			Long stopLongValue = longValue;
			Date stopDate = new Date(stopLongValue);
			// back to whatever its native type is
			Object stopValue = this.dataConverter.convert(property.getType(), stopDate);
			// re format the string under its native type
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}
	
	/**
	 * The "less than equal" relational operator does not
	 * effect the start bytes for an HBase partial row-key scan
	 * and this method therefore returns an empty
	 * byte array or "no-op". 
	 * @return an empty
	 * byte array or "no-op". 
	 */
	protected byte[] getLessThanEqualStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanEqualStopBytes() {
	    return this.getEqualsStopBytes();
	}
}
