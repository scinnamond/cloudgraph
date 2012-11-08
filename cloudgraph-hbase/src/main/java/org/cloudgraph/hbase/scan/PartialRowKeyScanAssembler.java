package org.cloudgraph.hbase.scan;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.KeyFieldConfig;
import org.cloudgraph.config.PreDefinedKeyFieldConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.key.KeySupport;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaType;


/**
 * Assembles a composite partial row (start/stop) key pair where each
 * field within the composite start and stop row keys are constructed 
 * based a set of query predicates.  
 * 
 * @see org.cloudgraph.config.DataGraphConfig
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.config.UserDefinedField 
 * @see org.cloudgraph.config.PredefinedField 
 * @see org.cloudgraph.config.PreDefinedFieldName 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class PartialRowKeyScanAssembler  
    implements RowKeyScanAssembler
{
    private static Log log = LogFactory.getLog(PartialRowKeyScanAssembler.class);
	protected int bufsize = 4000;
	protected ByteBuffer startKey = ByteBuffer.allocate(bufsize);
	protected ByteBuffer stopKey = ByteBuffer.allocate(bufsize);
	protected Hash hash;
	protected PlasmaType rootType;
	protected DataGraphConfig graph;
	protected TableConfig table;
	protected KeySupport keySupport = new KeySupport();
	protected Charset charset;
	protected ScanLiterals scanLiterals;
	protected int startRowFieldCount;
    protected int stopRowFieldCount;
    protected String rootUUID;
	
	@SuppressWarnings("unused")
	private PartialRowKeyScanAssembler() {}

	/**
	 * Constructor
	 * @param rootType the root type
	 */
	public PartialRowKeyScanAssembler(PlasmaType rootType)
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
	 * Constructor which enables the use of data object UUID as
	 * a pre-defined row key field. Only applicable for graph
	 * predicate "slice" queries. 
	 * @param rootType the root type
	 * @param rootUUID the root UUID.
	 */
	public PartialRowKeyScanAssembler(PlasmaType rootType, String rootUUID)
	{
		this(rootType);
		this.rootUUID = rootUUID;
	}
	
    /**
     * Assemble row key scan information based only on any
     * pre-defined row-key fields such as the
     * data graph root type or URI.
     * @see org.cloudgraph.config.PredefinedField 
     * @see org.cloudgraph.config.PreDefinedFieldName 
     */
	@Override
	public void assemble() {    	
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
    	assemblePredefinedFields();
	}
	
	/**
	 * Assemble row key scan information based on the given
	 * scan literals as well as pre-defined row-key fields such as the
     * data graph root type or URI.
	 * @param literalList the scan literals
     * @see org.cloudgraph.config.PredefinedField 
     * @see org.cloudgraph.config.PreDefinedFieldName 
	 */
	@Override
	public void assemble(ScanLiterals literals) {
		this.scanLiterals = literals;
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
    	assembleLiterals();
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
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
		
		if (log.isDebugEnabled())
    		log.debug("begin traverse");
    	
		ScanLiteralAssembler literalAssembler = 
				new ScanLiteralAssembler(this.rootType);
    	where.accept(literalAssembler); // traverse
    	
    	this.scanLiterals = literalAssembler.getResult();
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");      	

    	assembleLiterals();
    }
	
	private void assemblePredefinedFields()
	{
    	List<PreDefinedKeyFieldConfig> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
        for (int i = 0; i < preDefinedFields.size(); i++) {
        	PreDefinedKeyFieldConfig preDefinedField = preDefinedFields.get(i);
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
	
	private void assembleLiterals()
	{		
		for (KeyFieldConfig fieldConfig : this.graph.getRowKeyFields()) {
    		if (fieldConfig instanceof PreDefinedKeyFieldConfig) {
    			PreDefinedKeyFieldConfig predefinedConfig = (PreDefinedKeyFieldConfig)fieldConfig;
        		
        		byte[] tokenValue = null;
        		switch (predefinedConfig.getName()) {
        		case UUID:
        			if (this.rootUUID != null) {
        				tokenValue = this.rootUUID.getBytes(this.charset);
        				break;
        			}
        			else
        				continue;        			
        		default:	
        		    tokenValue = predefinedConfig.getKeyBytes(this.rootType);
        			break;
        		}        		
        		
        		if (fieldConfig.isHash()) {
    				int hashValue = hash.hash(tokenValue);
    				tokenValue = String.valueOf(hashValue).getBytes(charset);
    			}    			
        		if (startRowFieldCount > 0) 
            	    this.startKey.put(graph.getRowKeyFieldDelimiterBytes());
        		if (stopRowFieldCount > 0) 
            	    this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());
    			
           	    this.startKey.put(tokenValue);
           	    this.stopKey.put(tokenValue);
           	    this.startRowFieldCount++;
           	    this.stopRowFieldCount++;
    		}
    		else if (fieldConfig instanceof UserDefinedRowKeyFieldConfig) {
    			UserDefinedRowKeyFieldConfig userFieldConfig = (UserDefinedRowKeyFieldConfig)fieldConfig;
    			List<ScanLiteral> scanLiterals = this.scanLiterals.getLiterals(userFieldConfig);    				 
    			if (scanLiterals == null)
    				continue;
    			for (ScanLiteral scanLiteral : scanLiterals) {
    				byte[] startBytes = scanLiteral.getStartBytes();
    				if (startBytes.length > 0) {
    					if (this.startRowFieldCount > 0) {
    						this.startKey.put(graph.getRowKeyFieldDelimiterBytes());		
    					}
    					this.startKey.put(startBytes);
    					this.startRowFieldCount++;
    				}
    				
    				byte[] stopBytes = scanLiteral.getStopBytes();
    				if (stopBytes.length > 0) {
    					if (this.stopRowFieldCount > 0) {
    						this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());		
    					}
    					this.stopKey.put(stopBytes);
    					this.stopRowFieldCount++;
    				} 
    			}
    		}
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
}
