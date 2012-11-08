package org.cloudgraph.hbase.scan;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.PlasmaType;

/**
 * A real data "flavor" specific literal class used to abstract 
 * the complexities involved in assembling the various 
 * segments and fields of composite (scan start/stop) row keys 
 * under various relational and logical operator and
 * various configurable composite key-field hashing, formatting, padding 
 * and other features.
 * 
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.service.HBaseDataConverter
 * @author Scott Cinnamond
 * @since 0.5
 */
public class RealLiteral extends ScanLiteral {

	protected final double INCREMENT = Double.MIN_VALUE;

	public RealLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			LogicalOperator logicalOperator, UserDefinedRowKeyFieldConfig fieldConfig) {
		super(literal, rootType, relationalOperator, 
				logicalOperator, fieldConfig);
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getEqualsStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double startValue = this.dataConverter.toDouble(property.getType(), value);
		if (this.fieldConfig.isHash()) {
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		return startBytes;
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getEqualsStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double startValue = this.dataConverter.toDouble(property.getType(), value);
		
		if (this.fieldConfig.isHash()) {
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			int startHashValue = hash.hash(startValueStr.getBytes());
			
			// Note only increment/decrement the hashed value
			// for hashed fields
			Double stopValue = Double.valueOf(startHashValue + INCREMENT);
			String stopValueStr = String.valueOf(stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			Double stopValue = startValue + INCREMENT;
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}	

	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double startValue = this.dataConverter.toDouble(property.getType(), value);
		if (fieldConfig.isHash()) {
			String startValueStr = String.valueOf(startValue);
			int startHashValue = hash.hash(startValueStr.getBytes());
			// Note only increment/decrement the hashed value
			// for hashed fields
			startHashValue = startHashValue + HASH_INCREMENT;
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			startValue = startValue + INCREMENT;
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		return startBytes;
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanStopBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanEqualStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double startValue = this.dataConverter.toDouble(property.getType(), value);
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		if (fieldConfig.isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
		}
		return startBytes;
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	protected byte[] getGreaterThanEqualStopBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double stopValue = this.dataConverter.toDouble(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so just use
		// the literal value, no need to decrement it
		String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
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
	 * Returns the "start row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanEqualStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this real (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	protected byte[] getLessThanEqualStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Double stopValue = this.dataConverter.toDouble(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so increment
		// stop value to get this row for this field/literal
		if (fieldConfig.isHash()) {
			String stopValueStr = String.valueOf(stopValue);
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopHashValue++;
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			stopValue++;
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}
}
