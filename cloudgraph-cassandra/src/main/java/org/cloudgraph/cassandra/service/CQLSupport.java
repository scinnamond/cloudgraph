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
package org.cloudgraph.cassandra.service;

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
import org.cloudgraph.cassandra.filter.FilterAssembler;
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

public abstract class CQLSupport {
	
    private static Log log = LogFactory.getFactory().getInstance(CQLSupport.class);
	protected CQLDataConverter converter = CQLDataConverter.INSTANCE;
	
	protected CQLSupport() {
		
	}	

	protected StringBuilder createSelectForUpdate(PlasmaType type,
			List<PropertyPair> keyValues, int waitSeconds)  {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");		
		List<Property> props = new ArrayList<Property>();
		for (PropertyPair pair : keyValues)
			props.add(pair.getProp());
		
		Property lockingUserProperty = type.findProperty(ConcurrencyType.pessimistic, 
            	ConcurrentDataFlavor.user);
        if (lockingUserProperty != null)
        	props.add(lockingUserProperty);
        else
            if (log.isDebugEnabled())
                log.debug("could not find locking user property for type, "
                    + type.getURI() + "#" + type.getName());  
        
        Property lockingTimestampProperty = type.findProperty(ConcurrencyType.pessimistic, 
            	ConcurrentDataFlavor.time);
        if (lockingTimestampProperty != null)
        	props.add(lockingTimestampProperty);
        else
            if (log.isDebugEnabled())
                log.debug("could not find locking timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  

        Property concurrencyUserProperty = type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.user);
        if (concurrencyUserProperty != null)
        	props.add(concurrencyUserProperty);
        else
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency (username) property for type, "
                    + type.getURI() + "#" + type.getName());          
        
        Property concurrencyTimestampProperty = type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.time);
        if (concurrencyTimestampProperty != null)
        	props.add(concurrencyTimestampProperty);
        else
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
				
		int i = 0;
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (i > 0)
				sql.append(", ");
			sql.append(prop.getPhysicalName());
			i++;
		}
		sql.append(" FROM ");
		sql.append(getQualifiedPhysicalName(type));
		sql.append(" WHERE ");
        for (int k = 0; k < keyValues.size(); k++) {
        	if (k > 0)
        		sql.append(" AND ");
        	PropertyPair propValue = keyValues.get(k);
        	sql.append(propValue.getProp().getPhysicalName());
        	sql.append(" = "); 
        	appendValue(propValue, true, sql);
        }

		return sql;
	}
	
	protected String getQualifiedPhysicalName(PlasmaType type) {
		String packageName = type.getPackagePhysicalName();
		if (packageName != null) 
			return packageName + "." + type.getPhysicalName();
		else
			return type.getPhysicalName();
	}
	
	protected StringBuilder createSelect(PlasmaType type, Set<Property> props, 
			List<PropertyPair> keyValues, List<Object> params) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");		
		
		int count = 0;
		// always select pk props where not found in given list
		List<Property> pkProps = type.findProperties(KeyType.primary);
		for (Property pkProp : pkProps) {
			if (props.contains(pkProp))  
				continue;
			if (count > 0)
				sql.append(", ");
			sql.append(((PlasmaProperty)pkProp).getPhysicalName());			
			count++;
		}
		
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (count > 0)
				sql.append(", ");
			sql.append(prop.getPhysicalName());
			count++;
		}		
		
		sql.append(" FROM ");
		sql.append(getQualifiedPhysicalName(type));
		sql.append(" WHERE ");
        for (count = 0; count < keyValues.size(); count++) {
        	if (count > 0)
        		sql.append(" AND ");
        	PropertyPair propValue = keyValues.get(count);
        	sql.append(propValue.getProp().getPhysicalName());
        	sql.append(" = ?"); 
        	params.add(this.getParamValue(propValue));
        	//appendValue(propValue, sql);
        }
		
		return sql;
	}

	protected StringBuilder createSelect(PlasmaType type, Set<Property> props, 
			List<PropertyPair> keyValues,
			FilterAssembler filterAssembler,
			List<Object> params) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");		
		int count = 0;
		// always select pk props where not found in given list
		List<Property> pkProps = type.findProperties(KeyType.primary);
		for (Property pkProp : pkProps) {
			if (props.contains(pkProp))
				continue;
			if (count > 0)
				sql.append(", ");
			sql.append(((PlasmaProperty)pkProp).getPhysicalName());			
			count++;
		}
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (count > 0)
				sql.append(", ");
			sql.append(prop.getPhysicalName());
			count++;
		}
		sql.append(" FROM ");
		sql.append(getQualifiedPhysicalName(type));
    	sql.append(" ");
    	sql.append(filterAssembler.getFilter());
    	for (Object filterParam : filterAssembler.getParams())
    		params.add(filterParam);
        for (count = 0; count < keyValues.size(); count++) {
            sql.append(" AND ");
        	PropertyPair propValue = keyValues.get(count);
        	sql.append(propValue.getProp().getPhysicalName());
        	sql.append(" = ?"); 
        	params.add(this.getParamValue(propValue));
        	//appendValue(propValue, sql);
        }
        
        // add default ordering by given keys
    	sql.append(" ORDER BY ");
        for (count = 0; count < keyValues.size(); count++) {
        	if (count > 0)        		
                sql.append(", ");
        	PropertyPair propValue = keyValues.get(count);
        	sql.append(propValue.getProp().getPhysicalName());
        }
		
		return sql;
	}
	
	private void appendValue(PropertyPair pair, StringBuilder sql)  
	{
		appendValue(pair, false, sql);
	}
	
	private void appendValue(PropertyPair pair, boolean useOldValue, StringBuilder sql)  
	{
		PlasmaProperty valueProp = pair.getProp();
		if (pair.getValueProp() != null)
			valueProp = pair.getValueProp();
		
		Object jdbcValue = null;
		if (!useOldValue || pair.getOldValue() == null)
    	    jdbcValue = CQLDataConverter.INSTANCE.toCQLDataValue(valueProp, 
    			pair.getValue());
		else
    	    jdbcValue = CQLDataConverter.INSTANCE.toCQLDataValue(valueProp, 
    			pair.getOldValue());
    	
    	DataFlavor dataFlavor = CQLDataConverter.INSTANCE.toCQLDataFlavor(valueProp);
    	
    	switch (dataFlavor) {
    	case string:
    	case temporal:
    	case other:
    	    sql.append("'");
    	    sql.append(jdbcValue);
    	    sql.append("'");
    	    break;
    	default:
    	    sql.append(jdbcValue);
     	   break;
    	}		
	}
	
	private Object getParamValue(PropertyPair pair)  
	{
		PlasmaProperty valueProp = pair.getProp();
		if (pair.getValueProp() != null)
			valueProp = pair.getValueProp();
		
    	Object jdbcValue = CQLDataConverter.INSTANCE.toCQLDataValue(valueProp, 
    			pair.getValue());
    	DataFlavor dataFlavor = CQLDataConverter.INSTANCE.toCQLDataFlavor(valueProp);
    	
    	switch (dataFlavor) {
    	case string:
    	case temporal:
    	case other:
    	    break;
    	default:
     	   break;
    	}	
    	
    	return jdbcValue;
	}		
	
	protected StringBuilder createInsert(PlasmaType type, 
			Map<String, PropertyPair> values) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		sql.append(getQualifiedPhysicalName(type));
		sql.append("(");
		int i = 0;
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (i > 0)
				sql.append(", ");
			sql.append(pair.getProp().getPhysicalName());
			pair.setColumn(i+1);
			i++;
		}
		sql.append(") VALUES (");
		
		i = 0;
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (i > 0)
				sql.append(", ");
			sql.append("?");
			i++;
		}
		sql.append(")");
		return sql;
	}
	
	protected boolean hasUpdatableProperties(Map<String, PropertyPair> values) {
		
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue; // no such thing as updatable many reference property in RDBMS
			if (prop.isKey(KeyType.primary))
				if (pair.getOldValue() == null) // key not modified, we're not updating it
				    continue;  
			return true;
		}
		return false;
	}

	protected StringBuilder createUpdate(PlasmaType type,  
			Map<String, PropertyPair> values) {
		StringBuilder sql = new StringBuilder();
		
		// construct an 'update' for all non pri-keys and
		// excluding many reference properties
		sql.append("UPDATE ");		
		sql.append(getQualifiedPhysicalName(type));
		sql.append(" SET ");
		int col = 0;
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (prop.isKey(KeyType.primary)) {
				if (pair.getOldValue() == null) // key not modified
				    continue; // ignore keys here
			}
			if (col > 0)
				sql.append(", ");
			sql.append(prop.getPhysicalName());
        	sql.append(" = ?"); 
        	pair.setColumn(col+1);
			col++;
		}
        // construct a 'where' continuing to append parameters
		// for each pri-key
		int key = 0;
		sql.append(" WHERE ");
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (!prop.isKey(KeyType.primary))
				continue;
			if (key > 0)
				sql.append(" AND ");  
        	sql.append(pair.getProp().getPhysicalName());
        	sql.append(" = ?"); 
        	if (pair.getOldValue() == null) // key not modified
        	    pair.setColumn(col+1);
        	else
        		pair.setOldValueColumn(col+1);
        	col++; 
        	key++;
        }
		
		return sql;
	}
	
	protected StringBuilder createDelete(PlasmaType type,  
			Map<String, PropertyPair> values) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");		
		sql.append(getQualifiedPhysicalName(type));
		sql.append(" WHERE ");
		int i = 0;
		for (PropertyPair pair : values.values()) {
			PlasmaProperty prop = pair.getProp();
			if (prop.isMany() && !prop.getType().isDataType())
				continue;
			if (!prop.isKey(KeyType.primary))
				continue;
        	if (i > 0)
        		sql.append(" AND "); 
        	sql.append(pair.getProp().getPhysicalName());
        	sql.append(" = ? "); 
        	pair.setColumn(i+1);
        	i++;
        }
		
		return sql;
	}
	
	protected List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql, Session con)
	{
		return fetch(type, sql, new HashSet<Property>(), new Object[0], con);
	}

	protected List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql, Set<Property> props, Session con)
	{
		return fetch(type, sql, props, new Object[0], con);
	}
	
	protected List<List<PropertyPair>> fetch(PlasmaType type, StringBuilder sql, Set<Property> props, Object[] params, Session con)
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
	
	protected Map<String, PropertyPair> fetchRowMap(PlasmaType type, StringBuilder sql, Session con)
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

	protected List<PropertyPair> fetchRow(PlasmaType type, StringBuilder sql, Session con)
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
	
	protected void execute(PlasmaType type, StringBuilder sql, 
			Map<String, PropertyPair> values,
			Session con)
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
		
	protected void executeInsert(PlasmaType type, StringBuilder sql, 
			Map<String, PropertyPair> values,
			Session con)
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
