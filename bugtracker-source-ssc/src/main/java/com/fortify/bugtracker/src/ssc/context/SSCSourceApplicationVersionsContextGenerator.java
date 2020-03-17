/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.context.AbstractSourceContextGenerator;
import com.fortify.bugtracker.common.ssc.cli.ICLIOptionsSSC;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.context.IContextSSCCommon;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterListenerLoggerApplicationVersion;
import com.fortify.bugtracker.src.ssc.config.SSCSourceApplicationVersionsConfiguration;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.query.builder.EmbedType;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.cli.ICLIOptionDefinitionProvider;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.IJSONMapFilterListener;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterListenerLogger.LogLevel;
import com.fortify.util.spring.SpringExpressionUtil;

@Component
public class SSCSourceApplicationVersionsContextGenerator extends AbstractSourceContextGenerator<SSCSourceApplicationVersionsConfiguration, SSCApplicationVersionsQueryBuilder> implements ICLIOptionDefinitionProvider {
	
	@Override
	protected String getCLIOptionNameForId() {
		return ICLIOptionsSSC.PRP_SSC_APPLICATION_VERSION_ID;
	}
	
	@Override
	protected String getCLIOptionNameForName() {
		return ICLIOptionsSSC.PRP_SSC_APPLICATION_VERSION_NAME;
	}

	@Override
	protected String getCLIOptionNameForNamePatterns() {
		return ICLIOptionsSSC.PRP_SSC_APPLICATION_VERSION_NAME_PATTERNS;
	}

	@Override
	public void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		SSCConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_ID);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_NAME);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_NAME_PATTERNS);
	}
	
	@Override
	protected SSCSourceApplicationVersionsConfiguration getDefaultConfig() {
		return new SSCSourceApplicationVersionsConfiguration();
	}
	
	@Override
	protected SSCApplicationVersionsQueryBuilder createBaseQueryBuilder(Context context) {
		return SSCConnectionFactory.getConnection(context)
				.api(SSCApplicationVersionAPI.class).queryApplicationVersions()
				.embedAttributeValuesByName(EmbedType.PRELOAD);
	}
	
	@Override
	protected void updateQueryBuilderWithId(Context initialContext,	SSCApplicationVersionsQueryBuilder queryBuilder) {
		queryBuilder.id(ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_ID.getValue(initialContext));
	}

	@Override
	protected void updateQueryBuilderWithName(Context initialContext, SSCApplicationVersionsQueryBuilder queryBuilder) {
		queryBuilder.applicationAndOrVersionName(ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_NAME.getValue(initialContext));
	}

	@Override
	protected void updateContextForSourceObject(Context context, JSONMap applicationVersion) {
		IContextSSCCommon sscCtx = context.as(IContextSSCCommon.class);
		context.put(ICLIOptionsSSC.PRP_SSC_APPLICATION_VERSION_ID, applicationVersion.get("id", String.class));
		sscCtx.setApplicationVersion(applicationVersion);
		sscCtx.setSSCApplicationAndVersionName(getSourceObjectName(applicationVersion));
	}
	
	@Override
	protected IJSONMapFilterListener getFilterListenerForContextNamePatterns(Context initialContext) {
		return new SSCJSONMapFilterListenerLoggerApplicationVersion(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match application version names specified on command line");
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForConfiguredFilterExpression(Context initialContext) {
		return new SSCJSONMapFilterListenerLoggerApplicationVersion(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match configured filter expression");
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForConfiguredNamePatterns(Context initialContext) {
		return new SSCJSONMapFilterListenerLoggerApplicationVersion(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match any configured application version name");
	}

	@Override
	protected IJSONMapFilterListener getFilterListenerForConfiguredAttributes(Context initialContext) {
		return new SSCJSONMapFilterListenerLoggerApplicationVersion(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} have values for all attributes ${filter.requiredAttributeNames.toString()}");
	}

	@Override
	protected String getSourceObjectAttributeValue(JSONMap sourceObject, String attributeName) {
		JSONMap attributeValuesByName = sourceObject.get("attributeValuesByName", JSONMap.class);
		JSONList attributeValues = attributeValuesByName==null?null:attributeValuesByName.get(attributeName, JSONList.class);
		String attributeValue = CollectionUtils.isEmpty(attributeValues)?null:(String)attributeValues.get(0);
		return attributeValue;
	}

	@Override
	protected String getSourceObjectName(JSONMap sourceObject) {
		return SpringExpressionUtil.evaluateTemplateExpression(sourceObject, "${project.name}:${name}", String.class);
	}

}
