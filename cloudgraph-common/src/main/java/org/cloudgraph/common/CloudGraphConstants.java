package org.cloudgraph.common;

import org.apache.hadoop.hbase.util.Bytes;

public class CloudGraphConstants {
	public static final String DATA_TABLE_FAMILY_1 = "f1";
    public static final String DATA_TABLE_FAMILY_2 = "f2";

    public static final byte[] DATA_TABLE_FAMILY_1_BYTES = Bytes.toBytes(DATA_TABLE_FAMILY_1);
    public static final byte[] DATA_TABLE_FAMILY_2_BYTES = Bytes.toBytes(DATA_TABLE_FAMILY_2);;
    
    public static final String ROOT_UUID_COLUMN_NAME = "root";

    
    public static final String PROPERTY_HBASE_CONFIG_HASH_TYPE = "hbase.hash.type";
}
