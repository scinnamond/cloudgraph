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
package org.cloudgraph.hbase.expr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.filter.GraphFilterException;
import org.cloudgraph.hbase.key.CompositeColumnKeyFactory;
import org.plasma.query.model.Path;
import org.plasma.query.model.Property;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaProperty;
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
 * <p>
 * Subclasses may provide alternate implementations of {@link ExprAssembler}
 * which create binary expression tree nodes with specific evaluation
 * behavior.  
 * </p>
 *   
 * @author Scott Cinnamond
 * @since 0.5.3
 * 
 * @see Expr
 * @see CompositeColumnKeyFactory
 * 
 */
public abstract class PathPredicateBinaryExprTreeAssembler extends DefaultBinaryExprTreeAssembler 
{
    private static Log log = LogFactory.getLog(PathPredicateBinaryExprTreeAssembler.class);
	
	protected CompositeColumnKeyFactory columnKeyFactory;
	protected PlasmaType edgeType;
	
	
	/**
	 * Constructs an assembler based on the given predicate
	 * and graph edge type.  
	 * @param predicate the predicate
	 * @param edgeType the graph edge type which is the type for the
	 * reference property within the graph which represents an edge
	 * @param rootType the graph root type
	 */
	public PathPredicateBinaryExprTreeAssembler(Where predicate,
			PlasmaType edgeType, PlasmaType rootType) {
		super(predicate, rootType);
		this.edgeType = edgeType;
		this.columnKeyFactory = 
        		new CompositeColumnKeyFactory(this.rootType);

	}	
		
	/**
	 * Process the traversal end event for a query {@link org.plasma.query.model.Property property}
     * within an {@link org.plasma.query.model.Expression expression} setting up
     * context information for the endpoint property and its type, as well as
     * physical column qualifier name bytes which are set into the {@link #contextQueryProperty}
     * physical name bytes. 
     * for the current {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#end(org.plasma.query.model.Property)
	 */
	@Override
    public void end(Property property)
    {                
        org.plasma.query.model.FunctionValues function = property.getFunction();
        if (function != null)
            throw new GraphFilterException("aggregate functions only supported in subqueries not primary queries");
          
        Path path = property.getPath();
        PlasmaType targetType = (PlasmaType)this.edgeType;                
        if (path != null)
            throw new GraphFilterException("property paths not supported within path predicate expressions");
        	
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        this.contextType = targetType;
        this.contextQueryProperty = property;
        byte[] columnKey = this.columnKeyFactory.createColumnKey(this.edgeType, 
        		this.contextProperty);
        this.contextQueryProperty.setPhysicalNameBytes(columnKey);                
        
        super.start(property);
    }     
    
    
}
