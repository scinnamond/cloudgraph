package org.cloudgraph.web.model.profile;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.PlasmaDataGraphVisitor;

import org.cloudgraph.web.sdo.personalization.DefaultElementSetting;
import org.cloudgraph.web.sdo.personalization.Element;
import org.cloudgraph.web.sdo.personalization.ProfileElementSetting;
import org.cloudgraph.web.sdo.personalization.Setting;

import commonj.sdo.DataObject;

public class SettingCollector implements PlasmaDataGraphVisitor 
{
    private static Log log = LogFactory.getLog(SettingCollector.class);
    
    private Map<String, Map<String, Setting>> settings = new HashMap<String, Map<String, Setting>>();
    private Map<String, Element> elements = new HashMap<String, Element>();
	private String roleName;
    
	@SuppressWarnings("unused")
	private SettingCollector() {}
	public SettingCollector(String roleName) {
		this.roleName = roleName;
		if (this.roleName == null || this.roleName.trim().length() == 0)
			throw new IllegalArgumentException("expected roleName arg");
	}
	
	public void visit(DataObject target, DataObject source,
			String sourceKey, int level) {
		
		if (!(target instanceof Element))
			return;
		
		Element element = (Element)target;
		this.elements.put(element.getName(), element);
		
		Map<String, Setting> componentMap = settings.get(element.getName());
		
		// map defaults first
		if (element.getDefaultElementSettingCount() > 0) {
			if (componentMap == null) {
				componentMap = new HashMap<String, Setting>();
				settings.put(element.getName(), componentMap);
			}
		    for (DefaultElementSetting elementSetting : element.getDefaultElementSetting()) {
		    	if (elementSetting.getRole() != null && elementSetting.getRole().getName() != null) {
		    		if (elementSetting.getRole().getName().equals(this.roleName)) {
			            componentMap.put(elementSetting.getSetting().getName(), 
			    		    elementSetting.getSetting());
		    		}
		    	}
		    	else
		    		throw new IllegalStateException("expected Role for DefaultElementSetting");
		    }    
		}
		
        // let profile override / overwrite
		if (element.getProfileElementSettingCount() > 0) {
			if (componentMap == null) {
				componentMap = new HashMap<String, Setting>();
				settings.put(element.getName(), componentMap);
			}
		    for (ProfileElementSetting elementSetting : element.getProfileElementSetting())
			    componentMap.put(elementSetting.getSetting().getName(), 
			    		elementSetting.getSetting());
		}
	}

	public Map<String, Map<String, Setting>> getSettings() {
		return settings;
	}
	
	public Map<String, Element> getElements() {
		return elements;
	}

	public void clear() {
		this.settings.clear();
	}
	
}		

