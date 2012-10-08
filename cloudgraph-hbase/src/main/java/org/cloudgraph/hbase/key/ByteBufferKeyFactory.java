package org.cloudgraph.hbase.key;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.plasma.sdo.PlasmaType;

/**
 * A configuration driven abstract class which helps subclasses 
 * leverage {@link org.cloudgraph.config.TableConfig table} and {@link org.cloudgraph.config.DataGraphConfig data graph} specific 
 * configuration information,
 * such as the hashing algorithm and field level row and column
 * model settings, as well as 
 * java <a target="#" href="http://docs.oracle.com/javase/1.5.0/docs/api/java/nio/ByteBuffer.html">ByteBuffer</a> for
 * composite row and column key creation using byte arrays.  
 * <p>
 * The initial creation and subsequent reconstitution for query retrieval
 * purposes of both row and column keys in CloudGraph&#8482; is efficient, 
 * as it leverages byte array level API in both Java and the current 
 * underlying SDO 2.1 implementation, <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a>. Both composite row and 
 * column keys are composed in part of structural metadata, and the 
 * lightweight metadata API within <a target="#" href="http://plasma-sdo.org">PlasmaSDO&#8482;</a> contains byte-array level, 
 * cached lookup of all basic metadata elements including logical and 
 * physical type and property names.  
 * </p>
 * @see org.cloudgraph.config.CloudGraphConfig
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.config.DataGraphConfig
 */
public abstract class ByteBufferKeyFactory 
    implements ConfigurableKeyFactory
{
	protected int bufsize = 4000;
	protected ByteBuffer buf = ByteBuffer.allocate(bufsize);

	protected PlasmaType rootType;
	protected TableConfig table;
	protected Hash hash;
	protected DataGraphConfig graph;
	protected Charset charset;
	protected KeySupport keySupport = new KeySupport();
	
	@SuppressWarnings("unused")
	private ByteBufferKeyFactory() {}
	
	/**
	 * Constructor which looks up table and data graph specific 
	 * configuration information for the given SDO type.
	 * @param rootType the SDO type
	 */
	protected ByteBufferKeyFactory(PlasmaType rootType) {
		this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.table = CloudGraphConfig.getInstance().getTable(rootTypeQname);
		this.graph = CloudGraphConfig.getInstance().getDataGraph(
				rootTypeQname);
		this.hash = this.keySupport.getHashAlgorithm(this.table);
		this.charset = CloudGraphConfig.getInstance().getCharset();
	}

	public TableConfig getTable() {
		return table;
	}

	public DataGraphConfig getGraph() {
		return graph;
	}

	public ByteBuffer getBuf() {
		return buf;
	}

	public PlasmaType getRootType() {
		return rootType;
	}

}
