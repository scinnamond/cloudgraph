package org.cloudgraph.hbase.io;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.DuplicateRowException;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.MissingRowException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.key.CompositeRowKeyFactory;
import org.cloudgraph.state.GraphState;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;

/**
 * @author Scott Cinnamond
 * @since 0.5.1
 */
@Deprecated
public class OperationHelper {

    private static Log log = LogFactory.getLog(OperationHelper.class);

    public RowWriter createRowWriter(DataObject dataObject) throws IOException {
        CompositeRowKeyFactory rowKeyGen = new CompositeRowKeyFactory(
	        	(PlasmaType)dataObject.getType());
        
	    byte[] rowKey = rowKeyGen.createRowKeyBytes(dataObject); 
	    return createRowWriter(dataObject, rowKey);    
    }

	public RowWriter createRowWriter(DataObject dataObject,
    	byte[] rowKey) throws IOException
    {
        PlasmaType type = (PlasmaType)dataObject.getType();
    	TableWriter tableContext = createTableWriter(type);
    	
    	RowWriter rowContext = 
    		new GraphRowWriter(rowKey, dataObject, tableContext);

    	String uuid = ((PlasmaDataObject)dataObject).getUUIDAsString();
    	tableContext.addRowWriter(uuid, rowContext);
    	
    	return rowContext;
    }

	public TableWriter createTableWriter(PlasmaType type) 		
    {
        TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(
        		type.getQualifiedName());
        
    	TableWriter tableContext = new GraphTableWriter(
    		tableConfig);
    	
    	return tableContext;
    }
    
	public RowWriter addRowWriter( 
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
    
	
	/**
     * Initializes a graph state by querying for a row
     * based on the given row key and either creating a new (empty)
     * graph state for an entirely new graph, or otherwise initializing
     * a graph state based on state or state and management columns in
     * the existing returned row.   
     * 
     * @param rowKey the row key
     * @param dataGraph the data graph
     * @param changeSummary the change summary
     * @return the graph state
     * @throws IOException
     * @throws DuplicateRowException for a new graph if a row already exists
     * for the given row key
     * @throws GraphServiceException where except for a new graph, if no row
     * exists for the given row key
     */
    public GraphState createGraphState(byte[] rowKey, 
    		DataObject dataObject,
    		ChangeSummary changeSummary,
    		TableConfig tableConfig,
    		HTableInterface con) throws IOException
    {
    	GraphState graphState;
		// --ensure row exists unless a new row/graph
		// --use empty get with only necessary "state" management columns
		Get existing = new Get(rowKey);
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.UUID_MAP_COLUMN_NAME));
		existing.addColumn(tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.KEY_MAP_COLUMN_NAME));
		
		Result result = con.get(existing);
		
		// if entirely new graph for the given 
		// federated or sub-graph root
		if (changeSummary.isCreated(dataObject)) {
    		if (!result.isEmpty())
    			throw new DuplicateRowException("no row for id '"
    				+ Bytes.toString(rowKey) + "' expected when creating new row for table '"
    				+ tableConfig.getTable().getName() + "'"); 
    		graphState = new GraphState();
        }
		else {
    		if (result.isEmpty())
    			throw new MissingRowException(tableConfig.getTable().getName(),
    					Bytes.toString(rowKey));            	
    		byte[] uuids = result.getValue(Bytes.toBytes(tableConfig.getDataColumnFamilyName()), 
    				Bytes.toBytes(GraphState.UUID_MAP_COLUMN_NAME));
            if (uuids != null) {
            	if (log.isDebugEnabled())
            		log.debug(GraphState.UUID_MAP_COLUMN_NAME
            			+ ": " + Bytes.toString(uuids));
            }
            else
    			throw new GraphServiceException("expected column '"
    				+ GraphState.UUID_MAP_COLUMN_NAME + " for row " 
    				+ Bytes.toString(rowKey) + "'"); 
            
    		byte[] externalKeyMap = result.getValue(Bytes.toBytes(tableConfig.getDataColumnFamilyName()), 
    				Bytes.toBytes(GraphState.KEY_MAP_COLUMN_NAME));
            if (externalKeyMap != null) {
               	if (log.isDebugEnabled())
            		log.debug(GraphState.KEY_MAP_COLUMN_NAME
            			+ ": " + Bytes.toString(externalKeyMap));
                 }
            
    		// key map can be overwritten with empty string
    		if (externalKeyMap == null || externalKeyMap.length == 0)
                graphState = new GraphState(
                	Bytes.toString(uuids));
    		else
    			graphState = new GraphState(
    				Bytes.toString(uuids),
    				Bytes.toString(externalKeyMap));
    	}   		
    	return graphState;
    }
}
