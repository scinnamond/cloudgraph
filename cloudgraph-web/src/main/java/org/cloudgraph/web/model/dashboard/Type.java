package org.cloudgraph.web.model.dashboard;


/**
 * Wrapper class with "business logic" for pulling data from
 * SDO properties via delegation. This may include pulling resources
 * from a bundle in place of any generated or default values. 
 * @author scinnamond
 */
public class Type {
	
	private commonj.sdo.Type delegate;
	
    @SuppressWarnings("unused")
	private Type() {}
    
    public Type(commonj.sdo.Type delegate) {
    	this.delegate = delegate;
    }
    
    public commonj.sdo.Type getDelegate() {
    	return this.delegate;
    }    
    
    public String getName() {
    	return this.delegate.getName();
    }
    
    public String getDescription() {
    	return ""; // FIXME
    }
    
}
