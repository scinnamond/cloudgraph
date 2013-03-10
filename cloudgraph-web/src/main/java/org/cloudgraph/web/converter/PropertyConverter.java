package org.cloudgraph.web.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.cloudgraph.web.model.configuration.PropertyItem;

public class PropertyConverter implements javax.faces.convert.Converter {

	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		
		String[] words = value.split(":");
		String id = words[0];
		String name = words[1];
		return new PropertyItem(new Long(id), name);
	}

	public String getAsString(FacesContext context, UIComponent arg1,
			Object value) {
		PropertyItem prop = (PropertyItem) value;
		return prop.getId() + ":" + prop.getName();
	}
}
