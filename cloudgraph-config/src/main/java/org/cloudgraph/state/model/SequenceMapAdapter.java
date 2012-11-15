package org.cloudgraph.state.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SequenceMapAdapter extends XmlAdapter<SequenceMap, HashMap<QName, SequenceMapping>>{
    private static Log log = LogFactory.getLog(SequenceMapAdapter.class);

	@Override
	public HashMap<QName, SequenceMapping> unmarshal(SequenceMap source) throws Exception {
		HashMap<QName, SequenceMapping> result = new HashMap<QName, SequenceMapping>();
		for (URIMap uriMap : source.getURIS()) {
			for (TypeMap typeMap : uriMap.getTypes()) {
				SequenceMapping mapping = new SequenceMapping(
						uriMap.getUri(),
						typeMap.getName());
				QName qname = new QName(uriMap.getUri(), typeMap.getName());
				result.put(qname, mapping);
				for (TypeMapEntry entry : typeMap.getEntries())
				{
					mapping.put(entry.getUUID(), new Integer(entry.getID()));
				}
			}
		}
		return result;
	}

	@Override
	public SequenceMap marshal(HashMap<QName, SequenceMapping> source) throws Exception {
		SequenceMap result = new SequenceMap();		
		Map<String, URIMap> temp = new HashMap<String, URIMap>();
		
		if (source != null)
			for (Entry<QName, SequenceMapping> entry : source.entrySet()) {
				QName key = entry.getKey();	
				URIMap uriMap = temp.get(key.getNamespaceURI());
				if (uriMap == null) {
					uriMap = new URIMap();
					uriMap.setUri(key.getNamespaceURI());
					result.getURIS().add(uriMap);
					temp.put(key.getNamespaceURI(), uriMap);
				}
				
				SequenceMapping mapping = entry.getValue();
					
				TypeMap typeMap = new TypeMap();
				typeMap.setName(mapping.getTypeName());
				uriMap.getTypes().add(typeMap);
				
				for (Entry<String, Integer> e2 : mapping.getSequenceMap().entrySet())
				{
				    TypeMapEntry resultEntry = new TypeMapEntry();
				    resultEntry.setUUID(e2.getKey());
				
				    resultEntry.setID(e2.getValue());
				    typeMap.getEntries().add(resultEntry);
				}
			}
		
		return result;
	}

}
