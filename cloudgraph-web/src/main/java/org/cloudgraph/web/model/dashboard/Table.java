package org.cloudgraph.web.model.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.html.HtmlOutputText;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.ComponentShape;
import org.cloudgraph.web.sdo.personalization.ElementType;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;

public class Table extends AbstractComponent implements RowFactory {
	private static Log log = LogFactory.getLog(Table.class);

	private List<DataObject> data;
	private Type type;
	private Query query;
	private TableDataSource dataSource;
	private List<Column> columns;
	private String[] columnNames;	
	protected boolean isPaginated = false;
	protected int maxRows = -1;
	
	public Table(ComponentName name, 
			ComponentShape shape, 
			Class<?> sdoClass,
			String[] propertyNames,
			Dashboard dashboard,
			Container homeContainer) {
		super(name, shape, ElementType.TABLE, dashboard, homeContainer);
		this.columnNames = propertyNames;
	    construct(sdoClass);
	}
	
	private void construct(Class<?> sdoClass) {
		commonj.sdo.Type sdoType = PlasmaTypeHelper.INSTANCE.getType(sdoClass);
		this.type = new Type(sdoType);
	    this.columns = new ArrayList<Column>();
	    for (String columnName : this.columnNames) {    	    	
	    	commonj.sdo.Property sdoProp = sdoType.getProperty(columnName);
	        if (sdoProp == null)
	        	throw new IllegalArgumentException("property '" 
	        		+ columnName + "' is not defined for type '"
	        		+ sdoClass.getName() + "'");
	    	this.columns.add(new Column(sdoProp, this, this.dashboard));
	    }
	}
	
	public void clear() {
		this.data = null;
	}
	
	public Type getSdoType() {
		return this.type;
	}

	public boolean getIsPaginated() {
	    return this.isPaginated;	
	}
	
	public void setIsPaginated(boolean paginated) {
	    this.isPaginated = paginated;	
	}	
	
	public int getMaxRows() {
		return maxRows;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	public List<Column> getColumns() {
		if (this.columns == null)
			fetch();
		return this.columns;
	}
	
	public Column[] getColumnArray() {
		List<Column> list = getColumns();
		Column[] result = new Column[list.size()];
		list.toArray(result);				
		return result;
	}
	
	public int getColumnCount() {
		List<Column> cols = getColumns();
		if (cols != null)
			return cols.size();
		else
		    return 0;
	}
	
	public Row[] getData() {
		fetch();
		Row[] result = null;
		
		if (this.data != null) {
			result = new Row[this.data.size()];
			
			for (int i = 0; i < this.data.size(); i++) {
				Object[] rowData = new Object[this.columns.size()];
				DataObject rowDataObject = this.data.get(i);
				for (int j = 0; j < this.columns.size(); j++) {
					Column column = this.columns.get(j);
					Object value = rowDataObject.get(column.getDelegate());
					if (value != null)
					    rowData[j] = column.format(value);
					else
						rowData[j] = null;
				}
				result[i] = createRow(rowDataObject, rowData);
			}
		}
		else
			result = new Row[0];
		
		return result;
	}
	
	public Row[] getRows() {
		return getData();
	}
	
	public int getRowCount() {
		fetch();
		return this.data.size();
	}
	
	public Row createRow(DataObject dataObject, Object[] data) {
		return new DefaultRowAdapter(dataObject, data);
	}
	
	public List<HtmlOutputText> getDynamicData() {
		List<HtmlOutputText> result = new ArrayList<HtmlOutputText>();
		
	    for (Column p : getColumns()) {
		HtmlOutputText text = new HtmlOutputText();
		text.setValue(p.getName());
		result.add(text);
	    }
	    return result;
	}

	private List<DataObject> fetch() {
		if (this.data == null)
		    try {
		    	SDODataAccessClient service = new SDODataAccessClient();
		        
		    	Query qry = null;
		    	if (this.query != null)
		    		qry = this.query;
		    	else if (this.dataSource != null)
		    		qry = this.dataSource.createQuery();
		    	else
		        	throw new IllegalStateException("cannot fetch data with no query or datasource set - please add/set a query or datasource");
		    	DataGraph[] results = service.find(qry);
		        this.data = new ArrayList<DataObject>();
		        for (int i = 0; i < results.length; i++) {
		        	this.data.add(results[i].getRootObject()); // assumes flat results set
		        }
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
		
		return this.data;
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public TableDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(TableDataSource dataSource) {
		this.dataSource = dataSource;
	}

	
	public void setDrillDownCategory(String categoryName) {
		categories.push(categoryName);	
		clear();
	}

	public void setRollUpCategory(String categoryName)
	{
		String oldCat = this.getCategoryName();
		this.categories.pop();
		log.info("roll up from: " + oldCat
				+ " to " + this.getCategoryName());
		this.clear();		
	}
	
	public boolean getCanRollUp() {
		return this.categories.size() > 1;
	}
}
