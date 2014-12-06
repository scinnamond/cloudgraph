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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphAssembler;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;
import commonj.sdo.DataGraph;
import commonj.sdo.Property;

public class ThreadPoolGraphAssembler extends DefaultAssembler
    implements DataGraphAssembler {

    private static Log log = LogFactory.getLog(ThreadPoolGraphAssembler.class);
	
	public ThreadPoolGraphAssembler(PlasmaType rootType, SelectionCollector collector,
			Timestamp snapshotDate, Session con) {
		super(rootType, collector, snapshotDate, con);		 
	}
	
	@Override
	protected PlasmaDataObject findDataObject(PlasmaType type,
			List<PropertyPair> row) {
		synchronized (this) {
			return super.findDataObject(type, row);
		}
	}
	
	@Override
	protected PlasmaDataObject createDataObject(List<PropertyPair> row,
			PlasmaDataObject source, PlasmaProperty sourceProperty) {
		synchronized (this) {
			return super.createDataObject(row, source, sourceProperty);
		}
	}
		
	@Override
	public void assemble(List<PropertyPair> results)  {
		
    	long before = System.currentTimeMillis();

    	DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	this.root = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);		
		if (log.isDebugEnabled())
			log.debug("assembling root: " 
		        + this.root.getType().getName());
		
		CoreNode rootNode = (CoreNode)this.root;
        // add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, 
        			snapshotDate);
		// set data properties
		for (PropertyPair pair : results) {
			if (pair.getProp().getType().isDataType()) {
				rootNode.setValue(pair.getProp().getName(), 
						pair.getValue());
			}
		}
		
        // map it
        int key = createHashKey(
        	(PlasmaType)this.root.getType(), results);
        if (log.isDebugEnabled())
        	log.debug("mapping root " + key + "->" + this.root);
        this.dataObjectMap.put(key, this.root);  
		
        
        List<SubgraphAssembler> tasks = new ArrayList<SubgraphAssembler>();
        
		// singular reference props
		for (PropertyPair pair : results) {
			if (pair.getProp().isMany() || pair.getProp().getType().isDataType())
			    continue;
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
		    
			SubgraphAssembler task = new SubgraphTask(
					(PlasmaType)pair.getProp().getType(),
					(PlasmaDataObject)this.root,
					this.collector,
					this.snapshotDate, this.con,
					this.nameComparator,
					pair.getProp(), childKeyProps,
					1, tasks.size(), this);
			tasks.add(task);		    
		}
		
		// multi reference props (not found in results)
		Set<Property> props = this.collector.getProperties(this.rootType);
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType()) {
		    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
		    	if (opposite == null)
			    	throw new DataAccessException("no opposite property found"
				        + " - cannot map from many property, " + prop.toString());			    				    	
				List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
				List<Property> rootPkProps = ((PlasmaType)root.getType()).findProperties(KeyType.primary);
			    if (rootPkProps.size() == 1) {
			    	PlasmaProperty rootProp = (PlasmaProperty)rootPkProps.get(0);
			    	Object value = root.get(rootProp);
			    	if (value != null) {
			    		PropertyPair pair = new PropertyPair(opposite, value);
			    		pair.setValueProp(rootProp);
			    	    childKeyProps.add(pair);
			    	}
			    	else
			    		throw new GraphServiceException("no value found for key property, "  
			    				+ rootProp.toString());
			    }
			    else
				    throwPriKeyError(rootPkProps, 
				    		root.getType(), prop);
			    
				SubgraphAssembler task = new SubgraphTask(
						(PlasmaType)prop.getType(),
						(PlasmaDataObject)this.root,
						this.collector,
						this.snapshotDate, this.con,
						this.nameComparator,
						prop, childKeyProps,
						1, tasks.size(), this);
				tasks.add(task);
			}
		}
		
		for (SubgraphAssembler task : tasks)
			task.start();
		for (SubgraphAssembler task : tasks)
			task.join();
		
        if (log.isDebugEnabled())
        	log.debug("completed root " + key + "->" + this.root);
		
    	long after = System.currentTimeMillis();
    	
    	rootNode.getValueObject().put(
    		CloudGraphConstants.GRAPH_ASSEMBLY_TIME,
    		Long.valueOf(after - before));    	
    	
    	GraphMetricVisitor visitor = new GraphMetricVisitor();
    	this.root.accept(visitor);
    	
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_COUNT,
        		Long.valueOf(visitor.getCount()));
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_DEPTH,
        		Long.valueOf(visitor.getDepth()));
	}

	@Override
	protected void assemble(PlasmaType targetType, PlasmaDataObject source,
			PlasmaProperty sourceProperty, List<PropertyPair> childKeyPairs,
			int level) {
		// TODO Auto-generated method stub
		
	}
	
	
}
