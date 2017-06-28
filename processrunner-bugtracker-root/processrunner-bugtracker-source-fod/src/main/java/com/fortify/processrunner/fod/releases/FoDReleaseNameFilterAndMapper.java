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
package com.fortify.processrunner.fod.releases;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * Filter FoD releases based on application and release name,
 * and add corresponding context properties.
 * 
 * @author Ruud Senden
 *
 */
public class FoDReleaseNameFilterAndMapper implements IFoDReleaseFilter, IFoDReleaseContextUpdater {
	private LinkedHashMap<Pattern, Context> releaseNameMappings = null;
	
	public int getOrder() {
		return 0;
	}
	
	public boolean isReleaseIncluded(Context context, JSONMap release) {
		return getReleaseNameMapping(release)!=null;
	}
	
	public void updateContext(Context context, JSONMap release) {
		addMappedAttributes(context, release, getReleaseNameMapping(release).getValue());
	}

	public LinkedHashMap<Pattern, Context> getReleaseNameMappings() {
		return releaseNameMappings;
	}

	/**
	 * Configure a mapping between regular expressions that match
	 * [applicationName]:[releaseName], and corresponding context 
	 * properties. Context property values may contain Spring Template 
	 * Expressions referencing the release JSON attributes.
	 * @param releaseNameMappings
	 */
	public void setReleaseNameMappings(LinkedHashMap<Pattern, Context> releaseNameMappings) {
		this.releaseNameMappings = releaseNameMappings;
	}
	
	private Map.Entry<Pattern, Context> getReleaseNameMapping(JSONMap release) {
		String name = release.get("applicationName", String.class)+":"+release.get("releasName",String.class);
		LinkedHashMap<Pattern, Context> releaseNameMappings = getReleaseNameMappings();
		for ( Map.Entry<Pattern, Context> entry : releaseNameMappings.entrySet() ) {
			if ( entry.getKey().matcher(name).matches() ) { return entry; }
		}
		return null;
	}
	
	private void addMappedAttributes(Context initialContext, JSONMap release, Context mappedContext) {
		for ( Entry<String, Object> entry : mappedContext.entrySet() ) {
			initialContext.put(entry.getKey(), SpringExpressionUtil.evaluateTemplateExpression(release, (String)entry.getValue(), Object.class));
		}
	}
}
