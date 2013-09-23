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
package org.cloudgraph.hbase.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.CreatedCommitComparator;
import org.cloudgraph.common.service.DuplicateRowException;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.cloudgraph.hbase.io.FederatedGraphWriter;
import org.cloudgraph.hbase.io.FederatedWriter;
import org.cloudgraph.hbase.io.RowWriter;
import org.cloudgraph.hbase.io.TableWriter;
import org.cloudgraph.hbase.io.TableWriterCollector;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaEdge;
import org.plasma.sdo.PlasmaNode;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaSetting;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.DataGraphDispatcher;
import org.plasma.sdo.access.RequiredPropertyException;
import org.plasma.sdo.access.provider.common.DeletedObjectCollector;
import org.plasma.sdo.access.provider.common.ModifiedObjectCollector;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.core.NullValue;
import org.plasma.sdo.core.SnapshotMap;
import org.plasma.sdo.helper.DataConverter;
import org.plasma.sdo.profile.ConcurrencyType;
import org.plasma.sdo.profile.ConcurrentDataFlavor;
import org.plasma.sdo.profile.KeyType;

import sorts.InsertionSort;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * Propagates changes to a {@link commonj.sdo.DataGraph data graph} including
 * any number of creates (inserts), modifications (updates) and deletes
 * across one or more HBase table rows. 
 * <p>
 * For new (created) data graphs, a row key {org.cloudgraph.hbase.key.HBaseRowKeyFactory factory} 
 * is used to create a new composite HBase row key. The row key generation is
 * driven by a configured CloudGraph row key {@link org.cloudgraph.config.RowKeyModel
 * model} for a specific HTable {@link org.cloudgraph.config.Table configuration}.
 * A minimal set of {@link org.cloudgraph.state.GraphState state} information is 
 * persisted with each new data graph.     
 * </p>
 * <p>
 * For data graphs with any other combination of changes, e.g. 
 * data object modifications, deletes, etc... an existing HBase
 * row key is fetched using an HBase <a href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html" target="#">Get</a> 
 * operation.
 * </p>
 * @see org.cloudgraph.hbase.io.FederatedWriter
 * @see org.cloudgraph.common.key.GraphRowKeyFactory
 * @see org.cloudgraph.common.key.GraphColumnKeyFactory
 * @see org.cloudgraph.state.GraphState
 * 
 * @author Scott Cinnamond
 * @since 0.5
 */
public class GraphDispatcher
    implements DataGraphDispatcher 
{
    private static Log log = LogFactory.getLog(GraphDispatcher.class);
    private static List<Row> EMPTY_ROW_LIST = new ArrayList<Row>();
    private SnapshotMap snapshotMap;
    private String username;
    private ServiceContext context;
    private FederatedWriter graphWriter;
    
    @SuppressWarnings("unused")
    private GraphDispatcher() {}
    
    public GraphDispatcher(ServiceContext context, 
    		SnapshotMap snapshotMap, 
            String username) {
    	this.context = context;
        this.snapshotMap = snapshotMap;
        this.username = username;
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
            	        
	        
	        List<CoreDataObject> createdList = new ArrayList<CoreDataObject>();
	        for (DataObject changed : changeSummary.getChangedDataObjects()) {
	            if (changeSummary.isCreated(changed))
	            	createdList.add((CoreDataObject)changed);
	        }
	        CoreDataObject[] createdArray = new CoreDataObject[createdList.size()];
	        createdList.toArray(createdArray);
	        Comparator<CoreDataObject> comparator = new CreatedCommitComparator();
	        InsertionSort sort = new InsertionSort();
	        sort.sort(createdArray, comparator);
            PlasmaDataObject[] created = new PlasmaDataObject[createdArray.length];
            for (int i = 0; i < createdArray.length; i++)
            	created[i] = createdArray[i];
	        
	        ModifiedObjectCollector modified = new ModifiedObjectCollector(dataGraph);
	        DeletedObjectCollector deleted = new DeletedObjectCollector(dataGraph);

			TableWriterCollector collector = 
				new TableWriterCollector(dataGraph, 
					created, modified, deleted);
			
	        this.graphWriter = new FederatedGraphWriter( 
	        	dataGraph, collector,
	        	this.context.getMarshallingContext());
	        
    		this.create(dataGraph, created, this.graphWriter);    		    		
    		this.modify(dataGraph, modified, this.graphWriter);
    		this.delete(dataGraph, deleted, this.graphWriter);
            
    		// marshal state
            for (TableWriter tableWriter : graphWriter.getTableWriters()) {
            	for (RowWriter rowWriter : tableWriter.getAllRowWriters()) {
            		
            		if (rowWriter.isRootCreated()) {
            			String rootUUID = ((PlasmaDataObject)rowWriter.getRootDataObject()).getUUIDAsString();
            		    rowWriter.getRow().add(tableWriter.getTable().getDataColumnFamilyNameBytes(), 
                            Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME),
                            Bytes.toBytes(rootUUID)); 
            		}
            		else if (rowWriter.isRootDeleted()) {
            			continue; // row goes away - no need to marshal state
            		}
            		
            		String xml = rowWriter.getGraphState().marshal();
                	if (log.isDebugEnabled())
                		log.debug("marshalled state: " + xml);

            		rowWriter.getRow().add(Bytes.toBytes(tableWriter.getTable().getDataColumnFamilyName()), 
                            Bytes.toBytes(GraphState.STATE_COLUMN_NAME),
                            Bytes.toBytes(xml));    		        		
            	}
            }

    		// commit to HBase
    		for (TableWriter tableWriter : this.graphWriter.getTableWriters()) {
        		List<Row> actions = new ArrayList<Row>(); 
    			for (RowWriter rowWriter : tableWriter.getAllRowWriters()) {
    				if (!rowWriter.isRootDeleted()) {
    				    actions.add(rowWriter.getRow());
    				    if (rowWriter.hasRowDelete())
    				    	actions.add(rowWriter.getRowDelete());
    				}
    				else {
    					// add a toumbstone column
    					if (log.isDebugEnabled())
    						log.debug("adding toumbstone for root "
    							+ rowWriter.getRootDataObject().toString());
    					rowWriter.getRow().add(
    						tableWriter.getTable().getDataColumnFamilyNameBytes(), 
    						Bytes.toBytes(GraphState.TOUMBSTONE_COLUMN_NAME), 
    						Bytes.toBytes(this.snapshotMap.getSnapshotDate().getTime()));
    					actions.add(rowWriter.getRow());     					 
    					actions.add(rowWriter.getRowDelete()); //     					
    				}
    			}
    			tableWriter.getConnection().batch(actions);
    			tableWriter.getConnection().flushCommits();
    		}
    		
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
    
    private void delete(
    		DataGraph dataGraph,
    		DeletedObjectCollector deleted,
    		FederatedWriter graphWriter) throws IllegalAccessException, IOException 
    {
        for (PlasmaDataObject dataObject : deleted.getResult()) {

        	RowWriter rowWriter = graphWriter.getRowWriter(dataObject);
            TableWriter tableWriter = rowWriter.getTableWriter();        	
        	if (log.isDebugEnabled())
        		log.debug("deleting: " + dataObject.getType().getURI() 
        			+ "#" + dataObject.getType().getName());
        	
        	delete(dataGraph, dataObject, 
        		 graphWriter, tableWriter, rowWriter);
            rowWriter.getGraphState().archiveSequence(dataObject);
        }
    }
    
    private void modify(
    		DataGraph dataGraph,
    		ModifiedObjectCollector modified,
    		FederatedWriter graphWriter) throws IllegalAccessException, IOException 
    {
    	for (PlasmaDataObject dataObject : modified.getResult()) {
        	RowWriter rowWriter = graphWriter.getRowWriter(dataObject);
            TableWriter tableWriter = rowWriter.getTableWriter();
        	if (log.isDebugEnabled())
        		log.debug("validating modifications: " + dataObject.getType().getURI() 
        			+ "#" + dataObject.getType().getName());
            this.validateModifications(dataGraph, dataObject, rowWriter);
            
        	if (log.isDebugEnabled())
        		log.debug("modifying: " + dataObject.getType().getURI() 
        			+ "#" + dataObject.getType().getName());
            this.update(dataGraph, dataObject, 
            	graphWriter, tableWriter, rowWriter);
    	}
    }
    	
    private void create(DataGraph dataGraph,
    		PlasmaDataObject[] created,
    		FederatedWriter graphWriter) throws IOException, IllegalAccessException {

		for (PlasmaDataObject dataObject : created) {
        	RowWriter rowWriter = graphWriter.getRowWriter(dataObject);
        	
        	rowWriter.getGraphState().addSequence(dataObject);
        }
        
        for (PlasmaDataObject dataObject : created) {
        	RowWriter rowWriter = graphWriter.getRowWriter(dataObject);
            TableWriter tableWriter = rowWriter.getTableWriter();        	
        	if (log.isDebugEnabled())
        		log.debug("creating: " + dataObject.getType().getURI() 
        			+ "#" + dataObject.getType().getName());
            create(dataGraph, dataObject, 
            	graphWriter,  tableWriter,  rowWriter);
        }
        
    }    
    
    private void create(DataGraph dataGraph, 
    	PlasmaDataObject dataObject,
    	FederatedWriter graphWriter,
    	TableWriter tableWriter,
    	RowWriter rowWriter) throws IOException, IllegalAccessException 
    {
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("creating " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");
        PlasmaNode dataNode = (PlasmaNode)dataObject;
        
        this.updateKeys(dataObject,  type, rowWriter); 
        this.updateOrigination(dataObject, type, rowWriter);
        this.updateOptimistic(dataObject, type, rowWriter);
                
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
            if (property.isReadOnly())
    		    throw new IllegalAccessException("attempt to add read-only property, "
        			+ type.getURI() + "#" + type.getName() + "." + property.getName());
        	
    		byte[] valueBytes = null;
        	if (!property.getType().isDataType()) {
        		List <PlasmaEdge> edges = dataNode.getEdges(property);

        		List <PlasmaEdge> stateEdges = this.createEdgeState(dataObject, dataNode,
            		property, edges, graphWriter,
            		tableWriter, rowWriter);
        		
        		if (stateEdges.size() > 0) {
        		    rowWriter.getGraphState().addEdges(dataNode, stateEdges);
        		
        		    // create a formatted column value
        		    // for this edge collection
        		    valueBytes = this.createEdgeValueBytes(
        			    dataNode, stateEdges,  rowWriter);
        		    
                	this.updateCell(rowWriter, 
                    		rowWriter.getRow(), dataObject, 
                            property, valueBytes);
        		}
        	}
        	else {
                valueBytes = HBaseDataConverter.INSTANCE.toBytes(
                	property, value);
            	this.updateCell(rowWriter, 
                		rowWriter.getRow(), dataObject, 
                        property, valueBytes);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("inserting " + dataObject.getType().getName()); 
        }
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
                   + "' is required to update entity " 
                   + dataObject);
            //FIXME: check optimistic/pessimistic concurrency
        	this.setOptimistic(dataObject, type, snapshotDate);
        }
        else if (dataGraph.getChangeSummary().isDeleted(dataObject)) {
            //FIXME: check optimistic/pessimistic concurrency
        }
    }
    
    //FIXME: this method is confusing and overloaded
    private List <PlasmaEdge> createEdgeState(
        PlasmaDataObject dataObject,	
    	PlasmaNode dataNode,
    	Property property, 
    	List <PlasmaEdge> edges,
    	FederatedWriter graphWriter,
        TableWriter tableWriter, 
        RowWriter rowWriter) throws IOException
    {
    	List <PlasmaEdge> result = new ArrayList<PlasmaEdge>();
    	
		Property oppositeProperty = property.getOpposite();
		
		PlasmaType dataObjectType = (PlasmaType)dataObject.getType();
	    
		boolean thisTypeBound = CloudGraphConfig.getInstance().findTable(
	    		dataObjectType.getQualifiedName()) != null;
		
		for (PlasmaEdge edge : edges) {			
			
    		PlasmaDataObject opposite = edge.getOpposite(dataNode).getDataObject();
			//edge.getDirection()
    		
    		PlasmaType oppositeType = (PlasmaType)opposite.getType();
		    boolean oppositeTypeBound = CloudGraphConfig.getInstance().findTable(
		    		oppositeType.getQualifiedName()) != null;
		    			
			RowWriter oppositeRowWriter = graphWriter.findRowWriter(opposite);			
		    if (oppositeRowWriter == null) {
		    	oppositeRowWriter = graphWriter.createRowWriter(opposite);
		    }
	        TableWriter oppositeTableWriter = oppositeRowWriter.getTableWriter();

	        // If the opposite not bound to a table and
	        // it is already linked within another row, 
	        // don't write the edge. This graph does not
	        // own it. 
		    if (oppositeTypeBound || oppositeRowWriter.equals(rowWriter)) 
		    	result.add(edge);	        
	        
	        // maps opposite UUID to its row key
		    // in the state for this row
	        if (oppositeTypeBound)
	            rowWriter.getGraphState().addRowKey(opposite,
	        	    oppositeTableWriter.getTable(),
		            oppositeRowWriter.getRowKey());
	        
	        // Maps this DO uuid to current row key in opposite row
	        // If this data object is not "bound" to a
	        // table, disregard as it will have no opposite row
	        // but will be contained within this row
	        if (oppositeProperty != null && thisTypeBound) {
	            oppositeRowWriter.getGraphState().addRowKey(dataObject,
	            	tableWriter.getTable(),
	        		rowWriter.getRowKey());
	        }
		}
		return result;
    }
    
    private byte[] createEdgeValueBytes(
    	PlasmaNode dataNode,	
    	List <PlasmaEdge> edges,
        RowWriter rowWriter) throws IOException
    {
		String valueStr = rowWriter.getGraphState().marshalEdges(
				dataNode, edges);
    	return Bytes.toBytes(valueStr);
    }
    
    private void validateModifications(DataGraph dataGraph, PlasmaDataObject dataObject,
    		RowWriter rowWriter) 
        throws IllegalAccessException
    {   
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("updating " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");
        PlasmaNode dataNode = (PlasmaNode)dataObject;

        PlasmaType rootType = (PlasmaType)rowWriter.getRootDataObject().getType();
        DataGraphConfig dataGraphConfig = CloudGraphConfig.getInstance().getDataGraph(rootType.getQualifiedName());

        List<Property> properties = type.getProperties();
        for (Property p : properties)
        {
        	PlasmaProperty property = (PlasmaProperty)p;
            
            Object oldValue = dataGraph.getChangeSummary().getOldValue(dataObject, property);
            if (oldValue == null)
            	continue; // it's not been modified   
            
            if (property.isReadOnly())
    		    throw new IllegalAccessException("attempt to modify read-only property, "
        			+ type.getURI() + "#" + type.getName() + "." + property.getName());

            UserDefinedRowKeyFieldConfig userDefinedField = 
            	dataGraphConfig.findUserDefinedRowKeyField(property);
            if (userDefinedField != null) {
    		    throw new IllegalAccessException("attempt to modify row-key property, "
            		+ type.getURI() + "#" + type.getName() + "." + property.getName()
            		+ " - this property is configured as a row-key field for table '"
            		+ dataGraphConfig.getTable().getName() + "'");
            }
            //FIXME: what if an entire entity is deleted which is part
            // of the row key. Detect this. Or added for that matter. 
        }    
    }
     
    private void update(DataGraph dataGraph, PlasmaDataObject dataObject, 
    		FederatedWriter graphWriter,
    		TableWriter tableWriter,
        	RowWriter rowWriter) 
        throws IllegalAccessException, IOException
    {   
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("updating " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");
        PlasmaNode dataNode = (PlasmaNode)dataObject;

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
        updateOptimistic(dataObject, type, rowWriter);


        List<Property> properties = type.getProperties();
        for (Property p : properties)
        {
        	PlasmaProperty property = (PlasmaProperty)p;
            if (property.isKey(KeyType.primary))
                continue; // cannot be modified on an update
            
            if (property.getConcurrent() != null)
                continue; // processed above           
    		
            Object oldValue = dataGraph.getChangeSummary().getOldValue(dataObject, property);
            if (oldValue == null)
            	continue; // it's not been modified   
            
            if (property.isReadOnly())
    		    throw new IllegalAccessException("attempt to modify read-only property, "
        			+ type.getURI() + "#" + type.getName() + "." + property.getName());

            Object value = dataObject.get(property);
            if (value == null)
            	continue;
            
        	if (value instanceof NullValue) {
        		value = null;
        	}
        	
    		byte[] valueBytes = null;
        	if (!property.getType().isDataType()) {
    			// get old value setting/value - can be List or single data-object
        		PlasmaSetting setting = (PlasmaSetting)oldValue;
    			Object oldOppositeValue = setting.getValue();
        		
        		List <PlasmaEdge> edges = dataNode.getEdges(property);
        		
        		// Modifying this property but add all 
        		// its edges to row graph state
        		List<PlasmaEdge> stateEdges = this.createEdgeState(dataObject, dataNode,
            		property, edges, graphWriter,
            		tableWriter, rowWriter);
        		if (!property.isMany()) {
        			// move old value if exists into graph state history
        			if (!(oldOppositeValue instanceof NullValue)) {
    	    			if (!(oldOppositeValue instanceof List)) {
    	        			DataObject oldOpposite = (DataObject)oldOppositeValue;
    	    		    	rowWriter.getGraphState().archiveSequence(oldOpposite);
    	    		    	rowWriter.getGraphState().archiveRowKey(oldOpposite);
    	    			}
    	        		else 
                            throw new GraphServiceException("unexpected List as old value for property, "  
                            	+ property.toString());
        			}
        			// add the new value into graph state
              		rowWriter.getGraphState().addEdges(dataNode,  stateEdges);        		
            		// Create a formatted column value
            		// for this edge. 
            		valueBytes = this.createEdgeValueBytes(
            			dataNode, stateEdges,  rowWriter);
        		}
        		else {
        			HashMap<String, DataObject> oldEdgeMap = getOldEdgeMap(
        				oldOppositeValue, property);
        			
        			// add the new values into graph state
              		rowWriter.getGraphState().addEdges(dataNode,  stateEdges);        		

              		Edge[] updatedEdges = rowWriter.getGraphState().createEdges(dataNode, stateEdges);
        			HashSet<Edge> updatedEdgeHash = new HashSet<Edge>(
                			Arrays.asList(updatedEdges));
        			
        			// fetch the existing edges from data store
        		    byte[] existingValue = rowWriter.fetchColumnValue(dataObject, property);
        		    Edge[] existingEdges = null;
        		    if (existingValue != null && existingValue.length > 0)
        		        existingEdges = rowWriter.getGraphState().unmarshalEdges(existingValue);
        		    
        		    // merge
        		    List<Edge> list = new ArrayList<Edge>();
    		    	for (Edge updated : updatedEdges)
    		    		list.add(updated);
    		    	if (existingEdges != null)
	        		    for (Edge existing : existingEdges) {
	        		    	if (!updatedEdgeHash.contains(existing)) {
	        		    		// only exclude edge if explicitly removed by user
	        		    		DataObject oldDataObject = oldEdgeMap.get(existing.getUuid());
	        		    		if (oldDataObject == null) {
	        		    			list.add(existing);
	        		    		}
	        		    		else { // edge is obsolete - move to history
	        	    		    	rowWriter.getGraphState().archiveSequence(oldDataObject);
	        	    		    	rowWriter.getGraphState().archiveRowKey(oldDataObject);
	        		    		}
	        		    	} // else added it already above	        		    	
	        		    }
        		    
        		    Edge[] resultEdges = new Edge[list.size()];
        		    list.toArray(resultEdges);
        		    String valueString = rowWriter.getGraphState().marshalEdges(resultEdges);
        		    valueBytes = valueString.getBytes(GraphState.charset);
        		}
         	}
        	else {
        		// FIXME: research best way to encode multiple
        		// primitives as bytes
                valueBytes = HBaseDataConverter.INSTANCE.toBytes(
                	    property, value);
            }
        	
        	this.updateCell(rowWriter,
        		rowWriter.getRow(), dataObject, 
                property, valueBytes);
        }    
    }
    
    private HashMap<String, DataObject> getOldEdgeMap(Object oldValue, 
    		Property property) 
    {
		HashMap<String, DataObject> result = null;
		if (!(oldValue instanceof NullValue)) {
			if (oldValue instanceof List) {
				@SuppressWarnings("unchecked")
				List<DataObject> oldValueList = (List<DataObject>)oldValue;
				result = new HashMap<String, DataObject>(oldValueList.size());
				for (DataObject dataObject : oldValueList)
					result.put(((PlasmaDataObject)dataObject).getUUIDAsString(),
							dataObject);
			}
    		else {
    			result = new HashMap<String, DataObject>(1);
    			PlasmaDataObject oldOpposite = (PlasmaDataObject)oldValue;
				result.put(oldOpposite.getUUIDAsString(),
						oldOpposite);
    		}
		}
    	return result;
    }
 
    private void delete(DataGraph dataGraph, PlasmaDataObject dataObject, 
    	FederatedWriter graphWriter,
    	TableWriter context,
        RowWriter rowContext) throws IOException
    {
        PlasmaType type = (PlasmaType)dataObject.getType();
        if (log.isDebugEnabled())
            log.debug("deleting " + type.getName() + " '" + ((PlasmaDataObject)dataObject).getUUIDAsString()+ "'");

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
        	byte[] qualifier = rowContext.getColumnKeyFactory().createColumnKey(dataObject, 
        			property);
        	        	
            if (log.isDebugEnabled())
                log.debug("deleting column: " 
                		+ Bytes.toString(qualifier));
            rowContext.getRowDelete().deleteColumns(context.getTable().getDataColumnFamilyNameBytes(), 
        		qualifier);    	
        }    
    }    
        
    private void updateCell(
    		RowWriter rowContext, 
    		Put row, PlasmaDataObject dataObject, 
    		Property property, 
    		byte[] value) throws IOException
    {
    	PlasmaProperty prop = (PlasmaProperty)property;
    	byte[] qualifier = rowContext.getColumnKeyFactory().createColumnKey(
    		dataObject, prop);
    	TableConfig table = rowContext.getTableWriter().getTable();
        if (log.isDebugEnabled()) 
            log.debug("setting "
                + dataObject.getType().getName()
                + "." + property.getName() 
                + " as " + table.getName() + "."
                + new String(qualifier, table.getCharset()) + " = '"
                + new String(value, table.getCharset())
                + "'"); 
    	    	
     	// FIXME: adding NULL string as null on update to preserve history..is this correct?
    	row.add(table.getDataColumnFamilyNameBytes(), 
    		qualifier,
            value);     	
    }    
    
    private void setOrigination(PlasmaDataObject dataObject, 
    		PlasmaType type) {
        // FIXME - could be a reference to a user
        Property originationUserProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.user);
        if (originationUserProperty != null) {
        	if (!originationUserProperty.isReadOnly())
        	    dataObject.set(originationUserProperty, username);
        	else
                ((CoreDataObject)dataObject).setValue(originationUserProperty.getName(), username); // FIXME: bypassing readonly modification detection
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
        	if (!originationTimestampProperty.isReadOnly())
        	    dataObject.set(originationTimestampProperty, 
        			snapshot);
        	else
                ((CoreDataObject)dataObject).setValue(originationTimestampProperty.getName(), snapshot); // FIXME: bypassing readonly modification detection
        }
        else
            if (log.isDebugEnabled()) 
                log.debug("could not find origination date property for type, "
                    + type.getURI() + "#" + type.getName());      	
    }  
    
    private void updateKeys(PlasmaDataObject dataObject, 
    	PlasmaType type,
    	RowWriter rowWriter) throws IOException 
    {
        UUID uuid = ((CoreDataObject)dataObject).getUUID();
        if (uuid == null)
            throw new GraphServiceException("expected UUID for created entity '" 
            		+ type.getName() + "'");
        List<Property> pkList = type.findProperties(KeyType.primary);
        if (pkList == null || pkList.size() == 0)
        	return; // don't care for NOSQL services

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
                    pk = rowWriter.getGraphState().findSequence(dataObject);
                    if (pk == null)
                        pk = rowWriter.getGraphState().addSequence(dataObject); 
                    pk = DataConverter.INSTANCE.convert(targetPriKeyProperty.getType(), pk);
                    byte[] pkBytes = HBaseDataConverter.INSTANCE.toBytes(targetPriKeyProperty, pk);
                    this.updateCell(rowWriter, 
                    		rowWriter.getRow(), dataObject, 
                    	targetPriKeyProperty, pkBytes);
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
                this.updateCell(rowWriter, 
                	rowWriter.getRow(), dataObject, 
                	targetPriKeyProperty, pkBytes);
            }   
            if (log.isDebugEnabled()) {
                log.debug("mapping UUID '" + uuid + "' to pk (" + String.valueOf(pk) + ")");
            }
            snapshotMap.put(uuid, new PropertyPair(targetPriKeyProperty, pk));
        }    	
    }
    
    private void updateOrigination(PlasmaDataObject dataObject, PlasmaType type,
    		RowWriter rowContext) throws IOException {
        // FIXME - could be a reference to a user
        Property originationUserProperty = type.findProperty(ConcurrencyType.origination, 
            	ConcurrentDataFlavor.user);
        if (originationUserProperty != null) {
        	if (!originationUserProperty.isReadOnly())
        	    dataObject.set(originationUserProperty, username);
        	else
                ((CoreDataObject)dataObject).setValue(originationUserProperty.getName(), username); // FIXME: bypassing readonly modification detection
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
            this.updateCell(rowContext, rowContext.getRow(), dataObject, 
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
        	if (!concurrencyUserProperty.isReadOnly())
        	    dataObject.set(concurrencyUserProperty, username);
        	else
        		((CoreDataObject)dataObject).setValue(concurrencyUserProperty.getName(), username);
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
        	if (!concurrencyTimestampProperty.isReadOnly())
        	    dataObject.set(concurrencyTimestampProperty, snapshot);
        	else
        		((CoreDataObject)dataObject).setValue(concurrencyTimestampProperty.getName(), snapshot);
        }    	
    }

    //FIXME: deal with optimistic concurrency in HBase later
    private void updateOptimistic(PlasmaDataObject dataObject, PlasmaType type,
    		RowWriter rowContext) throws IOException 
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
            this.updateCell(rowContext, rowContext.getRow(), dataObject, 
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
            this.updateCell(rowContext, rowContext.getRow(), dataObject, 
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
