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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.expr.Expr;
import org.cloudgraph.hbase.expr.ExprVisitor;
import org.cloudgraph.hbase.expr.LogicalBinaryExpr;
import org.cloudgraph.hbase.expr.RelationalBinaryExpr;
import org.cloudgraph.hbase.expr.WildcardBinaryExpr;
import org.plasma.query.model.LogicalOperatorValues;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;

/**
 * Collector visitor which supports the "recognition" of one or more 
 * {@link PartialRowKeyScan partial}, {@link FuzzyRowKeyScan fuzzy} and other
 * scan constructs within the context of a binary 
 * {@link Expr expression} syntax tree encapsulating operator precedence
 * and other factors. 
 * <p>
 * Composite row key scans represent only {@link org.cloudgraph.hbase.expr.LogicalBinaryExpr logical binary} 'AND'
 * expressions across the key fields. So
 * for {@link org.cloudgraph.hbase.expr.RelationalBinaryExpr relational binary} expressions
 * linked within a query syntax tree by one or more logical binary 'AND', 
 * expressions, a single {@link PartialRowKeyScan partial} or {@link FuzzyRowKeyScan fuzzy}
 * row key scan may be used. But for {@link org.cloudgraph.hbase.expr.RelationalBinaryExpr relational binary} expressions
 * linked by {@link org.cloudgraph.hbase.expr.LogicalBinaryExpr logical binary} 'OR' expressions
 * multiple scans must be used. Clients of this collector class may execute the
 * resulting scans in series or in parallel depending on various performance
 * and other considerations.     
 * </p>
 * @author Scott Cinnamond
 * @since 0.5.3
 * @see org.cloudgraph.hbase.expr.Expr
 * @see org.cloudgraph.hbase.expr.BinaryExpr
 * @see org.cloudgraph.hbase.expr.ExprVisitor 
 * @see org.cloudgraph.config.DataGraphConfig 
 * @see org.cloudgraph.hbase.expr.LogicalBinaryExpr 
 * @see org.cloudgraph.hbase.expr.RelationalBinaryExpr 
 * @see org.cloudgraph.hbase.expr.WildcardBinaryExpr 
 */
public class ScanCollector implements ExprVisitor {

    private static Log log = LogFactory.getLog(ScanCollector.class);
    private List<Map<UserDefinedRowKeyFieldConfig, ScanLiteral>> literals = new ArrayList<Map<UserDefinedRowKeyFieldConfig, ScanLiteral>>();
    
	private PlasmaType rootType;
	private DataGraphConfig graph;
	private List<PartialRowKeyScan> partialKeyScans;
	private List<FuzzyRowKeyScan> fuzzyKeyScans;
	private ScanLiteralFactory factory = new ScanLiteralFactory();
	
	public ScanCollector(PlasmaType rootType) {
		this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.graph = CloudGraphConfig.getInstance().getDataGraph(
				rootTypeQname);
	}
	
	private void init() {
		if (partialKeyScans == null) {
			partialKeyScans = new ArrayList<PartialRowKeyScan>(this.literals.size());
			fuzzyKeyScans = new ArrayList<FuzzyRowKeyScan>(this.literals.size());
			for (Map<UserDefinedRowKeyFieldConfig, ScanLiteral> existing : literals) {
				ScanLiterals literalColl = new ScanLiterals();
				for (ScanLiteral literal : existing.values())
					literalColl.addLiteral(literal);
				
				if (literalColl.supportPartialRowKeyScan(this.graph)) {
				    PartialRowKeyScanAssembler assembler = new PartialRowKeyScanAssembler(this.rootType);
				    assembler.assemble(literalColl);
				    partialKeyScans.add(assembler);	
			    }
				else {
				    FuzzyRowKeyScanAssembler assembler = new FuzzyRowKeyScanAssembler(this.rootType);
				    assembler.assemble(literalColl);
				    fuzzyKeyScans.add(assembler);	
				}
			}
		}
	}
	
	public List<PartialRowKeyScan> getPartialRowKeyScans() {
		init();
		return partialKeyScans;
	}
	
	public List<FuzzyRowKeyScan> getFuzzyRowKeyScans() {
		init();
		return fuzzyKeyScans;
	}
	
	@Override
	public void visit(Expr target, Expr source, int level) {
	    if (target instanceof RelationalBinaryExpr) {
	    	RelationalBinaryExpr expr = (RelationalBinaryExpr)target;
	    	collect(expr, source);
	    }
	    else if (target instanceof WildcardBinaryExpr) {
	    	WildcardBinaryExpr expr = (WildcardBinaryExpr)target;
	    	collect(expr, source);
	    }
	}
	
	private void collect(RelationalBinaryExpr target, Expr source) {
		UserDefinedRowKeyFieldConfig fieldConfig = graph.getUserDefinedRowKeyField(target.getPropertyPath());
		if (fieldConfig == null) {
	        log.warn("no user defined row-key field for query path '"
			    	+ target.getPropertyPath() 
			    	+ "' - deferring to graph recogniser post processor");	    	
            return;			
		}
		PlasmaProperty property = (PlasmaProperty)fieldConfig.getEndpointProperty();
		
		ScanLiteral scanLiteral = factory.createLiteral(
			target.getLiteral().getValue(), property, 
			(PlasmaType)graph.getRootType(), 
			target.getOperator(), 
			fieldConfig);
		if (log.isDebugEnabled())
			log.debug("collecting path: " + target.getPropertyPath());
		collect(scanLiteral, fieldConfig, source);		
	}
	
	private void collect(WildcardBinaryExpr target, Expr source) {
		UserDefinedRowKeyFieldConfig fieldConfig = graph.getUserDefinedRowKeyField(target.getPropertyPath());
		if (fieldConfig == null) {
	        log.warn("no user defined row-key field for query path '"
			    	+ target.getPropertyPath() 
			    	+ "' - deferring to graph recogniser post processor");	    	
            return;			
		}
		PlasmaProperty property = (PlasmaProperty)fieldConfig.getEndpointProperty();
		
		ScanLiteral scanLiteral = factory.createLiteral(
			target.getLiteral().getValue(), 
			property, 
			(PlasmaType)graph.getRootType(), 
			target.getOperator(), 
			fieldConfig);
		if (log.isDebugEnabled())
			log.debug("collecting path: " + target.getPropertyPath());
		collect(scanLiteral, fieldConfig, source);		
	}
	
	private void collect(ScanLiteral scanLiteral, 
			UserDefinedRowKeyFieldConfig fieldConfig,
			Expr source) {
        if (source != null) {
			if (source instanceof LogicalBinaryExpr) {
				LogicalBinaryExpr lbe = (LogicalBinaryExpr)source;				
			    this.collect(fieldConfig, lbe,
				    scanLiteral);
			}
			else
				throw new IllegalOperatorMappingException("expected logical binary expression parent not, "
						+ source.getClass().getName());
        }
        else {
		    this.collect(fieldConfig, null,
				    scanLiteral);
        }		
	}
		
	private void collect (UserDefinedRowKeyFieldConfig fieldConfig,
			LogicalBinaryExpr source,
			ScanLiteral scanLiteral)
	{
		if (this.literals.size() == 0) {
			Map<UserDefinedRowKeyFieldConfig, ScanLiteral> map = new HashMap<UserDefinedRowKeyFieldConfig, ScanLiteral>();
			map.put(fieldConfig, scanLiteral);
			this.literals.add(map);
		}
		else if (this.literals.size() > 0) {
			boolean foundField = false;
			
			for (Map<UserDefinedRowKeyFieldConfig, ScanLiteral> existing : literals) {
				if (source == null || source.getOperator().getValue().ordinal() == LogicalOperatorValues.AND.ordinal()) {							
					if (existing.get(fieldConfig) == null) {
						existing.put(fieldConfig, scanLiteral);
					}
					else {
						throw new IllegalOperatorMappingException("logical operator '" 
					        + LogicalOperatorValues.AND + "' mapped multiple times "
					        + "to row key field property, "
					        + fieldConfig.getEndpointProperty().getContainingType().toString() 
					        + "." + fieldConfig.getEndpointProperty().getName());
					}	
				}
				else if (source.getOperator().getValue().ordinal() == LogicalOperatorValues.OR.ordinal()) {
					if (existing.get(fieldConfig) == null) {
						if (foundField)
							throw new IllegalStateException("expected for key field mapped to scans "
					        + "for row key field property, "
					        + fieldConfig.getEndpointProperty().getContainingType().toString() 
					        + "." + fieldConfig.getEndpointProperty().getName());

						existing.put(fieldConfig, scanLiteral);
					}
					else {
						foundField = true;
					}							
				}
				else {
					log.warn("unsuported logical operator, " 
				        + source.getOperator().getValue() + " - ignoring");
				}			
			}
			
			if (foundField) {
			    // duplicate any map with new literal
			    Map<UserDefinedRowKeyFieldConfig, ScanLiteral> next = newMap(literals.get(0),
				    fieldConfig, scanLiteral);
			    literals.add(next);
			}
		}
	}
	
	/**
	 * Duplicates the given map except with the given literal
	 * replacing the mapping for the given field configuration
	 * @param existing the existing source map
	 * @param fieldConfig the fiend configuration
	 * @param scanLiteral the literal
	 * @return the new map
	 */
	private Map<UserDefinedRowKeyFieldConfig, ScanLiteral> newMap(
			Map<UserDefinedRowKeyFieldConfig, ScanLiteral> existing,
			UserDefinedRowKeyFieldConfig fieldConfig,
			ScanLiteral scanLiteral) {
		Map<UserDefinedRowKeyFieldConfig, ScanLiteral> next = new HashMap<UserDefinedRowKeyFieldConfig, ScanLiteral>();
		for (UserDefinedRowKeyFieldConfig config : existing.keySet()) {
			if (!config.equals(fieldConfig)) {
				next.put(config, existing.get(config));
			}
			else {
				next.put(fieldConfig, scanLiteral);
			}
		}					
		return next;
	}

}
