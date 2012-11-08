package org.cloudgraph.hbase.scan;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.RelationalOperator;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

/**
 * Simple factory constructing data "flavor" and data type 
 * specific scan literals given various configuration specific
 * as well as predicate context specific relational and 
 * logical operators.
 * 
 * @see ScanLiteral
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ScanLiteralFactory {

	/**
	 * Creates and returns a data "flavor" and data type 
     * specific scan literal given various configuration specific
     * as well as predicate context specific relational and 
     * logical operators.
	 * @param content the literal string content
	 * @param property the context property
	 * @param rootType the graph root type
	 * @param relationalOperator the context relational operator 
	 * @param logicalOperator the context logical operator
	 * @param fieldConfig the row-key field configuration
	 * @return the data "flavor" and data type 
     * specific scan literal given various configuration specific
     * as well as predicate context specific relational and 
     * logical operators.
	 */
	public ScanLiteral createLiteral(String content,
			PlasmaProperty property, PlasmaType rootType,
			RelationalOperator relationalOperator,
			LogicalOperator logicalOperator, UserDefinedRowKeyFieldConfig fieldConfig) {

		ScanLiteral result = null;
		DataType dataType = DataType.valueOf(property.getType().getName());

		switch (property.getDataFlavor()) {
		case integral:
			result = new IntegralLiteral(content, rootType, relationalOperator,
					logicalOperator, fieldConfig);
			break;
		case string:
			result = new StringLiteral(content, rootType, relationalOperator,
					logicalOperator, fieldConfig);
			break;
		case real:
			result = new RealLiteral(content, rootType, relationalOperator,
					logicalOperator, fieldConfig);
			break;
		case temporal:
			switch (dataType) {
			case Date:
			case DateTime:
			default:	
				result = new TemporalLiteral(content, rootType, relationalOperator,
						logicalOperator, fieldConfig);
			}
			break;
		case other:
			throw new RuntimeException("data flavor not supported, '"
					+ property.getDataFlavor() + "'");
		}
		return result;
	}
}
