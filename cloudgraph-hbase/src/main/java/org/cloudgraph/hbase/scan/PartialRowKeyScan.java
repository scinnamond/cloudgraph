package org.cloudgraph.hbase.scan;

/**
 * A composite partial row key scan.   
 */
public interface PartialRowKeyScan {
    public byte[] getStartKey();
    public byte[] getStopKey();
}
