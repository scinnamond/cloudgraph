package org.cloudgraph.web.model.search;

import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyDatatype;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.util.BeanFinder;

import org.cloudgraph.web.sdo.core.Organization;

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
