package com.fortify.processrunner.ssc.appversion;

import java.util.List;
import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;

/**
 * This class generates default values for the {@link IContextSSCCommon#PRP_SSC_APPLICATION_VERSION_ID}
 * context property by loading all SSC application versions and filtering them through any
 * configured {@link ISSCApplicationVersionFilter} instances. Filters for configured {@link #requiredAttributeMappings}
 * are automatically added. For each SSC application version id, this class will generate dependent
 * {@link Context} properties based on the configured {@link #requiredAttributeMappings} and
 * {@link #optionalAttributeMappings}. 
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionAttributeContextPropertyMapper extends SSCApplicationVersionIdGenerator {
	private Map<String, String> optionalAttributeMappings = null;
	private Map<String, String> requiredAttributeMappings = null;
	
	@Override
	protected void addMappedContextProperties(Context context, String pvId) {
		Map<String, List<String>> avAttributes = SSCConnectionFactory.getConnection(context).getApplicationVersionAttributeValuesByName(pvId);
		addMappedAttributes(context, avAttributes, optionalAttributeMappings, false);
		addMappedAttributes(context, avAttributes, requiredAttributeMappings, true);
	}
	
	@Override
	protected void addExtraFilters(Context context, List<ISSCApplicationVersionFilter> filters) {
		if ( getRequiredAttributeMappings()!=null ) {
			SSCApplicationVersionFilterAttributeValue filter = new SSCApplicationVersionFilterAttributeValue();
			filter.setAttributesWithAnyValue(requiredAttributeMappings.keySet());
			filters.add(filter);
		}
	}

	public Map<String, String> getOptionalAttributeMappings() {
		return optionalAttributeMappings;
	}

	public void setOptionalAttributeMappings(Map<String, String> optionalAttributeMappings) {
		this.optionalAttributeMappings = optionalAttributeMappings;
	}

	public Map<String, String> getRequiredAttributeMappings() {
		return requiredAttributeMappings;
	}

	public void setRequiredAttributeMappings(Map<String, String> requiredAttributeMappings) {
		this.requiredAttributeMappings = requiredAttributeMappings;
	}
	
	private void addMappedAttributes(Context context, Map<String, List<String>> avAttributes, Map<String, String> mappings, boolean required) {
		if ( mappings != null ) {
			for ( Map.Entry<String, String> entry : mappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxKey = entry.getValue();
				List<String> values = avAttributes.get(attrName);
				if ( values!=null && values.size()==1 ) {
					context.put(ctxKey, values.get(0));
				} else if ( values!=null && values.size() > 1 ) {
					throw new IllegalStateException("Cannot map application version attribute "+attrName+" containing multiple values to context property "+ctxKey);
				} else if ( required && !context.containsKey(ctxKey) ) {
					throw new IllegalStateException("Required application version attribute "+attrName+" has no value");
				}
			}
		}
	}
}
