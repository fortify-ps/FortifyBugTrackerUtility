/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.ssc.appversion;

import java.util.Map;

import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.appversion.json.preprocessor.filter.SSCJSONMapFilterWithLoggerApplicationVersionHasValuesForAllAttributes;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;

/**
 * Filter SSC application versions based on application version attributes,
 * and map application version attribute values to context properties.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionAttributeBasedContextGenerator extends AbstractSSCApplicationVersionContextGenerator {
	private Map<String, String> optionalAttributeMappings = null;
	private Map<String, String> requiredAttributeMappings = null;
	
	/**
	 * This implementation adds the attributeValuesByName on-demand property.
	 */
	@Override
	protected void updateApplicationVersionsQueryBuilder(Context context, SSCApplicationVersionsQueryBuilder builder) {
		builder.onDemandAttributeValuesByName();
	}
	
	@Override
	protected void updateApplicationVersionsQueryBuilderForSearch(Context initialContext, SSCApplicationVersionsQueryBuilder builder) {
		if ( requiredAttributeMappings != null && !requiredAttributeMappings.isEmpty() ) {
			builder.preProcessor(new SSCJSONMapFilterWithLoggerApplicationVersionHasValuesForAllAttributes(MatchMode.INCLUDE, requiredAttributeMappings.keySet()));
		}
	}
	
	@Override
	public void updateContextForApplicationVersion(Context context, JSONMap applicationVersion) {
		JSONMap avAttributes = applicationVersion.get("attributeValuesByName", JSONMap.class);
		addMappedAttributes(context, avAttributes, optionalAttributeMappings, false);
		addMappedAttributes(context, avAttributes, requiredAttributeMappings, true);
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
	
	private void addMappedAttributes(Context context, JSONMap avAttributes, Map<String, String> mappings, boolean required) {
		if ( mappings != null ) {
			for ( Map.Entry<String, String> entry : mappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxKey = entry.getValue();
				JSONList values = avAttributes.get(attrName, JSONList.class);
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
