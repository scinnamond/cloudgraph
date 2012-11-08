package org.cloudgraph.hbase.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ScanLiterals {
	private Map<Integer, List<ScanLiteral>> literalMap = new HashMap<Integer, List<ScanLiteral>>();
	private List<ScanLiteral> literalList = new ArrayList<ScanLiteral>();
	public ScanLiterals() {}
	
    public List<ScanLiteral> getLiterals() {    	
		return literalList;
	}
    
    public List<ScanLiteral> getLiterals(
    		UserDefinedRowKeyFieldConfig fieldConfig) {
		return literalMap.get(fieldConfig.getSequenceNum());
	}
	
    public int size() {
    	return this.literalList.size();
    }
    
    public void addLiteral(ScanLiteral scanLiteral) {
    	UserDefinedRowKeyFieldConfig fieldConfig = scanLiteral.getFieldConfig();
		List<ScanLiteral> list = this.literalMap.get(fieldConfig.getSequenceNum());
		if (list == null) {
			list = new ArrayList<ScanLiteral>(4);
			this.literalMap.put(fieldConfig.getSequenceNum(), list);
		}
		list.add(scanLiteral);
		this.literalList.add(scanLiteral);    	
    }
	
}
