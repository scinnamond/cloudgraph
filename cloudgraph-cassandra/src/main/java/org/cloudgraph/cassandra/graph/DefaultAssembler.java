package org.cloudgraph.cassandra.graph;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.cql.CQLStatementExecutor;
import org.cloudgraph.cassandra.cql.CQLStatementFactory;
import org.cloudgraph.cassandra.cql.StatementExecutor;
import org.cloudgraph.cassandra.cql.StatementFactory;
import org.cloudgraph.cassandra.service.KeyPairGraphAssembler;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.GraphServiceException;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.DataAccessException;
import org.plasma.sdo.access.provider.common.PropertyPair;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.core.TraversalDirection;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.profile.KeyType;

import com.datastax.driver.core.Session;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public abstract class DefaultAssembler implements KeyPairGraphAssembler {
	private static Log log = LogFactory.getLog(DefaultAssembler.class);
	protected static Set<Property> EMPTY_PROPERTY_SET = new HashSet<Property>();
	protected static List<DataObject> EMPTY_DATA_OBJECT_LIST = new ArrayList<DataObject>();
	protected PlasmaType rootType;
	protected PlasmaDataObject root;
	protected SelectionCollector collector;
	protected Timestamp snapshotDate;
	protected Session con;
	protected Map<Integer, PlasmaDataObject> dataObjectMap = new HashMap<Integer, PlasmaDataObject>();
	protected Comparator<PropertyPair> nameComparator;
	protected StatementFactory statementFactory;
	protected StatementExecutor statementExecutor;

	public DefaultAssembler(PlasmaType rootType, SelectionCollector collector,
			Timestamp snapshotDate, Session con) {
		this.rootType = rootType;
		this.collector = collector;
		this.snapshotDate = snapshotDate;
		this.con = con;
		this.statementFactory = new CQLStatementFactory();
		this.statementExecutor = new CQLStatementExecutor(con);

		this.nameComparator = new Comparator<PropertyPair>() {
			@Override
			public int compare(PropertyPair o1, PropertyPair o2) {
				return o1.getProp().getName().compareTo(o2.getProp().getName());
			}
		};

	}
	
	/**
	 * Assembles a data object of the given target type by first forming a query using the
	 * given key/property pairs. If an existing data object is mapped for the given
	 * key pairs, the existing data object is linked. 
	 * @param targetType the type for the data object to be assembled
	 * @param source the source data object
	 * @param sourceProperty the source property
	 * @param childKeyPairs the key pairs for the data object to be assembled
	 */	
	protected abstract void assemble(PlasmaType targetType, PlasmaDataObject source, PlasmaProperty sourceProperty, 
			List<PropertyPair> childKeyPairs, int level);
	
	/**
	 * Initiates the assembly of a data graph based on the 
	 * given results list. 
	 * @param results the results list
	 * 
	 * @see DataGraphAssembler.getDataGraph()
	 */
	public void assemble(List<PropertyPair> results)  {
		
    	long before = System.currentTimeMillis();

    	DataGraph dataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	this.root = (PlasmaDataObject)dataGraph.createRootObject(this.rootType);		
		if (log.isDebugEnabled())
			log.debug("assembling root: " 
		        + this.root.getType().getName());
		
		CoreNode rootNode = (CoreNode)this.root;
        // add concurrency fields
        if (snapshotDate != null)
        	rootNode.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP, snapshotDate);
		// set data properties
		for (PropertyPair pair : results) {
			if (pair.getProp().getType().isDataType()) {
				rootNode.setValue(pair.getProp().getName(), 
						pair.getValue());
			}
		}
		
        // map it
        int key = createHashKey(
        	(PlasmaType)this.root.getType(), results);
        if (log.isDebugEnabled())
        	log.debug("mapping root " + key + "->" + this.root);
        this.dataObjectMap.put(key, this.root);  
		
		// singular reference props
		for (PropertyPair pair : results) {
			if (pair.getProp().isMany() || pair.getProp().getType().isDataType())
			    continue;
			List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
			PlasmaProperty supplier = pair.getProp().getKeySupplier();
			if (supplier != null) {
				PropertyPair childPair = new PropertyPair(supplier,
		    			pair.getValue());
				childPair.setValueProp(supplier);
		    	childKeyProps.add(childPair);
			}
			else {
				List<Property> childPkProps = ((PlasmaType)pair.getProp().getType()).findProperties(KeyType.primary);
			    if (childPkProps.size() == 1) {
			    	childKeyProps.add(
			    		new PropertyPair((PlasmaProperty)childPkProps.get(0),
			    			pair.getValue()));
			    }
			    else
				    throwPriKeyError(childPkProps, 
				    		pair.getProp().getType(), pair.getProp());
			}
		    
		    assemble((PlasmaType)pair.getProp().getType(), 
				(PlasmaDataObject)this.root, pair.getProp(),
				childKeyProps, 1);
		    
		}
		
		// multi reference props (not found in results)
		Set<Property> props = this.collector.getProperties(this.rootType);
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.isMany() && !prop.getType().isDataType()) {
		    	PlasmaProperty opposite = (PlasmaProperty)prop.getOpposite();
		    	if (opposite == null)
			    	throw new DataAccessException("no opposite property found"
				        + " - cannot map from many property, " + prop.toString());			    				    	
				List<PropertyPair> childKeyProps = new ArrayList<PropertyPair>();
				List<Property> rootPkProps = ((PlasmaType)root.getType()).findProperties(KeyType.primary);
			    if (rootPkProps.size() == 1) {
			    	PlasmaProperty rootProp = (PlasmaProperty)rootPkProps.get(0);
			    	Object value = root.get(rootProp);
			    	if (value != null) {
			    		PropertyPair pair = new PropertyPair(opposite, value);
			    		pair.setValueProp(rootProp);
			    	    childKeyProps.add(pair);
			    	}
			    	else
			    		throw new GraphServiceException("no value found for key property, "  
			    				+ rootProp.toString());
			    }
			    else
				    throwPriKeyError(rootPkProps, 
				    		root.getType(), prop);
			    
			    assemble((PlasmaType)prop.getType(), 
						(PlasmaDataObject)this.root, prop,
						childKeyProps, 1);
			}
		}
		
    	long after = System.currentTimeMillis();
    	
    	rootNode.getValueObject().put(
    		CloudGraphConstants.GRAPH_ASSEMBLY_TIME,
    		Long.valueOf(after - before));    	
    	
    	GraphMetricVisitor visitor = new GraphMetricVisitor();
    	this.root.accept(visitor);
    	
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_NODE_COUNT,
        		Long.valueOf(visitor.getCount()));
    	rootNode.getValueObject().put(
        		CloudGraphConstants.GRAPH_DEPTH,
        		Long.valueOf(visitor.getDepth()));
	}

	/**
	 * If the given property is a datatype property, returns a property pair
	 * with the given property set as the pair value property, otherwise
	 * traverses the data object graph via opposite property links until a
	 * datatype property is found, then returns the property value pair with the
	 * traversal end point property set as the pair value property.
	 * 
	 * @param dataObject
	 *            the data object
	 * @param prop
	 *            the property
	 * @param opposite
	 *            the opposite property
	 * @return the property value pair
	 */
	protected PropertyPair findNextKeyValue(PlasmaDataObject dataObject,
			PlasmaProperty prop, PlasmaProperty opposite) {
		PlasmaDataObject valueTarget = dataObject;
		PlasmaProperty valueProp = prop;

		Object value = valueTarget.get(valueProp.getName());
		while (!valueProp.getType().isDataType()) {
			valueTarget = (PlasmaDataObject) value;
			valueProp = this.statementFactory.getOppositePriKeyProperty(valueProp);
			value = valueTarget.get(valueProp.getName()); // FIXME use prop API
		}
		if (value != null) {
			PropertyPair pair = new PropertyPair(opposite, value);
			pair.setValueProp(valueProp);
			return pair;
		} else
			throw new GraphServiceException("no value found for key property, "
					+ valueProp.toString());
	}

	/**
	 * Creates a new data object contained by the given source data object and
	 * source property.
	 * 
	 * @param row
	 *            the results row
	 * @param source
	 *            the source data object
	 * @param sourceProperty
	 *            the source containment property
	 * @return the new data object
	 */
	protected PlasmaDataObject createDataObject(List<PropertyPair> row,
			PlasmaDataObject source, PlasmaProperty sourceProperty) {

		PlasmaDataObject target = (PlasmaDataObject) source
				.createDataObject(sourceProperty);
		CoreNode node = (CoreNode) target;
		if (log.isDebugEnabled())
			log.debug("create: " + source.getType().getName() + "."
					+ sourceProperty.getName() + "->"
					+ target.getType().getName());

		// add concurrency fields
		if (snapshotDate != null)
			node.setValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP,
					snapshotDate);

		// set data properties bypassing SDO "setter" API
		// to avoid triggering read-only property error
		for (PropertyPair pair : row) {
			if (pair.getProp().getType().isDataType()) {
				if (log.isDebugEnabled())
					log.debug("set: (" + pair.getValue() + ") "
							+ pair.getProp().getContainingType().getName()
							+ "." + pair.getProp().getName());
				node.setValue(pair.getProp().getName(), pair.getValue());
			}
		}

		// map it
		int key = createHashKey((PlasmaType) target.getType(), row);
		if (log.isDebugEnabled())
			log.debug("mapping " + key + "->" + target);
		this.dataObjectMap.put(key, target);

		return target;
	}

	/**
	 * Finds and returns an existing data object based on the given results row
	 * which is part if this assembly unit, or returns null if not exists
	 * 
	 * @param type
	 *            the target type
	 * @param row
	 *            the results row
	 * @return the data object
	 */
	protected PlasmaDataObject findDataObject(PlasmaType type,
			List<PropertyPair> row) {
		int key = createHashKey(type, row);
		PlasmaDataObject result = this.dataObjectMap.get(key);
		if (log.isDebugEnabled()) {
			if (result != null)
				log.debug("found existing mapping " + key + "->" + result);
			else
				log.debug("found no existing mapping for hash key: " + key);
		}
		return result;
	}

	/**
	 * Creates a unique mappable key using the qualified type name and all key
	 * property values from the given row.
	 * 
	 * @param type
	 *            the type
	 * @param row
	 *            the data values
	 * @return the key
	 */
	protected int createHashKey(PlasmaType type, List<PropertyPair> row) {
		PropertyPair[] pairs = new PropertyPair[row.size()];
		row.toArray(pairs);
		Arrays.sort(pairs, this.nameComparator);
		int pkHash = type.getQualifiedName().hashCode();
		int fallBackHash = type.getQualifiedName().hashCode();

		int pks = 0;
		for (int i = 0; i < pairs.length; i++) {
			Object value = pairs[i].getValue();
			if (value == null) {
				log.warn("null voue for property, "
						+ pairs[i].getProp().toString());
				continue;
			}
			if (pairs[i].getProp().isKey(KeyType.primary)) {
				pkHash = pkHash ^ value.hashCode();
				fallBackHash = fallBackHash ^ value.hashCode();
				pks++;
			} else {
				fallBackHash = fallBackHash ^ value.hashCode();
			}
		}
		if (pks > 0) {
			List<Property> pkProps = type.findProperties(KeyType.primary);
			if (pkProps.size() == pks)
				return pkHash;
		}

		return fallBackHash;
	}

	/**
	 * Creates a directed (link) between the given source and target data
	 * objects. The reference is created as a containment reference only if the
	 * given target has no container.
	 * 
	 * @param target
	 *            the data object which is the target
	 * @param source
	 *            the source data object
	 * @param sourceProperty
	 *            the source property
	 * 
	 * @see TraversalDirection
	 */
	protected void link(PlasmaDataObject target, PlasmaDataObject source,
			PlasmaProperty sourceProperty) {
		if (log.isDebugEnabled())
			log.debug("linking source (" + source.getUUIDAsString() + ") "
					+ source.getType().getURI() + "#"
					+ source.getType().getName() + "."
					+ sourceProperty.getName() + "->("
					+ target.getUUIDAsString() + ") "
					+ target.getType().getURI() + "#"
					+ target.getType().getName());

		if (sourceProperty.isMany()) {

			PlasmaProperty opposite = (PlasmaProperty) sourceProperty
					.getOpposite();
			if (opposite != null && !opposite.isMany()
					&& target.isSet(opposite)) {
				PlasmaDataObject existingOpposite = (PlasmaDataObject) target
						.get(opposite);
				if (existingOpposite != null) {
					log.warn("encountered existing opposite ("
							+ existingOpposite.getType().getName()
							+ ") value found while creating link "
							+ source.toString() + "."
							+ sourceProperty.getName() + "->"
							+ target.toString() + " - no link created");
					return;
				}
			}
			@SuppressWarnings("unchecked")
			List<DataObject> list = source.getList(sourceProperty);
			if (list == null)
				list = EMPTY_DATA_OBJECT_LIST;

			if (!list.contains(target)) {
				// check if any existing list members already have the opposite
				// property set
				for (DataObject existing : list) {
					if (opposite != null && !opposite.isMany()
							&& existing.isSet(opposite)) {
						PlasmaDataObject existingOpposite = (PlasmaDataObject) existing
								.get(opposite);
						if (existingOpposite != null) {
							log.warn("encountered existing opposite ("
									+ existingOpposite.getType().getName()
									+ ") value found while creating link "
									+ source.toString() + "."
									+ sourceProperty.getName() + "->"
									+ target.toString() + " - no link created");
							return;
						}
					}
				}

				if (log.isDebugEnabled())
					log.debug("adding target " + source.toString() + "."
							+ sourceProperty.getName() + "->"
							+ target.toString());
				if (target.getContainer() == null) {
					target.setContainer(source);
					target.setContainmentProperty(sourceProperty);
				}
				list.add(target);
				source.setList(sourceProperty, list);
				// FIXME: replaces existing list according to SDO spec (memory
				// churn)
				// store some temp instance-property list on DO and only set
				// using SDO
				// API on completion of graph.
			}
		} else {
			// Selection map keys are paths from the root entity and
			// elements in the path are often repeated. Expect repeated
			// events for repeated path elements, which
			// may be useful for some implementations, but not this one. So
			// we screen these out here.
			PlasmaDataObject existing = (PlasmaDataObject) source
					.get(sourceProperty);
			if (existing == null) {
				source.set(sourceProperty, target);
				// While the SDO spec seems to indicate (see 3.1.6 Containment)
				// that
				// a Type may have only 1 reference property which a containment
				// property, this seems too inflexible given the almost infinite
				// number of ways a graph could be constructed. We therefore
				// allow any reference
				// property to be a containment property, and let the graph
				// assembly
				// order determine which properties are containment properties
				// for a particular
				// graph result. The SDO spec is crystal clear that every Data
				// Object
				// other than the root, must have one-and-only-one container. We
				// set the container
				// here as well as the specific reference property that
				// currently is
				// the containment property, based on graph traversal order.
				// Note it would be
				// possible to specify exactly which property is containment in
				// a
				// query specification. We set no indication of containment on
				// the
				// (source) container object because all reference properties
				// are
				// potentially containment properties.
				if (target.getContainer() == null) {
					target.setContainer(source);
					target.setContainmentProperty(sourceProperty);
				}
			} else if (!existing.equals(target))
				if (log.isDebugEnabled())
					log.debug("encountered existing ("
							+ existing.getType().getName()
							+ ") value found while creating link "
							+ source.toString() + "."
							+ sourceProperty.getName() + "->"
							+ target.toString());
		}
	}

	protected void throwPriKeyError(List<Property> rootPkProps, Type type,
			Property prop) {
		if (prop.isMany())
			if (rootPkProps.size() == 0)
				throw new DataAccessException("no pri-keys found for "
						+ type.getURI() + "#" + type.getName()
						+ " - cannot map from many property, " + prop.getType()
						+ "." + prop.getName());
			else
				throw new DataAccessException("multiple pri-keys found for "
						+ type.getURI() + "#" + type.getName()
						+ " - cannot map from many property, " + prop.getType()
						+ "." + prop.getName());
		else if (rootPkProps.size() == 0)
			throw new DataAccessException("no pri-keys found for "
					+ type.getURI() + "#" + type.getName()
					+ " - cannot map from singular property, " + prop.getType()
					+ "." + prop.getName());
		else
			throw new DataAccessException("multiple pri-keys found for "
					+ type.getURI() + "#" + type.getName()
					+ " - cannot map from singular property, " + prop.getType()
					+ "." + prop.getName());
	}

	public PlasmaDataGraph getDataGraph() {
		return (PlasmaDataGraph) this.root.getDataGraph();
	}

	public void clear() {
		this.root = null;
		this.dataObjectMap.clear();
	}

	protected class GraphMetricVisitor implements PlasmaDataGraphVisitor {

		private long count = 0;
		private long depth = 0;

		@Override
		public void visit(DataObject target, DataObject source,
				String sourcePropertyName, int level) {
			count++;
			if (level > depth)
				depth = level;

		}

		public long getCount() {
			return count;
		}

		public long getDepth() {
			return depth;
		}
	}

}
