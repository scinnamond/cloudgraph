package org.cloudgraph.web.model.dashboard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.MissingResourceException;

import org.cloudgraph.web.ResourceManager;
import org.cloudgraph.web.ResourceType;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.repository.Comment;


/**
 * Wrapper class with "business logic" for pulling data from
 * SDO properties via delegation. This may include pulling resources
 * from a bundle in place of any generated or default values. 
 * @author scinnamond
 */
public class Column {
	
	private commonj.sdo.Property property;
	private Table table;
	private Dashboard dashboard;
	private String displayName;
	private String displayPattern;
	private String resourceKeyBase;
	private DecimalFormat decimalFormat;
	private String numberFormatDefaultpattern = "##########";
	private SimpleDateFormat dateFormat;
	private String dateFormatDefaultPattern = "yyyy.MM.dd G 'at' HH:mm:ss";
	
    @SuppressWarnings("unused")
	private Column() {}
    
    public Column(commonj.sdo.Property delegate, Table table, Dashboard dashboard) {
    	this.property = delegate;
    	this.table = table;
    	this.dashboard = dashboard;
    	this.resourceKeyBase = dashboard.getResourceContext() + ResourceManager.DELIM;
    	this.resourceKeyBase += this.table.getSdoType().getName() + ResourceManager.DELIM;
    	this.resourceKeyBase += this.property.getName();
    }
    
    public commonj.sdo.Property getDelegate() {
    	return this.property;
    }
    
    public String getName() {
    	return this.property.getName();
    }

    public String getDisplayName() {
    	if (this.displayName == null) {
    		this.displayName = this.property.getName();
	    	try {
	    		this.displayName = ResourceManager.instance().getString(
	    				this.resourceKeyBase, ResourceType.LABEL);
	    	}
	    	catch (MissingResourceException e) {
	    	}
    	}
	    return this.displayName;    	
    }
    
    public String getDescription() {
    	List list = ((PlasmaProperty)this.property).getDescription();
    	if (list != null && list.size() > 0)
    		return ((Comment)list.get(0)).getBody();
    	return "";
    }
    
    public boolean getIsNumber() {
    	if (this.property.getType().isDataType()) {
    	
    	    DataType dataType = DataType.valueOf(this.property.getType().getName());
    	    switch(dataType) {
    	    case Double:     
    	    case Float:     
    	    case Int:        
    	    case Integer:    
    	    case Long:
    	    	return true;
    	    }
    	}
    	return false;
    }

    public boolean getIsIntegral() {
    	if (this.property.getType().isDataType()) {
    	
    	    DataType dataType = DataType.valueOf(this.property.getType().getName());
    	    switch(dataType) {
    	    case Int:        
    	    case Integer:    
    	    case Long:
    	    	return true;
    	    }
    	}
    	return false;
    } 
    
    public boolean getIsFloatingPoint() {
    	if (this.property.getType().isDataType()) {
    	
    	    DataType dataType = DataType.valueOf(this.property.getType().getName());
    	    switch(dataType) {
    	    case Double:     
    	    case Float:     
    	    	return true;
    	    }
    	}
    	return false;
    } 
    
    public boolean getIsDateTime() {
    	if (this.property.getType().isDataType()) {
    	
    	    DataType dataType = DataType.valueOf(this.property.getType().getName());
    	    switch(dataType) {
    	    case Date:       
    	    case DateTime:   
    	    case Day:        
    	    case Month:      
    	    case MonthDay:   
    	    case Time:       
    	    case Year:       
    	    case YearMonth:  
    	    case YearMonthDay:    	    	
    	    	return true;
    	    }
    	}
    	return false;
    } 
    
    public String getDisplayPattern() {
    	if (this.displayPattern == null) {
    		if (this.getIsNumber()) 
    		    this.displayPattern = this.numberFormatDefaultpattern;
    		else if (this.getIsDateTime())
    			this.displayPattern = this.dateFormatDefaultPattern;
    		
	    	try {
	    		this.displayPattern = ResourceManager.instance().getString(
	    			this.resourceKeyBase, ResourceType.PATTERN);
	    	}
	    	catch (MissingResourceException e) {
	    	}
    	}
	    return this.displayPattern;    	
    }
    
    public String format(Object data) {
    	 if (getIsNumber()) {
             if (this.decimalFormat == null) {
            	 this.decimalFormat = new DecimalFormat(getDisplayPattern());
             }
             return decimalFormat.format(data);
    	 }
    	 else if (this.getIsDateTime()) {
             if (this.dateFormat == null) {
            	 this.dateFormat = new SimpleDateFormat(getDisplayPattern());
             }
             return dateFormat.format(data);
    	 }
    	 else
    		 return String.valueOf(data);
    }
    
}
