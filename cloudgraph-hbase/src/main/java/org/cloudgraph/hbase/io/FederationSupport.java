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
package org.cloudgraph.hbase.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cloudgraph.hbase.key.CompositeRowKeyFactory;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataObject;

public abstract class FederationSupport {
	protected Map<DataObject, RowWriter> rowWriterMap = new HashMap<DataObject, RowWriter>();	
	
	protected FederationSupport() {
		
	}
	
	protected RowWriter getContainerRowWriter(DataObject target) {		
		RowWriter result = null;
		DataObject source = target.getContainer();
		while (source != null) {
			result = this.rowWriterMap.get(source);
			if (result != null)
				return result;
			source = source.getContainer();
		}
		throw new OperationException("no row writer associated with data object, " 
	    	    + String.valueOf(target) + ", or its containment ancestry");
	}

	protected RowWriter findContainerRowWriter(DataObject target) {		
		RowWriter result = null;
		DataObject source = target.getContainer();
		while (source != null) {
			result = this.rowWriterMap.get(source);
			if (result != null)
				return result;
			source = source.getContainer();
		}
		return null;
	}
	
	protected RowWriter createRowWriter(TableWriter tableWriter, DataObject target) throws IOException
	{
		RowWriter rowWriter = null;
		if (target.getDataGraph().getRootObject().equals(target)) {// root object
			// if a new root
	        if (target.getDataGraph().getChangeSummary().isCreated(target)) {    				        
		        CompositeRowKeyFactory rowKeyGen = new CompositeRowKeyFactory(
			        	(PlasmaType)target.getType());		        
			    byte[] rowKey = rowKeyGen.createRowKeyBytes(target); 
	        	rowWriter = this.createRowWriter(tableWriter, target, rowKey);
	            ((PlasmaDataGraph)target.getDataGraph()).setId(rowWriter.getRowKey()); // FIXME: snapshot map for this? 
	        }
	        else { // expect existing row-key
	        	byte[] rowKey = (byte[])((PlasmaDataGraph)target.getDataGraph()).getId();
		        if (rowKey == null)
		        	throw new IllegalStateException("could not find existing row key for data graph "
		        		+ "with root type, "
		        		+ target.getType().getURI() + "#" + target.getType().getName());
		        rowWriter = this.createRowWriter(tableWriter,
		        		target, rowKey);
		    }
		}
		else {
	        CompositeRowKeyFactory rowKeyGen = new CompositeRowKeyFactory(
		        	(PlasmaType)target.getType());	        
		    byte[] rowKey = rowKeyGen.createRowKeyBytes(target); 
		    rowWriter = this.createRowWriter(tableWriter, target, rowKey);
		}	
		return rowWriter;
	}
	
	protected RowWriter createRowWriter(TableWriter tableContext,
			DataObject dataObject,
	    	byte[] rowKey) throws IOException
    {
    	
    	RowWriter rowContext = 
    		new GraphRowWriter(rowKey, dataObject, tableContext);

    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	tableContext.addRowWriter(uuid, rowContext);
    	
    	return rowContext;
    }

	protected RowWriter addRowWriter( 
    		DataObject dataObject,
    		TableWriter tableContext) throws IOException
    {
        CompositeRowKeyFactory rowKeyGen = new CompositeRowKeyFactory(
	        	(PlasmaType)dataObject.getType());
        
        byte[] rowKey = rowKeyGen.createRowKeyBytes(dataObject);
        RowWriter rowContext = new GraphRowWriter(
    		rowKey, dataObject, tableContext);
    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	tableContext.addRowWriter(uuid, rowContext);
    	return rowContext;
    }
}
