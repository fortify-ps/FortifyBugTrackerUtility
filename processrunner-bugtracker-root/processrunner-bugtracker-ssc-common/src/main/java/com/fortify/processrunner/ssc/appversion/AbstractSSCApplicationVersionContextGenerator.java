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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.api.ssc.connection.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.api.util.rest.json.JSONList;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;

public abstract class AbstractSSCApplicationVersionContextGenerator implements IContextGenerator, IContextPropertyDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(AbstractSSCApplicationVersionContextGenerator.class);
	private List<ISSCApplicationVersionQueryBuilderUpdater> queryBuilderUpdaters;
	
	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_APPLICATION_VERSIONS, "SSC application version names (<application>:<version>) or id's, separated by comma's", true).isAlternativeForProperties(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID));
	}
	
	private SSCApplicationVersionsQueryBuilder createApplicationVersionsQuery(Context context) {
		SSCApplicationVersionsQueryBuilder queryBuilder = SSCConnectionFactory.getConnection(context)
				.api().applicationVersion().queryApplicationVersions();
		updateApplicationVersionsQueryBuilder(context, queryBuilder);
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( ISSCApplicationVersionQueryBuilderUpdater updater : getQueryBuilderUpdaters() ) {
				updater.updateSSCApplicationVersionsQueryBuilder(context, queryBuilder);
			}
		}
		return queryBuilder;
	}
	
	/**
	 * This method can be overridden by subclasses to update the {@link SSCApplicationVersionsQueryBuilder}
	 * instance used to retrieve application version JSON objects. This can be used for example to add
	 * on-demand objects. This default implementation does nothing.
	 * @param context
	 * @param queryBuilder
	 */
	protected void updateApplicationVersionsQueryBuilder(Context context, SSCApplicationVersionsQueryBuilder queryBuilder) {}
	
	/**
	 * This method must be implemented by subclasses to update the {@link SSCApplicationVersionsQueryBuilder}
	 * to search for application versions that match some search criteria.
	 * @param initialContext
	 * @param builder
	 */
	protected abstract void updateApplicationVersionsQueryBuilderForSearch(Context initialContext, SSCApplicationVersionsQueryBuilder builder);
	
	/**
	 * This method must be implemented by subclasses to add {@link Context} properties for the given 
	 * application version.
	 * @param context
	 * @param applicationVersion
	 */
	protected abstract void updateContextForApplicationVersion(Context context, JSONMap applicationVersion);
	
	/**
	 * This method can be overridden by subclasses to perform additional filtering when searching for 
	 * matching application versions. This default implementation simply returns true.
	 * @param context
	 * @param applicationVersion
	 * @return
	 */
	protected boolean isApplicationVersionIncludedInSearch(Context context, JSONMap applicationVersion) {
		return true;
	}

	@Override
	public Collection<Context> generateContexts(Context initialContext) {
		IContextSSCCommon ctx = initialContext.as(IContextSSCCommon.class);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		String applicationVersionNamesOrIds = ctx.getSSCApplicationVersions();
		if ( StringUtils.isNotBlank(applicationVersionId) ) {
			return generateContextsForSingleApplicationVersionId(initialContext, applicationVersionId);
		} else if ( StringUtils.isNotBlank(applicationVersionNamesOrIds) ) {
			String[] applicationVersionNamesOrIdsArray = applicationVersionNamesOrIds.split(",");
			return generateContextsForApplicationVersionNamesOrIds(initialContext, applicationVersionNamesOrIdsArray);
		} else {
			return generateContextsForMatchedApplicationVersions(initialContext);
		}
	}

	private Collection<Context> generateContextsForSingleApplicationVersionId(Context initialContext, String applicationVersionId) {
		Collection<Context> result = new ArrayList<>(1);
		JSONMap applicationVersion = getApplicationVersionById(initialContext, applicationVersionId);
		addNonNullContextToResult(result, generateContextForApplicationVersion(initialContext, applicationVersion, "id "+applicationVersionId));
		return result;
	}

	private Context generateContextForApplicationVersion(Context initialContext, JSONMap applicationVersion, String applicationVersionDescription) {
		Context result = null;
		if ( applicationVersion == null ) {
			// TODO Have ISSCApplicationVersionQueryBuilderUpdater implementation provide filtering reasons to display in log message
			LOG.warn("[SSC] Application version with "+applicationVersionDescription+" not found, or doesn't match criteria");
		} else {
			result = new Context(initialContext);
			result.put(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID, applicationVersion.get("id", String.class));
			updateContextForApplicationVersion(result, applicationVersion);
		}
		return result;
	}

	private Collection<Context> generateContextsForApplicationVersionNamesOrIds(Context initialContext, String[] applicationVersionNamesOrIdsArray) {
		Collection<Context> result = new ArrayList<>(applicationVersionNamesOrIdsArray.length);
		for ( String applicationVersionNameOrId : applicationVersionNamesOrIdsArray ) {
			JSONMap applicationVersion = getApplicationVersionByNameOrId(initialContext, applicationVersionNameOrId);
			addNonNullContextToResult(result, generateContextForApplicationVersion(initialContext, applicationVersion, "name or id "+applicationVersionNameOrId));
		}
		return result;
	}
	
	private Collection<Context> generateContextsForMatchedApplicationVersions(Context initialContext) {
		JSONList applicationVersions = getApplicationVersionsBySearchCriteria(initialContext);
		Collection<Context> result = new ArrayList<>(applicationVersions.size());
		for ( JSONMap applicationVersion : applicationVersions.asValueType(JSONMap.class) ) {
			if ( isApplicationVersionIncludedInSearch(initialContext, applicationVersion) ) {
				addNonNullContextToResult(result, generateContextForApplicationVersion(initialContext, applicationVersion, null));
			}
		}
		return result;
	}
	
	private void addNonNullContextToResult(Collection<Context> result, Context context) {
		if ( context != null ) {
			result.add(context);
		}
	}

	private JSONMap getApplicationVersionById(Context initialContext, String applicationVersionId) {
		return createApplicationVersionsQuery(initialContext).id(applicationVersionId).build().getUnique();
	}

	private JSONMap getApplicationVersionByNameOrId(Context initialContext, String applicationVersionNameOrId) {
		return createApplicationVersionsQuery(initialContext).nameOrId(applicationVersionNameOrId).build().getUnique();
	}

	private JSONList getApplicationVersionsBySearchCriteria(Context initialContext) {
		SSCApplicationVersionsQueryBuilder builder = createApplicationVersionsQuery(initialContext);
		updateApplicationVersionsQueryBuilderForSearch(initialContext, builder);
		return builder.build().getAll();
	}

	public final List<ISSCApplicationVersionQueryBuilderUpdater> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public final void setQueryBuilderUpdaters(List<ISSCApplicationVersionQueryBuilderUpdater> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}
}
