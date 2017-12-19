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
package com.fortify.processrunner.fod.processor.composite;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.api.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.api.fod.connection.api.query.builder.FoDReleaseVulnerabilityQueryBuilder;
import com.fortify.api.util.rest.json.preprocessor.JSONMapFilterRegEx;
import com.fortify.api.util.rest.query.IRestConnectionQuery;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.common.bugtracker.issue.IIssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssueCommentHelper;
import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.common.processor.IProcessorSubmitVulnerabilities;
import com.fortify.processrunner.common.source.vulnerability.INewIssueVulnerabilityUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.fod.json.preprocessor.FoDJSONMapFilterOnBugLink;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.IProcessor;

/**
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link FoDProcessorRetrieveVulnerabilities}, {@link FoDBugTrackerProcessorConfiguration} 
 * and {@link IProcessorSubmitIssueForVulnerabilities} (provided by the bug tracker 
 * implementation) to allow for submitting FoD vulnerabilities to bug trackers or
 * other external systems.</p> 
 * 
 * <p>This combined configuration will retrieve all open FoD vulnerabilities based on
 * configured search/filtering criteria, optionally group the vulnerabilities based on
 * a configurable grouping expression (if supported by the bug tracker implementation),
 * and then submit the grouped vulnerabilities to the bug tracker or other external system.
 * Optionally, the bug tracker issue link can be stored in FoD for each submitted 
 * vulnerability, for state management purposes (see {@link FoDProcessorUpdateState}) and
 * to allow the user to navigate back and forth between FoD and bug tracker.</p> 
 * 
 * @author Ruud Senden
 */
@Component
public class FoDProcessorSubmitVulnerabilities extends AbstractFoDVulnerabilityProcessor implements IProcessorSubmitVulnerabilities, INewIssueVulnerabilityUpdater {
	private IProcessorSubmitIssueForVulnerabilities vulnerabilityProcessor;
	
	@Override
	public IRestConnectionQuery getVulnerabilityQuery(Context context) {
		// TODO Properly take isVulnerabilityOpenExpression into account, instead of just depending on paramIncludeFixed and paramIncludeSuppressed 
		FoDReleaseVulnerabilityQueryBuilder builder = createVulnerabilityBaseQueryBuilder(context)
				.paramIncludeFixed(false)
				.paramIncludeSuppressed(false)
				.paramFilterAnd(getConfiguration().getFilterStringForVulnerabilitiesToBeSubmitted());
		if ( getVulnerabilityProcessor().isIgnorePreviouslySubmittedIssues() ) {
			builder.preProcessor(new FoDJSONMapFilterOnBugLink(true));
			if ( getConfiguration().isAddNativeBugLink() ) {
				builder.paramFilterAnd("bugSubmitted", "false");
			}
		}
		if ( getConfiguration().getRegExFiltersForVulnerabilitiesToBeSubmitted()!=null ) {
			builder.preProcessor(new JSONMapFilterRegEx(getConfiguration().getRegExFiltersForVulnerabilitiesToBeSubmitted(), false));
		}
		return builder.build();
	}
	
	@Override
	protected String getPurpose() {
		return "submitting new vulnerabilities";
	}

	@Override
	public IProcessorSubmitIssueForVulnerabilities getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	@Autowired
	public void setVulnerabilityProcessor(IProcessorSubmitIssueForVulnerabilities vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}

	@SuppressWarnings("unchecked")
	public void updateVulnerabilityStateForNewIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		IContextFoD ctx = context.as(IContextFoD.class);
		FoDAuthenticatingRestConnection conn = FoDConnectionFactory.getConnection(context);
		String releaseId = ctx.getFoDReleaseId();
		Collection<String> vulnIds = SpringExpressionUtil.evaluateExpression(vulnerabilities, "#root.![vulnId]", Collection.class);
		if ( getConfiguration().isAddBugDataAsComment() ) {
			String comment = SubmittedIssueCommentHelper.getCommentForSubmittedIssue(bugTrackerName, submittedIssue);
			conn.api().vulnerability().addCommentToVulnerabilities(releaseId, comment, vulnIds);
		} else if ( getConfiguration().isAddNativeBugLink() ) {
			conn.api().bugTracker().addBugLinkToVulnerabilities(releaseId, submittedIssue.getDeepLink(), vulnIds);
		}
	}
}
