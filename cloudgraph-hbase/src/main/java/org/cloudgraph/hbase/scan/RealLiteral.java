package org.cloudgraph.hbase.scan;

import java.math.BigDecimal;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.DataType;

import commonj.sdo.Type;

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

	protected final float INCREMENT_FLOAT = Float.MIN_VALUE;
	protected final double INCREMENT_DOUBLE = Double.MIN_VALUE;
	protected final BigDecimal INCREMENT_DECIMAL = BigDecimal.valueOf(Double.MIN_VALUE);

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
		Object startValue = this.dataConverter.convert(property.getType(), this.literal);
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
		if (this.fieldConfig.isHash()) {
			String stopValueStr = incrementHash(property.getType(), value);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			String stopValueStr = increment(property.getType(), value);
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
		if (this.fieldConfig.isHash()) {
			String startValueStr = incrementHash(property.getType(), value);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			String startValueStr = increment(property.getType(), value);
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
		Object startValue = this.dataConverter.convert(property.getType(), this.literal);
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
		Object stopValue = this.dataConverter.convert(property.getType(), this.literal);
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
		if (this.fieldConfig.isHash()) {
			String stopValueStr = incrementHash(property.getType(), value);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			String stopValueStr = increment(property.getType(), value);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		return stopBytes;
	}
	
	private String incrementHash(Type type, Object value) {
		String valueStr = this.dataConverter.toString(property.getType(), value);
		int hashValue = hash.hash(valueStr.getBytes());
		int resultHash = hashValue + HASH_INCREMENT;
		String result = String.valueOf(resultHash);
		return result;
	}
	
	private String increment(Type type, Object value) {
		String result = "";
        DataType sourceDataType = DataType.valueOf(type.getName());
        switch (sourceDataType) {
        case Float:
    		Float floatValue = this.dataConverter.toFloat(property.getType(), value);
		    int intBits = Float.floatToRawIntBits(floatValue.floatValue());
		    intBits++;
		    Float floatResult = Float.valueOf(Float.intBitsToFloat(intBits));
		    result = this.dataConverter.toString(type, floatResult);
    		break;
        case Double:
    		Double doubleValue = this.dataConverter.toDouble(property.getType(), value);
		    long longBits = Double.doubleToRawLongBits(doubleValue.doubleValue());
		    longBits++;
		    Double doubleResult = Double.valueOf(Double.longBitsToDouble(longBits));
		    result = this.dataConverter.toString(type, doubleResult);
    		break;
        case Decimal:        	        	
    		BigDecimal decimalValue = this.dataConverter.toDecimal(property.getType(), value);
    		//FIXME: loss of precision
    		double temp = decimalValue.doubleValue();
		    longBits = Double.doubleToRawLongBits(temp);
		    longBits++;
		    doubleResult = Double.valueOf(Double.longBitsToDouble(longBits));
		    result = this.dataConverter.toString(type, doubleResult);
    		break;
        default:
        	throw new ScanException("expected real (Float, Double, Decinal)datatype not, "
        			+ sourceDataType.name());
        }
        return result;
	}

}
