package org.cloudgraph.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import org.cloudgraph.config.ColumnKeyField;
import org.cloudgraph.config.DataGraph;
import org.cloudgraph.config.PreDefinedFieldName;
import org.cloudgraph.config.RowKeyField;
import org.cloudgraph.config.UserDefinedField;

import commonj.sdo.Type;

public class DataGraphConfig {
    private DataGraph graph;
    private Map<PreDefinedFieldName, RowKeyField> preDefinedRowKeyFields = new HashMap<PreDefinedFieldName, RowKeyField>(); 
    private Map<PreDefinedFieldName, ColumnKeyField> preDefinedColumnKeyFields = new HashMap<PreDefinedFieldName, ColumnKeyField>(); 
	private List<UserDefinedFieldConfig> userDefinedRowKeyFields = new ArrayList<UserDefinedFieldConfig>();
	private Map<String, UserDefinedFieldConfig> pathTouserDefinedRowKeyMap = new HashMap<String, UserDefinedFieldConfig>();
	private byte[] rowKeyFieldDelimiterBytes;
	private byte[] rowKeySectionDelimiterBytes;
	private byte[] columnKeyFieldDelimiterBytes;
	private byte[] columnKeySectionDelimiterBytes;
    
    @SuppressWarnings("unused")
	private DataGraphConfig() {}
	public DataGraphConfig(DataGraph graph) {
		super();
		this.graph = graph;
		for (RowKeyField token : this.graph.getRowKeyModel().getRowKeyFields())
			preDefinedRowKeyFields.put(token.getName(), token);
		
		for (ColumnKeyField ctoken : this.graph.getColumnKeyModel().getColumnKeyFields())
			preDefinedColumnKeyFields.put(ctoken.getName(), ctoken);
		
		int seqNum = 1;
	    for (UserDefinedField token : this.graph.getRowKeyModel().getUserDefinedFields())
	    {
	    	UserDefinedFieldConfig tokenConfig = new UserDefinedFieldConfig(this, token, seqNum);
	    	userDefinedRowKeyFields.add(tokenConfig);
	    	if (this.pathTouserDefinedRowKeyMap.get(tokenConfig.getPropertyPath()) != null) 
	    		throw new CloudGraphConfigurationException("a user defined token path '" 
	    				+ tokenConfig.getPathExpression()
	    				+ "' already exists with property path '"
	    				+ tokenConfig.getPropertyPath()
	    				+ "' for data graph of type, "
	    				+ this.graph.getUri() + "#" + this.graph.getType());
	    	this.pathTouserDefinedRowKeyMap.put(tokenConfig.getPropertyPath(), tokenConfig);
	    	seqNum++;
	    }
	}

	public DataGraph getGraph() {
		return this.graph;
	}
	
	public Type getRootType() {
		return PlasmaTypeHelper.INSTANCE.getType(
				this.graph.getUri(), this.graph.getType());
	}
	
	public List<RowKeyField> getPreDefinedRowKeyFields()
	{
		return graph.getRowKeyModel().getRowKeyFields();
	}
	
	public RowKeyField getRowKeyField(PreDefinedFieldName name) {
		return preDefinedRowKeyFields.get(name);
	}
	
	public String getRowKeyFieldDelimiter() {
        return this.graph.getRowKeyModel().getFieldDelimiter();		
	}
	
	public String getRowKeySectionDelimiter() {
        return this.graph.getRowKeyModel().getSectionDelimiter();		
	}
	
	public byte[] getRowKeyFieldDelimiterBytes() {
		if (rowKeyFieldDelimiterBytes == null) {
			this.rowKeyFieldDelimiterBytes = this.graph.getRowKeyModel().getFieldDelimiter().getBytes(
	        		Charset.forName( CoreConstants.UTF8_ENCODING ));
		}
        return rowKeyFieldDelimiterBytes;		
	}
	
	public byte[] getRowKeySectionDelimiterBytes() {
		if (rowKeySectionDelimiterBytes == null) {
			this.rowKeySectionDelimiterBytes = this.graph.getRowKeyModel().getSectionDelimiter().getBytes(
	        		Charset.forName( CoreConstants.UTF8_ENCODING ));
		}
        return rowKeySectionDelimiterBytes;		
	}

	public boolean hasUserDefinedRowKeyFields() {
		return this.graph.getRowKeyModel().getUserDefinedFields().size() > 0;
	}
	
	public List<UserDefinedFieldConfig> getUserDefinedRowKeyFields() {
		return userDefinedRowKeyFields;
	}
	
	public UserDefinedFieldConfig getUserDefinedRowKeyField(String path) {
		return this.pathTouserDefinedRowKeyMap.get(path);
	}

	public ColumnKeyField getColumnKeyField(PreDefinedFieldName name) {
		return preDefinedColumnKeyFields.get(name);
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
}
