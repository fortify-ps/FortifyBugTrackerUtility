package com.fortify.processrunner.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * <p>This abstract base class combines {@link IContextGenerator} and {@link IContextUpdater}
 * to allow implementations to generate default values for the configured {@link #contextPropertyName},
 * and add mapped context properties.</p>
 *  
 * @author Ruud Senden
 *
 */
public abstract class AbstractContextGeneratorAndUpdater implements IContextGenerator, IContextUpdater {
	private final String isContextGeneratedFromDefaultValuesKey = "isContextGeneratedFromDefaultValues-"+this.getClass().getName();
	private String contextPropertyName;
	private boolean useForDefaultValueGeneration = false;
	
	public boolean isContextGeneratorEnabled() {
		return isUseForDefaultValueGeneration();
	}
	
	public final Collection<Context> generateContexts(Context initialContext) {
		Collection<Context> result = new ArrayList<Context>();
		Object value = initialContext.get(getContextPropertyName());
		if ( value!=null && !(value instanceof String && StringUtils.isBlank((String)value)) ) {
			addMappedContextProperties(initialContext, value);
			result.add(initialContext);
		} else {
			for ( Map.Entry<Object, Context> entry : getDefaultValuesWithMappedContextProperties(initialContext).entrySet() ) {
				Context newContext = new Context(initialContext);
				newContext.putAll(initialContext);
				newContext.put(getContextPropertyName(), entry.getKey());
				newContext.put(isContextGeneratedFromDefaultValuesKey, true);
				result.add(newContext);
			}
		}
		return result;
	}
	
	public void updateContext(Context initialContext) {
		if ( !initialContext.containsKey(isContextGeneratedFromDefaultValuesKey) ) {
			addMappedContextProperties(initialContext, initialContext.get(getContextPropertyName()));
		}
	}
	
	public boolean isUseForDefaultValueGeneration() {
		return useForDefaultValueGeneration;
	}

	public void setUseForDefaultValueGeneration(boolean useForDefaultValueGeneration) {
		this.useForDefaultValueGeneration = useForDefaultValueGeneration;
	}

	public String getContextPropertyName() {
		return contextPropertyName;
	}

	public void setContextPropertyName(String contextPropertyName) {
		this.contextPropertyName = contextPropertyName;
	}
	
	/**
	 * p>Get the default values for the configured context property name,
	 * together with all mapped context properties for each default value.</p>
	 * 
	 * <p>The {@link #addMappedContextProperties(Context, Object)} will not
	 * be called for contexts returned by this method. Implementations are 
	 * responsible for generating a new {@link Context} instance for every
	 * returned default value, containing any mapped context properties.<p>
	 * 
	 * <p>This allows implementations to load all necessary data for both
	 * generating default values and the corresponding mapped context 
	 * properties as a single operation, instead of first generating all
	 * default values, and then later on iterating over the list of default
	 * values and adding mapped context properties for each of them.</p>
	 * 
	 * @param initialContext
	 * @return
	 */
	protected abstract Map<Object, Context> getDefaultValuesWithMappedContextProperties(Context initialContext);

	/**
	 * Add mapped context properties for the given value for the configured context
	 * property name to the given context.
	 * 
	 * @param context
	 * @param contextPropertyValue
	 */
	protected abstract void addMappedContextProperties(Context context, Object contextPropertyValue);
}
