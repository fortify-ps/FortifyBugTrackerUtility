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
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.fortify.api.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.api.util.spring.expression.SimpleExpression;
import com.fortify.processrunner.common.bugtracker.issue.IIssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssueCommentHelper;
import com.fortify.processrunner.common.source.vulnerability.IVulnerabilityUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithOnDemandBugLinkFromComment;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithOnDemandIssueDetails;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnState;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnBugLink;
import com.fortify.processrunner.processor.CompositeOrderedProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This class holds all FoD-related configuration properties used to submit vulnerabilities
 * to a bug tracker or other external system, and performing issue state management.
 * Based on these configuration properties, this class provides various functionalities
 * like checking the current {@link Context}, generating filters and vulnerability enrichers,
 * and updating FoD vulnerabilities with information about submitted bug tracker issues. 
 * 
 * @author Ruud Senden
 *
 */
public class FoDBugTrackerProcessorConfiguration implements IVulnerabilityUpdater {
	private String filterStringForVulnerabilitiesToBeSubmitted = null;
	private Map<SimpleExpression,Pattern> regExFiltersForVulnerabilitiesToBeSubmitted = null;
	private boolean addBugDataAsComment = false;
	private boolean addNativeBugLink = false;
	private final FoDProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new FoDProcessorEnrichWithVulnState();
	
	/**
	 * For now, this method simply returns true
	 * @param context
	 * @return
	 */
	public boolean checkContext(Context context) {
		return true;
	}
	
	/**
	 * Get the full FoD filter string for vulnerabilities that need to be submitted to the bug tracker
	 * @return
	 */
	public String getFullFoDFilterStringForVulnerabilitiesToBeSubmitted(boolean ignorePreviouslySubmittedIssues) {
		String result = getFilterStringForVulnerabilitiesToBeSubmitted();
		if ( ignorePreviouslySubmittedIssues && isAddNativeBugLink() ) {
			result = appendFilterString(result, "bugSubmitted:false");
		}
		return result;
	}
	
	/**
	 * Get the full FoD filter string for vulnerabilities that have been previously submitted
	 * @return
	 */
	public String getFullFoDFilterStringForVulnerabilitiesAlreadySubmitted() {
		String result = "";
		if ( isAddNativeBugLink() ) {
			result = appendFilterString(result, "bugSubmitted:true");
		}
		if ( isAddBugDataAsComment() ) {
			result = appendFilterString(result, "hasComments:true");
		}
		return result;
	}
	
	private String appendFilterString(String originalFilter, String filterToAppend) {
		return (StringUtils.isBlank(originalFilter) ? "" : (originalFilter+"+"))+filterToAppend;
	}
	
	/**
	 * Get filters to include only vulnerabilities that need to be submitted to the bug tracker
	 */
	public IProcessor getFiltersForVulnerabilitiesToBeSubmitted(boolean ignorePreviouslySubmittedIssues) {
		CompositeOrderedProcessor result = new CompositeOrderedProcessor();
		if ( ignorePreviouslySubmittedIssues ) {
			result.addProcessors(new FoDFilterOnBugLink(true));
		}
		if ( getRegExFiltersForVulnerabilitiesToBeSubmitted()!=null ) {
			result.addProcessors(FilterRegEx.createFromMap("CurrentVulnerability", getRegExFiltersForVulnerabilitiesToBeSubmitted(), false));
		}
		return result;
	}
	
	/**
	 * Get filters to include only vulnerabilities that have been previously submitted
	 */
	public IProcessor getFiltersForVulnerabilitiesAlreadySubmitted() {
		CompositeOrderedProcessor result = new CompositeOrderedProcessor();
		result.addProcessors(new FoDFilterOnBugLink(false));
		return result;
	}
	
	public IProcessor getEnrichersForVulnerabilitiesToBeSubmitted() {
		return getDefaultEnrichers();
	}
	
	public IProcessor getEnrichersForVulnerabilitiesAlreadySubmitted() {
		return getDefaultEnrichers();
	}
	
	public IProcessor getDefaultEnrichers() {
		CompositeProcessor result = new CompositeProcessor(
				new FoDProcessorEnrichWithOnDemandIssueDetails(),
				new FoDProcessorEnrichWithVulnDeepLink(),
				getEnrichWithVulnStateProcessor());
		if ( isAddBugDataAsComment() ) {
			result.addProcessors(new FoDProcessorEnrichWithOnDemandBugLinkFromComment());
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void updateVulnerabilityStateForNewIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		IContextFoD ctx = context.as(IContextFoD.class);
		FoDAuthenticatingRestConnection conn = FoDConnectionFactory.getConnection(context);
		String releaseId = ctx.getFoDReleaseId();
		Collection<String> vulnIds = SpringExpressionUtil.evaluateExpression(vulnerabilities, "#root.![vulnId]", Collection.class);
		if ( isAddBugDataAsComment() ) {
			String comment = SubmittedIssueCommentHelper.getCommentForSubmittedIssue(bugTrackerName, submittedIssue);
			conn.api().vulnerability().addCommentToVulnerabilities(releaseId, comment, vulnIds);
		} else if ( isAddNativeBugLink() ) {
			conn.api().bugTracker().addBugLinkToVulnerabilities(releaseId, submittedIssue.getDeepLink(), vulnIds);
		}
	}

	public void updateVulnerabilityStateForExistingIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		
	}
	
	
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.enrichWithVulnStateProcessor.setIsVulnerabilityOpenExpression(isVulnerabilityOpenExpression);
	}
	
	public String getFilterStringForVulnerabilitiesToBeSubmitted() {
		return filterStringForVulnerabilitiesToBeSubmitted;
	}
	public void setFilterStringForVulnerabilitiesToBeSubmitted(String filterStringForVulnerabilitiesToBeSubmitted) {
		this.filterStringForVulnerabilitiesToBeSubmitted = filterStringForVulnerabilitiesToBeSubmitted;
	}
	public Map<SimpleExpression, Pattern> getRegExFiltersForVulnerabilitiesToBeSubmitted() {
		return regExFiltersForVulnerabilitiesToBeSubmitted;
	}
	public void setRegExFiltersForVulnerabilitiesToBeSubmitted(
			Map<SimpleExpression, Pattern> regExFiltersForVulnerabilitiesToBeSubmitted) {
		this.regExFiltersForVulnerabilitiesToBeSubmitted = regExFiltersForVulnerabilitiesToBeSubmitted;
	}
	public boolean isAddBugDataAsComment() {
		return addBugDataAsComment;
	}
	public void setAddBugDataAsComment(boolean addBugDataAsComment) {
		this.addBugDataAsComment = addBugDataAsComment;
	}
	public boolean isAddNativeBugLink() {
		return addNativeBugLink;
	}
	public void setAddNativeBugLink(boolean addNativeBugLink) {
		this.addNativeBugLink = addNativeBugLink;
	}
	public FoDProcessorEnrichWithVulnState getEnrichWithVulnStateProcessor() {
		return enrichWithVulnStateProcessor;
	}
}
