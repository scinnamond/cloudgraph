package org.cloudgraph.store.lang;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.provider.common.PropertyPair;

import commonj.sdo.Property;

public interface StatementExecutor {

	public abstract List<List<PropertyPair>> fetch(PlasmaType type,
			StringBuilder sql);

	public abstract List<List<PropertyPair>> fetch(PlasmaType type,
			StringBuilder sql, Set<Property> props);

	public abstract List<List<PropertyPair>> fetch(PlasmaType type,
			StringBuilder sql, Set<Property> props, Object[] params);

	public abstract Map<String, PropertyPair> fetchRowMap(PlasmaType type,
			StringBuilder sql);

	public abstract List<PropertyPair> fetchRow(PlasmaType type,
			StringBuilder sql);

	public abstract void execute(PlasmaType type, StringBuilder sql,
			Map<String, PropertyPair> values);

	public abstract void executeInsert(PlasmaType type, StringBuilder sql,
			Map<String, PropertyPair> values);

	public List<PropertyPair> executeInsertWithGeneratedKeys(PlasmaType type, StringBuilder sql, 
			Map<String, PropertyPair> values);
}