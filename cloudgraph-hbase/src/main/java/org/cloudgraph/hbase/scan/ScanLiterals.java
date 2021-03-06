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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.PreDefinedKeyFieldConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.Wildcard;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.RelationalOperatorValues;

/**
 * A collection of scan literals which provides various accessor methods which
 * indicate the applicability of the scan literal collection under various scan operations. Given that a query
 * may represent any number of scans or gets, clients should ensure that the collection is
 * populated with literals applicable for its query expression context. 
 * @see ScanLiteral
 * @author Scott Cinnamond
 * @since 0.5
 */
public class ScanLiterals {
    private static Log log = LogFactory.getLog(ScanLiterals.class);
	private Map<Integer, List<ScanLiteral>> literalMap = new HashMap<Integer, List<ScanLiteral>>();
	private List<ScanLiteral> literalList = new ArrayList<ScanLiteral>();
	private boolean hasWildcardLiterals = false;
	private boolean hasMultipleWildcardLiterals = false;
	private boolean hasOtherThanSingleTrailingWildcards = false;
	private boolean hasOnlyEqualityRelationalOperators = true;
	private Map<DataGraphConfig, Boolean> hasContiguousPartialKeyScanFieldValuesMap;
	private Map<DataGraphConfig, Boolean> hasContiguousKeyFieldValuesMap;

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
    	if (scanLiteral instanceof WildcardStringLiteral) {
    		if (this.hasWildcardLiterals)
    			this.hasMultipleWildcardLiterals = true;
    		this.hasWildcardLiterals = true;
    		
    		WildcardStringLiteral wildcardStringLiteral = (WildcardStringLiteral)scanLiteral;
    		String content = wildcardStringLiteral.getContent().trim();
    		if (!content.endsWith(Wildcard.WILDCARD_CHAR))
    		{
    			this.hasOtherThanSingleTrailingWildcards = true;
    		}
    		else {
    			// it has another wildcard preceding the trailing one
    			if (content.indexOf(Wildcard.WILDCARD_CHAR) < content.length()-1) {
    				this.hasOtherThanSingleTrailingWildcards = true;
    			}
    		}
    	}
    	
    	RelationalOperator oper = scanLiteral.getRelationalOperator();
	    if (oper != null) {
	    	switch (oper.getValue()) {
	    	case EQUALS:
	    		break;
	    	default:
	    		this.hasOnlyEqualityRelationalOperators	= false;
	    	}
    	}
    	
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
    	if (this.hasMultipleWildcardLiterals || this.hasOtherThanSingleTrailingWildcards)
    		return false;
    	
    	// ensure if there is a wildcard literal that its the last literal
    	// in terms of sequence within the row key definition
    	if (this.hasWildcardLiterals) {
    		int maxLiteralSeq = 0;
    		int wildcardLiteralSeq = 0;
    		for (ScanLiteral literal : literalList) {
    			if (literal.getFieldConfig().getSeqNum() > maxLiteralSeq)
    				maxLiteralSeq = literal.getFieldConfig().getSeqNum();
    			if (literal instanceof WildcardStringLiteral) {
    				if (wildcardLiteralSeq > 0)
    					log.warn("detected multiple wildcard literals - ignoring");
    				wildcardLiteralSeq = literal.getFieldConfig().getSeqNum();
    			}
    		}
    		if (wildcardLiteralSeq != maxLiteralSeq)
    			return false;
		}
    	
    	if (hasContiguousPartialKeyScanFieldValuesMap == null)
    		hasContiguousPartialKeyScanFieldValuesMap = new HashMap<DataGraphConfig, Boolean>();
    	
    	if (this.hasContiguousPartialKeyScanFieldValuesMap.get(graph) == null) {
    		boolean hasContiguousPartialKeyScanFieldValues = true;
    		
        	int size = graph.getUserDefinedRowKeyFields().size();
	    	int[] scanLiteralCount = initScanLiteralCount(graph);
	    	
	    	// If any field literal 'gap' found, i.e. if no literals found
	    	// for a field and where the next field DOES have literals
	    	for (int i = 0; i < size-1; i++)
	    		if (scanLiteralCount[i] == 0 && scanLiteralCount[i+1] > 0)
	    			hasContiguousPartialKeyScanFieldValues = false; 
	    	
	    	this.hasContiguousPartialKeyScanFieldValuesMap.put(graph, hasContiguousPartialKeyScanFieldValues);
    	}
    	
    	return this.hasContiguousPartialKeyScanFieldValuesMap.get(graph).booleanValue();
    }
 
    /**
     * Returns true if this set of literals can support
     * a partial row key scan for the given graph
     * @param graph the graph 
     * @return true if this set of literals can support
     * a partial row key scan for the given graph
     */
    public boolean supportCompleteRowKey(DataGraphConfig graph)
    {
    	if (this.hasWildcardLiterals)
    		return false;
    	
    	if (!this.hasOnlyEqualityRelationalOperators)
    		return false;

    	if (hasContiguousKeyFieldValuesMap == null)
    		hasContiguousKeyFieldValuesMap = new HashMap<DataGraphConfig, Boolean>();
    	if (this.hasContiguousKeyFieldValuesMap.get(graph) == null) {
    		boolean hasContiguousFieldValues = true;
        	
    		
        	int size = graph.getUserDefinedRowKeyFields().size();
	    	int[] scanLiteralCount = initScanLiteralCount(graph);
	    	
	    	// If any field literal 'gap' found
	    	for (int i = 0; i < size; i++)
	    		if (scanLiteralCount[i] == 0)
	    			hasContiguousFieldValues = false;  
	    	
	    	for (PreDefinedKeyFieldConfig field : graph.getPreDefinedRowKeyFields()) {
	    		switch (field.getName()) {
	    		case URI: 
	    		case TYPE:
	    			break;
	    		case UUID:
	    			// Because the UUID predefined field exists in the row key definition
	    			// and the UUID cannot be used in a query, as it is an internal value for
	    			// a data object and has no accessor/mutator per se, this makes
	    			// a complete/get operation impossible
	    			hasContiguousFieldValues = false;  
	    			break;  
	    		default:
	    		}
	        }    	
	    	
	    	this.hasContiguousKeyFieldValuesMap.put(graph, hasContiguousFieldValues);
    	}
    
    	return this.hasContiguousKeyFieldValuesMap.get(graph).booleanValue();
    }
    
    private int[] initScanLiteralCount(DataGraphConfig graph) {
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
    	
    	return scanLiteralCount;
   	
    }
	
}
