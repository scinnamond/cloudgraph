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
 * @author Scott Cinnamond
 * @since 0.5
 */
public class StringLiteral extends ScanLiteral {

	// FIXME: appending the first lexicographic ASCII char
	// to the row key "works" as a stop key. But need
	// to understand more about why, say the min unicode char does not. 
	public static final String INCREMENT = "A";

	public StringLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			LogicalOperator logicalOperator, UserDefinedRowKeyFieldConfig fieldConfig) {
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
			int startHashValue = hash.hash(startValueStr.getBytes(this.charset));
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
			int stopHashValue = hash.hash(stopValueStr.getBytes(this.charset));
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
			int stopHashValue = hash.hash(stopValueStr.getBytes(this.charset));
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
