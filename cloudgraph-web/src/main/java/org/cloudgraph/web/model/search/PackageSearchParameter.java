package org.cloudgraph.web.model.search;

import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyDatatype;

public class PackageSearchParameter extends SearchParameter {
			
	public PackageSearchParameter(ComponentName name,
			PropertyDatatype dataType, Object defaultValue, Search search,
			SearchParameterListener listener) {
		super(name, dataType, defaultValue, search, listener);
	}

	public String getDisplayValue() {
		return super.getDisplayValue();
	}
	
	public String getValueIconName() {
		return "";
	}

}
