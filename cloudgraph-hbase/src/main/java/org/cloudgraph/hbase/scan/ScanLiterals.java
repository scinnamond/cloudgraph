/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.hbase.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;

/**
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ScanLiterals {
	private Map<Integer, List<ScanLiteral>> literalMap = new HashMap<Integer, List<ScanLiteral>>();
	private List<ScanLiteral> literalList = new ArrayList<ScanLiteral>();
	private boolean hasWildcards = false;
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
    	if (scanLiteral instanceof WildcardStringLiteral)
    		this.hasWildcards = true;
    	
    	UserDefinedRowKeyFieldConfig fieldConfig = scanLiteral.getFieldConfig();
		List<ScanLiteral> list = this.literalMap.get(fieldConfig.getSequenceNum());
		if (list == null) {
			list = new ArrayList<ScanLiteral>(4);
			this.literalMap.put(fieldConfig.getSequenceNum(), list);
		}
		list.add(scanLiteral);
		this.literalList.add(scanLiteral);    	
    }
    
    /**
     * Returns true if this set of literals can support
     * a partial row key scan for the given graph
     * @param graph the graph 
     * @return true if this set of literals can support
     * a partial row key scan for the given graph
     */
    public boolean supportPartialRowKeyScan(DataGraphConfig graph)
    {
    	if (this.hasWildcards)
    		return false;
    	
    	boolean hasContiguousPartialKeyScanFieldValues = true;
    	int size = graph.getUserDefinedRowKeyFields().size();
    	int[] scanLiteralCount = new int[size];
    	
    	for (int i = 0; i < size; i++) {
    		UserDefinedRowKeyFieldConfig fieldConfig = graph.getUserDefinedRowKeyFields().get(i); 
    		List<ScanLiteral> list = this.getLiterals(fieldConfig);
    		if (list != null)
    		    scanLiteralCount[i] = list.size();
    		else
    			scanLiteralCount[i] = 0; 
    	}
    	
    	for (int i = 0; i < size-1; i++)
    		if (scanLiteralCount[i] == 0 && scanLiteralCount[i+1] > 0)
    			hasContiguousPartialKeyScanFieldValues = false;    	
    
    	return hasContiguousPartialKeyScanFieldValues;
    }
	
}
