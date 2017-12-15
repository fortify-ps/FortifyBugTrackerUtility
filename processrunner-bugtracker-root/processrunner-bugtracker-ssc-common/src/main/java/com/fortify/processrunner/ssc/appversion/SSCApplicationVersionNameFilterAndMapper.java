/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.context.Context;

/**
 * Filter SSC application versions based on application and version name,
 * and add corresponding context properties.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionNameFilterAndMapper implements ISSCApplicationVersionFilter, ISSCApplicationVersionContextUpdater {
	private LinkedHashMap<Pattern, Context> applicationVersionNameMappings = null;
	
	public int getOrder() {
		return 0;
	}
	
	public boolean isApplicationVersionIncluded(Context context, JSONMap applicationVersion) {
		return getApplicationVersionNameNameMapping(applicationVersion)!=null;
	}
	
	public void updateContext(Context context, JSONMap applicationVersion) {
		addMappedAttributes(context, applicationVersion, getApplicationVersionNameNameMapping(applicationVersion).getValue());
	}

	public LinkedHashMap<Pattern, Context> getApplicationVersionNameMappings() {
		return applicationVersionNameMappings;
	}

	/**
	 * Configure a mapping between regular expressions that match
	 * [applicationName]:[versionName], and corresponding context 
	 * properties. Context property values may contain Spring Template 
	 * Expressions referencing the application version JSON attributes.
	 * @param applicationVersionNameMappings
	 */
	public void setApplicationVersionNameMappings(LinkedHashMap<Pattern, Context> applicationVersionNameMappings) {
		this.applicationVersionNameMappings = applicationVersionNameMappings;
	}
	
	private Map.Entry<Pattern, Context> getApplicationVersionNameNameMapping(JSONMap applicationVersion) {
		String name = SpringExpressionUtil.evaluateExpression(applicationVersion, "project.name+':'+name", String.class);
		for ( Map.Entry<Pattern, Context> entry : getApplicationVersionNameMappings().entrySet() ) {
			if ( entry.getKey().matcher(name).matches() ) { return entry; }
		}
		return null;
	}
	
	private void addMappedAttributes(Context initialContext, JSONMap applicationVersion, Context mappedContext) {
		for ( Entry<String, Object> entry : mappedContext.entrySet() ) {
			initialContext.put(entry.getKey(), SpringExpressionUtil.evaluateTemplateExpression(applicationVersion, (String)entry.getValue(), Object.class));
		}
	}
}
