package org.cloudgraph.cassandra.cql;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.provider.common.PropertyPair;

import commonj.sdo.Property;

public interface StatementFactory {

	public abstract StringBuilder createSelect(PlasmaType type,
			List<PropertyPair> keyValues);

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
}