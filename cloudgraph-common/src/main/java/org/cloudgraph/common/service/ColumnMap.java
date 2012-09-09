package org.cloudgraph.common.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Local column qualifier/value map which is mutable such that
 * column results can be incrementally added. 
 */
public class ColumnMap {
	
	// Note: byte[] uses object identity for equals and hashCode
	// so can't be an effective map key
	private Map<String, byte[]> map;
	
    @SuppressWarnings("unused")
	private ColumnMap() {}
    
    public ColumnMap(Result row) {
		// FIXME: take mapping by column family 
		// into account
		this.map = new HashMap<String, byte[]>();
	  	for (KeyValue keyValue : row.list()) {
	  	    map.put(Bytes.toString(keyValue.getQualifier()), 
	  	    	keyValue.getValue());
	  	}
    }
    
    public void clear() {
    	this.map.clear();
    }
    
    public void addColumn(KeyValue keyValue) {
    	this.map.put(Bytes.toString(keyValue.getQualifier()), 
    		keyValue.getValue());
    }
    
    public void addColumn(byte[] family, byte[] qual, byte[] value) {
    	this.map.put(Bytes.toString(qual), value);
    }
    
    public boolean containsColumn(byte[] family, byte[] qual) {
    	return this.map.containsKey(Bytes.toString(qual));
    }
    
    public byte[] getColumnValue(byte[] family, byte[] qual) {
    	return this.map.get(Bytes.toString(qual));
    }
    
    public String toString() {
    	StringBuilder buf = new StringBuilder();
    	buf.append("map: ");
    	Iterator<String> iter = this.map.keySet().iterator();
    	for (int i = 0; iter.hasNext(); i++) {
    		buf.append("\n\t");
    		String key = iter.next();
    		byte[] value = this.map.get(key);
    		buf.append(key);
    		buf.append("\t");
    		buf.append(Bytes.toString(value));
    	}
    	return buf.toString();
    }
}
