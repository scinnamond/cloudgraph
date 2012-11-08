package org.cloudgraph.hbase.scan;

/**
 * A composite partial row key scan.   
 * @author Scott Cinnamond
 * @since 0.5
 */
public interface PartialRowKeyScan {
    public byte[] getStartKey();
    public byte[] getStopKey();
}
