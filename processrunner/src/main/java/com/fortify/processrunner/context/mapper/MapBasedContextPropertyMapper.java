package com.fortify.processrunner.context.mapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fortify.processrunner.context.Context;

/**
 * <p>This implementation of {@link IContextPropertyMapper} allows for configuring
 * a static Map of values for a configured context property name with their
 * corresponding dependent context properties.</p>
 * 
 * For example:
 * <ul>
 *  <li>Configured context property name: SomeIdProperty
 *  <li>Map contents:
 *    <ul>
 *      <li>key=1, value="DependentProperty1=SomeValue1,DependentProperty2=SomeValue2"</li>
 *      <li>key=1, value="DependentProperty1=SomeOtherValue1,DependentProperty2=SomeOtherValue2"</li>
 *    </ul>
 *  </li>
 * </ul>
 * If the user specified value 1 for SomeIdProperty, the corresponding values for DependentProperty1
 * and DependentProperty2 will be added to the {@link Context}. If the user didn't generate a value
 * for SomeIdProperty, this class will provide default values 1 and 2 for SomeIdProperty, together
 * with the corresponding context properties. 
 * @author Ruud Senden
 *
 */
public class MapBasedContextPropertyMapper extends AbstractContextPropertyMapper {
	private Map<Object, Context> contexts = new HashMap<Object, Context>();

	public void addMappedContextProperties(Context context, Object contextPropertyValue) {
		Map<String, Object> mappedContextProperties = contexts.get(contextPropertyValue);
		if ( mappedContextProperties!=null ) {
			context.putAll(mappedContextProperties);
		}
	}
	
	public Map<Object, Context> getDefaultValuesWithExtraContextProperties(Context initialContext) {
		return getContexts();
	}

	public Map<Object, Context> getContexts() {
		return contexts;
	}

	public void setContexts(Map<Object, Context> contexts) {
		this.contexts = contexts;
	}
	
	public static final class ContextPropertiesMap extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = 1L;
	}


}
