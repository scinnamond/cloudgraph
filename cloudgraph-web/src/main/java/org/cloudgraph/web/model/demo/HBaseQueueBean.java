package org.cloudgraph.web.model.demo;

// java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.model.common.CategorizedPropertySupport;
import org.cloudgraph.web.model.common.PropertySelector;
import org.cloudgraph.web.model.common.QueueBean;
import org.cloudgraph.web.model.configuration.PropertyItem;
import org.cloudgraph.web.model.search.SearchBean;
import org.cloudgraph.web.model.taxonomy.TaxonomyConstants;
import org.cloudgraph.web.query.InstanceSpecificationQuery;
import org.cloudgraph.web.query.PropertyQuery;
import org.cloudgraph.web.query.PropertyViewQuery;
import org.cloudgraph.web.sdo.adapter.InstanceSpecificationQueueAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyAdapter;
import org.cloudgraph.web.sdo.adapter.PropertyViewAdapter;
import org.cloudgraph.web.util.BeanFinder;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.core.PropertyCategorization;
import org.cloudgraph.web.sdo.core.PropertyView;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Property;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;


/**
 */
public class HBaseQueueBean extends CloudQueueBean 
{
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(HBaseQueueBean.class);
	private String modelRootURI;
	private String modelRootType;
	private String modelTableName;

    public HBaseQueueBean() {
    }
    
    List<CloudColumn> columns = new ArrayList<CloudColumn>(); 
	public List<CloudColumn> getColumns()
	{
		getData();
		return columns;
	}
       
	private boolean reQuery = true;
	private List<CloudRow> data;
    public List<CloudRow> getData() {
		String type = this.beanFinder.findDemoBean().getModelRootType();
		String uri = this.beanFinder.findDemoBean().getModelRootURI();
		String tableName = this.beanFinder.findDemoBean().getSelectedTable();
		if (type != null)
			if (this.modelRootType == null || !this.modelRootType.equals(type)) {
				this.modelRootType = type;
				this.reQuery = true;
			}
		if (uri != null)
			if (this.modelRootURI == null || !this.modelRootURI.equals(uri)) {
				this.modelRootURI = uri;
				this.reQuery = true;
			}
		if (tableName != null)
			if (this.modelTableName == null || !this.modelTableName.equals(tableName)) {
				this.modelTableName = tableName;
				this.reQuery = true;
			}

    	if (this.reQuery && this.modelRootType != null && this.modelRootURI != null) {
			this.data = new ArrayList<CloudRow>();
		    try {
				HBaseClient client = new HBaseClient();
				Map<String, Map<String, String>> results = null;
				if (this.modelTableName != null) {
					results = client.get(tableName);
				}
				else {
					results = client.get(this.modelRootType, this.modelRootURI);
				}
				HashSet<String> uniqueQuals = new HashSet<String>();
				for (Map<String, String> row : results.values()) {
					uniqueQuals.addAll(row.keySet());
				}
				int maxRow = uniqueQuals.size() + 1;
				
				columns = new ArrayList<CloudColumn>(); 
				CloudColumn rowKyeCol = new CloudColumn("Composite Row Key");
				columns.add(rowKyeCol);
				for (String qual : uniqueQuals) {
					columns.add(new CloudColumn(qual));
				}
				
				Iterator<String> iter = results.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					Map<String, String> row = results.get(key);
					Object[] data = new Object[maxRow+1]; // max plus row key
					data[0] = key;
					int i = 1;
					for (String qual : uniqueQuals) {
						String value = row.get(qual);
						try {
						if (value != null)
						    data[i] = value;
						else
							data[i] = "";
						}
						catch (ArrayIndexOutOfBoundsException e) {
							log.error(e);
						}
						i++;
					}
					CloudRow cloudRow = new CloudRow(key, data);
					this.data.add(cloudRow);
		        	wrappedData.put(new Integer(i), cloudRow); // assumes flat results set
				    i++;
				}
				this.reQuery = false;
		    }   
		    catch (Throwable t) {
		    	log.error(t.getMessage(), t);
		    }
    	}
    	return this.data;
    }
    
	@Override
	public void clear() {
		if (this.data != null)
			this.data.clear();
		
	}
	
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range,
			Object argument) throws IOException {
		// TODO Auto-generated method stub
		
	}
        
}
