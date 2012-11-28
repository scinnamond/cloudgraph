package org.cloudgraph.hbase.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.key.CompositeRowKeyFactory;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.provider.common.CreatedObjectCollector;
import org.plasma.sdo.access.provider.common.DeletedObjectCollector;
import org.plasma.sdo.access.provider.common.ModifiedObjectCollector;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * Traverses the change summary of the 
 * given <s href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html">Data Graph<a> collecting
 * a list of {@link TableWriter} elements with nested {@link RowWriter}
 * elements. Each {@link RowWriter} represents the root of a new
 * or existing <a href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html">Data Graph</a> within
 * the HBase table associated with the parent {@link TableWriter}.
 * <p>
 * Graph nodes which are "unbound" (not assigned directly to a specific table) are assigned based on the
 * first parent node within the graph. Other parent nodes (if exist) are ignored. 
 * </p>
 * @see org.cloudgraph.config.CloudGraphConfig
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.io.TableWriter
 * @see org.cloudgraph.hbase.io.RowWriter
 * 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class TableWriterCollector extends FederationSupport {
    
    private static Log log = LogFactory.getLog(TableWriterCollector.class);
	private DataGraph dataGraph;
	private ChangeSummary changeSummary;
	private PlasmaDataObject root;
	private TableWriter rootTableWriter;
	private Map<String, TableWriter> result = new HashMap<String, TableWriter>();
	private CreatedObjectCollector created;   	
	private ModifiedObjectCollector modified;
	private DeletedObjectCollector deleted;

	public TableWriterCollector(DataGraph dataGraph,
	        CreatedObjectCollector created,   	
	        ModifiedObjectCollector modified,
	        DeletedObjectCollector deleted) throws IOException {
		this.dataGraph = dataGraph;
		this.changeSummary = dataGraph.getChangeSummary();
		this.root = (PlasmaDataObject)dataGraph.getRootObject();
		this.created = created;
		this.modified = modified;
		this.deleted = deleted;
		collect();
	}
		
	public List<TableWriter> getTableWriters() {
		List<TableWriter> list = 
				new ArrayList<TableWriter>(this.result.size());
		for (TableWriter table : this.result.values())
			list.add(table);
		return list;
	}

	public TableWriter getRootTableWriter() {
		return rootTableWriter;
	}
	
	public Map<DataObject, RowWriter> getRowWriterMap() {
		return this.rowWriterMap;		
	}
	
	private void collect() throws IOException {
		// May need opposite writers just for sake of getting its
		// row writer and getting the row key to link into
		// modified graph.
		// FIXME: how do we detect this condition? Can we create
		// a row reader here instead of a writer??
		
		// collect all "bound" data objects up front
		for (DataObject dataObject : this.changeSummary.getChangedDataObjects())
		{
			PlasmaType type = (PlasmaType)dataObject.getType();
			TableConfig table = CloudGraphConfig.getInstance().findTable(type);
			if (table != null)
				associate(table, dataObject);
		}
		
        for (PlasmaDataObject dataObject : this.created.getResult()) {
			PlasmaType type = (PlasmaType)dataObject.getType();
			TableConfig table = CloudGraphConfig.getInstance().findTable(type);
        	if (log.isDebugEnabled())
        		log.debug("collecting unbound created: " + dataObject);
			if (table == null)
				associate(dataObject);
        }
        
        for (PlasmaDataObject dataObject : this.modified.getResult()) {
			PlasmaType type = (PlasmaType)dataObject.getType();
			TableConfig table = CloudGraphConfig.getInstance().findTable(type);
        	if (log.isDebugEnabled())
        		log.debug("collecting unbound modified: " + dataObject);
			if (table == null)
				associate(dataObject);
        }
        
        
        List<PlasmaDataObject> deletedResult = this.deleted.getResult();
        for (int i = deletedResult.size()-1; i >= 0; i--) {
        	PlasmaDataObject dataObject = deletedResult.get(i);
			PlasmaType type = (PlasmaType)dataObject.getType();
			TableConfig table = CloudGraphConfig.getInstance().findTable(type);
        	if (log.isDebugEnabled())
        		log.debug("collecting unbound deleted: " + dataObject);
			if (table == null)
				associate(dataObject);
        }
	}
	
	/**
	 * Links the given "unbound" data object to a row writer. An unbound
	 * data object is not directly associated with a table but only
	 * as part of a containment hierarchy within a graph. 
	 * @param target the data object
	 */
	private void associate(DataObject target) {
	
    	RowWriter rowWriter = this.rowWriterMap.get(target);
    	if (rowWriter == null) {
    		rowWriter = getContainerRowWriter(target);
    		if (log.isDebugEnabled())
    		    log.debug("associating " 
    		       + target.getType().getURI() + "#" + target.getType().getName() + " with table '"
    		       + rowWriter.getTableWriter().getTable().getName() + "'");
    		rowWriter.addDataObject(target);
    	    this.rowWriterMap.put(target, rowWriter);
    	}
    	else {
    		if (log.isDebugEnabled())
    		    log.debug("type " 
    		       + target.getType().getURI() + "#" + target.getType().getName() + " already associated with table '"
    		       + rowWriter.getTableWriter().getTable().getName() + "' by means of another source/parent");
    	}
	}
	
	
	/**
	 * Links the given "bound" data object to a row writer. A bound
	 * data object is directly associated with a table 
	 * @param target the data object
	 */
	private void associate(TableConfig table, DataObject target) throws IOException {
		        
        // a table is configured with this type as root
		TableWriter tableWriter = (TableWriter)result.get(table.getName());
		RowWriter rowWriter = null;
		if (tableWriter == null) {
			tableWriter = new GraphTableWriter(
	        		table);
			rowWriter = createRowWriter(tableWriter, target);
	        tableWriter = rowWriter.getTableWriter();
		    result.put(tableWriter.getTable().getName(), tableWriter);
		}
		else { // just add a row writer to existing table writer    			
			rowWriter = this.addRowWriter(
					target, tableWriter);
		}
		if (log.isDebugEnabled())
		    log.debug("associating (root) " 
		       + target.getType().getURI() + "#" + target.getType().getName() + " with table '"
		       + rowWriter.getTableWriter().getTable().getName() + "'");
		
    	this.rowWriterMap.put(target, rowWriter);
		
		if (this.dataGraph.getRootObject().equals(target)) // root object
			this.rootTableWriter = tableWriter;
	}	
	
}
