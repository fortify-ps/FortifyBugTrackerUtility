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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.context.AbstractSourceContextGenerator;
import com.fortify.bugtracker.src.fod.connection.FoDConnectionFactory;
import com.fortify.bugtracker.src.fod.releases.IFoDReleaseQueryBuilderUpdater;
import com.fortify.client.fod.api.FoDReleaseAPI;
import com.fortify.client.fod.api.query.builder.FoDReleasesQueryBuilder;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;

@Component
public class FoDSourceApplicationReleasesContextGenerator extends AbstractSourceContextGenerator implements IContextPropertyDefinitionProvider {
	private List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters;

	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_FOD_RELEASES, "FoD application release names (<application>:<release>) or id's, separated by comma's", true).isAlternativeForProperties(IContextFoD.PRP_FOD_RELEASE_ID));
	}
	
	@Override
	protected AbstractRestConnectionQueryBuilder<?, ?> createBaseQueryBuilder(Context context, boolean hasFilterExpressions) {
		IContextFoD fodCtx = context.as(IContextFoD.class);
		FoDReleasesQueryBuilder queryBuilder = FoDConnectionFactory.getConnection(context)
				.api(FoDReleaseAPI.class).queryReleases();
		queryBuilder.onDemandApplicationWithAttributesMap();
		if ( StringUtils.isNotBlank(fodCtx.getFoDReleaseId()) ) {
			queryBuilder.releaseId(fodCtx.getFoDReleaseId());
		} else if ( StringUtils.isNotBlank(fodCtx.getFoDReleases()) ) {
			queryBuilder.namesOrIds(fodCtx.getFoDReleases());
		} else if ( !hasFilterExpressions ) {
			throw new IllegalArgumentException("Either release(s) need to be specified, or configuration file must define release filters");
		}
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( IFoDReleaseQueryBuilderUpdater updater : getQueryBuilderUpdaters() ) {
				updater.updateFoDReleaseQueryBuilder(context, queryBuilder);
			}
		}
		return queryBuilder;
	}
	
	@Override
	protected Context updateContextFromJSONMap(Context context, JSONMap json) {
		IContextFoD fodCtx = context.as(IContextFoD.class);
		fodCtx.setFoDReleaseId(json.get("releaseId", String.class));
		fodCtx.setRelease(json);
		return context;
	}
	
	public final List<IFoDReleaseQueryBuilderUpdater> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public final void setQueryBuilderUpdaters(List<IFoDReleaseQueryBuilderUpdater> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}

}
