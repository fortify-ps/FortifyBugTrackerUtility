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
package com.fortify.bugtracker.src.ssc.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.processor.ISourceProcessorSubmitVulnsToTarget;
import com.fortify.bugtracker.common.src.updater.INewIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.context.IContextSSCCommon;
import com.fortify.bugtracker.common.ssc.json.preprocessor.enrich.SSCJSONMapEnrichWithRevisionFromDetails;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterHasBugURL;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.bugtracker.common.tgt.processor.ITargetProcessorSubmitIssues;
import com.fortify.bugtracker.src.ssc.config.SSCSourceVulnerabilitiesConfiguration;
import com.fortify.client.ssc.api.SSCBugTrackerAPI;
import com.fortify.client.ssc.api.SSCCustomTagAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;

/**
 * TODO Update JavaDoc?
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link SSCSourceProcessorRetrieveVulnerabilities}, {@link SSCSourceVulnerabilitiesConfiguration} 
 * and {@link ITargetProcessorSubmitIssues} (provided by the bug tracker 
 * implementation) to allow for submitting SSC vulnerabilities to bug trackers or
 * other external systems.</p> 
 * 
 * <p>This combined configuration will retrieve all open SSC vulnerabilities based on
 * configured search/filtering criteria, optionally group the vulnerabilities based on
 * a configurable grouping expression (if supported by the bug tracker implementation),
 * and then submit the grouped vulnerabilities to the bug tracker or other external system.
 * Optionally, the bug tracker issue link can be stored in SSC for each submitted 
 * vulnerability, for state management purposes (see {@link SSCSourceProcessorUpdateVulnsOnTarget}) and
 * to allow the user to navigate back and forth between SSC and bug tracker. Additional
 * custom tags to hold information about the submitted bug tracker issue can be 
 * configured as well.</p> 
 * 
 * @author Ruud Senden
 */
@Component
public class SSCSourceProcessorSubmitVulnsToTarget extends AbstractSSCSourceVulnerabilityProcessor implements ISourceProcessorSubmitVulnsToTarget, INewIssueVulnerabilityUpdater {
	private static final Log LOG = LogFactory.getLog(SSCSourceProcessorSubmitVulnsToTarget.class);
	
	@Override
	protected SourceVulnerabilityProcessorHelper getSourceVulnerabilityProcessorHelper() {
		return new SSCSourceVulnerabilityProcessorHelperSubmit();
	}
	
	private class SSCSourceVulnerabilityProcessorHelperSubmit extends SourceVulnerabilityProcessorHelperSubmit {
		@Override
		public AbstractRestConnectionQueryBuilder<?, ?> createBaseVulnerabilityQueryBuilder(Context context) {
			SSCApplicationVersionIssuesQueryBuilder builder = createSSCVulnerabilityBaseQueryBuilder(context)
					.paramQm(QueryMode.issues)
					.includeHidden(false)
					.includeRemoved(false)
					.includeSuppressed(false)
					.paramQ(getFullSSCFilterString());
			if ( getVulnerabilityProcessor().isIgnorePreviouslySubmittedIssues() ) {
				builder.preProcessor(new SSCJSONMapFilterHasBugURL(MatchMode.EXCLUDE));
			}
			if ( getConfiguration().isEnableRevisionWorkAround() ) {
				builder.preProcessor(new SSCJSONMapEnrichWithRevisionFromDetails());
			}
			return builder;
		}
		
		/**
		 * Get the full SSC filter string for vulnerabilities that need to be submitted to the bug tracker
		 * @return
		 */
		private String getFullSSCFilterString() {
			String result = getConfiguration().getFilterStringForVulnerabilitiesToBeSubmitted();
			if ( getVulnerabilityProcessor().isIgnorePreviouslySubmittedIssues() && StringUtils.isNotBlank(getConfiguration().getBugLinkCustomTagName()) ) {
				result = StringUtils.isBlank(result) ? "" : (result+" ");
				result += getConfiguration().getBugLinkCustomTagName()+":<none>";
			}
			// SSC doesn't allow filtering on bugURL, so this is handled in createFilterForVulnerabilitiesToBeSubmitted
			return result;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateVulnerabilityStateForNewIssue(Context context, String bugTrackerName, TargetIssueLocatorAndFields targetIssueLocatorAndFields, Collection<Object> vulnerabilities) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		Map<String,String> customTagValues = getExtraCustomTagValues(context, targetIssueLocatorAndFields, vulnerabilities);
		
		if ( StringUtils.isNotBlank(getConfiguration().getBugLinkCustomTagName()) ) {
			customTagValues.put(getConfiguration().getBugLinkCustomTagName(), targetIssueLocatorAndFields.getLocator().getDeepLink());
		} 
		if ( !customTagValues.isEmpty() ) {
			conn.api(SSCCustomTagAPI.class).setCustomTagValues(applicationVersionId, customTagValues, vulnerabilities);
			LOG.info("[SSC] Updated custom tag values for SSC vulnerabilities");
		}
		if ( getConfiguration().isAddNativeBugLink() ) {
			Map<String, Object> issueDetails = new HashMap<String, Object>();
			issueDetails.put("existingBugLink", targetIssueLocatorAndFields.getLocator().getDeepLink());
			List<String> issueInstanceIds = ContextSpringExpressionUtil.evaluateExpression(context, vulnerabilities, "#root.![issueInstanceId]", List.class);
			
			SSCBugTrackerAPI bugTrackerAPI = conn.api(SSCBugTrackerAPI.class);
			if ( bugTrackerAPI.isBugTrackerAuthenticationRequired(applicationVersionId) ) {
				// If SSC bug tracker username/password are not specified, we use dummy values;
				// 'Add Existing Bugs' doesn't care about credentials but requires authentication
				// to work around SSC 17.20+ bugs
				String btUserName = StringUtils.defaultIfBlank(ctx.getSSCBugTrackerUserName(), "dummy");
				String btPassword = StringUtils.defaultIfBlank(ctx.getSSCBugTrackerPassword(), "dummy");
				bugTrackerAPI.authenticateForBugFiling(applicationVersionId, btUserName, btPassword);
			}
			
			conn.api(SSCBugTrackerAPI.class).fileBug(applicationVersionId, issueDetails, issueInstanceIds);
			LOG.info("[SSC] Added bug link for SSC vulnerabilities using '"+getConfiguration().getAddNativeBugLinkBugTrackerName()+"' bug tracker");
		}
	}

	
	
}
