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
package org.cloudgraph.cassandra.graph;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.cql.FilterAssembler;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphAssembler;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;
import commonj.sdo.Property;

public class BlockingGraphAssembler extends DefaultAssembler
    implements DataGraphAssembler {

    private static Log log = LogFactory.getLog(BlockingGraphAssembler.class);
		
	public BlockingGraphAssembler(PlasmaType rootType, SelectionCollector collector,
			Timestamp snapshotDate, Session con) {
		super(rootType, collector, snapshotDate, con);		 
	}
	
	/**
	 * Assembles a data object of the given target type by first forming a query using the
	 * given key/property pairs. If an existing data object is mapped for the given
	 * key pairs, the existing data object is linked. 
	 * @param targetType the type for the data object to be assembled
	 * @param source the source data object
	 * @param sourceProperty the source property
	 * @param childKeyPairs the key pairs for the data object to be assembled
	 */
	protected void assemble(PlasmaType targetType, PlasmaDataObject source, PlasmaProperty sourceProperty, 
			List<PropertyPair> childKeyPairs, int level)  {
		Set<Property> props = this.collector.getProperties(targetType, level);
		if (props == null)
			props = EMPTY_PROPERTY_SET;
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":assemble: " + source.getType().getName() 
					+ "." + sourceProperty.getName() + "->" + targetType.getName() + ": "
					+ props);
		
		List<List<PropertyPair>> result = null;
		Where where = this.collector.getPredicate(sourceProperty);
		if (where == null) {        
			List<Object> params = new ArrayList<Object>();
			StringBuilder query = this.statementFactory.createSelect(targetType, props, childKeyPairs, params);
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
		
		if (log.isDebugEnabled())
			log.debug(String.valueOf(level) + ":results: "  + result.size());
	    
		// first create (or link existing) data objects 
		// "filling out" the containment hierarchy at this traversal level
		// BEFORE recursing, as we may "cancel" out an object
		// at the current level if it is first encountered
		// within the recursion.
		Map<PlasmaDataObject, List<PropertyPair>> resultMap = new HashMap<PlasmaDataObject, List<PropertyPair>>();
		for (List<PropertyPair> row : result) {
			
			PlasmaDataObject target = findDataObject(targetType, row);
			// if no existing data-object in graph
			if (target == null) {
			    target = createDataObject(row, source, 
					sourceProperty);
			    resultMap.put(target, row); // add only new object for later traversal
			}
			else { 
				link(target, source, sourceProperty);
				continue; // don't map it for later traversal
				// Assume we traverse no farther given no traversal
				// direction or containment info. We only know that we 
				// encountered an existing node. Need more path specific 
				// info including containment and traversal direction to construct
				// a directed graph here. 
				// Since the current selection collector maps any and all
				// properties selected to a type, for each type/data-object
				// we will, at this point, have gotten all the properties we expect anyway.
				// So we create a link from the source to the existing DO, but
				// traverse no further. 
			}   
		}	
		
		// now traverse
		Iterator<PlasmaDataObject> iter = resultMap.keySet().iterator();
		while (iter.hasNext()) {
			PlasmaDataObject target = iter.next();
			List<PropertyPair> row = resultMap.get(target);
			// traverse singular results props
			for (PropertyPair pair : row) {
				if (pair.getProp().isMany() || pair.getProp().getType().isDataType()) 
				    continue; // only singular reference props
				if (!pair.isQueryProperty())
					continue; // property is a key or other property not explicitly cited in the source query, don't traverse it
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
				    	PropertyPair nextPair = findNextKeyValue(target, 
				    			nextProp, opposite);
				    	nextKeyPairs.add(nextPair);
			    	}
				    else {
					    if (log.isDebugEnabled())
						    log.debug(String.valueOf(level) + ":found multiple PK's - throwing PK error");
					    throwPriKeyError(nextKeyProps, 
							pair.getProp().getType(), pair.getProp());
				    }
				}
								    
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + pair.getProp().isMany() 
							+ ") " + pair.getProp().toString() + ":" + String.valueOf(pair.getValue()));
				assemble((PlasmaType)pair.getProp().getType(), 
						target, pair.getProp(), nextKeyPairs, level+1);				
			}
			
			// traverse multi props based, not on the results
			// row, but on keys within this data object
			for (Property p : props) {
				PlasmaProperty prop = (PlasmaProperty)p;
				if (!prop.isMany() || prop.getType().isDataType())
				    continue; // only many reference props
				
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
			    	PropertyPair pair = findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
		    	}			    
				else if (nextKeyProps.size() == 1) {
			    	PlasmaProperty nextProp = (PlasmaProperty)nextKeyProps.get(0);	
			    	PropertyPair pair = findNextKeyValue(target, 
			    			nextProp, opposite);
			    	childKeyProps.add(pair);
			    }
			    else {  
				     throwPriKeyError(nextKeyProps, 
					    		targetType, prop);
			    }
				if (log.isDebugEnabled())
					log.debug(String.valueOf(level) + ":traverse: (" + prop.isMany() 
							+ ") " + prop.toString() + " - " + childKeyProps.toArray().toString());
			    assemble((PlasmaType)prop.getType(), 
			    		target, prop,
						childKeyProps, level+1);
			}				
		}
	}
	
}
