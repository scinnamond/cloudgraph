package org.cloudgraph.hbase.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

/**
 * Traverses the given <s href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html">Data Graph<a> collecting
 * a list of {@link TableWriter} elements with nested {@link RowWriter}
 * elements. Each {@link RowWriter} represents the root of a new
 * or existing <s href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html">Data Graph<a> within
 * the HBase table associated with the parent {@link TableWriter}.
 * A stack oriented approach during graph traversal which leverages the {@link TableConfig} facility 
 * which detects and assigns all data objects within any arbitrary 
 * federated graph to a specific table row writer. 
 * 
 * @see org.cloudgraph.config.CloudGraphConfig
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.io.TableWriter
 * @see org.cloudgraph.hbase.io.RowWriter
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class TableWriterCollector implements PlasmaDataGraphVisitor {
    
	private DataGraph dataGraph;
	private ChangeSummary changeSummary;
	private PlasmaDataObject root;
	private OperationHelper operHelper;
	private TableWriter rootTableWriter;
	private Stack<WriterFrame> stack = new Stack<WriterFrame>();
	private Map<DataObject, RowWriter> rowWriterMap = new HashMap<DataObject, RowWriter>();	
	private Map<String, TableWriter> result = new HashMap<String, TableWriter>();

	public TableWriterCollector(DataGraph dataGraph,
			FederatedOperation federatedOperation) {
		this.operHelper = new OperationHelper(federatedOperation);
		this.dataGraph = dataGraph;
		this.changeSummary = dataGraph.getChangeSummary();
		this.root = (PlasmaDataObject)dataGraph.getRootObject();
		this.root.accept(this);
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
	
	@Override
	public void visit(DataObject target, DataObject source,
			String sourcePropertyName, int level) {
		
		// May need opposite just for sake of getting its
		// row writer and getting the row key to link into
		// modified graph.
		// FIXME: how do we detect this condition? Can we create
		// a row reader here??
		//if (!(this.changeSummary.isCreated(target) || 
		//	this.changeSummary.isModified(target) || 
		//	this.changeSummary.isDeleted(target))) {
		//	return; // no changes
		//}
		
		PlasmaType type = (PlasmaType)target.getType();
		
		TableConfig table = CloudGraphConfig.getInstance().findTable(
				type.getQualifiedName());
        if (table == null) {
        	WriterFrame top = this.stack.peek();
        	while (top.getLevel() > level) {
        		this.stack.pop();
        		top = this.stack.peek();
        	}
        	// for case of multiple parents
        	if (!top.getRowWriter().contains(target)) {
        	    top.getRowWriter().addDataObject(target);
        	    this.rowWriterMap.put(target, top.getRowWriter());
        	}
        	return;
        }
        
        // a table is configured with this type as root
		try {
    		TableWriter tableWriter = (TableWriter)result.get(table.getName());
    		RowWriter rowWriter = null;
    		if (tableWriter == null) {
    			rowWriter = createRowWriter(target, source, 
    				sourcePropertyName, level);
		        tableWriter = rowWriter.getTableWriter();
			    result.put(tableWriter.getTable().getName(), tableWriter);
    		}
    		else { // just add a row writer to existing table writer    			
    			rowWriter = this.operHelper.addRowWriter(
    					target, tableWriter);
    		}
    		
    		WriterFrame node = new WriterFrame(rowWriter, level);
    		stack.push(node);
        	this.rowWriterMap.put(target, rowWriter);
    		
			if (source == null) // traverse root
				this.rootTableWriter = tableWriter;
		} catch (IOException e) {
			throw new OperationException(e);
		}
	}
	
	private RowWriter createRowWriter(DataObject target, DataObject source,
			String sourcePropertyName, int level) throws IOException
	{
		RowWriter rowWriter = null;
		if (source == null) {// traverse root
			// if a new root
	        if (changeSummary.isCreated(target)) {    				        
	        	rowWriter = this.operHelper.createRowWriter(target);
	            ((PlasmaDataGraph)dataGraph).setId(rowWriter.getRowKey()); // FIXME: snapshot map for this? 
	        }
	        else { // expect existing row-key
	        	byte[] rowKey = (byte[])((PlasmaDataGraph)dataGraph).getId();
		        if (rowKey == null)
		        	throw new IllegalStateException("could not find existing row key for data graph "
		        		+ "with root type, "
		        		+ target.getType().getURI() + "#" + target.getType().getName());
		        rowWriter = this.operHelper.createRowWriter(
		        		target, rowKey);
		    }
		}
		else {
		    rowWriter = this.operHelper.createRowWriter(target);
		}	
		return rowWriter;
	}
	
	private class WriterFrame {
		private RowWriter rowWriter;
		private int level;
		public WriterFrame(RowWriter rowWriter, int level) {
			super();
			this.rowWriter = rowWriter;
			this.level = level;
		}
		public RowWriter getRowWriter() {
			return rowWriter;
		}
		public int getLevel() {
			return level;
		}		
	}
}
