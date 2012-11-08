package org.cloudgraph.config;

/**
 * The configuration for a column key.
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class ColumnKeyFieldConfig extends PreDefinedKeyFieldConfig {

	private ColumnKeyField columnKeyField;
	
	public ColumnKeyFieldConfig(ColumnKeyField field, int seqNum) {
		super(field, seqNum);
		this.columnKeyField = field;
	}
}
