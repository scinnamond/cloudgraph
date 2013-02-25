package org.cloudgraph.web.etl.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * This utility is temporary (throw away) code used to merge XML/JAXB staging data
 * into SDO objects. It leverages the SDO metadata API on the SDO side and Java 
 * reflection on the XML side. It makes unfortunate assumptions about the naming of
 * methods in the XML being the same as SDO property names. SDO has the notion of a local-name,
 * which is a name used for XML marshaling which may differ from its metadata or logical name. 
 * To leverage the SDO local-name facilities, both source and target objects would need
 * to be SDO's. 
 *    
 * @author scinnamond
 */
public class LoaderUtils {
    private static Logger log = Logger.getLogger(LoaderUtils.class);

	
    public static void merge(DataObject target, Object source) {
		
		for (Property property : target.getType().getDeclaredProperties()) {
			if (property.isMany())
				continue;
			if (!property.getType().isDataType())
				continue;
			if (property.isReadOnly())
				continue;
			String getterName = "get" 
				+ property.getName().substring(0, 1).toUpperCase() 
				+ property.getName().substring(1);
			log.debug("method: " + source.getClass().getSimpleName() + "." + getterName);
			Method getter = findMethod(source, getterName);
			if (getter != null) {
				Object value = null;
				try {
					value = getter.invoke(source, new Object[] {});
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				Object oldValue = target.get(property);
				
				if (value != null) {
				    if (value.getClass().isEnum())
					    value = value.toString();
				    if (value instanceof XMLGregorianCalendar) {
				    	value = ((XMLGregorianCalendar)value).toGregorianCalendar().getTime();
				    }
				    if (value instanceof String)
				    	value = ((String)value).trim();
					if (oldValue == null) {
						log.info("merging "+target.getType().getName() + "." + property.getName()+" value: " + value);
				        target.set(property, value);
					} else {
					    if (oldValue instanceof String)
					    	oldValue = ((String)oldValue).trim();
					    if (value instanceof Number) {
						    if (((Number)value).doubleValue() != ((Number)oldValue).doubleValue()) {
							    log.info("merging "+target.getType().getName() + "." + property.getName()+" old value " +  oldValue + " with value: " + value);
					            target.set(property, value);
						    }    
					    }
					    else {
						    if (!value.equals(oldValue)) {
							    log.info("merging "+target.getType().getName() + "." + property.getName()+" old value " +  oldValue + " with value: " + value);
					            target.set(property, value);
						    }    
					    }
					}
				} else {
					if (oldValue != null)
						target.unset(property); // user nulled it out
				}
			}
			else
				log.debug("not found method: " + source.getClass().getSimpleName() + "." + getterName);
		}
	}
	
	public static void copy(DataObject target, Object source) {
		
		for (Property property : target.getType().getDeclaredProperties()) {
			if (property.isMany())
				continue;
			if (!property.getType().isDataType())
				continue;
			if (property.isReadOnly())
				continue;
			String getterName = "get" 
				+ property.getName().substring(0, 1).toUpperCase() 
				+ property.getName().substring(1);
			log.debug("method: " + source.getClass().getSimpleName() + "." + getterName);
			Method getter = findMethod(source, getterName);
			if (getter != null) {
				Object value = null;
				try {
					value = getter.invoke(source, new Object[] {});
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				if (value != null) {
				    if (value.getClass().isEnum())
					    value = value.toString();
				    if (value instanceof XMLGregorianCalendar) {
				    	value = ((XMLGregorianCalendar)value).toGregorianCalendar().getTime();
				    }
				    if (value instanceof String)
				    	value = ((String)value).trim();
					log.debug("copy "+target.getType().getName() + "." + property.getName()+" value: " + value);
				    target.set(property, value);
				}				
			}
			else
				log.debug("not found method: " + source.getClass().getSimpleName() + "." + getterName);
		}
	}
	
	public static Method findMethod(Object source, String name) {
		for (Method method : source.getClass().getMethods()) {
			if (method.getName().equals(name))
				return method;
		}
		return null;
	}
	
	public static String formatName(String literal) {
	    String result = literal.trim();
	    result = literal.replace("&amp;", "*");
	    result = result.replace('&', '*');
	    result = result.replace('\t', '*');
	    result = normalizeSpace(result);
	    return result;
	}
	
	/**
	 * Strips leading and trailing white space and replacing sequences of 
	 * white space characters with a single space
	 * @param literal
	 * @return the normalized literal
	 */
	public static String normalizeSpace(String literal) {
	    String result = literal.trim();
	    StringBuilder builder = new StringBuilder(result.length());
	    char[] chars = result.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
			    if (i > 0 && Character.isWhitespace(chars[i-1]))
			        continue; 
			    else
				    builder.append(' ');			    	
			}
			else
			    builder.append(chars[i]);
		}
		return builder.toString();
	}
	
}
