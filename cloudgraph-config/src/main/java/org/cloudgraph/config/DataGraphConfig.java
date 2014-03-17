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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.Type;

/**
 * Encapsulates logic related to access of graph specific
 * configuration information.  
 * @author Scott Cinnamond
 * @since 0.5
 */
public class DataGraphConfig {
    private DataGraph graph;
    private TableConfig table;
    private Map<PreDefinedFieldName, PreDefinedKeyFieldConfig> preDefinedRowKeyFieldMap = new HashMap<PreDefinedFieldName, PreDefinedKeyFieldConfig>(); 
    private List<PreDefinedKeyFieldConfig> preDefinedRowKeyFieldList = new ArrayList<PreDefinedKeyFieldConfig>(); 
    private Map<PreDefinedFieldName, ColumnKeyFieldConfig> preDefinedColumnKeyFieldMap = new HashMap<PreDefinedFieldName, ColumnKeyFieldConfig>(); 
	private List<UserDefinedRowKeyFieldConfig> userDefinedRowKeyFieldList = new ArrayList<UserDefinedRowKeyFieldConfig>();
	private Map<String, UserDefinedRowKeyFieldConfig> pathToUserDefinedRowKeyMap = new HashMap<String, UserDefinedRowKeyFieldConfig>();
	private Map<commonj.sdo.Property, UserDefinedRowKeyFieldConfig> propertyToUserDefinedRowKeyMap = new HashMap<commonj.sdo.Property, UserDefinedRowKeyFieldConfig>();
    private List<KeyFieldConfig> rowKeyFieldList = new ArrayList<KeyFieldConfig>(); 
    private List<KeyFieldConfig> columnKeyFieldList = new ArrayList<KeyFieldConfig>(); 
    private Map<String, Property> propertyNameToPropertyMap = new HashMap<String, Property>();

	
	private byte[] rowKeyFieldDelimiterBytes;
	private byte[] columnKeyFieldDelimiterBytes;
	private byte[] columnKeySectionDelimiterBytes;
    
    @SuppressWarnings("unused")
	private DataGraphConfig() {}
	public DataGraphConfig(DataGraph graph, TableConfig table) {
		super();
		this.graph = graph;
		this.table = table;
        for (Property prop : graph.getProperties())
        	propertyNameToPropertyMap.put(prop.getName(), prop);
		
        
        int totalRowKeyFields = this.graph.getRowKeyModel().getRowKeyFields().size();
		int seqNum = 1;
		for (RowKeyField rowKeyField : this.graph.getRowKeyModel().getRowKeyFields()) {
			if (rowKeyField.getPredefinedField() != null) {
				PredefinedField predefinedField = rowKeyField.getPredefinedField();
				PreDefinedKeyFieldConfig predefinedFieldConfig = new PreDefinedKeyFieldConfig(predefinedField, seqNum, totalRowKeyFields);
			    preDefinedRowKeyFieldMap.put(predefinedField.getName(), predefinedFieldConfig);
			    preDefinedRowKeyFieldList.add(predefinedFieldConfig);
			    this.rowKeyFieldList.add(predefinedFieldConfig);
			}
			else if (rowKeyField.getUserDefinedField() != null) {
				UserDefinedField userField = rowKeyField.getUserDefinedField();
		    	UserDefinedRowKeyFieldConfig userFieldConfig = new UserDefinedRowKeyFieldConfig(this, userField, seqNum, totalRowKeyFields);
		    	userDefinedRowKeyFieldList.add(userFieldConfig);
		    	if (this.pathToUserDefinedRowKeyMap.get(userFieldConfig.getPropertyPath()) != null) 
		    		throw new CloudGraphConfigurationException("a user defined token path '" 
		    				+ userFieldConfig.getPathExpression()
		    				+ "' already exists with property path '"
		    				+ userFieldConfig.getPropertyPath()
		    				+ "' for data graph of type, "
		    				+ this.graph.getUri() + "#" + this.graph.getType());
		    	this.pathToUserDefinedRowKeyMap.put(userFieldConfig.getPropertyPath(), userFieldConfig);
		    	this.propertyToUserDefinedRowKeyMap.put(userFieldConfig.getEndpointProperty(), userFieldConfig);
			    this.rowKeyFieldList.add(userFieldConfig);
			}
			else
				throw new CloudGraphConfigurationException("unexpected row key model field instance, "
						+ rowKeyField.getClass().getName());
			seqNum++;
		}
		
        int totalColumnKeyFields = this.graph.getColumnKeyModel().getColumnKeyFields().size();
		seqNum = 1;
		for (ColumnKeyField ctoken : this.graph.getColumnKeyModel().getColumnKeyFields()) {
			ColumnKeyFieldConfig columnFieldConfig = new ColumnKeyFieldConfig(ctoken, seqNum, totalColumnKeyFields);
			preDefinedColumnKeyFieldMap.put(ctoken.getName(), columnFieldConfig);
			this.columnKeyFieldList.add(columnFieldConfig);
			seqNum++;
		}
	}

	public DataGraph getGraph() {
		return this.graph;
	}
	
	public ColumnKeyModel getColumnKeyModel() {
		return this.graph.getColumnKeyModel();
	}
	
	public Type getRootType() {
		return PlasmaTypeHelper.INSTANCE.getType(
				this.graph.getUri(), this.graph.getType());
	}
	
	public List<Property> getProperties() {
	    return this.graph.properties;
	} 
	    
	public Property findProperty(String name) {
	    return this.propertyNameToPropertyMap.get(name);
	}	
	
	public List<PreDefinedKeyFieldConfig> getPreDefinedRowKeyFields()
	{
		return this.preDefinedRowKeyFieldList;
	}
	
	public PreDefinedKeyFieldConfig getPreDefinedRowKeyField(PreDefinedFieldName name) {
		return this.preDefinedRowKeyFieldMap.get(name);
	}
	
	public String getRowKeyFieldDelimiter() {
        return this.graph.getRowKeyModel().getFieldDelimiter();		
	}
	
	public byte[] getRowKeyFieldDelimiterBytes() {
		if (rowKeyFieldDelimiterBytes == null) {
			this.rowKeyFieldDelimiterBytes = this.graph.getRowKeyModel().getFieldDelimiter().getBytes(
	        		Charset.forName( CoreConstants.UTF8_ENCODING ));
		}
        return rowKeyFieldDelimiterBytes;		
	}
	
	public boolean hasUserDefinedRowKeyFields() {
		return this.userDefinedRowKeyFieldList.size() > 0;
	}
	
	public List<UserDefinedRowKeyFieldConfig> getUserDefinedRowKeyFields() {
		return userDefinedRowKeyFieldList;
	}
	
	public UserDefinedRowKeyFieldConfig getUserDefinedRowKeyField(String path) {
		return this.pathToUserDefinedRowKeyMap.get(path);
	}
	
	public List<KeyFieldConfig> getRowKeyFields()
	{
		return this.rowKeyFieldList;
	}
	
	public List<KeyFieldConfig> getColumnKeyFields()
	{
		return this.columnKeyFieldList;
	}

	/**
	 * Returns the row key field config for the given path endpoint property, or
	 * null if not exists. An endpoint property is a property which terminates
	 * an SDO XPath.
	 * @param property the endpoint property
	 * @return the row key field config for the given path endpoint property, or
	 * null if not exists.
	 */
	public UserDefinedRowKeyFieldConfig findUserDefinedRowKeyField(commonj.sdo.Property property) {
		return this.propertyToUserDefinedRowKeyMap.get(property);
	}

	public ColumnKeyFieldConfig getColumnKeyField(PreDefinedFieldName name) {
		return preDefinedColumnKeyFieldMap.get(name);
	}
	
	public String getColumnKeyFieldDelimiter() {
        return this.graph.getColumnKeyModel().getFieldDelimiter();		
	}
	
	public String getColumnKeySectionDelimiter() {
        return this.graph.getColumnKeyModel().getSectionDelimiter();		
	}
	
	public byte[] getColumnKeyFieldDelimiterBytes() {
		if (columnKeyFieldDelimiterBytes == null) {
			this.columnKeyFieldDelimiterBytes = this.graph.getColumnKeyModel().getFieldDelimiter().getBytes(
	        		Charset.forName( CoreConstants.UTF8_ENCODING ));
		}
        return columnKeyFieldDelimiterBytes;		
	}

	public byte[] getColumnKeySectionDelimiterBytes() {
		if (columnKeySectionDelimiterBytes == null) {
			this.columnKeySectionDelimiterBytes = this.graph.getColumnKeyModel().getSectionDelimiter().getBytes(
	        		Charset.forName( CoreConstants.UTF8_ENCODING ));
		}
        return columnKeySectionDelimiterBytes;		
	}
	
	/**
	 * Returns the configured table for this data graph config. 
	 * @return the configured table for this data graph config.
	 */
	public TableConfig getTable() {
		return table;
	}
	
	
}
