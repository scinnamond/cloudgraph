package org.cloudgraph.hbase.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.plasma.sdo.PlasmaType;

import commonj.sdo.DataObject;
import commonj.sdo.Type;


/**
 * Encapsulates one or more
 * graph table reader components for federation across
 * multiple physical tables and/or physical table rows. 
 * Maps physical configured
 * table names to respective table readers. In most usage
 * scenarios, a "root" table reader is typically added 
 * initially, then other reader are incrementally added
 * as association target types are detected and found configured as
 * graph roots within another distinct table context. 
 * <p>
 * Acts as a container for one or more {@link TableReader} elements
 * encapsulating a set of component table read operations 
 * for federated assembly across multiple tables, or a single table
 * in the most simple (degenerate) case.  
 * </p>
 * @see org.cloudgraph.hbase.io.GraphTableReader
 * @see org.cloudgraph.state.GraphTable
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public class FederatedGraphReader implements FederatedReader {

	private TableReader rootReader;
	/** maps table names to table readers */
	private Map<String, TableReader> tableReaderMap = new HashMap<String, TableReader>();
	/** maps qualified type names to table readers */
	private Map<QName, TableReader> typeTableReaderMap = new HashMap<QName, TableReader>();
	
	// maps data objects to row readers
	private Map<DataObject, RowReader> rowReaderMap = new HashMap<DataObject, RowReader>();
	
	@SuppressWarnings("unused")
	private FederatedGraphReader() {}
	
	public FederatedGraphReader(Type rootType, List<Type> types) {
	    PlasmaType root = (PlasmaType)rootType;
	    TableConfig rootTable = CloudGraphConfig.getInstance().getTable(
	    		root.getQualifiedName());
	    
	    TableReader tableReader = new GraphTableReader(rootTable);
	    this.tableReaderMap.put(tableReader.getTable().getName(), 
	    	tableReader);
		this.rootReader = tableReader;
	    this.typeTableReaderMap.put(((PlasmaType)rootType).getQualifiedName(), 
	    		this.rootReader);
		
		for (Type t : types) {
		    PlasmaType type = (PlasmaType)t;
		    TableConfig table = CloudGraphConfig.getInstance().findTable(
				type.getQualifiedName());
		    if (table == null || table.getName().equals(rootTable.getName()))
		    	continue; // added above
		    tableReader = new GraphTableReader(table);
		    this.tableReaderMap.put(tableReader.getTable().getName(), 
		    	tableReader);		    
		    this.typeTableReaderMap.put(type.getQualifiedName(), tableReader);
		}
	}	
	
	/**
	 * Returns the table reader for the given 
	 * configured table name, or null of not exists.
	 * @param tableName the name of the configured table. 
	 * @return the table reader for the given 
	 * configured table name, or null of not exists.
	 */
	@Override
	public TableReader getTableReader(String tableName) {
		return this.tableReaderMap.get(tableName);
	}

	/**
	 * Returns the table reader for the given 
	 * qualified type name, or null if not exists.
	 * @param qualifiedTypeName the qualified type name. 
	 * @return the table reader for the given 
	 * qualified type name, or null if not exists.
	 */
    public TableReader getTableReader(QName qualifiedTypeName) 
    {
    	return this.typeTableReaderMap.get(qualifiedTypeName);
    }
    
    /**
	 * Adds the given table reader to the container
	 * @param reader the table reader. 
	 */
	@Override
	public void addTableReader(TableReader reader) {
		String name = reader.getTable().getName();
		if (this.tableReaderMap.get(name) != null)
			throw new OperationException("table reader for '" + 
				name + "' already exists");
		this.tableReaderMap.put(name, reader);
	}

	/**
	 * Returns the count of table readers for this 
	 * container.
	 * @return the count of table readers for this 
	 * container
	 */
	@Override
	public int getTableReaderCount() {
		return this.tableReaderMap.size();
	}

    /**
     * Returns all table readers for the this container
     * @return all table readers for the this container
     */
    public List<TableReader> getTableReaders() {
    	List<TableReader> result = new ArrayList<TableReader>();
    	result.addAll(this.tableReaderMap.values());
    	return result;
    }
    
    /**
     * Returns the table reader associated with the
     * data graph root. 
     * @return the table reader associated with the
     * data graph root.
     */
    public TableReader getRootTableReader() {
    	return this.rootReader;
    }

    /**
     * Sets the table reader associated with the
     * data graph root. 
     * @param reader the table reader
     */
    public void setRootTableReader(TableReader reader) {
    	this.rootReader = reader;
    	this.tableReaderMap.put(rootReader.getTable().getName(), rootReader);
    }
    
    
    /**
     * Returns the row reader associated with the given data object
     * @param dataObject the data object
     * @return the row reader associated with the given data object
     * @throws IllegalArgumentException if the given data object
     * is not associated with any row reader.
     */
    public RowReader getRowReader(DataObject dataObject) {
    	RowReader result = rowReaderMap.get(dataObject);
    	if (result == null)
    		throw new IllegalArgumentException("the given data object of type "
    		   + dataObject.getType().getURI() + "#" + dataObject.getType().getName()		
    	       + " is not associated with any row reader");
    	return result;
    }

    public void mapRowReader(DataObject dataObject, 
    		RowReader rowReader) {
    	RowReader existing = this.rowReaderMap.get(dataObject);
    	if (existing != null)
    		throw new IllegalArgumentException("the given data object of type "
    		   + dataObject.getType().getURI() + "#" + dataObject.getType().getName()		
    	       + " is already associated with a row reader");
    	rowReaderMap.put(dataObject, rowReader);
    }
    
    /**
     * Frees resources associated with this reader and any
     * component readers. 
     */
    public void clear() {
    	this.rowReaderMap.clear();
    	// table readers are created based entirely on metadata
    	// i.e. selected types for a query
    	for (TableReader tableReader : tableReaderMap.values())
    		tableReader.clear();    	
    }
}