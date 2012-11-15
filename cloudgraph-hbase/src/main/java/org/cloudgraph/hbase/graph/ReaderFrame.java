package org.cloudgraph.hbase.graph;

import org.cloudgraph.hbase.io.RowReader;

/**
 * Associates a row reader with a given graph 
 * (traversal) level. For use in detecting a target 
 * row reader based on its level. 
 * 
 * @see org.cloudgraph.hbase.io.RowReader
 * 
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
