package org.cloudgraph.config;

import java.nio.charset.Charset;

import org.plasma.sdo.core.CoreConstants;

import commonj.sdo.DataObject;

/**
 * The configuration for a row or column key.
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class KeyFieldConfig {
    protected int sequenceNum;
    protected Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );
    private KeyField field;
    
    @SuppressWarnings("unused")
	private KeyFieldConfig() {}
    
 	public KeyFieldConfig(KeyField field, int sequenceNum) {
		super();
		this.field = field;
		this.sequenceNum = sequenceNum;
	}

	public int getSeqNum() {
		return sequenceNum;
	}
	
	public boolean isHash() {
		return this.field.isHash();
	}
	
	public abstract byte[] getKeyBytes(
			commonj.sdo.DataGraph dataGraph);

	/**
	 * Returns a key value from the given data object
	 * @param dataObject the root data object 
	 * @return the key value
	 */
	public abstract byte[] getKeyBytes(
			DataObject dataObject);
}
