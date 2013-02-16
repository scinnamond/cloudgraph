package org.cloudgraph.hbase.query;

import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.config.DataGraphConfig;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.sdo.PlasmaType;

/**
 * A binary expression tree assembler which constructs an operator 
 * precedence map, then {@link org.cloudgraph.hbase.filter.ExpresionVisitorSupport visits} (traverses) 
 * the given predicate expression syntax tree depth-first 
 * using an adapted shunting-yard algorithm and assembles a 
 * resulting binary tree structure. In typical usage scenarios, a single 
 * expression tree is assembled once, and then used to evaluate any 
 * number of graph edge or other results based on a given context.
 * <p>
 * The adapted shunting-yard algorithm in general uses a stack of 
 * operators and operands, and as new binary tree nodes are detected and 
 * created they are pushed onto the operand stack based on operator precedence.
 * The resulting binary expression tree reflects the syntax of the
 * underlying query expression including the precedence of its operators.
 * </p>
 * <p>
 * The use of binary expression tree evaluation for post processing 
 * of graph edge results is necessary in columnar data stores, as an 
 * entity with multiple properties is necessarily persisted across multiple
 * columns. And while these data stores provide many useful column oriented
 * filters, the capability to select an entity based on complex criteria
 * which spans several columns is generally not supported, as such filters are
 * column oriented. Yet even for simple queries (e.g. "where entity.c1 = 'foo' 
 * and entity.c2 = 'bar'") column c1 and its value exists in one cell and
 * column c2 exists in another table cell. Since columnar data store
 * filters cannot generally span columns, both cells must be returned
 * and the results post processed within the context of the binary 
 * expression tree.      
 * </p>
 *   
 * @author Scott Cinnamond
 * @since 0.5.2
 * 
 * @see RecogniserRelationalBinaryExpr
 * @see RecogniserWildcardBinaryExpr
 * @see ExprAssembler
 */
public class RecogniserSyntaxTreeAssembler extends DefaultBinaryExprTreeAssembler 
{
	protected DataGraphConfig graphConfig;
	
	/**
	 * Constructs an assembler based on the given predicate
	 * and graph edge type.
	 * @param predicate the predicate
	 * @param edgeType the graph edge type which is the type for the
	 * reference property within the graph which represents an edge
	 * @param rootType the graph root type
	 * @param graphConfig the graph config
	 */
	public RecogniserSyntaxTreeAssembler(Where predicate,
			DataGraphConfig graphConfig, 
			PlasmaType edgeType, PlasmaType rootType) {
		super(predicate, edgeType, rootType);
		this.graphConfig = graphConfig;
	}
	
	@Override
	public RelationalBinaryExpr createRelationalBinaryExpr(Property property,
			Literal literal, RelationalOperator operator) {
		
		String qual = Bytes.toString(this.contextQueryProperty.getPhysicalNameBytes());
		String delim = this.graphConfig.getColumnKeySectionDelimiter();				
		String qualPrefix = qual + delim;
	    return new RecogniserRelationalBinaryExpr(
	    		property, qualPrefix, literal, operator);
	}
	
	@Override
	public WildcardBinaryExpr createWildcardBinaryExpr(Property property,
			Literal literal, WildcardOperator operator) {
		String qual = Bytes.toString(this.contextQueryProperty.getPhysicalNameBytes());
		String delim = this.graphConfig.getColumnKeySectionDelimiter();				
		String qualPrefix = qual + delim;
	    return new RecogniserWildcardBinaryExpr(
	    		property, qualPrefix, literal, operator);
	}
	
	@Override
	public LogicalBinaryExpr createLogicalBinaryExpr(Expr left, Expr right,
			LogicalOperator operator) {
		return new DefaultLogicalBinaryExpr(left, 
				right, operator);
	}
	
}
