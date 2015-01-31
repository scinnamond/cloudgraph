package org.cloudgraph.store.lang;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.provider.common.PropertyPair;

import commonj.sdo.Property;
import commonj.sdo.Type;

public interface StatementFactory {

	public abstract StringBuilder createSelectConcurrent(PlasmaType type,
			List<PropertyPair> keyValues, int waitSeconds, List<Object> params);

	public abstract StringBuilder createSelect(PlasmaType type,
			Set<Property> props, List<PropertyPair> keyValues,
			List<Object> params);

	public abstract StringBuilder createSelect(PlasmaType type,
			Set<Property> props, List<PropertyPair> keyValues,
			FilterAssembler filterAssembler, List<Object> params);

	public abstract StringBuilder createInsert(PlasmaType type,
			Map<String, PropertyPair> values);

	public abstract StringBuilder createUpdate(PlasmaType type,
			Map<String, PropertyPair> values);

	public abstract StringBuilder createDelete(PlasmaType type,
			Map<String, PropertyPair> values);

	public abstract PlasmaProperty getOppositePriKeyProperty(Property targetProperty);
	public abstract boolean hasUpdatableProperties(Map<String, PropertyPair> values);
	
	public abstract FilterAssembler createFilterAssembler(Where where, Type targetType);
	
	
}