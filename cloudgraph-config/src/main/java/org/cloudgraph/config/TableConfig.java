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
package org.cloudgraph.config;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.plasma.sdo.core.CoreConstants;

/**
 * Encapsulates logic related to access of HTable specific
 * configuration information.  
 * @author Scott Cinnamond
 * @since 0.5
 */
public class TableConfig {
    private Table table;
    private CloudGraphConfig config;
    private Charset charset;
    private Map<String, Property> propertyNameToPropertyMap = new HashMap<String, Property>();
    
    @SuppressWarnings("unused")
	private TableConfig() {}
    
	public TableConfig(Table table, CloudGraphConfig config) {
		super();
		this.table = table;
		this.config = config;
		this.charset = Charset.forName( CoreConstants.UTF8_ENCODING );
        for (Property prop : table.getProperties())
        	propertyNameToPropertyMap.put(prop.getName(), prop);
		
	}

	/**
	 * Returns the configuration for the HTable.
	 * @return the configuration for the HTable.
	 */
	public Table getTable() {
		return table;
	}
	
	/**
	 * Returns the table name for this table configuration.
	 * @return the table name for this table configuration.
	 */
	public String getName() {
		return this.table.getName();
	}
	
	public String getDataColumnFamilyName() {
		return this.table.getDataColumnFamilyName();
	}
	
	public byte[] getDataColumnFamilyNameBytes() {
		return this.table.getDataColumnFamilyName().getBytes(
				this.charset);
	}	
	
	/**
	 * Returns true if the table has a specific hash algorithm 
	 * configured.
	 * @return true if the table has a specific hash algorithm 
	 * configured.
	 */
	public boolean hasHashAlgorithm() {
		return this.getTable().getHashAlgorithm() != null;
	}

	public Charset getCharset() {
		return charset;
	}
	
	public List<Property> getProperties() {
	    return table.properties;
	} 
	    
	public Property findProperty(String name) {
	    return this.propertyNameToPropertyMap.get(name);
	}	
	
	private Boolean uniqueChecksVar = null;
	public boolean uniqueChecks() {
		if (uniqueChecksVar == null) {
			String value = System.getProperty(ConfigurationProperty.CLOUDGRAPH___UNIQUE___CHECKS.value());
			if (value != null) {
				this.uniqueChecksVar = Boolean.valueOf(value);
			}
			else {		
				if (!this.table.isUniqueChecks()) { // default (true) overridden
				    this.uniqueChecksVar = Boolean.valueOf(this.table.uniqueChecks);	
				}
				else { // check for table level generic prop
					Property tableProp = this.findProperty(ConfigurationProperty.CLOUDGRAPH___UNIQUE___CHECKS.value());
					if (tableProp != null) {
						this.uniqueChecksVar = Boolean.valueOf(tableProp.getValue());
					}
					else { // check for global config prop
						Property globalProp = this.config.findProperty(ConfigurationProperty.CLOUDGRAPH___UNIQUE___CHECKS.value()); 
						if (globalProp != null) {
							this.uniqueChecksVar = Boolean.valueOf(globalProp.getValue());
						}
						else { // otherwise use schema default
							this.uniqueChecksVar = Boolean.valueOf(this.table.isUniqueChecks());
						}
					}
				}
			}
		}
		return this.uniqueChecksVar.booleanValue();
	}

 }