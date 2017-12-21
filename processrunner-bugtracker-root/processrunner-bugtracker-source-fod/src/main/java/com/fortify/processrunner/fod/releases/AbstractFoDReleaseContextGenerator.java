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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.api.fod.connection.api.query.builder.FoDReleaseQueryBuilder;
import com.fortify.api.util.rest.json.JSONList;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;

public abstract class AbstractFoDReleaseContextGenerator implements IContextGenerator, IContextPropertyDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(AbstractFoDReleaseContextGenerator.class);
	private List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters;
	
	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_FOD_RELEASES, "FoD application release names (<application>:<release>) or id's, separated by comma's", true).isAlternativeForProperties(IContextFoD.PRP_FOD_RELEASE_ID));
	}
	
	private FoDReleaseQueryBuilder createReleaseQuery(Context context) {
		FoDReleaseQueryBuilder queryBuilder = FoDConnectionFactory.getConnection(context)
				.api().release().queryReleases();
		updateReleaseQueryBuilder(context, queryBuilder);
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( IFoDReleaseQueryBuilderUpdater updater : getQueryBuilderUpdaters() ) {
				updater.updateFoDReleaseQueryBuilder(context, queryBuilder);
			}
		}
		return queryBuilder;
	}
	
	/**
	 * This method can be overridden by subclasses to update the {@link FoDReleaseQueryBuilder}
	 * instance used to retrieve application release JSON objects. This can be used for example 
	 * to add on-demand objects. This default implementation does nothing.
	 * @param context
	 * @param queryBuilder
	 */
	protected void updateReleaseQueryBuilder(Context context, FoDReleaseQueryBuilder queryBuilder) {}
	
	/**
	 * This method must be implemented by subclasses to update the {@link FoDReleaseQueryBuilder}
	 * to search for application releases that match some search criteria.
	 * @param initialContext
	 * @param builder
	 */
	protected abstract void updateReleaseQueryBuilderForSearch(Context initialContext, FoDReleaseQueryBuilder builder);
	
	/**
	 * This method must be implemented by subclasses to add {@link Context} properties for the given 
	 * application release.
	 * @param context
	 * @param release
	 */
	protected abstract void updateContextForRelease(Context context, JSONMap release);
	
	/**
	 * This method can be overridden by subclasses to perform additional filtering when searching for 
	 * matching application releases. This default implementation simply returns true.
	 * @param context
	 * @param release
	 * @return
	 */
	protected boolean isReleaseIncludedInSearch(Context context, JSONMap release) {
		return true;
	}

	@Override
	public Collection<Context> generateContexts(Context initialContext) {
		IContextFoD ctx = initialContext.as(IContextFoD.class);
		String releaseId = ctx.getFoDReleaseId();
		String releaseNamesOrIds = ctx.getFoDReleases();
		if ( StringUtils.isNotBlank(releaseId) ) {
			return generateContextsForSingleReleaseId(initialContext, releaseId);
		} else if ( StringUtils.isNotBlank(releaseNamesOrIds) ) {
			String[] releaseNamesOrIdsArray = releaseNamesOrIds.split(",");
			return generateContextsForReleaseNamesOrIds(initialContext, releaseNamesOrIdsArray);
		} else {
			return generateContextsForMatchedRelease(initialContext);
		}
	}

	private Collection<Context> generateContextsForSingleReleaseId(Context initialContext, String releaseId) {
		Collection<Context> result = new ArrayList<>(1);
		JSONMap release = getReleaseById(initialContext, releaseId);
		addNonNullContextToResult(result, generateContextForRelease(initialContext, release, "id "+releaseId));
		return result;
	}

	private Context generateContextForRelease(Context initialContext, JSONMap release, String releaseDescription) {
		Context result = null;
		if ( release == null ) {
			// TODO Have IFoDReleaseQueryBuilderUpdater implementation provide filtering reasons to display in log message
			LOG.warn("[FoD] Application release with "+releaseDescription+" not found, or doesn't match criteria");
		} else {
			result = new Context(initialContext);
			result.put(IContextFoD.PRP_FOD_RELEASE_ID, release.get("releaseId", String.class));
			updateContextForRelease(result, release);
		}
		return result;
	}

	private Collection<Context> generateContextsForReleaseNamesOrIds(Context initialContext, String[] releaseNamesOrIdsArray) {
		Collection<Context> result = new ArrayList<>(releaseNamesOrIdsArray.length);
		for ( String releaseNameOrId : releaseNamesOrIdsArray ) {
			JSONMap release = getReleaseByNameOrId(initialContext, releaseNameOrId);
			addNonNullContextToResult(result, generateContextForRelease(initialContext, release, "name or id "+releaseNameOrId));
		}
		return result;
	}
	
	private Collection<Context> generateContextsForMatchedRelease(Context initialContext) {
		JSONList releases = getReleaseBySearchCriteria(initialContext);
		Collection<Context> result = new ArrayList<>(releases.size());
		for ( JSONMap release : releases.asValueType(JSONMap.class) ) {
			if ( isReleaseIncludedInSearch(initialContext, release) ) {
				addNonNullContextToResult(result, generateContextForRelease(initialContext, release, null));
			}
		}
		return result;
	}
	
	private void addNonNullContextToResult(Collection<Context> result, Context context) {
		if ( context != null ) {
			result.add(context);
		}
	}

	private JSONMap getReleaseById(Context initialContext, String releaseId) {
		return createReleaseQuery(initialContext).releaseId(releaseId).build().getUnique();
	}

	private JSONMap getReleaseByNameOrId(Context initialContext, String releaseNameOrId) {
		return createReleaseQuery(initialContext).nameOrId(releaseNameOrId).build().getUnique();
	}

	private JSONList getReleaseBySearchCriteria(Context initialContext) {
		FoDReleaseQueryBuilder builder = createReleaseQuery(initialContext);
		updateReleaseQueryBuilderForSearch(initialContext, builder);
		return builder.build().getAll();
	}

	public final List<IFoDReleaseQueryBuilderUpdater> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public final void setQueryBuilderUpdaters(List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}
}
