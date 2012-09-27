package org.cloudgraph.web.util;

import org.plasma.sdo.PlasmaEnum;

public class ResourceUtils {
    
	public static String constructResourceLabelKey(Class<?> cls, PlasmaEnum e) {
    	return constructResourceLabelKey(cls, e.getInstanceName());
    }

    public static String constructResourceLabelKey(Class<?> cls, String instanceName) {
    	String key = cls.getSimpleName() + "_" 
	        + instanceName.toLowerCase() + "_label";
    	return key;
    }
    
    public static PlasmaEnum getEnumForInstanceName(PlasmaEnum[] enums, String name) {
    	for (PlasmaEnum e : enums) {
    		if (e.getInstanceName().equals(name))
    			return e;
    	}
    	throw new IllegalArgumentException("name not found, " + name);
    }

}
