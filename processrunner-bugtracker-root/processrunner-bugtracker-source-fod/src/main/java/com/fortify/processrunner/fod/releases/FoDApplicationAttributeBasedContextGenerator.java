/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.processrunner.fod.releases;

import java.util.Map;

import com.fortify.api.fod.connection.api.query.builder.FoDReleasesQueryBuilder;
import com.fortify.api.fod.connection.api.query.builder.FoDOrderByDirection;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;

/**
 * Filter FoD application release based on application attributes,
 * and map application attribute values to context properties.
 * 
 * @author Ruud Senden
 *
 */
public class FoDApplicationAttributeBasedContextGenerator extends AbstractFoDReleaseContextGenerator {
	private Map<String, String> optionalAttributeMappings = null;
	private Map<String, String> requiredAttributeMappings = null;
	
	/**
	 * This implementation adds the order by parameter to allow for optimal
	 * application cache use.
	 */
	@Override
	protected void updateReleaseQueryBuilder(Context context, FoDReleasesQueryBuilder builder) {
		builder.paramOrderBy("applicationId", FoDOrderByDirection.ASC);
	}
	
	@Override
	protected void updateReleaseQueryBuilderForSearch(Context initialContext, FoDReleasesQueryBuilder builder) {
		// Nothing to do, we filter afterwards in isReleaseIncludedInSearch()
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean isReleaseIncludedInSearch(Context context, JSONMap release) {
		JSONMap application = FoDConnectionFactory.getConnection(context).api().application().getApplicationById(release.get("applicationId", String.class));
		release.put("application", application);
		return application.get("attributesMap", Map.class).keySet().containsAll(requiredAttributeMappings.keySet());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateContextForRelease(Context context, JSONMap release) {
		Map<String, String> avAttributes = release.getPath("application.attributesMap", Map.class);
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
	
	private void addMappedAttributes(Context context, Map<String, String> avAttributes, Map<String, String> mappings, boolean required) {
		if ( mappings != null ) {
			for ( Map.Entry<String, String> entry : mappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxKey = entry.getValue();
				String value = avAttributes.get(attrName);
				context.put(ctxKey, value);
			}
		}
	}
}
