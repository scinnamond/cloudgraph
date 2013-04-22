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
package org.cloudgraph.common;

import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.core.CoreProperty;

/**
 * Common constants. 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class CloudGraphConstants {
    
	
	/**
	 * An SDO instance property representing the time
	 * in milliseconds taken to assemble a data graph. 
	 */
    public static final String GRAPH_ASSEMBLY_TIME = "GraphAssemblyTime";

    /**
	 * An SDO instance property representing the number
	 * of graph nodes contained by a data graph. 
	 */
    public static final String GRAPH_NODE_COUNT = "GraphNodeCount";
    
    /**
	 * An SDO instance property representing the 
	 * depth of a data graph. 
	 */
    public static final String GRAPH_DEPTH = "GraphDepth";
    
    /**
	 * An SDO instance property representing the 
	 * table names for all physical tables used to
	 * assemble a graph. 
	 */
    public static final String GRAPH_TABLE_NAMES = "GraphTableNames";
	
	/**
	 * The configuration property name for the property which 
	 * indicates the non-cryptographic hash algorithm type.
	 */
	public static final String PROPERTY_CONFIG_HASH_TYPE = "hash.type";
	 
}
