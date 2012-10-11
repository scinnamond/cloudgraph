package org.cloudgraph.hbase.scan;

import org.cloudgraph.config.UserDefinedFieldConfig;
import org.cloudgraph.hbase.service.HBaseDataConverter;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.PlasmaType;

/**
 * A string data "flavor" specific literal class used to abstract 
 * the complexities involved in assembling the various 
 * segments and fields of composite (scan start/stop) row keys 
 * under various relational and logical operator and
 * various configurable composite key-field hashing, formatting, padding 
 * and other features.
 * 
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.service.HBaseDataConverter
 */
public class StringLiteral extends ScanLiteral {

	// FIXME: appending the first lexicographic ASCII char
	// to the row key "works" as a stop key. But need
	// to understand more about why, say the min unicode char does not. 
	protected final String INCREMENT = "A";

	public StringLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			LogicalOperator logicalOperator, UserDefinedFieldConfig fieldConfig) {
		super(literal, rootType, relationalOperator, 
				logicalOperator, fieldConfig);
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
		String startValueStr = this.literal;
		if (fieldConfig.isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
		}
		else {
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
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
		String startValueStr = this.literal;
		if (fieldConfig.isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			
			int stopHashValue = hash.hash(startValueStr.getBytes());
			stopHashValue = stopHashValue + this.HASH_INCREMENT;
			String stopValueStr = String.valueOf(stopHashValue);
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, stopValueStr);
		}
		else {
			String stopValueStr = startValueStr + INCREMENT;  
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, stopValueStr);
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
		String startValueStr = this.literal;
		if (fieldConfig.isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startHashValue = startHashValue + this.HASH_INCREMENT;
			startValueStr = String.valueOf(startHashValue);
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
		}
		else {
			startValueStr = startValueStr + INCREMENT;
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
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
		String stopValueStr = this.literal;
		// Note: in HBase the stop row is exclusive, so just use
		// the literal value, no need to decrement it
		if (fieldConfig.isHash()) {
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
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
		byte[] stopBytes = null;
		String stopValueStr = this.literal;
		// Note: in HBase the stop row is exclusive, so increment
		// stop value to get this row for this field/literal
		if (fieldConfig.isHash()) {
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopHashValue = stopHashValue + this.HASH_INCREMENT;
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			stopValueStr = stopValueStr + this.INCREMENT;
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}
}
