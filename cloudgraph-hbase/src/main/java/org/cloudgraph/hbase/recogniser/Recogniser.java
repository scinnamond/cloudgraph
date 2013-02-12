package org.cloudgraph.hbase.recogniser;

import org.plasma.sdo.PlasmaType;

/**
 * 
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public interface Recogniser {
	
	/**
	 * Returns whether the given sequence represents and entity which is part
	 * of the current results according to the resulting expression 
	 * truth table. 
	 * @param sequence the sequence
	 * @return whether the given sequence represents and entity which is part
	 * of the results according to the resulting expression truth table. 
	 */
	public boolean recognise(Integer sequence);

}
