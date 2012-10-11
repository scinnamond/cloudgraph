package org.cloudgraph.hbase.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.key.GraphStatefullColumnKeyFactory;
import org.cloudgraph.common.service.DuplicateRowException;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.common.service.GraphState;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.key.CompositeRowKeyFactory;
import org.cloudgraph.hbase.key.StatefullColumnKeyFactory;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphDispatcher;
import org.plasma.sdo.access.RequiredPropertyException;
import org.plasma.sdo.access.provider.common.CreatedObjectCollector;
import org.plasma.sdo.access.provider.common.DeletedObjectCollector;
import org.plasma.sdo.access.provider.common.ModifiedObjectCollector;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.core.NullValue;
import org.plasma.sdo.core.SnapshotMap;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.profile.ConcurrencyType;
import org.plasma.sdo.profile.ConcurrentDataFlavor;
import org.plasma.sdo.profile.KeyType;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * Propagates changes to a {@link commonj.sdo.DataGraph data graph} including
 * any number of creates (inserts), modifications (updates) and deletes
 * to a single HBase table row. 
 * <p>
 * For new (created) data graphs, a row key {org.cloudgraph.hbase.key.HBaseRowKeyFactory factory} 
 * is used to create a new composite HBase row key. The row key generation is
 * driven by a configured CloudGraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}.
 * A minimal set of {@link org.cloudgraph.common.service.GraphState state} information is 
 * persisted with each new data graph.     
 * </p>
 * <p>
 * For data graphs with any other combination of changes, e.g. 
 * data object modifications, deletes, etc... an existing HBase
 * row key is fetched using an HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html" target="#">Get</a> 
 * operation
 * 
 * </p>
 * <p>
 * </p>
 * @see org.cloudgraph.common.key.GraphRowKeyFactory
 * @see org.cloudgraph.common.key.GraphColumnKeyFactory
 * @see org.cloudgraph.common.service.GraphState
 */
public class HBaseGraphDispatcher
    implements DataGraphDispatcher 
{
    private static Log log = LogFactory.getLog(HBaseGraphDispatcher.class);
    private static List<Row> EMPTY_ROW_LIST = new ArrayList<Row>();
    private HTableInterface con;
    private TableConfig tableConfig;
    private SnapshotMap snapshotMap;
    private String username;
    
    @SuppressWarnings("unused")
    private HBaseGraphDispatcher() {}
    
    public HBaseGraphDispatcher(SnapshotMap snapshotMap, 
            String username, TableConfig tableConfig, 
            HTableInterface con) {
        this.snapshotMap = snapshotMap;
        this.username = username;
        this.tableConfig = tableConfig;
        this.con = con;        
    }
    
    public void close()
    {
    }
     
    /**
     * Propagates changes to the given <a href="http://docs.plasma-sdo.org/api/org/plasma/sdo/PlasmaDataGraph.html" target="#">data graph</a> including
     * any number of creates (inserts), modifications (updates) and deletes
     * to a single HBase table row. 
     * @return a map of internally managed concurrency property values and data 
     * store generated keys.
     * @throws DuplicateRowException if for a new data graph, the generated row key
     * already exists in the HBase table configured . 
     */
    public SnapshotMap commit(DataGraph dataGraph) {
        
        if (username == null || username.length() == 0)
            throw new IllegalArgumentException("expected username param not, '" + String.valueOf(username) + "'");
        else
            if (log.isDebugEnabled()) {
                log.debug("current user is '" + username + "'");
            }
        
        PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)dataGraph.getChangeSummary();
        if (log.isDebugEnabled())
            log.debug(changeSummary.toString());
        
        try {
	        for (DataObject changed : changeSummary.getChangedDataObjects()) 
	        	this.checkConcurrency(dataGraph, (PlasmaDataObject)changed);           
            
	        byte[] rowKey = null;
	        if (changeSummary.isCreated(dataGraph.getRootObject())) {
		        CompositeRowKeyFactory rowKeyGen = new CompositeRowKeyFactory(
		        	(PlasmaType)dataGraph.getRootObject().getType());
		        rowKey = rowKeyGen.createRowKeyBytes(dataGraph);
	            ((PlasmaDataGraph)dataGraph).setId(rowKey); // FIXME: use snapshot map
	        }
	        else 
	        	rowKey = (byte[])((PlasmaDataGraph)dataGraph).getId();
           
	        if (rowKey == null)
	        	throw new IllegalStateException("could not find or create row key");
            if (log.isDebugEnabled())
    			log.debug("row-id: " + Bytes.toString(rowKey));
            
    		GraphState graphState = this.initGraphState(rowKey, 
    				dataGraph, changeSummary);
    		
    		StatefullColumnKeyFactory colGen = new StatefullColumnKeyFactory(
    			(PlasmaType)dataGraph.getRootObject().getType(), graphState);
    		List<Row> created = this.create(rowKey, dataGraph, changeSummary, graphState, colGen);
    		List<Row> modified = this.modify(rowKey, dataGraph, changeSummary, graphState, colGen);
    		List<Row> deleted = this.delete(rowKey, dataGraph, changeSummary, graphState, colGen);
            
    		List<Row> actions = new ArrayList<Row>(); 
    		actions.addAll(created);
    		actions.addAll(modified);
    		actions.addAll(deleted);
            
    		// commit to HBase
            this.con.batch(actions);
            this.con.flushCommits();

            return snapshotMap;
        }                                                         
        catch(IOException e) {                         
            throw new DataAccessException(e);                         
        }                                                         
        catch(InterruptedException e) {                         
            throw new DataAccessException(e);                         
        }        
        catch(IllegalAccessException e) {                         
            throw new DataAccessException(e);                         
        }               
    }
    
    private List<Row> delete(byte[] rowKey,  
    		DataGraph dataGraph,
    		PlasmaChangeSummary changeSummary,
    		GraphState graphState, StatefullColumnKeyFactory colGen) throws IllegalAccessException 
    {
        DeletedObjectCollector deleted = new DeletedObjectCollector(dataGraph);
        if (deleted.getResult().size() == 0)
            return EMPTY_ROW_LIST;

        List<Row> result = new ArrayList<Row>();;
		// if delete the row/graph
		if (changeSummary.isDeleted(dataGraph.getRootObject())) {
    		Delete rowDelete = new Delete(rowKey);
    		result.add(rowDelete);
		}
		else {
    		Delete columnDelete = new Delete(rowKey);
            for (PlasmaDataObject dataObject : deleted.getResult()) {
                delete(dataGraph, dataObject, graphState, colGen, columnDelete);
                graphState.removeSequence(dataObject);
            }
            result.add(columnDelete);
    		Put uuidMapUpdate = new Put(rowKey);
    		uuidMapUpdate.add(this.tableConfig.getDataColumnFamilyNameBytes(), 
                    Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME),
                    Bytes.toBytes(graphState.formatUUIDMap())); 
    		result.add(uuidMapUpdate);
		}
		return result;
    }
    
    private List<Row> modify(byte[] rowKey,  
    		DataGraph dataGraph,
    		PlasmaChangeSummary changeSummary,
    		GraphState graphState, StatefullColumnKeyFactory colGen) throws IllegalAccessException 
    {
        ModifiedObjectCollector modified = new ModifiedObjectCollector(dataGraph);
        if (modified.getResult().size() == 0)
            return EMPTY_ROW_LIST;
    	List<Row> result = new ArrayList<Row>();
    	Put modify = new Put(rowKey);
    	for (PlasmaDataObject dataObject : modified.getResult())
            update(dataGraph, dataObject, graphState, colGen, modify);
    	result.add(modify);
		return result;
    }
    	
    private List<Row> create(byte[] rowKey,   
    		DataGraph dataGraph,
    		PlasmaChangeSummary changeSummary,
    		GraphState graphState, StatefullColumnKeyFactory colGen) {
        CreatedObjectCollector created = new CreatedObjectCollector(dataGraph);   	
		if (created.getResult().size() == 0) 
			return EMPTY_ROW_LIST;		

        List<Row> result = new ArrayList<Row>();;
		Put create = new Put(rowKey);
    	// if new graph
        if (changeSummary.isCreated(dataGraph.getRootObject()))	{
            String uuid = (String)((PlasmaDataObject)dataGraph.getRootObject()).getUUIDAsString();
            if (uuid == null)
                throw new GraphServiceException("expected UUID for data object '" 
                		+ dataGraph.getRootObject().getType().getName() + "'");
            for (PlasmaDataObject dataObject : created.getResult())
            	graphState.createSequence(dataObject);
            
            for (PlasmaDataObject dataObject : created.getResult())
                create(dataGraph, dataObject, graphState, colGen, create);
             
    		create.add(this.tableConfig.getDataColumnFamilyNameBytes(), 
                    Bytes.toBytes(CloudGraphConstants.ROOT_UUID_COLUMN_NAME),
                    Bytes.toBytes(uuid));    		        		
    
    		create.add(Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
                Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME),
                Bytes.toBytes(graphState.formatUUIDMap()));    		        		
    	}
        else { // partially new graph
            for (PlasmaDataObject dataObject : created.getResult()) {
            	graphState.createSequence(dataObject);
            }
            for (PlasmaDataObject dataObject : created.getResult()) {
                create(dataGraph, dataObject, graphState, colGen, create);
            }
            create.add(Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
                    Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME),
                    Bytes.toBytes(graphState.formatUUIDMap())); 
        }
        result.add(create);
		return result;
    }
    
    private GraphState initGraphState(byte[] rowKey, 
    		DataGraph dataGraph,
    		PlasmaChangeSummary changeSummary) throws IOException
    {
    	GraphState graphState;
		// --ensure row exists unless a new row/graph
		// --use empty get with only necessary "state" column
		Get existing = new Get(rowKey);
		existing.addColumn(this.tableConfig.getDataColumnFamilyNameBytes(), 
				Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME));
		
		Result result = this.con.get(existing);
		
		// if entirely new graph
		if (changeSummary.isCreated(dataGraph.getRootObject())) {
    		if (!result.isEmpty())
    			throw new DuplicateRowException("no row for id '"
    				+ Bytes.toString(rowKey) + "' expected"); 
    		graphState = new GraphState();
        }
		else {
    		if (result.isEmpty())
    			throw new GraphServiceException("expected row for id '"
    					+ Bytes.toString(rowKey) + "'");            	
    		byte[] uuids = result.getValue(Bytes.toBytes(this.tableConfig.getDataColumnFamilyName()), 
    				Bytes.toBytes(GraphState.STATE_MAP_COLUMN_NAME));
            if (uuids != null) {
            	if (log.isDebugEnabled())
            		log.debug(GraphState.STATE_MAP_COLUMN_NAME
            			+ ": " + new String(uuids));
            }
            else
    			throw new GraphServiceException("expected column '"
    				+ GraphState.STATE_MAP_COLUMN_NAME + " for row " 
    				+ Bytes.toString(rowKey) + "'");            	
            graphState = new GraphState(new String(uuids));
    	}   		
    	return graphState;
    }
    
    private void checkConcurrency(DataGraph dataGraph, PlasmaDataObject dataObject) {
        PlasmaType type = (PlasmaType)dataObject.getType();
        
        if (dataGraph.getChangeSummary().isCreated(dataObject)) {
            this.setOrigination(dataObject, type);
        }
        else if (dataGraph.getChangeSummary().isModified(dataObject)) {
            Timestamp snapshotDate = (Timestamp)((CoreDataObject)dataObject).getValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP);                                     
            if (snapshotDate == null)                                                                    
                throw new RequiredPropertyException("instance property '" + CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP                
                   + "' is required to update entity '" 
                   + type.getURI() + "#" + type.getName() + "'");
            //FIXME: check optimistic/pessimistic concurrency
        	this.setOptimistic(dataObject, type, snapshotDate);
        }
        else if (dataGraph.getChangeSummary().isDeleted(dataObject)) {
            //FIXME: check optimistic/pessimistic concurrency
        }
    }
    
    private void create(DataGraph dataGraph, PlasmaDataObject dataObject, GraphState graphState, GraphStatefullColumnKeyFactory colGen, Put row) {
        PlasmaType type = (PlasmaType)dataObject.getType();
        String uuid = (String)((CoreDataObject)dataObject).getUUIDAsString();
        if (uuid == null)
            throw new DataAccessException("expected UUID for created entity '" 
            		+ type.getName() + "'");
        if (log.isDebugEnabled())
            log.debug("creating " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");
        
        List<Property> pkList = type.findProperties(KeyType.primary);
        if (pkList == null || pkList.size() == 0)
            throw new DataAccessException("no pri-key properties found for type '" 
                    + dataObject.getType().getName() + "'");

        for (Property pkp : pkList) {
            PlasmaProperty targetPriKeyProperty = (PlasmaProperty)pkp;
            Object pk = dataObject.get(targetPriKeyProperty);
            if (pk == null)
            {
            	DataFlavor dataFlavor = targetPriKeyProperty.getDataFlavor();
            	switch (dataFlavor) {
            	case integral:
                    if (log.isDebugEnabled()) {
                        log.debug("getting seq-num for " + type.getName());
                    }
                    pk = graphState.createSequence(dataObject); 
                    byte[] pkBytes = HBaseDataConverter.INSTANCE.toBytes(targetPriKeyProperty, pk);
                    this.updateCell(colGen, row, dataObject, 
                    		targetPriKeyProperty, pkBytes);
                    //entity.set(targetPriKeyProperty.getName(), pk);                 
                    ((CoreDataObject)dataObject).setValue(targetPriKeyProperty.getName(), pk); // FIXME: bypassing modification detection on pri-key
            		break;
            	default:
                    throw new DataAccessException("found null primary key property '"
                    		+ targetPriKeyProperty.getName() + "' for type, "
                            + type.getURI() + "#" + type.getName());  
            	}
            }
            else
            {
                byte[] pkBytes = HBaseDataConverter.INSTANCE.toBytes(targetPriKeyProperty, pk);
                this.updateCell(colGen, row, dataObject, 
                		targetPriKeyProperty, pkBytes);
            }   
            if (log.isDebugEnabled()) {
                log.debug("mapping UUID '" + uuid + "' to pk (" + String.valueOf(pk) + ")");
            }
            // FIXME: multiple PK's not supported
            snapshotMap.put(uuid, pk); // map new PK back to UUID
        }
        
        this.updateOrigination(dataObject, type, colGen, row);
        this.updateOptimistic(dataObject, type, colGen, row);
                
        List<Property> properties = type.getProperties();
        for (Property p : properties)
        {
        	PlasmaProperty property = (PlasmaProperty)p;
            if (property.isKey(KeyType.primary))
                continue; // processed above
            
            if (property.getConcurrent() != null)
                continue; // processed above           
        	
            Object value = dataObject.get(property);
            if (value == null)
            	continue;
            
        	if (value instanceof NullValue) {
        		value = null;
        	}
        	
    		byte[] valueBytes = null;
        	if (!property.getType().isDataType()) {
        		List <PlasmaEdge> edges = ((PlasmaNode)dataObject).getEdges(property);
        		String data = graphState.formatEdges(dataObject, edges);
        		valueBytes = Bytes.toBytes(data);
        	}
        	else {
                valueBytes = HBaseDataConverter.INSTANCE.toBytes(
                	property, value);
            }
        	this.updateCell(colGen, row, dataObject, 
                	property, valueBytes);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("inserting " + dataObject.getType().getName()); 
        }
    }
    
    private void update(DataGraph dataGraph, PlasmaDataObject dataObject, GraphState graphState, GraphStatefullColumnKeyFactory colGen, Put row) 
        throws IllegalAccessException
    {   
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("updating " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");

        List<Property> pkList = type.findProperties(KeyType.primary);
        if (pkList == null || pkList.size() == 0)
            throw new DataAccessException("no pri-key properties found for type '" 
                    + dataObject.getType().getName() + "'");

        // FIXME: get rid of cast - define instance properties in 'base type'
        Timestamp snapshotDate = (Timestamp)((CoreDataObject)dataObject).getValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP);                                     
        if (snapshotDate == null)                                                                    
            throw new RequiredPropertyException("instance property '" + CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP                
               + "' is required to update entity '" 
               + type.getURI() + "#" + type.getName() + "'"); 
        else
            if (log.isDebugEnabled())
                log.debug("snapshot date: " + String.valueOf(snapshotDate)); 
        
        checkLock(dataObject, type, snapshotDate);
        updateOptimistic(dataObject, type, colGen, row);
        
        List<Property> properties = type.getProperties();
        for (Property p : properties)
        {
        	PlasmaProperty property = (PlasmaProperty)p;
            if (property.isKey(KeyType.primary))
                continue; // cannot be modified on an update
            
            if (property.getConcurrent() != null)
                continue; // processed above           
    		
            Object oldValue = dataGraph.getChangeSummary().getOldValue(dataObject, property);
            if (oldValue != null) { // it's been modified  
            	if (!property.isReadOnly()) {
                    Object value = dataObject.get(property);
                    if (value == null)
                    	continue;
                    
                	if (value instanceof NullValue) {
                		value = null;
                	}
                	
            		byte[] valueBytes = null;
                	if (!property.getType().isDataType()) {
                		List <PlasmaEdge> edges = ((PlasmaNode)dataObject).getEdges(property);
                		String data = graphState.formatEdges(dataObject, edges);
                		valueBytes = Bytes.toBytes(data);
                	}
                	else {
                        valueBytes = HBaseDataConverter.INSTANCE.toBytes(
                        	property, value);
                    }
                	this.updateCell(colGen, row, dataObject, 
                        	property, valueBytes);
            	}
            	else
            		throw new IllegalAccessException("attempt to modify read-only property, "
            			+ type.getURI() + "#" + type.getName() + "." + property.getName());
            }
        }    
    }
 
    private void delete(DataGraph dataGraph, PlasmaDataObject dataObject, GraphState graphState, GraphStatefullColumnKeyFactory colGen, Delete row)
    {
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("deleting " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");
        List<Property> pkList = type.findProperties(KeyType.primary);
        if (pkList == null || pkList.size() == 0)
            throw new DataAccessException("no pri-key properties found for type '" 
                    + dataObject.getType().getName() + "'");

        // FIXME: get rid of cast - define instance properties in 'base type'
        Timestamp snapshotDate = (Timestamp)((CoreDataObject)dataObject).getValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP);                                     
        if (snapshotDate == null)                                                                    
            throw new RequiredPropertyException("property '" + CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP                
               + "' is required to update entity '" + type.getName() + "'"); 
        
        this.checkLock(dataObject, type, snapshotDate);
        this.checkOptimistic(dataObject, type, snapshotDate);

        PlasmaProperty concurrencyUserProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
            	ConcurrentDataFlavor.user);
        if (concurrencyUserProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency (username) property for type, "
                    + type.getURI() + "#" + type.getName());          
        }
        
        PlasmaProperty concurrencyTimestampProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.time);
        if (concurrencyTimestampProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
        } 
                        
        List<Property> properties = type.getProperties();
        for (Property p : properties)
        {
        	PlasmaProperty property = (PlasmaProperty)p;
        	byte[] qualifier = colGen.createColumnKey(dataObject, 
        			property);
        	
        	
            if (log.isDebugEnabled())
                log.debug("deleting column: " 
                		+ Bytes.toString(qualifier));  
        	row.deleteColumns(this.tableConfig.getDataColumnFamilyNameBytes(), 
        			qualifier);    	
        }    
    }    
        
    private void updateCell(GraphStatefullColumnKeyFactory colGen, 
    		Put row, PlasmaDataObject dataObject, Property property, 
    		byte[] value)
    {
    	PlasmaProperty prop = (PlasmaProperty)property;
    	byte[] qualifier = colGen.createColumnKey(
    		dataObject, prop);
    	    	
     	// FIXME: adding NULL string as null on update to preserve history..is this correct?
    	row.add(this.tableConfig.getDataColumnFamilyNameBytes(), 
    		qualifier,
            value);     	
    }    
    
    private void setOrigination(PlasmaDataObject dataObject, PlasmaType type) {
        // FIXME - could be a reference to a user
        Property originationUserProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.user);
        if (originationUserProperty != null) {
        	dataObject.set(originationUserProperty, username);
        } 
        else
            if (log.isDebugEnabled()) 
                log.debug("could not find origination (username) property for type, "
                    + type.getURI() + "#" + type.getName());  

        Property originationTimestampProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.time);
        if (originationTimestampProperty != null) {
        	Date dateSnapshot = new Date(
        		this.snapshotMap.getSnapshotDate().getTime());
        	Object snapshot = DataConverter.INSTANCE.convert(
        		originationTimestampProperty.getType(), dateSnapshot);
        	dataObject.set(originationTimestampProperty, 
        			snapshot);
        }
        else
            if (log.isDebugEnabled()) 
                log.debug("could not find origination date property for type, "
                    + type + "#" + type.getName());      	
    }  
    
    private void updateOrigination(PlasmaDataObject dataObject, PlasmaType type,
    		GraphStatefullColumnKeyFactory colGen, Put row) {
        // FIXME - could be a reference to a user
        Property originationUserProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.user);
        if (originationUserProperty != null) {
        	dataObject.set(originationUserProperty, username);
        } 
        else
            if (log.isDebugEnabled()) 
                log.debug("could not find origination (username) property for type, "
                    + type.getURI() + "#" + type.getName());  

        Property originationTimestampProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.time);
        if (originationTimestampProperty != null) {
        	Date dateSnapshot = new Date(
            	this.snapshotMap.getSnapshotDate().getTime());
            Object snapshot = DataConverter.INSTANCE.convert(
            	originationTimestampProperty.getType(), dateSnapshot);
            byte[] bytes = HBaseDataConverter.INSTANCE.toBytes(originationTimestampProperty, 
            		snapshot);
            this.updateCell(colGen, row, dataObject, 
            		originationTimestampProperty, 
            		bytes);
        }
        else
            if (log.isDebugEnabled()) 
                log.debug("could not find origination date property for type, "
                    + type + "#" + type.getName());      	
    }
    
    private void setOptimistic(PlasmaDataObject dataObject, PlasmaType type, 
    		Timestamp snapshotDate) 
    {
        PlasmaProperty concurrencyUserProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
            	ConcurrentDataFlavor.user);
        if (concurrencyUserProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency (username) property for type, "
                    + type.getURI() + "#" + type.getName());          
        }
        else
        {
        	dataObject.set(concurrencyUserProperty, username);	
        }
        
        PlasmaProperty concurrencyTimestampProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.time);
        if (concurrencyTimestampProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
        } 
        else
        {
        	Date dateSnapshot = new Date(
            		this.snapshotMap.getSnapshotDate().getTime());
            Object snapshot = DataConverter.INSTANCE.convert(
            	concurrencyTimestampProperty.getType(), dateSnapshot);
        	dataObject.set(concurrencyTimestampProperty, snapshot);     	
        }    	
    }

    //FIXME: deal with optimistic concurrency in HBase later
    private void updateOptimistic(PlasmaDataObject dataObject, PlasmaType type,
    		GraphStatefullColumnKeyFactory colGen, Put row) 
    {
        PlasmaProperty concurrencyUserProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
            	ConcurrentDataFlavor.user);
        if (concurrencyUserProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency (username) property for type, "
                    + type.getURI() + "#" + type.getName());          
        }
        else
        {
            byte[] bytes = HBaseDataConverter.INSTANCE.toBytes(concurrencyUserProperty, 
            		username);
            this.updateCell(colGen, row, dataObject, 
            		concurrencyUserProperty,   
            		bytes);       	
        }
        
        PlasmaProperty concurrencyTimestampProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.time);
        if (concurrencyTimestampProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
        } 
        else
        {
        	Date dateSnapshot = new Date(
            		this.snapshotMap.getSnapshotDate().getTime());
            Object snapshot = DataConverter.INSTANCE.convert(
            	concurrencyTimestampProperty.getType(), dateSnapshot);
            byte[] bytes = HBaseDataConverter.INSTANCE.toBytes(concurrencyTimestampProperty, 
            	snapshot);
            this.updateCell(colGen, row, dataObject, 
            		concurrencyTimestampProperty,   
            		bytes);       	
        }    	
    }

    //FIXME: deal with optimistic concurrency in HBase later
    private void checkOptimistic(PlasmaDataObject dataObject, PlasmaType type,
    		Timestamp snapshotDate) 
    {
        PlasmaProperty concurrencyUserProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
            	ConcurrentDataFlavor.user);
        if (concurrencyUserProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency (username) property for type, "
                    + type.getURI() + "#" + type.getName());          
        }
        
        PlasmaProperty concurrencyTimestampProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.optimistic, 
        	ConcurrentDataFlavor.time);
        if (concurrencyTimestampProperty == null) {
            if (log.isDebugEnabled())
                log.debug("could not find optimistic concurrency timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
        } 
    }
    
    //FIXME: deal with pessimistic concurrency in HBase later
    private void checkLock(PlasmaDataObject dataObject, PlasmaType type, 
    		Timestamp snapshotDate) {
        PlasmaProperty lockingUserProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.pessimistic, 
            	ConcurrentDataFlavor.user);
        if (lockingUserProperty == null)
            if (log.isDebugEnabled())
                log.debug("could not find locking user property for type, "
                    + type.getURI() + "#" + type.getName());  
        
        PlasmaProperty lockingTimestampProperty = (PlasmaProperty)type.findProperty(ConcurrencyType.pessimistic, 
            	ConcurrentDataFlavor.time);
        if (lockingTimestampProperty == null)
            if (log.isDebugEnabled())
                log.debug("could not find locking timestamp property for type, "
                    + type.getURI() + "#" + type.getName());  
    	
    }
    
    private void logChangeInfo(PlasmaChangeSummary changeSummary)
    {
        if (log.isDebugEnabled()) {
	        List<DataObject> list = changeSummary.getChangedDataObjects();
	        DataObject[] changed = new DataObject[list.size()];
	        list.toArray(changed);
            StringBuffer buf = new StringBuffer();
            buf.append('\n');
            for (int i = 0; i < changed.length; i++) {
                DataObject dataObject = changed[i];
                if (changeSummary.isCreated(dataObject))
                    buf.append("created: ");
                else if (changeSummary.isModified(dataObject))
                    buf.append("modified: ");
                else if (changeSummary.isDeleted(dataObject))
                    buf.append("deleted: ");
                buf.append(dataObject.getType().getName() + " (" + dataObject.toString() + ")");
                buf.append(" depth: " + changeSummary.getPathDepth(dataObject));
                
                buf.append('\n');
            }
            log.debug("commit list: " + buf.toString());
        }
    }
}
