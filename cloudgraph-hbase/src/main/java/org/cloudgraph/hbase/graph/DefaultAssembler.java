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
package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.hbase.service.HBaseDataConverter;
import org.cloudgraph.hbase.util.FilterUtil;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.store.service.GraphServiceException;
import org.plasma.query.collector.Selection;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * Supports both distributed and non-distributed graph assemblers by
 * providing default functionality.
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class DefaultAssembler {

    private static Log log = LogFactory.getLog(DefaultAssembler.class);
	
    protected PlasmaType rootType;
	protected PlasmaDataObject root;
	protected TableReader rootTableReader;
	protected Selection selection;
	protected Timestamp snapshotDate;
	
	@SuppressWarnings("unused")
	private DefaultAssembler() {}
	
	/**
	 * Constructor.
	 * @param rootType the SDO root type for the result data graph
	 * @param selection the selection properties for the graph to assemble.
	 * @param snapshotDate the query snapshot date which is populated
	 * into every data object in the result data graph. 
	 */
	public DefaultAssembler(PlasmaType rootType,
			Selection selection,
			TableReader rootTableReader,
			Timestamp snapshotDate) {
		this.rootType = rootType;
		this.selection = selection;
		this.rootTableReader = rootTableReader;
		this.snapshotDate = snapshotDate;
	}
	
	/**
	 * Returns the assembled data graph.
	 */
	public PlasmaDataGraph getDataGraph() {
		return (PlasmaDataGraph)this.root.getDataGraph();
	}
	
	protected PlasmaDataObject createRoot(Result resultRow) {
        // build the graph
    	PlasmaDataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	dataGraph.setId(resultRow.getRow());    	
    	PlasmaDataObject rootObject = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);				
		CoreNode rootNode = (CoreNode)rootObject;
        
		// add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_THREAD_NAME,
        		Thread.currentThread().getName());

        // need to reconstruct the original graph, so need original UUID
		byte[] rootUuid = resultRow.getValue(Bytes.toBytes(
				this.rootTableReader.getTable().getDataColumnFamilyName()), 
                Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
		if (rootUuid == null)
			throw new GraphServiceException("expected column: "
				+ this.rootTableReader.getTable().getDataColumnFamilyName() + ":"
				+ GraphState.ROOT_UUID_COLUMN_NAME);
		String uuidStr = null;
		uuidStr = new String(rootUuid, 
				this.rootTableReader.getTable().getCharset());
		UUID uuid = UUID.fromString(uuidStr);
		rootObject.resetUUID(uuid);
		return rootObject;
	}
	
	/**
	 * Populates data properties for the given target data object using
	 * the given property name list.
	 * @param target the data object to populate
	 * @param names the property names
	 * @param rowReader the row reader
	 * @throws IOException if a remote or network exception occurs.
	 */
	protected void assembleData(PlasmaDataObject target,
		Set<Property> props,
		RowReader rowReader) throws IOException
	{
		CoreNode targetDataNode = (CoreNode)target;
		TableReader tableReader = rowReader.getTableReader();
		TableConfig tableConfig = tableReader.getTable();
		
		// add concurrency fields
        if (this.snapshotDate != null)
        	targetDataNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);

		// data props
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (!prop.getType().isDataType())
				continue;
			
			byte[] keyValue = getColumnValue(target, prop, 
					tableConfig, rowReader);
			
			if (keyValue == null || keyValue.length == 0 ) {
				continue; // zero length can happen on modification or delete as we keep cell history
			}
			
			Object value = HBaseDataConverter.INSTANCE.fromBytes(prop, 
					keyValue);
	        
			if (log.isDebugEnabled())
				log.debug("set: (" + prop.getName() + ") " + String.valueOf(value));

			if (!prop.isReadOnly()) {
			    target.set(prop, value);
			}
			else {
				targetDataNode.setValue(prop.getName(), 
						value);
			}
		}		
	}
	
	/**
	 * Returns a value for the given property from the given row reader
	 * by generating a column qualifier based on column key model 
	 * configurations settings, graph state information and other 
	 * factors. Returns null if the qualifier does not exist.  
	 * @param target the data object 
	 * @param prop the property
	 * @param tableConfig the table configuration
	 * @param rowReader the row reader
	 * @return a value for the given property from the given row reader
	 * by generating a column qualifier based on column key model 
	 * configurations settings, graph state information and other factors. 
	 * @throws IOException if a remote or network exception occurs.
	 */
	protected byte[] getColumnValue(PlasmaDataObject target, 
		PlasmaProperty prop, TableConfig tableConfig, 
		RowReader rowReader) throws IOException
	{
		byte[] family = tableConfig.getDataColumnFamilyNameBytes();
		
		byte[] qualifier = rowReader.getColumnKeyFactory().getColumnKey(
				target, prop);
		 	
		if (!rowReader.getRow().containsColumn(family, qualifier)) {
			if (log.isDebugEnabled()) {
				String qualifierStr = Bytes.toString(qualifier);
				log.debug("qualifier not found: "
						+ qualifierStr + " - continuing...");
			}
			return null;
		}
		
		return rowReader.getRow().getColumnValue(
			family, qualifier);		
	}
	
	/**
	 * Associates the source data object with the target as a non-containment
	 * reference. 
	 * @param target the data object target
	 * @param source the data object source
	 * @param sourceProperty the reference property
	 * @throws IllegalStateException if the target data object does not have a container
	 */
    @SuppressWarnings("unchecked")
	protected void link(PlasmaDataObject target, PlasmaDataObject source, 
    		Property sourceProperty)
    {       
        if (log.isDebugEnabled())
            log.debug("linking source (" + source.getUUIDAsString() + ") "
                    + source.getType().getURI() + "#" + source.getType().getName() 
                    + "." + sourceProperty.getName() + "->("
                    + target.getUUIDAsString() + ") "
                    + target.getType().getURI() + "#" + target.getType().getName());
        
        if (sourceProperty.isMany()) {
        	PlasmaProperty opposite = (PlasmaProperty)sourceProperty.getOpposite();
        	if (opposite != null && !opposite.isMany() && target.isSet(opposite)) {
                PlasmaDataObject existingOpposite = (PlasmaDataObject)target.get(opposite);
                if (existingOpposite != null) {
                	if (log.isDebugEnabled())
                        log.debug("encountered existing opposite (" + existingOpposite.getType().getName()
                            + ") value found while creating link " + source.toString()  
                            + "." + sourceProperty.getName() + "->"
                            + target.toString() + " - no link created");
        		    return;
                }
        	}

        	List<DataObject> list = source.getList(sourceProperty);
            if (list == null) 
                list = new ArrayList<DataObject>();  
            if (!list.contains(target)) {
            	// check if any existing list members already have the opposite property set
            	for (DataObject existing : list) {
                	if (opposite != null && !opposite.isMany() && existing.isSet(opposite)) {
                        PlasmaDataObject existingOpposite = (PlasmaDataObject)existing.get(opposite);
                        if (existingOpposite != null) {
                        	if (log.isDebugEnabled())
                                log.debug("encountered existing opposite (" + existingOpposite.getType().getName()
                                    + ") value found while creating link " + source.toString()  
                                    + "." + sourceProperty.getName() + "->"
                                    + target.toString() + " - no link created");
                		    return; 
                        }
                	}
            	}
                if (log.isDebugEnabled())
                    log.debug("adding target " + source.toString()  
                        + "." + sourceProperty.getName() + "->" + target.toString());
                if (target.getContainer() == null) {
                	if (source.getDataGraph().getRootObject().equals(target)) {
                		log.warn("linking root object, " + target.toString() + " to source, "
                			+ source.toString());
                	}
                	else
                	    throw new IllegalStateException("the given target has no container: " + target.toString());
                }
                list.add(target);   
                source.setList(sourceProperty, list); 
            }
        }
        else {
            PlasmaDataObject existing = (PlasmaDataObject)source.get(sourceProperty);
            if (existing == null) {
                if (target.getContainer() == null) {
                	if (source.getDataGraph().getRootObject().equals(target)) {
                		log.warn("linking root object, " + target.toString() + " to source, "
                			+ source.toString());
                	}
                	else
                	    throw new IllegalStateException("the given target has no container: " + target.toString());
                }
                source.set(sourceProperty, target); 
            }
            else
                if (!existing.equals(target))
                	if (log.isDebugEnabled())
                        log.debug("encountered existing (" + existing.getType().getName()
	                        + ") value found while creating link " + source.toString() 
	                        + "." + sourceProperty.getName() + "->"
	                        + target.toString());
        }
    }
	
    /**
     * Returns the selection graph as a single result.
     * @param rowKey the row key
     * @param tableReader the table reader
     * @param dataObject the 
     * @return the selection graph as a single result.
     * @throws IOException if a remote or network exception occurs.
     * @see GraphFetchColumnFilterAssembler
     */
    protected Result fetchGraph(byte[] rowKey, TableReader tableReader, 
    		PlasmaType type) throws IOException {
        Get row = new Get(rowKey);
        FilterList rootFilter = new FilterList(
    			FilterList.Operator.MUST_PASS_ALL);
        row.setFilter(rootFilter);
		GraphFetchColumnFilterAssembler columnFilterAssembler = 
    		new GraphFetchColumnFilterAssembler(
    			this.selection, type);
        rootFilter.addFilter(columnFilterAssembler.getFilter());
    	long before = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
        	log.debug(FilterUtil.printFilterTree(rootFilter));
        if (log.isDebugEnabled() ) 
            log.debug("executing get...");
        
        Result result = tableReader.getConnection().get(row);
        if (result == null || result.isEmpty())
        	throw new GraphServiceException("expected result from table "
                + tableReader.getTable().getName() + " for row '"
        		+ new String(rowKey) + "'");        
    	if (log.isTraceEnabled()) {
  	        log.trace("row: " + new String(result.getRow()));              	  
      	    for (KeyValue keyValue : result.list()) {
      	    	log.trace("\tkey: " 
      	    		+ new String(keyValue.getQualifier())
      	    	    + "\tvalue: " + new String(keyValue.getValue()));
      	    }
    	}
        long after = System.currentTimeMillis();
        if (log.isDebugEnabled() ) 
            log.debug("assembled 1 results ("
        	    + String.valueOf(after - before) + ")");
        return result;
	}
}
