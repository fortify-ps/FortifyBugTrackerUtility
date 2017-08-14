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
package com.fortify.processrunner.common.processor;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.bugtracker.issue.IssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.source.vulnerability.IVulnerabilityUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * <p>This abstract {@link IProcessor} implementation can update issue state for previously submitted
 * issues, based on possibly updated vulnerability data. Based on our superclass 
 * {@link AbstractProcessorBuildObjectMapFromGroupedObjects}, we first group all previously
 * submitted vulnerabilities by the corresponding external system issue link, and then update one or 
 * more fields for the external issue using the grouped vulnerability data.</p>
 * 
 * <p>The fields to be updated can be configured using the {@link #setFieldsToUpdate(String[])} method;
 * the corresponding field content expressions will be taken from the configured 
 * {@link AbstractProcessorSubmitIssueForVulnerabilities} instance.</p>
 * 
 * <p>Subclasses need to implement the {@link #updateIssueFields(Context, SubmittedIssue, LinkedHashMap)}
 * method to actually update previously submitted issues.</p>
 * 
 * @author Ruud Senden
 *
 * @param <IssueStateDetailsType>
 */
public abstract class AbstractProcessorUpdateIssueStateForVulnerabilities<IssueStateDetailsType> extends AbstractBugTrackerFieldsBasedProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorUpdateIssueStateForVulnerabilities.class);
	private IVulnerabilityUpdater vulnerabilityUpdater;
	private SimpleExpression isVulnStateOpenExpression;
	private SimpleExpression vulnBugIdExpression;
	private SimpleExpression vulnBugLinkExpression;
	private SimpleExpression isIssueOpenableExpression;
	private SimpleExpression isIssueCloseableExpression;
	
	public AbstractProcessorUpdateIssueStateForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	public final void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextPropertyDefinitions.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> vulnerabilities, LinkedHashMap<String, Object> map) {
		SubmittedIssue submittedIssue = getSubmittedIssue(vulnerabilities.get(0));
		String fieldNames = map.keySet().toString();
		if ( updateIssueFields(context, submittedIssue, map) ) {
			LOG.info(String.format("[%s] Updated field(s) %s for issue %s", getBugTrackerName(), fieldNames, submittedIssue.getDeepLink()));
		}
		if ( hasOpenVulnerabilities(vulnerabilities) ) {
			if ( openIssueIfClosed(context, submittedIssue) ) {
				LOG.info(String.format("[%s] Re-opened issue %s", getBugTrackerName(), submittedIssue.getDeepLink()));
			}
		} else {
			if ( closeIssueIfOpen(context, submittedIssue) ) {
				LOG.info(String.format("[%s] Closed issue %s", getBugTrackerName(), submittedIssue.getDeepLink()));
			}
		}
		if ( vulnerabilityUpdater!=null ) {
			vulnerabilityUpdater.updateVulnerabilityStateForExistingIssue(context, getBugTrackerName(), submittedIssue, getIssueStateDetailsRetriever(), vulnerabilities);
		}
		
		return true;
	}
	
	protected SubmittedIssue getSubmittedIssue(Object vulnerability) {
		SubmittedIssue result = new SubmittedIssue();
		result.setId(SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugIdExpression, String.class));
		result.setDeepLink(SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugLinkExpression, String.class));
		return result;
	}
	
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}
	
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		return false;
	}
	
	protected boolean openIssueIfClosed(Context context, SubmittedIssue submittedIssue) {
		if ( canDetemineIssueIsClosed(context, submittedIssue) ) {
			IssueStateDetailsType currentIssueState = getCurrentIssueStateDetails(context, submittedIssue);
			if ( isIssueOpenable(context, submittedIssue, currentIssueState) ) {
				return openIssue(context, submittedIssue, currentIssueState);
			}
		}
		return false;
	}

	protected boolean closeIssueIfOpen(Context context, SubmittedIssue submittedIssue) {
		if ( canDetemineIssueIsOpen(context, submittedIssue) ) {
			IssueStateDetailsType currentIssueState = getCurrentIssueStateDetails(context, submittedIssue);
			if ( isIssueCloseable(context, submittedIssue, currentIssueState) ) {
				return closeIssue(context, submittedIssue, currentIssueState);
			}
		}
		return false;
	}

	protected boolean openIssue(Context context, SubmittedIssue submittedIssue, IssueStateDetailsType currentIssueState) {
		return false;
	}
	
	protected boolean closeIssue(Context context, SubmittedIssue submittedIssue, IssueStateDetailsType currentIssueState) {
		return false;
	}
	
	protected final IssueStateDetailsType getCurrentIssueStateDetails(Context context, SubmittedIssue submittedIssue) {
		return getIssueStateDetailsRetriever()==null ? null : getIssueStateDetailsRetriever().getIssueStateDetails(context, submittedIssue);
	}
	
	protected IssueStateDetailsRetriever<IssueStateDetailsType> getIssueStateDetailsRetriever() {
		return null;
	}
	
	public abstract String getBugTrackerName();
	
	protected boolean canDetemineIssueIsClosed(Context context, SubmittedIssue submittedIssue) {
		return getIsIssueCloseableExpression()!=null;
	}
	
	protected boolean canDetemineIssueIsOpen(Context context, SubmittedIssue submittedIssue) {
		return getIsIssueOpenableExpression()!=null;
	}
	
	protected boolean isIssueCloseable(Context context, SubmittedIssue submittedIssue, IssueStateDetailsType currentIssueState) {
		return doesIssueStateMatchExpression(context, submittedIssue, currentIssueState, getIsIssueCloseableExpression());
	}

	protected boolean isIssueOpenable(Context context, SubmittedIssue submittedIssue, IssueStateDetailsType currentIssueState) {
		return doesIssueStateMatchExpression(context, submittedIssue, currentIssueState, getIsIssueOpenableExpression());
	}
	
	protected boolean doesIssueStateMatchExpression(Context context, SubmittedIssue submittedIssue, IssueStateDetailsType currentIssueState, SimpleExpression expression) {
		if ( expression!=null && currentIssueState!=null ) {
			return ContextSpringExpressionUtil.evaluateExpression(context, currentIssueState, expression, Boolean.class);
		}
		return false;
	}
	
	private boolean hasOpenVulnerabilities(List<Object> currentGroup) {
		for ( Object o : currentGroup ) {
			if ( SpringExpressionUtil.evaluateExpression(o, getIsVulnStateOpenExpression(), Boolean.class) ) {
				return true;
			}
		}
		return false;
	}

	public SimpleExpression getIsVulnStateOpenExpression() {
		return isVulnStateOpenExpression;
	}

	public void setIsVulnStateOpenExpression(SimpleExpression isVulnStateOpenExpression) {
		this.isVulnStateOpenExpression = isVulnStateOpenExpression;
	}

	/**
	 * @return the bugIdExpression
	 */
	public SimpleExpression getVulnBugIdExpression() {
		return vulnBugIdExpression;
	}

	/**
	 * @param bugIdExpression the bugIdExpression to set
	 */
	public void setVulnBugIdExpression(SimpleExpression vulnBugIdExpression) {
		this.vulnBugIdExpression = vulnBugIdExpression;
	}

	/**
	 * @return the bugLinkExpression
	 */
	public SimpleExpression getVulnBugLinkExpression() {
		return vulnBugLinkExpression;
	}

	/**
	 * @param bugLinkExpression the bugLinkExpression to set
	 */
	public void setVulnBugLinkExpression(SimpleExpression vulnBugLinkExpression) {
		this.vulnBugLinkExpression = vulnBugLinkExpression;
	}

	/**
	 * @return the isIssueOpenableExpression
	 */
	public SimpleExpression getIsIssueOpenableExpression() {
		return isIssueOpenableExpression;
	}

	/**
	 * @param isIssueOpenableExpression the isIssueOpenableExpression to set
	 */
	public void setIsIssueOpenableExpression(SimpleExpression isIssueOpenableExpression) {
		this.isIssueOpenableExpression = isIssueOpenableExpression;
	}

	/**
	 * @return the isIssueCloseableExpression
	 */
	public SimpleExpression getIsIssueCloseableExpression() {
		return isIssueCloseableExpression;
	}

	/**
	 * @param isIssueCloseableExpression the isIssueCloseableExpression to set
	 */
	public void setIsIssueCloseableExpression(SimpleExpression isIssueCloseableExpression) {
		this.isIssueCloseableExpression = isIssueCloseableExpression;
	}
	
	@Override
	public boolean isForceGrouping() {
		return true;
	}
	
	/** 
	 * Indicate that we want only bug tracker fields that need to be updated
	 * during state management to be configured on this instance.
	 */
	@Override
	protected boolean includeOnlyFieldsToBeUpdated() {
		return true;
	}
	
	@Autowired(required=false)
	public void setVulnerabilityUpdater(IVulnerabilityUpdater vulnerabilityUpdater) {
		this.vulnerabilityUpdater = vulnerabilityUpdater;
	}
}
