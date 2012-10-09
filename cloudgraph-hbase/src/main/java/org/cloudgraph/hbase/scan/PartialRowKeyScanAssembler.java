package org.cloudgraph.hbase.scan;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.RowKeyField;
import org.cloudgraph.config.TableConfig;
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
 * @see KeySupport
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
	protected List<ScanLiteral> literalList = new ArrayList<ScanLiteral>();
    protected int startRowFieldCount;
    protected int stopRowFieldCount;
	
	@SuppressWarnings("unused")
	private PartialRowKeyScanAssembler() {}
	
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
     * Assemble row key scan information based only on the
     * data graph root type information such as the URI
     * and type name or physical name. 
     */
	@Override
	public void assemble() {    	
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
    	assemblePredefinedFields();
	}
	
	/**
	 * Assemble row key scan information based on the given
	 * scan literals.
	 * @param literalList the scan literals
	 */
	@Override
	public void assemble(List<ScanLiteral> literalList) {
		this.literalList = literalList;
		this.startKey = ByteBuffer.allocate(bufsize);
		this.stopKey = ByteBuffer.allocate(bufsize);
    	assemblePredefinedFields();
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
    	this.literalList.addAll(literalAssembler.getLiteralList());
    	
    	if (log.isDebugEnabled())
    		log.debug("end traverse");      	

    	assemblePredefinedFields();
    	assembleLiterals();
    }
	
	private void assemblePredefinedFields()
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
	
	private void assembleLiterals()
	{
    	List<RowKeyField> preDefinedFields = this.graph.getPreDefinedRowKeyFields();
		if (preDefinedFields.size() > 0) {
		    this.startKey.put(graph.getRowKeySectionDelimiterBytes());
		    this.stopKey.put(graph.getRowKeySectionDelimiterBytes());
		}
		
		ScanLiteral[] literalArray = new ScanLiteral[literalList.size()];
		literalList.toArray(literalArray);
		Arrays.sort(literalArray, new Comparator<ScanLiteral>() {
			public int compare(ScanLiteral o1, ScanLiteral o2) {
				Integer seq1 = Integer.valueOf(
						o1.getFieldConfig().getSequenceNum());
				Integer seq2 = Integer.valueOf(
						o2.getFieldConfig().getSequenceNum());
				return seq1.compareTo(seq2);
			}
		});
		
		for (ScanLiteral literal : literalArray) {
			byte[] startBytes = literal.getStartBytes();
			if (startBytes.length > 0) {
				if (this.startRowFieldCount > 0) {
					this.startKey.put(graph.getRowKeyFieldDelimiterBytes());		
				}
				this.startKey.put(startBytes);
				this.startRowFieldCount++;
			}
			
			byte[] stopBytes = literal.getStopBytes();
			if (stopBytes.length > 0) {
				if (this.stopRowFieldCount > 0) {
					this.stopKey.put(graph.getRowKeyFieldDelimiterBytes());		
				}
				this.stopKey.put(stopBytes);
				this.stopRowFieldCount++;
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
