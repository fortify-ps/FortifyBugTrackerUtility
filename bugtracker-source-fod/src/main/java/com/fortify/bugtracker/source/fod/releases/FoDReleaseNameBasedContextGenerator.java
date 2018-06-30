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
package com.fortify.bugtracker.source.fod.releases;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.fortify.bugtracker.source.fod.releases.json.preprocessor.filter.FoDJSONMapFilterListenerLoggerRelease;
import com.fortify.client.fod.api.query.builder.FoDReleasesQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterListenerLogger.LogLevel;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * Filter FoD application versions based on application and release name,
 * and add corresponding context properties.
 * 
 * @author Ruud Senden
 *
 */
public class FoDReleaseNameBasedContextGenerator extends AbstractFoDReleaseContextGenerator {
	private LinkedHashMap<Pattern, Context> releaseNamePatternToContextMap = null;
	
	@Override
	protected void updateReleaseQueryBuilderForSearch(Context initialContext, FoDReleasesQueryBuilder builder) {
		builder.preProcessor(new FoDJSONMapFilterReleaseNamePatterns(MatchMode.INCLUDE, releaseNamePatternToContextMap.keySet()));
	}
	
	@Override
	public void updateContextForRelease(Context context, JSONMap release) {
		addMappedAttributes(context, release, getContextForRelease(release));
	}

	/**
	 * Configure a mapping between regular expressions that match
	 * [applicationName]:[versionName], and corresponding context 
	 * properties. Context property values may contain Spring Template 
	 * Expressions referencing the application version JSON attributes.
	 * @param releaseNameMappings
	 */
	public void setReleaseNameMappings(LinkedHashMap<Pattern, Context> releaseNameMappings) {
		this.releaseNamePatternToContextMap = releaseNameMappings;
	}
	
	private Context getContextForRelease(JSONMap release) {
		String name = SpringExpressionUtil.evaluateExpression(release, "applicationName+':'+releaseName", String.class);
		for ( Map.Entry<Pattern, Context> entry : releaseNamePatternToContextMap.entrySet() ) {
			if ( entry.getKey().matcher(name).matches() ) { return entry.getValue(); }
		}
		return null;
	}
	
	private void addMappedAttributes(Context initialContext, JSONMap release, Context mappedContext) {
		if ( mappedContext != null ) {
			for ( Entry<String, Object> entry : mappedContext.entrySet() ) {
				initialContext.put(entry.getKey(), SpringExpressionUtil.evaluateTemplateExpression(release, (String)entry.getValue(), Object.class));
			}
		}
	}
	
	private static final class FoDJSONMapFilterReleaseNamePatterns extends AbstractJSONMapFilter {
		private final Set<Pattern> releaseNamePatterns;
		public FoDJSONMapFilterReleaseNamePatterns(MatchMode matchMode, Set<Pattern> releaseNamePatterns) {
			super(matchMode);
			this.releaseNamePatterns = releaseNamePatterns;
			addFilterListeners(new FoDJSONMapFilterListenerLoggerRelease(LogLevel.INFO, 
				null,
				"name ${textObjectDoesOrDoesnt} match any RegEx "+releaseNamePatterns));
		}
		
		@Override
		protected boolean isMatching(JSONMap release) {
			String name = SpringExpressionUtil.evaluateExpression(release, "applicationName+':'+releaseName", String.class);
			for ( Pattern pattern : releaseNamePatterns ) {
				if ( pattern.matcher(name).matches() ) { return true; }
			}
			return false;
		}
		
	}
}
