package org.cloudgraph.cassandra.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.cql.CQLStatementExecutor;
import org.cloudgraph.cassandra.cql.CQLStatementFactory;
import org.cloudgraph.cassandra.cql.FilterAssembler;
import org.cloudgraph.cassandra.cql.StatementExecutor;
import org.cloudgraph.cassandra.cql.StatementFactory;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;

import commonj.sdo.Property;
import commonj.sdo.Type;

public abstract class AssemblerSupport {
    private static Log log = LogFactory.getLog(AssemblerSupport.class);
	protected SelectionCollector collector;
	protected StatementFactory statementFactory;
	protected StatementExecutor statementExecutor;
	protected Session con;
	@SuppressWarnings("unused")
	private AssemblerSupport() {}
	public AssemblerSupport(SelectionCollector collector, Session con) {
		this.collector = collector;
		this.con = con;
		this.statementFactory = new CQLStatementFactory();
		this.statementExecutor = new CQLStatementExecutor(con);
	}
	
	protected List<PropertyPair> getChildKeyPairs(PropertyPair pair)
	{
		List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
		PlasmaProperty supplier = pair.getProp().getKeySupplier();
		if (supplier != null) {
			PropertyPair childPair = new PropertyPair(supplier,
	    			pair.getValue());
			childPair.setValueProp(supplier);
	    	childKeyProps.add(childPair);
		}
		else {
			List<Property> childPkProps = ((PlasmaType)pair.getProp().getType()).findProperties(KeyType.primary);
		    if (childPkProps.size() == 1) {
		    	childKeyProps.add(
		    		new PropertyPair((PlasmaProperty)childPkProps.get(0),
		    			pair.getValue()));
		    }
		    else
			    throwPriKeyError(childPkProps, 
			    		pair.getProp().getType(), pair.getProp());
		}		    
		return childKeyProps;
	}
	
	protected List<PropertyPair> getChildKeyPairs(PlasmaDataObject dataObject, PlasmaProperty prop)
	{
    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
    	if (opposite == null)
	    	throw new DataAccessException("no opposite property found"
		        + " - cannot map from many property, " + prop.toString());			    				    	
		List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
		List<Property> pkProps = ((PlasmaType)dataObject.getType()).findProperties(KeyType.primary);
	    if (pkProps.size() == 1) {
	    	PlasmaProperty pkProp = (PlasmaProperty)pkProps.get(0);
	    	Object value = dataObject.get(pkProp);
	    	if (value != null) {
	    		PropertyPair pair = new PropertyPair(opposite, value);
	    		pair.setValueProp(pkProp);
	    	    childKeyProps.add(pair);
	    	}
	    	else
	    		throw new GraphServiceException("no value found for key property, "  
	    				+ pkProp.toString());
	    }
	    else
		    throwPriKeyError(pkProps, 
		    		dataObject.getType(), prop);
		return childKeyProps;
	}
	
	protected List<List<PropertyPair>> getPredicateResult(PlasmaType targetType, PlasmaProperty sourceProperty,
			Set<Property> props,
			List<PropertyPair> childKeyPairs) {
		List<List<PropertyPair>> result = null;
		Where where = this.collector.getPredicate(sourceProperty);
		if (where == null) {        
			List<Object> params = new ArrayList<Object>();
			StringBuilder query = this.statementFactory.createSelect(targetType, 
					props, childKeyPairs, params);
			//The partition key is the first field in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			result = this.statementExecutor.fetch(targetType, query, props, paramArray);
		}
		else {
			FilterAssembler filterAssembler = new FilterAssembler(where, 
					targetType);
			List<Object> params = new ArrayList<Object>();			
			StringBuilder query = this.statementFactory.createSelect(targetType,
		    	props, childKeyPairs, filterAssembler, params);
			
			//The partition key is the first field in the primary key definition
			//The absence of this partition key makes that Cassandra has to send your query 
			//to all nodes in the cluster, which is inefficient and therefore disabled 
			//by default. The 'ALLOW FILTERING' clause enables such searches,
			query.append(" ALLOW FILTERING");
			
			Object[] paramArray = new Object[params.size()];
			params.toArray(paramArray);
			
			result = this.statementExecutor.fetch(targetType, query, props, paramArray);
		}		
		return result;
	}
	
	protected List<PropertyPair> getNextKeyPairs(PlasmaDataObject target, PropertyPair pair, int level)
	{
		List<PropertyPair> nextKeyPairs = new ArrayList<PropertyPair>();
		List<Property> nextKeyProps = ((PlasmaType)pair.getProp().getType()).findProperties(KeyType.primary);
	    			
    	if (nextKeyProps.size() == 1) {
			if (log.isDebugEnabled())
				log.debug(String.valueOf(level) + ":found single PK for type, " + pair.getProp().getType());
			PlasmaProperty next = (PlasmaProperty)nextKeyProps.get(0);
			
			PropertyPair nextPair = new PropertyPair(next, pair.getValue());					
			nextKeyPairs.add(nextPair);
			if (log.isDebugEnabled())
				log.debug(String.valueOf(level) + ":added single PK, " + next.toString());
	    }
		else {
	    	PlasmaProperty opposite = (PlasmaProperty)pair.getProp().getOpposite();
	    	if (opposite == null)
		    	throw new DataAccessException("no opposite property found"
		        + " - cannot map from singular property, "
		        + pair.getProp().toString());	
	    	PlasmaProperty supplier = opposite.getKeySupplier();
	    	if (supplier != null) {
	    		PlasmaProperty nextProp = supplier;
		    	PropertyPair nextPair = this.findNextKeyValue(target, 
		    			nextProp, opposite);
		    	nextKeyPairs.add(nextPair);
	    	}
		    else {
			    if (log.isDebugEnabled())
				    log.debug(String.valueOf(level) + ":found multiple PK's - throwing PK error");
			    this.throwPriKeyError(nextKeyProps, 
					pair.getProp().getType(), pair.getProp());
		    }
		}
		return nextKeyPairs;
	}

	protected List<PropertyPair> getChildKeyProps(PlasmaDataObject target, PlasmaType targetType, PlasmaProperty prop)
	{
    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
    	if (opposite == null)
	    	throw new DataAccessException("no opposite property found"
	        + " - cannot map from many property, "
	        + prop.getType() + "." + prop.getName());	
		List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
		List<Property> nextKeyProps = ((PlasmaType)targetType).findProperties(KeyType.primary);
		PlasmaProperty supplier = opposite.getKeySupplier();
		if (supplier != null) {
    		PlasmaProperty nextProp = supplier;
	    	PropertyPair pair = this.findNextKeyValue(target, 
	    			nextProp, opposite);
	    	childKeyProps.add(pair);
    	}			    
		else if (nextKeyProps.size() == 1) {
	    	PlasmaProperty nextProp = (PlasmaProperty)nextKeyProps.get(0);	
	    	PropertyPair pair = this.findNextKeyValue(target, 
	    			nextProp, opposite);
	    	childKeyProps.add(pair);
	    }
	    else {  
	    	this.throwPriKeyError(nextKeyProps, 
			    		targetType, prop);
	    }
		return childKeyProps;
	}
	
	/**
	 * If the given property is a datatype property, returns a property pair
	 * with the given property set as the pair value property, otherwise
	 * traverses the data object graph via opposite property links until a
	 * datatype property is found, then returns the property value pair with the
	 * traversal end point property set as the pair value property.
	 * 
	 * @param dataObject
	 *            the data object
	 * @param prop
	 *            the property
	 * @param opposite
	 *            the opposite property
	 * @return the property value pair
	 */
	protected PropertyPair findNextKeyValue(PlasmaDataObject dataObject,
			PlasmaProperty prop, PlasmaProperty opposite) {
		PlasmaDataObject valueTarget = dataObject;
		PlasmaProperty valueProp = prop;

		Object value = valueTarget.get(valueProp.getName());
		while (!valueProp.getType().isDataType()) {
			valueTarget = (PlasmaDataObject) value;
			valueProp = this.statementFactory.getOppositePriKeyProperty(valueProp);
			value = valueTarget.get(valueProp.getName()); // FIXME use prop API
		}
		if (value != null) {
			PropertyPair pair = new PropertyPair(opposite, value);
			pair.setValueProp(valueProp);
			return pair;
		} else
			throw new GraphServiceException("no value found for key property, "
					+ valueProp.toString());
	}	
	protected void throwPriKeyError(List<Property> rootPkProps, Type type,
			Property prop) {
		if (prop.isMany())
			if (rootPkProps.size() == 0)
				throw new DataAccessException("no pri-keys found for "
						+ type.getURI() + "#" + type.getName()
						+ " - cannot map from many property, " + prop.getType()
						+ "." + prop.getName());
			else
				throw new DataAccessException("multiple pri-keys found for "
						+ type.getURI() + "#" + type.getName()
						+ " - cannot map from many property, " + prop.getType()
						+ "." + prop.getName());
		else if (rootPkProps.size() == 0)
			throw new DataAccessException("no pri-keys found for "
					+ type.getURI() + "#" + type.getName()
					+ " - cannot map from singular property, " + prop.getType()
					+ "." + prop.getName());
		else
			throw new DataAccessException("multiple pri-keys found for "
					+ type.getURI() + "#" + type.getName()
					+ " - cannot map from singular property, " + prop.getType()
					+ "." + prop.getName());
	}
}
