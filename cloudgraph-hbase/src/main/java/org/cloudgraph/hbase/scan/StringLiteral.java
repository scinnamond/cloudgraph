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

import java.util.Arrays;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaType;

import com.google.common.primitives.Bytes;

/**
 * A string data "flavor" specific literal class used to abstract 
 * the complexities involved in assembling the various 
 * segments and fields of composite (scan start/stop) row keys 
 * under various relational and logical operator and
 * various configurable composite key-field hashing, formatting, padding 
 * and other features. A string literal does not contain or involve wildcards, see {@link WildcardStringLiteral}, but
 * nevertheless may "participate" in a fuzzy scan as part of a composite row key and
 * therefore implements {@link FuzzyRowKeyLiteral} supplying only default key and
 * info bytes.  
 * 
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.service.HBaseDataConverter
 * @see WildcardStringLiteral
 * @author Scott Cinnamond
 * @since 0.5
 */
public class StringLiteral extends ScanLiteral 
    implements PartialRowKeyLiteral, FuzzyRowKeyLiteral, CompleteRowKeyLiteral {

	public static final byte INCREMENT = Byte.MIN_VALUE;

	public StringLiteral(String literal,
			PlasmaType rootType,
			RelationalOperator relationalOperator,
			UserDefinedRowKeyFieldConfig fieldConfig) {
		super(literal, rootType, relationalOperator, 
			fieldConfig);
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
	@Override
	public byte[] getEqualsStartBytes() {
		byte[] startBytes = null;
		String startValueStr = this.literal;
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
	@Override
	public byte[] getEqualsStopBytes() {
		byte[] stopBytes = null;
		String stopValueStr = this.literal;
		if (fieldConfig.isHash()) {
			stopBytes = this.hashing.toStringBytes(stopValueStr,
					this.HASH_INCREMENT);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			byte[] literalBytes = stopValueStr.getBytes(this.charset);
			literalBytes = this.padding.pad(literalBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
			stopBytes = new byte[literalBytes.length + 1];
			System.arraycopy(literalBytes, 0, stopBytes, 0, literalBytes.length);
			stopBytes[stopBytes.length-1] = INCREMENT;
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
	@Override
	public byte[] getGreaterThanStartBytes() {
		byte[] startBytes = null;
		String startValueStr = this.literal;
		if (fieldConfig.isHash()) {
			startBytes = this.hashing.toStringBytes(startValueStr,
					this.HASH_INCREMENT);
			startBytes = this.padding.pad(startBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {			
			byte[] literalStartBytes = startValueStr.getBytes(this.charset);
			startBytes = new byte[literalStartBytes.length + 1];
			System.arraycopy(literalStartBytes, 0, startBytes, 0, literalStartBytes.length);
			startBytes[startBytes.length-1] = INCREMENT;
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
	@Override
	public byte[] getGreaterThanStopBytes() {
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
	@Override
	public byte[] getGreaterThanEqualStartBytes() {
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
	@Override
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
	@Override
	public byte[] getLessThanStartBytes() {
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
	@Override
	public byte[] getLessThanStopBytes() {
		byte[] stopBytes = null;
		String stopValueStr = this.literal;
		// Note: in HBase the stop row is exclusive, so just use
		// the literal value, no need to decrement it
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
	@Override
	public byte[] getLessThanEqualStartBytes() {
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
	@Override
	public byte[] getLessThanEqualStopBytes() {
		byte[] stopBytes = null;
		String stopValueStr = this.literal;
		// Note: in HBase the stop row is exclusive, so increment
		// stop value to get this row for this field/literal
		if (fieldConfig.isHash()) {
			stopBytes = this.hashing.toStringBytes(stopValueStr,
					this.HASH_INCREMENT);
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			byte[] literalStopBytes = stopValueStr.getBytes(this.charset);
			stopBytes = new byte[literalStopBytes.length + 1];
			System.arraycopy(literalStopBytes, 0, stopBytes, 0, literalStopBytes.length);
			stopBytes[stopBytes.length-1] = INCREMENT;
			stopBytes = this.padding.pad(stopBytes, 
					this.fieldConfig.getMaxLength(), 
					this.fieldConfig.getDataFlavor());
		}
		return stopBytes;
	}

	@Override
	public byte[] getFuzzyKeyBytes() {
		byte[] keyBytes = null;
		String keyValueStr = this.literal;
		
		if (fieldConfig.isHash()) {
			keyBytes = hashing.toStringBytes(keyValueStr);
			keyBytes = this.padding.pad(keyBytes, 
					this.fieldConfig.getMaxLength(), 
					DataFlavor.integral);
		}
		else {
			// no need for data-type conversion as
			// literal is data-type/data-flavor specific
			keyBytes = keyValueStr.getBytes(this.charset);
			keyBytes = this.padding.pad(keyBytes, 
				this.fieldConfig.getMaxLength(), 
				this.fieldConfig.getDataFlavor());
		}		
		
		return keyBytes;
	}
	
	@Override
	public byte[] getFuzzyInfoBytes() {
		byte[] infoBytes = new byte[this.fieldConfig.getMaxLength()];
		Arrays.fill(infoBytes, (byte)0); // fixed char 
		return infoBytes;
	}
	
	/**
	 * Returns the bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase row-key 'Get' operation for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 * @return the "start row" bytes 
	 * used to represent "equals" relational operator 
	 * under an HBase partial row-key scan for this string (data flavor) literal under 
	 * the various optionally configurable hashing, 
	 * formatting and padding features.
	 */
	@Override
	public byte[] getEqualsBytes() {
		return getEqualsStartBytes();
	}	
}
