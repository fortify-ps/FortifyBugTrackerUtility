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
package com.fortify.bugtracker.tgt.ssc.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.updater.IExistingIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterWithLoggerApplicationVersionHasBugTrackerShortDisplayName;
import com.fortify.bugtracker.common.ssc.query.ISSCApplicationVersionQueryBuilderUpdater;
import com.fortify.bugtracker.common.tgt.context.IContextBugTracker;
import com.fortify.bugtracker.common.tgt.processor.ITargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.ssc.config.SSCTargetConfiguration;
import com.fortify.bugtracker.tgt.ssc.context.IContextSSCTarget;
import com.fortify.client.ssc.api.SSCBugTrackerAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionsQueryBuilder;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class submits a set of vulnerabilities through a native SSC bug tracker integration.
 * The fields to be submitted are configured through our {@link AbstractProcessorBuildObjectMapFromGroupedObjects}
 * superclass.
 * 
 * @author Ruud Senden
 *
 */
@Component
public class SSCTargetProcessorSubmitIssues extends AbstractProcessorBuildObjectMapFromGroupedObjects implements ITargetProcessorSubmitIssues, ISSCApplicationVersionQueryBuilderUpdater {
	private static final Log LOG = LogFactory.getLog(SSCTargetProcessorSubmitIssues.class);
	private String sscBugTrackerName;
	
	public SSCTargetProcessorSubmitIssues() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	public String getTargetName() {
		return getSscBugTrackerName()+" through SSC";
	}
	
	@Override
	public void updateQueryBuilder(Context context, SSCApplicationVersionsQueryBuilder builder) {
		builder.preProcessor(new SSCJSONMapFilterWithLoggerApplicationVersionHasBugTrackerShortDisplayName(MatchMode.INCLUDE, getSscBugTrackerName()));
	}

	public boolean setVulnerabilityUpdater(IExistingIssueVulnerabilityUpdater issueSubmittedListener) {
		// We ignore the issueSubmittedListener since we don't need to update SSC state
		// after submitting a bug through SSC. We return false to indicate that we don't
		// support an issue submitted listener.
		return false;
	}
	
	
	@Override
	protected void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCBugTracker.PRP_USER_NAME, getSscBugTrackerName()+" user name (required if SSC bug tracker requires authentication)", false).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCBugTracker.PRP_PASSWORD, getSscBugTrackerName()+" password", true).readFromConsole(true).isPassword(true).dependsOnProperties(IContextSSCBugTracker.PRP_USER_NAME));
		context.as(IContextBugTracker.class).setBugTrackerName(getTargetName());
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	/**
	 * Autowire the configuration from the Spring configuration file.
	 * @param config
	 */
	@Autowired
	public void setConfiguration(SSCTargetConfiguration config) {
		super.setGroupTemplateExpression(config.getGroupTemplateExpressionForSubmit());
		super.setFields(config.getFieldsForSubmit());
		super.setAppendedFields(config.getAppendedFieldsForSubmit());
		setSscBugTrackerName(config.getSscBugTrackerName());
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		SSCBugTrackerAPI bugTrackerAPI = conn.api(SSCBugTrackerAPI.class);
		if ( bugTrackerAPI.isBugTrackerAuthenticationRequired(applicationVersionId) ) {
			String btUserName = ctx.getSSCBugTrackerUserName();
			String btPassword = ctx.getSSCBugTrackerPassword();
			if ( StringUtils.isBlank(btUserName) || StringUtils.isBlank(btPassword) ) {
				throw new IllegalArgumentException("SSC bug tracker requires authentication, but no username or password provided");
			}
			bugTrackerAPI.authenticateForBugFiling(applicationVersionId, ctx.getSSCBugTrackerUserName(), ctx.getSSCBugTrackerPassword());
		}
		List<String> issueInstanceIds = new ArrayList<String>();
		for ( Object issue : currentGroup ) {
			issueInstanceIds.add(ContextSpringExpressionUtil.evaluateExpression(context, issue, "issueInstanceId", String.class));
		}
		JSONMap result = bugTrackerAPI.fileBug(ctx.getSSCApplicationVersionId(), map, issueInstanceIds);
		String bugLink = ContextSpringExpressionUtil.evaluateExpression(context, result, "data?.values?.externalBugDeepLink", String.class);
		LOG.info(String.format("[SSC] Submitted %d vulnerabilities via SSC to %s", currentGroup.size(), bugLink));
		return true;
	}
	
	public String getSscBugTrackerName() {
		return sscBugTrackerName;
	}

	public void setSscBugTrackerName(String sscBugTrackerName) {
		this.sscBugTrackerName = sscBugTrackerName;
	}
	
	public boolean isIgnorePreviouslySubmittedIssues() {
		return true;
	}
	
	private interface IContextSSCBugTracker {
		public static final String PRP_USER_NAME = "SSCBugTrackerUserName";
		public static final String PRP_PASSWORD = "SSCBugTrackerPassword";
		
		public void setSSCBugTrackerUserName(String userName);
		public String getSSCBugTrackerUserName();
		public void setSSCBugTrackerPassword(String password);
		public String getSSCBugTrackerPassword();
		
	}
}
