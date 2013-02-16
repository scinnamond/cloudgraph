package org.cloudgraph.hbase.query;

import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;

/**
 * Context which supports the evaluation and 
 * "recognition" of a given data graph graph entity 
 * sequence value by a binary expression tree, within
 * the context of the expression syntax and a given
 * HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/KeyValue.html">KeyValue</a> 
 * map. 
 * <p>
 * A sequence uniquely identifies an data graph entity within a 
 * local or federated data graph and is mapped internally
 * to provide global uniqueness.   
 * </p>
 * @author Scott Cinnamond
 * @since 0.5.2
 * 
 * @see org.cloudgraph.hbase.graph.HBaseGraphAssembler
 * @see org.cloudgraph.hbase.graph.GraphSliceSupport
 */
public class RecogniserContext implements EvaluationContext {

    private Map<String, KeyValue> keyMap;
    private Integer sequence;
    
    /**
     * Constructs an empty context.
     */
    public RecogniserContext() {    	
    }
    
    /**
     * Returns the HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/KeyValue.html">KeyValue</a>
     * specific for the current sequence. 
     * @return the HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/KeyValue.html">KeyValue</a>
     * specific for the current sequence. 
     */
	public Map<String, KeyValue> getKeyMap() {
		return keyMap;
	}
	
	
    /**
     * Sets the HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/KeyValue.html">KeyValue</a>
     * specific for the current sequence. 
     */
	public void setKeyMap(Map<String, KeyValue> keyMap) {
		this.keyMap = keyMap;
	}
	
	/**
	 * Returns the current sequence. 
	 * @return the current sequence.
	 */
	public Integer getSequence() {
		return sequence;
	}
	
	/**
	 * Sets the current sequence. 
	 */
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}    
}
