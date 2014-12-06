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
package org.cloudgraph.cassandra.cql;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.profile.ConcurrencyType;
import org.plasma.sdo.profile.ConcurrentDataFlavor;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

import commonj.sdo.Property;

public class CQLStatementExecutor implements StatementExecutor {
	
    private static Log log = LogFactory.getFactory().getInstance(CQLStatementExecutor.class);
	protected CQLDataConverter converter = CQLDataConverter.INSTANCE;
	private Session con;
	
	public CQLStatementExecutor(Session con) {
		this.con = con;
	}	
 
	
	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#fetch(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, com.datastax.driver.core.Session)
	 */
	@Override
	public List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql)
	{
		return fetch(type, sql, new HashSet<Property>(), new Object[0]);
	}

	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#fetch(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, java.util.Set, com.datastax.driver.core.Session)
	 */
	@Override
	public List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql, Set<Property> props)
	{
		return fetch(type, sql, props, new Object[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#fetch(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, java.util.Set, java.lang.Object[], com.datastax.driver.core.Session)
	 */
	@Override
	public List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql, Set<Property> props, Object[] params)
	{
		List<List<PropertyPair>> result = new ArrayList<List<PropertyPair>>();
		SimpleStatement statement = null;
		ResultSet rs = null; 
        try {
            if (log.isDebugEnabled() ){
                if (params == null || params.length == 0) {
                    log.debug("fetch: "+ sql.toString());                	
                }
                else
                {
                    StringBuilder paramBuf = new StringBuilder();
                	paramBuf.append(" [");
                    for (int p = 0; p < params.length; p++)
                    {
                        if (p > 0)
                        	paramBuf.append(", ");
                        paramBuf.append(String.valueOf(params[p]));
                    }
                    paramBuf.append("]");
                    log.debug("fetch: "+ sql.toString() 
                    		+ " " + paramBuf.toString());
                }
            } 
            statement = new SimpleStatement(sql.toString(), params);
            rs = con.execute(statement);
            ColumnDefinitions rsMeta = rs.getColumnDefinitions();
		           
            int numcols = rsMeta.size();
            Iterator<Row> iter = rs.iterator();
            int count = 0;
            while (iter.hasNext()) {
            	Row dataRow = iter.next();
            	List<PropertyPair> row = new ArrayList<PropertyPair>(numcols);
            	result.add(row);
            	int column = 0;
            	for(ColumnDefinitions.Definition def : rs.getColumnDefinitions()) {
            		String columnName = def.getName();
            		DataType columnType = def.getType();
            		PlasmaProperty prop = (PlasmaProperty)type.getProperty(columnName);
            		PlasmaProperty valueProp = prop;
			    	while (!valueProp.getType().isDataType()) {
			    		valueProp = getOppositePriKeyProperty(valueProp);
			    	}
              		Object value = converter.fromCQLDataType(dataRow, 
              				column, columnType, valueProp);
            		if (value != null) {
            		    PropertyPair pair = new PropertyPair(
            			    (PlasmaProperty)prop, value);
            		    if (!valueProp.equals(prop))
            		    	pair.setValueProp(valueProp);
            		    if (!props.contains(prop))
            		    	pair.setQueryProperty(false);
            		    row.add(pair);
            		}
            		column++;
                }
            	count++;
            }
            if (log.isDebugEnabled())
                log.debug("returned "+ count + " results");
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	// no CQL driver RS or statement close()
        }
        return result;
 	}
	
	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#fetchRowMap(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, com.datastax.driver.core.Session)
	 */
	@Override
	public Map<String, PropertyPair> fetchRowMap(PlasmaType type, StringBuilder sql)
	{
		Map<String, PropertyPair> result = new HashMap<String, PropertyPair>();
		SimpleStatement statement = null;
        ResultSet rs = null; 
        try {
            if (log.isDebugEnabled() ){
                log.debug("fetch: " + sql.toString());
            } 
            
            statement = new SimpleStatement(sql.toString());
		 
            rs = con.execute(statement);
            ColumnDefinitions rsMeta = rs.getColumnDefinitions();
            Iterator<Row> iter = rs.iterator();
            int count = 0;
            while (iter.hasNext()) {
            	Row dataRow = iter.next();
            	int column = 0;
            	for(ColumnDefinitions.Definition def : rs.getColumnDefinitions()) {
            		String columnName = def.getName();
            		DataType columnType = def.getType();
            		PlasmaProperty prop = (PlasmaProperty)type.getProperty(columnName);
            		PlasmaProperty valueProp = prop;
			    	while (!valueProp.getType().isDataType()) {
			    		valueProp = getOppositePriKeyProperty(valueProp);
			    	}
              		Object value = converter.fromCQLDataType(dataRow, 
              				column, columnType, valueProp);
            		if (value != null) {
            		    PropertyPair pair = new PropertyPair(
            			    (PlasmaProperty)prop, value);
            		    if (!valueProp.equals(prop))
            		    	pair.setValueProp(valueProp);
            		    result.put(prop.getName(), pair);
            		}
            		column++;
                }
            	count++;
            }
            if (log.isDebugEnabled())
                log.debug("returned "+ count + " results");
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	// no CQL driver RS or statement close()
        }
        return result;
 	}

	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#fetchRow(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, com.datastax.driver.core.Session)
	 */
	@Override
	public List<PropertyPair> fetchRow(PlasmaType type, StringBuilder sql)
	{
		List<PropertyPair> result = new ArrayList<PropertyPair>();
		SimpleStatement statement = null;
        ResultSet rs = null; 
        try {
            if (log.isDebugEnabled() ){
                log.debug("fetch: " + sql.toString());
            } 
            statement = new SimpleStatement(sql.toString());
		            
            
            rs = con.execute(statement);
            ColumnDefinitions rsMeta = rs.getColumnDefinitions();
            Iterator<Row> iter = rs.iterator();
            int count = 0;
            while (iter.hasNext()) {
            	Row dataRow = iter.next();
            	int column = 0;
            	for(ColumnDefinitions.Definition def : rs.getColumnDefinitions()) {
            		String columnName = def.getName();
            		DataType columnType = def.getType();
            		PlasmaProperty prop = (PlasmaProperty)type.getProperty(columnName);
            		PlasmaProperty valueProp = prop;
			    	while (!valueProp.getType().isDataType()) {
			    		valueProp = getOppositePriKeyProperty(valueProp);
			    	}
              		Object value = converter.fromCQLDataType(dataRow, 
              				column, columnType, valueProp);
            		if (value != null) {
            		    PropertyPair pair = new PropertyPair(
            			    (PlasmaProperty)prop, value);
            		    if (!valueProp.equals(prop))
            		    	pair.setValueProp(valueProp);
            		    result.add(pair);
            		}
            		column++;
                }
            	count++;
            }
            if (log.isDebugEnabled())
                log.debug("returned "+ count + " results");
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	// no CQL driver RS or statement close()
        }
        return result;
 	}	
	
	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#execute(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, java.util.Map, com.datastax.driver.core.Session)
	 */
	@Override
	public void execute(PlasmaType type, StringBuilder sql, 
			Map<String, PropertyPair> values)
	{
		SimpleStatement statement = null;
        try {		
            if (log.isDebugEnabled() ){
                log.debug("execute: " + sql.toString());
                StringBuilder paramBuf = createParamDebug(values);
                log.debug("params: " + paramBuf.toString());
            } 
            List<Object> list = new ArrayList<Object>();
    		for (PropertyPair pair : values.values()) {
    			PlasmaProperty valueProp = pair.getProp();
    			if (pair.getValueProp() != null)
    				valueProp = pair.getValueProp();
    			int jdbcType = converter.toCQLDataType(valueProp, pair.getValue());
    			Object jdbcValue = converter.toCQLDataValue(valueProp, pair.getValue());
    			list.add(jdbcValue);
    		}            
            
            statement = new SimpleStatement(sql.toString(), list.toArray());            
            con.execute(statement);
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	// no CQL driver RS or statement close()
        }
 	}
		
	/* (non-Javadoc)
	 * @see org.cloudgraph.cassandra.cql.StatementExecutor#executeInsert(org.plasma.sdo.PlasmaType, java.lang.StringBuilder, java.util.Map, com.datastax.driver.core.Session)
	 */
	@Override
	public void executeInsert(PlasmaType type, StringBuilder sql, 
			Map<String, PropertyPair> values)
	{
		SimpleStatement statement = null;
        List<InputStream> streams = null;
        try {
		
            if (log.isDebugEnabled() ){
                log.debug("execute: " + sql.toString());
                StringBuilder paramBuf = createParamDebug(values);
                log.debug("params: " + paramBuf.toString());
            } 
            List<Object> list = new ArrayList<Object>();
    		for (PropertyPair pair : values.values()) {
    			PlasmaProperty valueProp = pair.getProp();
    			if (pair.getValueProp() != null)
    				valueProp = pair.getValueProp();
    			int jdbcType = converter.toCQLDataType(valueProp, pair.getValue());
    			Object jdbcValue = converter.toCQLDataValue(valueProp, pair.getValue());
    			list.add(jdbcValue);
    		}            
             
            statement = new SimpleStatement(sql.toString(), list.toArray());
            
            con.execute(statement);
        }
        catch (Throwable t) {
            throw new DataAccessException(t);
        }
        finally {
        	// no CQL driver RS or statement close()
        }
 	}
		
	protected PlasmaProperty getOppositePriKeyProperty(Property targetProperty) {
    	PlasmaProperty opposite = (PlasmaProperty)targetProperty.getOpposite();
    	PlasmaType oppositeType = null;
    	    	
    	if (opposite != null) {
    		oppositeType = (PlasmaType)opposite.getContainingType();
    	}
    	else {
    		oppositeType = (PlasmaType)targetProperty.getType();
    	}
    	
		List<Property> pkeyProps = oppositeType.findProperties(KeyType.primary);
	    if (pkeyProps.size() == 0) {
	    	throw new DataAccessException("no opposite pri-key properties found"
			        + " - cannot map from reference property, "
			        + targetProperty.toString());	
	    }
    	PlasmaProperty supplier = ((PlasmaProperty)targetProperty).getKeySupplier();
    	if (supplier != null) {
    		return supplier;
    	}
	    else if (pkeyProps.size() == 1) {
	    	return (PlasmaProperty)pkeyProps.get(0);
	    }
	    else {
	    	throw new DataAccessException("multiple opposite pri-key properties found"
			    + " - cannot map from reference property, "
			    + targetProperty.toString() + " - please add a derivation supplier");
	    }		
	}
	
	private StringBuilder createParamDebug(Map<String, PropertyPair> values)  {
		StringBuilder paramBuf = new StringBuilder();
        paramBuf.append("[");
        paramBuf.append("[");
		int i = 1;
		for (PropertyPair pair : values.values()) {
			PlasmaProperty valueProp = pair.getProp();
			if (pair.getValueProp() != null)
				valueProp = pair.getValueProp();
			int jdbcType = converter.toCQLDataType(valueProp, pair.getValue());
			Object jdbcValue = converter.toCQLDataValue(valueProp, pair.getValue());
			Object jdbcOldValue = null;
			if (pair.getOldValue() != null) 
				jdbcOldValue = converter.toCQLDataValue(valueProp, pair.getOldValue());
			 
        	if (i > 1) {
        		paramBuf.append(", ");
        	}
        	paramBuf.append("(");
        	paramBuf.append(jdbcValue.getClass().getSimpleName());
        	paramBuf.append("/");
        	paramBuf.append(converter.getCQLTypeName(jdbcType));
        	paramBuf.append(")");
        	paramBuf.append(String.valueOf(jdbcValue));
        	if (jdbcOldValue != null) {
        		paramBuf.append("(");
        		paramBuf.append(String.valueOf(jdbcOldValue));
        		paramBuf.append(")");
        	}
			i++;		
		}
    	paramBuf.append("]");
		return paramBuf;
	}

}
