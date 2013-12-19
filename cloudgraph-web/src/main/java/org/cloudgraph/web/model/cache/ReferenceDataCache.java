package org.cloudgraph.web.model.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.WebConstants;
import org.cloudgraph.web.query.ClassQuery;
import org.cloudgraph.web.query.EnumerationQuery;
import org.cloudgraph.web.query.PackageQuery;
import org.cloudgraph.web.query.PrimitiveTypeQuery;
import org.cloudgraph.web.query.PropertyQuery;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.Taxonomy;
import org.cloudgraph.web.sdo.categorization.TaxonomyMap;
import org.cloudgraph.web.sdo.core.Organization;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.Enumeration;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.PrimitiveType;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.visitor.OrganizationCollector;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.helper.XMLDocument;

@ManagedBean(name="ReferenceDataCache")
@ApplicationScoped
public class ReferenceDataCache 
    implements Serializable 
{
    private static Log log = LogFactory.getLog(ReferenceDataCache.class);
	private static final long serialVersionUID = 1L;

	
	
	
	private Map<String, Taxonomy> taxonomyMap = new HashMap<String, Taxonomy>();
    /**
     * Maps categories for all taxonomies by sequence id for quick lookup
     */
    private Map<Long, Category> categorySeqIdMap = new HashMap<Long, Category>();
    /**
     * Maps categories for all taxonomies back to their taxonomy
     */
    private Map<Long, Taxonomy> categorySeqIdTaxonomyMap = new HashMap<Long, Taxonomy>();
    
    public static String TAXONOMY_NAME_SAM = "Segment Architecture Model";
    public static String TAXONOMY_NAME_GMSPM = "gEMS Perspective Model";
    public static String TAXONOMY_NAME_INVPM = "Inventory Perspective Model";
    public static String TAXONOMY_NAME_ICAMPM = "ICAM Perspective Model";
    public static String TAXONOMY_NAME_ORDERING = "Ordering Model";

    
    private String[] TAX_NAMES = {
    	TAXONOMY_NAME_SAM,
    	TAXONOMY_NAME_GMSPM,
    	TAXONOMY_NAME_INVPM,
    	TAXONOMY_NAME_ICAMPM,
    	TAXONOMY_NAME_ORDERING
    };
    private static int TAXONOMY_SAM = 0;
    private static int TAXONOMY_GMSPM = 1;
    private static int TAXONOMY_INVPM = 2;
    private static int TAXONOMY_ICAMPM = 3;
    private static int TAXONOMY_ORDERING = 4;
    
    private List<Taxonomy> taxonomyList;
    private List<TaxonomyMap> taxonomyMapList;
    private List<Organization> deputyAreaList;
    private List<Organization> businessUnitList;
    private Map<Long, List<Property>> sourceClassSeqIdPropertyMap = new HashMap<Long, List<Property>>();
    private Map<String, List<Property>> sourceClassNamePropertyMap = new HashMap<String, List<Property>>();
    
    private ReferenceDataCacheMonitor referenceDataCacheMonitor = null; 
    

    /**
     * Only to support managed bean facility and test harnesses. NOT
     * for client code in general. 
     * Start a cache monitor Thread for expiration and eviction purposes.
     */
	public ReferenceDataCache() {
		log.debug("ReferenceDataCache CTOR!!!");
		referenceDataCacheMonitor = new ReferenceDataCacheMonitor(this);
	}
	
	/**
	 * Returns a mapped category based on the given sequence id pri-key.
	 * @param seqId
	 * @return the Category
	 */
	public synchronized Category getCategory(Long seqId) {
		return this.categorySeqIdMap.get(seqId);
	}	
	
	/**
	 * Returns a mapped Taxonomy based on the given category sequence id pri-key.
	 * @param categoryId
	 * @return the Taxonomy
	 */
	public synchronized Taxonomy getTaxonomyForCategoryId(Long categoryId) {
		return this.categorySeqIdTaxonomyMap.get(categoryId);
	}	
	
    public synchronized Taxonomy getSegmentArchitectureModel() {
    	return getTaxonomy(this.TAX_NAMES[TAXONOMY_SAM]);
    }  
    
    public synchronized Taxonomy getGemsPerspectiveModel() {
    	return getTaxonomy(this.TAX_NAMES[TAXONOMY_GMSPM]);
    }  
    
    public synchronized Taxonomy getInventoryPerspectiveModel() {
    	return getTaxonomy(this.TAX_NAMES[TAXONOMY_INVPM]);
    }  
    
    public synchronized Taxonomy getICAMPerspectiveModel() {
    	return getTaxonomy(this.TAX_NAMES[TAXONOMY_ICAMPM]);
    } 
    
    public synchronized Taxonomy getOrderingModel() {
    	return getTaxonomy(this.TAX_NAMES[TAXONOMY_ORDERING]);
    }  
    
    public synchronized Taxonomy getTaxonomy(String name)
    {
    	Taxonomy result = this.taxonomyMap.get(name);
    	if (result == null)
    	{
		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(TaxonomyQuery.createQuery(
		    		name));
		    DataGraph graph = results[0]; 
		    if (log.isDebugEnabled())
		    try {
				log.debug(this.serializeGraph(graph));
			} catch (IOException e) {
			}
		    final Taxonomy tax = (Taxonomy)graph.getRootObject();
		    log.debug("Caching Taxonomy " + name);
		    this.taxonomyMap.put(name, tax);
		    referenceDataCacheMonitor.monitor("taxonomyMap:" + name);
		    
		    // Map categories and remove the data-graph header from the graph nodes as this is
		    // reference data and will will want to reference it from other graphs. 
		    PlasmaDataGraphVisitor visitor = new PlasmaDataGraphVisitor() {
				public void visit(DataObject target, DataObject source,
						String sourceKey, int level) {
					
					if (target instanceof Category) {
						Category cat = (Category)target;
					    log.debug("Caching Taxonomy Category Seq. ID" + cat.getSeqId());
					    Long longId = Long.valueOf(cat.getSeqId());
					    categorySeqIdMap.put(longId, cat);
					    categorySeqIdTaxonomyMap.put(longId, tax);
                       // Refreshed when Taxonomy name expires.
                       // referenceDataCacheMonitor.monitor("categorySeqIdMap:" + cat.getSeqId());
					}
					((PlasmaDataObject)target).setDataGraph(null);					
				}
		    };
		    ((PlasmaDataObject)tax).accept(visitor);		    
		    ((PlasmaDataGraph)graph).removeRootObject();
		    result = tax;
    	}
    	return result; 	
    }

    
    private String getHashKey(String name, int year) {
    	return name + ":" 
    	    + String.valueOf(year);
    }
   
    public synchronized List<Taxonomy> getTaxonomies() {
    	if (taxonomyList == null) {
    		taxonomyList = new ArrayList<Taxonomy>();    		
 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(TaxonomyQuery.createQuery());
    		log.debug("Caching Taxonomy List");
		    for (int i = 0; i < results.length; i++) {
		    	Taxonomy tax = (Taxonomy)results[i].getRootObject();
		    	taxonomyList.add(tax);
		    	((PlasmaDataGraph)results[i]).removeRootObject();
		    }
		    referenceDataCacheMonitor.monitor("taxonomyList");
    	}
    	return taxonomyList;
    }

    public synchronized List<TaxonomyMap> getTaxonomyMaps() {
    	if (taxonomyMapList == null) {
    		taxonomyMapList = new ArrayList<TaxonomyMap>();    		
 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(TaxonomyMapQuery.createQuery());
    		log.debug("Caching Taxonomy Map List");
		    for (int i = 0; i < results.length; i++) {
		    	TaxonomyMap map = (TaxonomyMap)results[i].getRootObject();
		    	taxonomyMapList.add(map);
		    	((PlasmaDataGraph)results[i]).removeRootObject();
		    }
		    referenceDataCacheMonitor.monitor("taxonomyMapList");
    	}
    	return taxonomyMapList;
    }
    
    
    private List<SelectItem> deputyAreaOrgItems;
    private List<SelectItem> businessUnitOrgItems;
    
    public synchronized List<SelectItem> getDeputyAreaItems() {
    	if (deputyAreaOrgItems == null) {
    		initOrgItems();    	}
    	return deputyAreaOrgItems;
    }
    
    public synchronized List<SelectItem> getBusinessUnitItems() {
    	if (businessUnitOrgItems == null) {
    		initOrgItems();    	}
    	return businessUnitOrgItems;
    }
    
    private Map<Long, Organization> organizationMap;
    
    public synchronized Organization getOrganization(long id) {
    	if (organizationMap == null) {
    		initOrgItems();    	
    	}
    	return organizationMap.get(id);
    }  
    
    public synchronized List<Organization> getDeputyAreas() {
    	if (deputyAreaList == null) {
    		initOrgItems();    	
    	}
    	return deputyAreaList;
    }

    public synchronized List<Organization> getBusinessUnits() {
    	if (businessUnitList == null) {
    		initOrgItems();    	
    	}
    	return businessUnitList;
    }
    
    private void initOrgItems() {
    	organizationMap = new HashMap<Long, Organization>();
		deputyAreaOrgItems =new ArrayList<SelectItem>();
		deputyAreaOrgItems.add(new SelectItem(new Long(-1), 
				WebConstants.ANY_SELECTION));
		businessUnitOrgItems =new ArrayList<SelectItem>();
		businessUnitOrgItems.add(new SelectItem(new Long(-1), 
				WebConstants.ANY_SELECTION));
		deputyAreaList = new ArrayList<Organization>();
		businessUnitList = new ArrayList<Organization>();
		
		SDODataAccessClient service = new SDODataAccessClient();
	    DataGraph[] results = service.find(OrganizationQuery.createHierarchyQuery("USDA"));
	    Organization root = (Organization)results[0].getRootObject();
	    
	    log.debug("Caching Org Items");

	    Map<String, Organization> sorted = new TreeMap<String, Organization>();
	    // collect level 3 orgs
	    OrganizationCollector collector = new OrganizationCollector(2);
	    ((PlasmaDataObject)root).accept(collector);
	    for (Organization org : collector.getResult()) {	    	
	    	sorted.put(org.getCode(), org);
	    	organizationMap.put(org.getSeqId(), org);
	    }
	    for (Organization org : sorted.values()) {
	    	deputyAreaList.add(org);
            deputyAreaOrgItems.add(new SelectItem(org.getSeqId(), 
					"(" + org.getCode() + ") " + org.getName()));
	    }      	
	    
	    // collect level 5 orgs
	    sorted.clear();
	    collector = new OrganizationCollector(4);
	    ((PlasmaDataObject)root).accept(collector);
	    for (Organization org : collector.getResult()) {
	    	sorted.put(org.getCode(), org);
	    	organizationMap.put(org.getSeqId(), org);
	    }		    
	    for (Organization org : sorted.values()) {
	    	businessUnitList.add(org);
	    	businessUnitOrgItems.add(new SelectItem(org.getSeqId(), 
					"(" + org.getCode() + ") " + org.getName()));
	    }      	
   	
	    referenceDataCacheMonitor.monitor("initOrgItems");
    }

    private List<SelectItem> packageItems;
    public synchronized List<SelectItem> getPackageItems() {
    	if (packageItems == null) {
    		packageItems = new ArrayList<SelectItem>();
    		SelectItem item = new SelectItem(new Long(-1), 
	    			WebConstants.DEFAULT_SELECTION);
    		packageItems.add(item);
    		for (Package type : getPackages()) {    			
    			String def = type.getDefinition();
    			if (def != null && def.length() > 26)
    				def = def.substring(0, 23) + "...";
    			classItems.add(new SelectItem(
    					type.getSeqId(), 
    					type.getName(),
    					def));
    		}
		    referenceDataCacheMonitor.monitor("packageItems");
    	}
    	return classItems;
    }    
    
    private Map<Long, Package> packageSeqIdMap = new HashMap<Long, Package>();   
	public synchronized Package getPackage(Long seqId) {
		if (this.packageSeqIdMap.size() == 0)
			getPackages();
		return this.packageSeqIdMap.get(seqId);
	}	
    private Map<String, Package> packageUUIDMap = new HashMap<String, Package>();   
	public synchronized Package getPackage(String uuid) {
		if (this.packageUUIDMap.size() == 0)
			getPackages();
		return this.packageUUIDMap.get(uuid);
	}	
    private List<Package> packages;
    public synchronized List<Package> getPackages() {
    	if (packages == null) {
    		packages = new ArrayList<Package>();

 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(PackageQuery.createQuery());
    		log.debug("Caching package types");
		    for (int i = 0; i < results.length; i++) {
		    	Package pkg = (Package)results[i].getRootObject();
		    	packages.add(pkg);
		    	packageSeqIdMap.put(pkg.getSeqId(), pkg);
		    	packageUUIDMap.put(pkg.getExternalId(), pkg);
		    	if (log.isDebugEnabled()) {
					try {
			    		log.debug(serializeGraph(pkg.getDataGraph()));
					} catch (IOException e) {
					}
		    	}			    	
		    }
		    referenceDataCacheMonitor.monitor("classes");
    	}
    	return packages;
    }  
    
    public synchronized void expirePackages() {
    	this.packageItems = null;
    	this.packages = null;
    	this.packageSeqIdMap.clear();
    	this.packageUUIDMap.clear();
    }
      
    private List<SelectItem> classItems;
    public synchronized List<SelectItem> getClassItems() {
    	if (classItems == null) {
    		classItems = new ArrayList<SelectItem>();
    		SelectItem item = new SelectItem(new Long(-1), 
	    			WebConstants.DEFAULT_SELECTION);
    		classItems.add(item);
    		for (Clazz type : getClasses()) {    			
    			String def = type.getClassifier().getDefinition();
    			if (def != null && def.length() > 26)
    				def = def.substring(0, 23) + "...";
    			// NOTE: using classifier seq-id here
    			// making result items more "generic" as classes
    			// are linked to many entities through many properties
    			classItems.add(new SelectItem(
    					type.getClassifier().getSeqId(), 
    					type.getClassifier().getName(),
    					def));
    		}
		    referenceDataCacheMonitor.monitor("classItems");
    	}
    	return classItems;
    }    
    
    private Map<Long, Clazz> classSeqIdMap = new HashMap<Long, Clazz>();   
	public synchronized Clazz getClazz(Long seqId) {
		if (this.classSeqIdMap.size() == 0)
			getClasses();
		return this.classSeqIdMap.get(seqId);
	}	
    private Map<String, Clazz> classUUIDMap = new HashMap<String, Clazz>();   
	public synchronized Clazz getClazz(String uuid) {
		if (this.classUUIDMap.size() == 0)
			getClasses();
		return this.classUUIDMap.get(uuid);
	}	

    public synchronized List<Clazz> getClassesByPackageId(Long id) {
    	List<Clazz> result = new ArrayList<Clazz>();
    	for (Clazz clzz : getClasses()) {
        	if (clzz.getClassifier().getPackageableType().get_package().getSeqId() == id)
        		result.add(clzz);
        }
    	return result;
    }
	
	private List<Clazz> classes;
    public synchronized List<Clazz> getClasses() {
    	if (classes == null) {
    		classes = new ArrayList<Clazz>();

 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(ClassQuery.createQuery());
    		log.debug("Caching class types");
		    for (int i = 0; i < results.length; i++) {
		    	Clazz clazz = (Clazz)results[i].getRootObject();
		    	classes.add(clazz);
		    	classSeqIdMap.put(clazz.getSeqId(), clazz);
		    	classUUIDMap.put(clazz.getExternalId(), clazz);
		    	if (log.isDebugEnabled()) {
					try {
			    		log.debug(serializeGraph(clazz.getDataGraph()));
					} catch (IOException e) {
					}
		    	}			    	
		    }
		    referenceDataCacheMonitor.monitor("classes");
    	}
    	return classes;
    }  
    
    public synchronized void expireClasses() {
    	this.classItems = null;
    	this.classes = null;
    	this.classSeqIdMap.clear();
    	this.classUUIDMap.clear();
    }
    
    public synchronized void expireEnumerations() {
    	this.expire("enumerationItems");
    	this.expire("enumerations");
    	this.enumerationItems = null;
    	this.enumerations = null;
    }
    
    private List<SelectItem> enumerationItems;
    public synchronized List<SelectItem> getEnumerationItems() {
    	if (enumerationItems == null) {
    		enumerationItems = new ArrayList<SelectItem>();
    		SelectItem item = new SelectItem(new Long(-1), 
	    			WebConstants.DEFAULT_SELECTION);
    		enumerationItems.add(item);
    		for (Enumeration type : getEnumerations()) {    			
    			String def = type.getDataType().getClassifier().getDefinition();
    			if (def != null && def.length() > 26)
    				def = def.substring(0, 23) + "...";
    			enumerationItems.add(new SelectItem(
    					type.getDataType().getClassifier().getSeqId(), 
    					type.getDataType().getClassifier().getName(),
    					def));
    		}
		    referenceDataCacheMonitor.monitor("enumerationItems");
    	}
    	return enumerationItems;
    }    
    
    List<Enumeration> enumerations = null;
    public synchronized List<Enumeration> getEnumerations() {
    	if (enumerations == null) {
    		enumerations = new ArrayList<Enumeration>();

 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(EnumerationQuery.createQuery());
    		log.debug("Caching Enumeration Types");
		    for (int i = 0; i < results.length; i++) {
		    	Enumeration type = (Enumeration)results[i].getRootObject();
		    	enumerations.add(type);
		    }
		    referenceDataCacheMonitor.monitor("enumerations");
    	}
    	return enumerations;
    }    

    private List<SelectItem> primitiveTypeItems;
    public synchronized List<SelectItem> getPrimitiveTypeItems() {
    	if (primitiveTypeItems == null) {
    		primitiveTypeItems = new ArrayList<SelectItem>();
    		SelectItem item = new SelectItem(new Long(-1), 
	    			WebConstants.DEFAULT_SELECTION);
    		primitiveTypeItems.add(item);
    		for (PrimitiveType type : getPrimitiveTypes()) { 
    			String def = type.getDataType().getClassifier().getDefinition();
    			if (def != null && def.length() > 26)
    				def = def.substring(0, 23) + "...";
    			primitiveTypeItems.add(new SelectItem(
    					type.getDataType().getClassifier().getSeqId(), 
    					type.getDataType().getClassifier().getName(),
    					def));
    		}
		    referenceDataCacheMonitor.monitor("primitiveTypeItems");
    	}
    	return primitiveTypeItems;
    }    
    
    List<PrimitiveType> primitiveTypes = null;
    public synchronized List<PrimitiveType> getPrimitiveTypes() {
    	if (primitiveTypes == null) {
    		primitiveTypes = new ArrayList<PrimitiveType>();

 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(PrimitiveTypeQuery.createQuery());
    		log.debug("Caching Primitive Types");
		    for (int i = 0; i < results.length; i++) {
		    	PrimitiveType type = (PrimitiveType)results[i].getRootObject();
		    	primitiveTypes.add(type);
		    }
		    referenceDataCacheMonitor.monitor("primitiveTypes");
    	}
    	return primitiveTypes;
    }    
   
    public synchronized List<Property> getProperties(Long sourceClassId) {
    	List<Property> result = this.sourceClassSeqIdPropertyMap.get(sourceClassId);
    	if (result == null) {
    		result = new ArrayList<Property>();
    		Query query = PropertyQuery.createQueryBySourceClassId(
    				sourceClassId);	
 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(query);
    		log.debug("Caching properties for source class: " + sourceClassId);
		    for (int i = 0; i < results.length; i++) {
		    	Property prop = (Property)results[i].getRootObject();
		    	result.add(prop);
		    }
		    this.sourceClassSeqIdPropertyMap.put(sourceClassId, 
		    		result);
    	}
    	return result;	
    }

    public synchronized List<Property> getProperties(String sourceClassName) {
    	List<Property> result = this.sourceClassNamePropertyMap.get(sourceClassName);
    	if (result == null) {
    		result = new ArrayList<Property>();
    		Query query = PropertyQuery.createQueryBySourceClassifierName(
    				sourceClassName);	
 		    SDODataAccessClient service = new SDODataAccessClient();
		    DataGraph[] results = service.find(query);
    		log.debug("Caching properties for source class: " + sourceClassName);
		    for (int i = 0; i < results.length; i++) {
		    	Property prop = (Property)results[i].getRootObject();
		    	result.add(prop);
		    }
		    this.sourceClassNamePropertyMap.put(sourceClassName, 
		    		result);
    	}
    	return result;	
    }
    
    public synchronized void expireProperties(Long sourceClassId) {
    	
    	List<Property> removed = this.sourceClassSeqIdPropertyMap.remove(sourceClassId);
    	if (removed != null) {
    		if (log.isDebugEnabled())
    		    log.debug("removed " + removed.size() + " properties");
    	}
    	else
    		log.warn("no properties removed for source class: " + sourceClassId);
    	
    	Clazz c = this.classSeqIdMap.get(sourceClassId);
    	if (c != null) {
        	removed = this.sourceClassNamePropertyMap.remove(c.getClassifier().getName());
        	if (removed != null) {
        		if (log.isDebugEnabled())
        		    log.debug("removed " + removed.size() + " properties");
        	}
        	else
        		log.warn("no properties removed for source class: " + c.getClassifier().getName());
    		
    	}
    }
    
    /**
     * The ReferenceDataCacheMonitor calls this method when it detects a cached item
     * that has expired. Evicts by resetting Map or List to null, causing a next time
     * access DB reload.
     * @param objName - the cached item that has expired.
     */
    public synchronized void expire(String objName)
    {
    	
	    log.debug("Expire " + objName);
	    
	    if (objName.startsWith("taxonomyMap:"))
	    {
	    	String taxName = objName.substring("taxonomyMap:".length());
	    	this.taxonomyMap.remove(taxName);
	    }
	    else
	    if (objName.startsWith("categorySeqIdMap:"))
	    {
	    	String seqId = objName.substring("categorySeqIdMap:".length());
	    	categorySeqIdMap.remove(new Long(seqId));
	    }
	    else
	    if ("taxonomyList".equals(objName))
	    {
	    	taxonomyList = null;
	    }
	    else
	    if ("taxonomyMapList".equals(objName))
	    {
	    	taxonomyMapList = null;
	    }
	    else
	    if ("initOrgItems".equals(objName))
	    {
	    	organizationMap = null;
			deputyAreaOrgItems = null;
			businessUnitOrgItems = null;
			deputyAreaList = null;
			businessUnitList = null;
	    }
	    
    } // expire
    
    private String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("dump");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
}
