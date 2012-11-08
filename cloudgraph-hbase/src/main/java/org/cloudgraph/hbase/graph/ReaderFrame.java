package org.cloudgraph.hbase.graph;

import org.cloudgraph.hbase.io.RowReader;

/**
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class ReaderFrame {
	private RowReader rowReader;
	private int level;
	public ReaderFrame(RowReader rowReader, int level) {
		super();
		this.rowReader = rowReader;
		this.level = level;
	}
	public RowReader getRowReader() {
		return rowReader;
	}
	public int getLevel() {
		return level;
	}		
}
