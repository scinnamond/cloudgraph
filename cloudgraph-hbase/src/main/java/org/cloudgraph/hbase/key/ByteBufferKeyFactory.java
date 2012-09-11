package org.cloudgraph.hbase.key;

import java.nio.ByteBuffer;

import javax.xml.namespace.QName;

import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.plasma.sdo.PlasmaType;

/**
 * A configuration driven abstract class which helps subclasses 
 * leverage table and data graph specific configuration information,
 * such as the hashing algorithm and field level row and column
 * model settings, as well as 
 * java <a target="#" href="http://docs.oracle.com/javase/1.5.0/docs/api/java/nio/ByteBuffer.html">ByteBuffer</a> for
 * composite row and column key creation using byte arrays.  
 * 
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
		this.hash = table.getHashAlgorithm();
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
