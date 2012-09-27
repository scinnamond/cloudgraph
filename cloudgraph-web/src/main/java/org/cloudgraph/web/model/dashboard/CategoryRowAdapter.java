package org.cloudgraph.web.model.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

public class CategoryRowAdapter extends DefaultRowAdapter {
	private static Log log = LogFactory.getLog(Table.class);
	int level = 0;
	int levelCount = 0;
		
	public CategoryRowAdapter(DataObject dataObject, Object[] data) {
		super(dataObject, data);
		try {
			Property catLevel = this.dataObject.getType().getProperty("categoryLevel"); // force check
			Property catLevelCount = this.dataObject.getType().getProperty("categoryLevelCount");
			
			
			this.level = this.dataObject.getInt(catLevel);
			this.levelCount = this.dataObject.getInt(catLevelCount);
		}
		catch (IllegalArgumentException e) {
		    log.error(e.getMessage(), e); // property above does not exist	
		}
	}
	
	public String getType() {
		return RowType.CATEGORY.name();
	}

	public boolean getIsRoot() {
		return level == 1;
	}
	
	public boolean getIsLeaf() {
		return level >= levelCount -1;
	}
	
}
