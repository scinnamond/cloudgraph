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
package org.cloudgraph.hbase.scan;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaType;


/**
 * An integral data "flavor" specific literal class used to abstract 
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
public class IntegralLiteral extends ScanLiteral 
    implements PartialRowKeyLiteral  {

	public static final int INCREMENT = 1;
	
	public IntegralLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			UserDefinedRowKeyFieldConfig fieldConfig) {
		super(literal, rootType, relationalOperator, 
			  fieldConfig);
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	public byte[] getEqualsStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		if (this.fieldConfig.isHash()) {
			startBytes = this.hashing.toStringBytes(startValueStr);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
		}
		return startBytes;
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	public byte[] getEqualsStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		
		if (this.fieldConfig.isHash()) {
			String stopValueStr = this.dataConverter.toString(property.getType(), startValue);
			stopBytes = this.hashing.toStringBytes(stopValueStr, HASH_INCREMENT);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			Long stopValue = startValue + INCREMENT;
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
		}
		return stopBytes;
	}	

	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	public byte[] getGreaterThanStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		if (fieldConfig.isHash()) {
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			startBytes = this.hashing.toStringBytes(startValueStr, HASH_INCREMENT);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			startValue = startValue + INCREMENT;
			String startValueStr = this.dataConverter.toString(property.getType(), startValue);
			startBytes = startValueStr.getBytes(this.charset);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
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
	public byte[] getGreaterThanStopBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "greater than equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	public byte[] getGreaterThanEqualStartBytes() {
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		if (fieldConfig.isHash()) {
			startBytes = this.hashing.toStringBytes(startValueStr);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
		}
		return startBytes;
	}
	
	/**
	 * The "greater than equals" relational operator does not
	 * effect the stop bytes for an HBase partial row-key scan
	 * and this method therefore returns an empty
	 * byte array or "no-op". 
	 * @return an empty
	 * byte array or "no-op". 
	 */
	public byte[] getGreaterThanEqualStopBytes() {
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
	public byte[] getLessThanStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	public byte[] getLessThanStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long stopValue = this.dataConverter.toLong(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so just use
		// the literal value, no need to decrement it
		String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
		if (fieldConfig.isHash()) {
			stopBytes = this.hashing.toStringBytes(stopValueStr);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);		
		}
		else {
			stopBytes = stopValueStr.getBytes(this.charset);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
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
	public byte[] getLessThanEqualStartBytes() {
	    return new byte[0];
	}
	
	/**
	 * Returns the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "stop row" bytes 
	 * used to represent "less than equals" relational operator 
	 * under an HBase partial row-key scan for this integral (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */	
	public byte[] getLessThanEqualStopBytes() {
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), this.literal);
		Long stopValue = this.dataConverter.toLong(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so increment
		// stop value to get this row for this field/literal
		if (fieldConfig.isHash()) {
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			stopBytes = this.hashing.toStringBytes(stopValueStr, this.HASH_INCREMENT);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);		
		}
		else {
			stopValue = stopValue + this.INCREMENT;
			String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
			stopBytes = stopValueStr.getBytes(this.charset);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
		}
		return stopBytes;
	}
}
