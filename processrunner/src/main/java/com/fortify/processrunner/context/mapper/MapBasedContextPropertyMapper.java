package com.fortify.processrunner.context.mapper;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fortify.processrunner.context.Context;

public class MapBasedContextPropertyMapper extends AbstractContextPropertyMapper {
	private Map<Object, ContextPropertiesMap> contextProperties = new HashMap<Object, ContextPropertiesMap>();

	public void addMappedContextProperties(Context context, Object contextPropertyValue) {
		Map<String, Object> mappedContextProperties = contextProperties.get(contextPropertyValue);
		if ( mappedContextProperties!=null ) {
			context.putAll(mappedContextProperties);
		}
	}
	
	public Set<Object> getDefaultValues() {
		return contextProperties.keySet();
	}

	public Map<Object, ContextPropertiesMap> getContextProperties() {
		return contextProperties;
	}

	public void setContextProperties(Map<Object, ContextPropertiesMap> contextProperties) {
		this.contextProperties = contextProperties;
	}
	
	public static final class ContextPropertiesMap extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = 1L;
	}
	
	public static final class ContextPropertiesMapEditor extends PropertyEditorSupport {
		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			String[] contextPropertiesWithValues = text.split(",");
			for ( String contextPropertyWithValue : contextPropertiesWithValues ) {
				int idx = contextPropertyWithValue.indexOf('=');
				String name = contextPropertyWithValue.substring(0, idx);
				String value = contextPropertyWithValue.substring(idx+1);
				map.put(name, value);
			}
			setValue(map);
		}
	}


}
