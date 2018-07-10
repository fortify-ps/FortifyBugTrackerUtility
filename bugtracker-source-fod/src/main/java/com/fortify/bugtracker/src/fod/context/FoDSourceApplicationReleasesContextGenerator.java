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
package com.fortify.bugtracker.src.fod.context;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.context.AbstractSourceContextGenerator;
import com.fortify.bugtracker.src.fod.config.FoDSourceReleasesConfiguration;
import com.fortify.bugtracker.src.fod.connection.FoDConnectionFactory;
import com.fortify.bugtracker.src.fod.releases.IFoDReleaseQueryBuilderUpdater;
import com.fortify.bugtracker.src.fod.releases.json.preprocessor.filter.FoDJSONMapFilterListenerLoggerRelease;
import com.fortify.client.fod.api.FoDReleaseAPI;
import com.fortify.client.fod.api.query.builder.FoDReleasesQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.IJSONMapFilterListener;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterListenerLogger.LogLevel;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;
import com.fortify.util.spring.SpringExpressionUtil;

@Component
public class FoDSourceApplicationReleasesContextGenerator extends AbstractSourceContextGenerator<FoDSourceReleasesConfiguration> implements IContextPropertyDefinitionProvider {
	private List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters;

	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_FOD_RELEASES, "FoD application release names (<application>:<release>) or id's, separated by comma's", true).isAlternativeForProperties(IContextFoD.PRP_FOD_RELEASE_ID));
	}
	
	@Override
	protected AbstractRestConnectionQueryBuilder<?, ?> createBaseQueryBuilder(Context context) {
		IContextFoD fodCtx = context.as(IContextFoD.class);
		FoDReleasesQueryBuilder queryBuilder = FoDConnectionFactory.getConnection(context)
				.api(FoDReleaseAPI.class).queryReleases();
		queryBuilder.onDemandApplicationWithAttributesMap();
		if ( StringUtils.isNotBlank(fodCtx.getFoDReleaseId()) ) {
			queryBuilder.releaseId(fodCtx.getFoDReleaseId());
		} else if ( StringUtils.isNotBlank(fodCtx.getFoDReleases()) ) {
			queryBuilder.namesOrIds(fodCtx.getFoDReleases());
		} 
		
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( IFoDReleaseQueryBuilderUpdater updater : getQueryBuilderUpdaters() ) {
				updater.updateFoDReleaseQueryBuilder(context, queryBuilder);
			}
		}
		return queryBuilder;
	}
	
	@Override
	protected void updateContextForSourceObject(Context context, JSONMap sourceObject) {
		IContextFoD fodCtx = context.as(IContextFoD.class);
		fodCtx.setFoDReleaseId(sourceObject.get("releaseId", String.class));
		fodCtx.setRelease(sourceObject);
		fodCtx.setFoDApplicationAndReleaseName(getName(sourceObject));
	}
	
	public final List<IFoDReleaseQueryBuilderUpdater> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public final void setQueryBuilderUpdaters(List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}

	@Override
	protected FoDSourceReleasesConfiguration getDefaultConfig() {
		return new FoDSourceReleasesConfiguration();
	}

	@Override
	protected boolean ignoreConfiguredFilters(Context initialContext) {
		IContextFoD sscCtx = initialContext.as(IContextFoD.class);
		return StringUtils.isNotBlank(sscCtx.getFoDReleaseId()) 
				|| StringUtils.isNotBlank(sscCtx.getFoDReleases());
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForFilterExpression(Context initialContext) {
		return new FoDJSONMapFilterListenerLoggerRelease(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match configured filter expression");
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForNamePatterns(Context initialContext) {
		return new FoDJSONMapFilterListenerLoggerRelease(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match any configured application version name");
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForAttributes(Context initialContext) {
		return new FoDJSONMapFilterListenerLoggerRelease(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} have values for all attributes ${filter.requiredAttributeNames.toString()}");
	}

	@Override
	protected String getAttributeValue(JSONMap sourceObject, String attributeName) {
		JSONMap attributesMap = SpringExpressionUtil.evaluateExpression(sourceObject, "applicationWithAttributesMap.attributesMap", JSONMap.class);
		JSONList attributeValues = attributesMap==null?null:attributesMap.get(attributeName, JSONList.class);
		String attributeValue = CollectionUtils.isEmpty(attributeValues)?null:(String)attributeValues.get(0);
		return attributeValue;
	}

	@Override
	protected String getName(JSONMap sourceObject) {
		return SpringExpressionUtil.evaluateTemplateExpression(sourceObject, "${applicationName}:${releaseName}", String.class);
	}

}
