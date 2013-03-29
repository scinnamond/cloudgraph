package org.cloudgraph.web.etl.loader;

import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.sdo.categorization.Category;
import org.cloudgraph.web.sdo.categorization.query.QCategory;
import org.cloudgraph.web.sdo.meta.Classifier;
import org.cloudgraph.web.sdo.meta.Clazz;
import org.cloudgraph.web.sdo.meta.InstanceSpecification;
import org.cloudgraph.web.sdo.meta.Package;
import org.cloudgraph.web.sdo.meta.Property;
import org.cloudgraph.web.sdo.meta.query.QClassifier;
import org.cloudgraph.web.sdo.meta.query.QClazz;
import org.cloudgraph.web.sdo.meta.query.QInstanceSpecification;
import org.cloudgraph.web.sdo.meta.query.QPackage;
import org.cloudgraph.web.sdo.meta.query.QProperty;
import org.cloudgraph.web.sdo.personalization.Role;
import org.cloudgraph.web.sdo.personalization.query.QRole;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.access.client.PojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;

public abstract class AbstractLoader {

	protected SDODataAccessClient service;

	protected AbstractLoader(DataImport dataImport) {
		this.service = new SDODataAccessClient(
			new PojoDataAccessClient(
				DataAccessProviderName.valueOf(dataImport.getProviderName())));
	}
	
	
    protected Package fetchPackage(String uuid) {
		
		QPackage query = QPackage.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no package results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Package result = (Package)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
    
    protected Role fetchRole(String uuid) {
		
		QRole query = QRole.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no package results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Role result = (Role)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}

    protected Category fetchCat(String uuid) {
		
		QCategory query = QCategory.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Category result = (Category)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
	
    protected Clazz fetchClazz(String uuid) {
		
		QClazz query = QClazz.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Clazz result = (Clazz)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
				
    protected Classifier fetchClassifier(String uuid) {
		
		QClassifier query = QClassifier.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Classifier result = (Classifier)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
   
    protected InstanceSpecification fetchInstance(String uuid) {
		
		QInstanceSpecification query = QInstanceSpecification.newQuery();
		query.select(query.wildcard()); 
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no package results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		InstanceSpecification result = (InstanceSpecification)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
    
    protected Property fetchProperty(String uuid) {
		
		QProperty query = QProperty.newQuery();
		query.select(query.wildcard());
		query.where(query.externalId().eq(uuid));
		
		DataGraph[] results = service.find(query);
		if (results == null)
			throw new RuntimeException("no package results for, "
					+ uuid);
		if (results.length > 1)
			throw new RuntimeException("multiple results for, "
					+ uuid);
		Property result = (Property)results[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
}
