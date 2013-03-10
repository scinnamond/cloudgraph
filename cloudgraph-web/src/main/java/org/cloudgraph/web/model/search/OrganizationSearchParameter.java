package org.cloudgraph.web.model.search;

import org.cloudgraph.web.config.web.ComponentName;
import org.cloudgraph.web.config.web.PropertyDatatype;
import org.cloudgraph.web.model.cache.ReferenceDataCache;
import org.cloudgraph.web.sdo.core.Organization;
import org.cloudgraph.web.util.BeanFinder;

public class OrganizationSearchParameter extends SearchParameter {
			
	public OrganizationSearchParameter(ComponentName name,
			PropertyDatatype dataType, Object defaultValue, Search search,
			SearchParameterListener listener) {
		super(name, dataType, defaultValue, search, listener);
	}

	public String getDisplayValue() {
		BeanFinder finder = new BeanFinder();
		ReferenceDataCache cache = finder.findReferenceDataCache();
		if (this.getHasValue()) {
			Organization org = cache.getOrganization((Long)this.value);
		    return org.getCode();
		}
		return "";
	}
	
	public String getValueIconName() {
		return "";
	}

}
