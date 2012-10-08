package org.cloudgraph.hbase.scan;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.RowKeyField;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.config.UserDefinedFieldConfig;
import org.cloudgraph.hbase.key.KeySupport;
import org.cloudgraph.hbase.service.HBaseDataConverter;
import org.plasma.query.model.AbstractPathElement;
import org.plasma.query.model.GroupOperator;
import org.plasma.query.model.Literal;
import org.plasma.query.model.LogicalOperator;
import org.plasma.query.model.NullLiteral;
import org.plasma.query.model.Path;
import org.plasma.query.model.PathElement;
import org.plasma.query.model.Property;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Where;
import org.plasma.query.model.WildcardOperator;
import org.plasma.query.model.WildcardPathElement;
import org.plasma.query.visitor.DefaultQueryVisitor;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.helper.DataConverter;


public class CompositeRowKeyScanAssembler extends DefaultQueryVisitor 
    implements RowKeyScanAssembler
{
    private static Log log = LogFactory.getLog(CompositeRowKeyScanAssembler.class);
	protected int bufsize = 4000;
	protected ByteBuffer startKey = ByteBuffer.allocate(bufsize);
	protected ByteBuffer stopKey = ByteBuffer.allocate(bufsize);
	protected Hash hash;
	protected PlasmaType rootType;
	protected PlasmaType contextType;
	protected PlasmaProperty contextProperty;
	protected String contextPropertyPath;
	protected DataGraphConfig graph;
	protected TableConfig table;
	protected RelationalOperator contextRelationalOperator;
	protected LogicalOperator contextLogicalOperator;
	protected KeySupport keySupport = new KeySupport();
	protected Charset charset;
	protected DataConverter dataConverter = DataConverter.INSTANCE;
	protected List<LiteralContext> literalList = new ArrayList<LiteralContext>();
    protected int startRowFieldCount;
    protected int stopRowFieldCount;
	
	@SuppressWarnings("unused")
	private CompositeRowKeyScanAssembler() {}
	
	public CompositeRowKeyScanAssembler(PlasmaType rootType)
	{
    	this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.graph = CloudGraphConfig.getInstance().getDataGraph(
				rootTypeQname);
		this.table = CloudGraphConfig.getInstance().getTable(rootTypeQname);
		this.hash = this.keySupport.getHashAlgorithm(this.table);
		this.charset = CloudGraphConfig.getInstance().getCharset();
	}
	
    /**
     * Assemble row key scan information based on one or more
     * given query predicates.
     * @param where the row predicate hierarchy
     * @param contextType the context type which may be the root type or another
     * type linked by one or more relations to the root
     */
	@Override
	public void assemble(Where where, PlasmaType contextType) {
    	
		this.contextType = contextType;
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
		
		if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
    	where.accept(this); // traverse
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");      	

    	appendPredefinedFields();

    	List<RowKeyField> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
		if (preDefinedFields.size() > 0) {
		    this.startKey.put(graph.getRowKeySectionDelimiterBytes());
		    this.stopKey.put(graph.getRowKeySectionDelimiterBytes());
		}
		
		LiteralContext[] literalArray = new LiteralContext[literalList.size()];
		literalList.toArray(literalArray);
		Arrays.sort(literalArray, new Comparator<LiteralContext>() {
			public int compare(LiteralContext o1, LiteralContext o2) {
				Integer seq1 = Integer.valueOf(
						o1.fieldConfig.getSequenceNum());
				Integer seq2 = Integer.valueOf(
						o2.fieldConfig.getSequenceNum());
				return seq1.compareTo(seq2);
			}
		});
		
		for (LiteralContext literal : literalArray) {
			PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
			addLiteral(literal, property.getDataFlavor());
		}
    }

	private void addLiteral(LiteralContext literal, DataFlavor dataFlavor) {
		switch (dataFlavor) {
		case integral:
			switch (literal.getRelationalOperator().getValue()) {
			case EQUALS:
				appendIntegralEquals(literal);
				break;
			case GREATER_THAN:
				appendIntegralGreaterThan(literal);
				break;
			case GREATER_THAN_EQUALS:
				appendIntegralGreaterThanEqual(literal);
				break;
			case LESS_THAN:
				appendIntegralLessThan(literal);
				break;
			case LESS_THAN_EQUALS:
				appendIntegralLessThanEqual(literal);
				break;
			case NOT_EQUALS:
			default:
				throw new GraphServiceException("unknown relational operator '"
						+ literal.getRelationalOperator().getValue().toString() + "'");
			}
			break;
		case string:
			switch (literal.getRelationalOperator().getValue()) {
			case EQUALS:
				addStringEquals(literal);
				break;
			case NOT_EQUALS:
			case GREATER_THAN:
			case GREATER_THAN_EQUALS:
			case LESS_THAN:
			case LESS_THAN_EQUALS:
				break;
			default:
				throw new GraphServiceException("unknown relational operator '"
						+ literal.getRelationalOperator().getValue().toString() + "'");
			}
			break;
		case real:
			switch (literal.getRelationalOperator().getValue()) {
			case EQUALS:
			case NOT_EQUALS:
			case GREATER_THAN:
			case GREATER_THAN_EQUALS:
			case LESS_THAN:
			case LESS_THAN_EQUALS:
				break;
			default:
				throw new GraphServiceException("unknown relational operator '"
						+ literal.getRelationalOperator().getValue().toString() + "'");
			}
			break;
		case temporal:
			switch (literal.getRelationalOperator().getValue()) {
			case EQUALS:
			case NOT_EQUALS:
			case GREATER_THAN:
			case GREATER_THAN_EQUALS:
			case LESS_THAN:
			case LESS_THAN_EQUALS:
				break;
			default:
				throw new GraphServiceException("unknown relational operator '"
						+ literal.getRelationalOperator().getValue().toString() + "'");
			}
			break;
		case other:	
			throw new RuntimeException("data flavor not supported, "
					+ dataFlavor);
		}
	}
	
	private void appendIntegralEquals(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] startBytes = null;
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), literal.getLiteral());
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		Long stopValue = startValue + 1;
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
		if (literal.getFieldConfig().isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
			
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		if (this.startRowFieldCount > 0) {
			this.startKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.startKey.put(startBytes);
		this.startRowFieldCount++;
		
		if (this.stopRowFieldCount > 0) {
			this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.stopKey.put(stopBytes);
		this.stopRowFieldCount++;
	}
	
	private void appendIntegralGreaterThan(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), literal.getLiteral());
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		startValue = startValue + 1;
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		if (literal.getFieldConfig().isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
		}
		if (this.startRowFieldCount > 0) {
			this.startKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.startKey.put(startBytes);
		this.startRowFieldCount++;
	}
	
	private void appendIntegralGreaterThanEqual(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] startBytes = null;
		Object value = this.dataConverter.convert(property.getType(), literal.getLiteral());
		Long startValue = this.dataConverter.toLong(property.getType(), value);
		String startValueStr = this.dataConverter.toString(property.getType(), startValue);
		if (literal.getFieldConfig().isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = startValueStr.getBytes(this.charset);
		}
		else {
			startBytes = startValueStr.getBytes(this.charset);
		}
		if (this.startRowFieldCount > 0) {
			this.startKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.startKey.put(startBytes);
		this.startRowFieldCount++;
	}

	private void appendIntegralLessThan(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), literal.getLiteral());
		Long stopValue = this.dataConverter.toLong(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so just use
		// the literal value, no need to decrement it
		String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
		if (literal.getFieldConfig().isHash()) {
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		if (this.stopRowFieldCount > 0) {
			this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.stopKey.put(stopBytes);
		this.stopRowFieldCount++;
	}	

	private void appendIntegralLessThanEqual(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] stopBytes = null;
		Object value = this.dataConverter.convert(property.getType(), literal.getLiteral());
		Long stopValue = this.dataConverter.toLong(property.getType(), value);
		// Note: in HBase the stop row is exclusive, so increment
		// stop value to get this row for this field/literal
		stopValue++;
		String stopValueStr = this.dataConverter.toString(property.getType(), stopValue);
		if (literal.getFieldConfig().isHash()) {
			int stopHashValue = hash.hash(stopValueStr.getBytes());
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		else {
			stopBytes = stopValueStr.getBytes(this.charset);
		}
		if (this.stopRowFieldCount > 0) {
			this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());		
		}
		this.stopKey.put(stopBytes);
		this.stopRowFieldCount++;
	}	
	
	private void addStringEquals(LiteralContext literal) {
		PlasmaProperty property = (PlasmaProperty)literal.getFieldConfig().getEndpointProperty();
		byte[] startBytes = null;
		byte[] stopBytes = null;
		String startValueStr = literal.getLiteral();
		String stopValueStr = startValueStr + "A"; // FIXME: 
		if (literal.getFieldConfig().isHash()) {
			int startHashValue = hash.hash(startValueStr.getBytes());
			startValueStr = String.valueOf(startHashValue);
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
			
			int stopHashValue = hash.hash(startValueStr.getBytes());
			stopValueStr = String.valueOf(stopHashValue);
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, stopValueStr);
		}
		else {
			startBytes = HBaseDataConverter.INSTANCE.toBytes(property, startValueStr);
			stopBytes = HBaseDataConverter.INSTANCE.toBytes(property, stopValueStr);
		}
		this.startKey.put(startBytes);
		this.stopKey.put(stopBytes);
	}
	
	private void appendPredefinedFields()
	{
    	List<RowKeyField> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	RowKeyField preDefinedField = preDefinedFields.get(i);
    		if (i > 0) {
        	    this.startKey.put(graph.getRowKeyFieldDelimiterBytes());
        	    this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());
    		}
    		byte[] tokenValue = this.keySupport.getPredefinedFieldValueBytes(this.rootType, 
       	    		hash, preDefinedField);
       	    this.startKey.put(tokenValue);
       	    this.stopKey.put(tokenValue);
        }				
	}
	
	/**
	 * Returns the start row key as a byte array.
	 * @return the start row key
	 * @throws IllegalStateException if row keys are not yet assembled
	 */
	@Override
	public byte[] getStartKey() {
		if (this.startKey == null)
			throw new IllegalStateException("row keys not assembled - first call assemble(...)");
		// ByteBuffer.array() returns unsized array so don't sent that back to clients
		// to misuse. 
		// Use native arraycopy() method as it uses native memcopy to create result array
		// and because 
		// ByteBuffer.get(byte[] dst,int offset, int length) is not native
	    byte [] result = new byte[this.startKey.position()];
	    System.arraycopy(this.startKey.array(), this.startKey.arrayOffset(), result, 0, this.startKey.position()); 
		return result;
	}

	/**
	 * Returns the stop row key as a byte array.
	 * @return the stop row key
	 * @throws IllegalStateException if row keys are not yet assembled
	 */
	@Override
	public byte[] getStopKey() {
		if (this.stopKey == null)
			throw new IllegalStateException("row keys not assembled - first call assemble(...)");
	    byte [] result = new byte[this.stopKey.position()];
	    System.arraycopy(this.stopKey.array(), this.stopKey.arrayOffset(), result, 0, this.stopKey.position()); 
		return result;
	}

	/**
	 * Process the traversal start event for a query {@link org.plasma.query.model.Property property}
     * within an {@link org.plasma.query.model.Expression expression} just
     * traversing the property path if exists and capturing context information
     * for the current {@link org.plasma.query.model.Expression expression}.  
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#start(org.plasma.query.model.Property)
	 */
	@Override
    public void start(Property property)
    {                
        org.plasma.query.model.FunctionValues function = property.getFunction();
        if (function != null)
            throw new GraphServiceException("aggregate functions only supported in subqueries not primary queries");
          
        Path path = property.getPath();
        PlasmaType targetType = (PlasmaType)this.rootType;                
        if (path != null)
        {
            for (int i = 0 ; i < path.getPathNodes().size(); i++)
            {    
            	AbstractPathElement pathElem = path.getPathNodes().get(i).getPathElement();
                if (pathElem instanceof WildcardPathElement)
                    throw new GraphServiceException("wildcard path elements applicable for 'Select' clause paths only, not 'Where' clause paths");
                String elem = ((PathElement)pathElem).getValue();
                PlasmaProperty prop = (PlasmaProperty)targetType.getProperty(elem);                
                targetType = (PlasmaType)prop.getType(); // traverse
            }
        }
        PlasmaProperty endpointProp = (PlasmaProperty)targetType.getProperty(property.getName());
        this.contextProperty = endpointProp;
        this.contextType = targetType;
        this.contextPropertyPath = property.asPathString();
        
        super.start(property);
    }     

	public void start(WildcardOperator operator) {
		switch (operator.getValue()) {
		default:
			throw new GraphServiceException("unsupported operator '"
					+ operator.getValue().toString() + "'");
		}
	}	
	
    /**
     * Process the traversal start event for a query {@link org.plasma.query.model.Literal literal}
     * within an {@link org.plasma.query.model.Expression expression}.
     * @param literal the expression literal
     * @throws GraphServiceException if no user defined row-key token
     * is configured for the current literal context.
     */
	@Override
	public void start(Literal literal) {
		String content = literal.getValue();
		if (this.contextProperty == null)
			throw new IllegalStateException("expected context property for literal");
		if (this.contextType == null)
			throw new IllegalStateException("expected context type for literal");
		if (this.rootType == null)
			throw new IllegalStateException("expected context type for literal");

		// Match the current property to a user defined 
		// row key token, if found we can process
		UserDefinedFieldConfig fieldConfig = this.graph.getUserDefinedRowKeyField(this.contextPropertyPath);
		if (fieldConfig != null) 
		{
			LiteralContext context = new LiteralContext(content,
					this.contextRelationalOperator,
					this.contextLogicalOperator,
					fieldConfig);
			literalList.add(context);
		}
		else
	        throw new GraphServiceException("no user defined row-key field for query path '"
			    	+ this.contextPropertyPath + "'");
		
		super.start(literal);
	}

	/**
	 * (non-Javadoc)
	 * @see org.plasma.query.visitor.DefaultQueryVisitor#start(org.plasma.query.model.NullLiteral)
	 */
	@Override
	public void start(NullLiteral nullLiteral) {
        throw new GraphServiceException("null literals for row scans not yet supported");
	}
	
	/**
	 * Process a {@link org.plasma.query.model.LogicalOperator logical operator} query traversal
	 * start event. 
	 */
	public void start(LogicalOperator operator) {
		
		switch (operator.getValue()) {
		case AND:
		case OR:	
			this.contextLogicalOperator = operator;
		}
		super.start(operator);
	}
    
	public void start(RelationalOperator operator) {
		switch (operator.getValue()) {
		case EQUALS:
		case NOT_EQUALS:
		case GREATER_THAN:
		case GREATER_THAN_EQUALS:
		case LESS_THAN:
		case LESS_THAN_EQUALS:
			this.contextRelationalOperator = operator;
			break;
		default:
			throw new GraphServiceException("unknown relational operator '"
					+ operator.getValue().toString() + "'");
		}
		super.start(operator);
	}
	
	public void start(GroupOperator operator) {
		switch (operator.getValue()) {
		case RP_1:  		
	        break;
		case RP_2:  		
		    break;
		case RP_3:  			
			break;
		case LP_1:  
			break;
		case LP_2:  			
			break;
		case LP_3:  
			break;
		default:
			throw new GraphServiceException("unsupported group operator, "
						+ operator.getValue().name());
		}
		super.start(operator);
	}
	
	class LiteralContext {
		private String literal;
		private RelationalOperator relationalOperator;
		private LogicalOperator logicalOperator;
		private UserDefinedFieldConfig fieldConfig;
		
		@SuppressWarnings("unused")
		private LiteralContext() {}

		public LiteralContext(String literal,
				RelationalOperator relationalOperator,
				LogicalOperator logicalOperator,
				UserDefinedFieldConfig fieldConfig) {
			super();
			this.literal = literal;
			this.relationalOperator = relationalOperator;
			this.logicalOperator = logicalOperator;
			this.fieldConfig = fieldConfig;
		}

		public String getLiteral() {
			return literal;
		}

		public RelationalOperator getRelationalOperator() {
			return relationalOperator;
		}

		public LogicalOperator getLogicalOperator() {
			return logicalOperator;
		}

		public UserDefinedFieldConfig getFieldConfig() {
			return fieldConfig;
		}
		
		
	}
}
