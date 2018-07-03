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
package com.fortify.bugtracker.src.ssc.context;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.context.AbstractSourceContextGenerator;
import com.fortify.bugtracker.common.ssc.appversion.ISSCApplicationVersionQueryBuilderUpdater;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.context.IContextSSCCommon;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;

@Component
public class SSCSourceApplicationVersionsContextGenerator extends AbstractSourceContextGenerator implements IContextPropertyDefinitionProvider {
	private List<ISSCApplicationVersionQueryBuilderUpdater> queryBuilderUpdaters;

	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_APPLICATION_VERSIONS, "SSC application version names (<application>:<version>) or id's, separated by comma's", true).isAlternativeForProperties(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID));
	}
	
	@Override
	protected AbstractRestConnectionQueryBuilder<?, ?> createBaseQueryBuilder(Context context, boolean hasFilterExpressions) {
		IContextSSCCommon sscCtx = context.as(IContextSSCCommon.class);
		SSCApplicationVersionsQueryBuilder queryBuilder = SSCConnectionFactory.getConnection(context)
				.api(SSCApplicationVersionAPI.class).queryApplicationVersions();
		queryBuilder.onDemandAttributeValuesByName();
		if ( StringUtils.isNotBlank(sscCtx.getSSCApplicationVersionId()) ) {
			queryBuilder.id(sscCtx.getSSCApplicationVersionId());
		} else if ( StringUtils.isNotBlank(sscCtx.getSSCApplicationVersions()) ) {
			queryBuilder.namesOrIds(sscCtx.getSSCApplicationVersions());
		} else if ( !hasFilterExpressions ) {
			throw new IllegalArgumentException("Either application version(s) need to be specified, or configuration file must define application version filters");
		}
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( ISSCApplicationVersionQueryBuilderUpdater updater : getQueryBuilderUpdaters() ) {
				updater.updateSSCApplicationVersionsQueryBuilder(context, queryBuilder);
			}
		}
		return queryBuilder;
	}
	
	@Override
	protected Context updateContextFromJSONMap(Context context, JSONMap json) {
		IContextSSCCommon sscCtx = context.as(IContextSSCCommon.class);
		sscCtx.setSSCApplicationVersionId(json.get("id", String.class));
		sscCtx.setApplicationVersion(json);
		return context;
	}
	
	public final List<ISSCApplicationVersionQueryBuilderUpdater> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public final void setQueryBuilderUpdaters(List<ISSCApplicationVersionQueryBuilderUpdater> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}

}
