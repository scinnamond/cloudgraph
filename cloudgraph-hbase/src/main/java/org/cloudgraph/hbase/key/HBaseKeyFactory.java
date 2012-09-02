package org.cloudgraph.hbase.key;

import java.nio.ByteBuffer;

import javax.xml.namespace.QName;

import org.apache.hadoop.hbase.util.Hash;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.plasma.sdo.PlasmaType;

public abstract class HBaseKeyFactory {
	protected int bufsize = 4000;
	protected ByteBuffer buf = ByteBuffer.allocate(bufsize);

	protected PlasmaType rootType;
	protected TableConfig table;
	protected Hash hash;
	protected DataGraphConfig graph;
	
	@SuppressWarnings("unused")
	private HBaseKeyFactory() {}
	
	protected HBaseKeyFactory(PlasmaType rootType) {
		this.rootType = rootType;
		QName rootTypeQname = this.rootType.getQualifiedName();
		this.table = CloudGraphConfig.getInstance().getHTable(rootTypeQname);
		this.graph = CloudGraphConfig.getInstance().getCloudGraph(
				rootTypeQname);
		this.hash = table.getHashAlgorithm();
	}

}
