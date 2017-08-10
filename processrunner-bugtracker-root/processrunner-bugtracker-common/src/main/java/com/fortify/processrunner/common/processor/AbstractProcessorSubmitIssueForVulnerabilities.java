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

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This abstract {@link IProcessor} implementation allows for submitting issues to external systems
 * based on vulnerability data. Based on our superclass {@link AbstractProcessorBuildObjectMapFromGroupedObjects},
 * we build a map containing issue information to be submitted from (optionally grouped) vulnerability
 * data. Actual implementations will need to implement the {@link #submitIssue(Context, LinkedHashMap)} method
 * to actually submit the issue to the external system.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessorSubmitIssueForVulnerabilities extends AbstractBugTrackerFieldsBasedProcessor implements IProcessorSubmitIssueForVulnerabilities {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorSubmitIssueForVulnerabilities.class);
	private IIssueSubmittedListener issueSubmittedListener;
	
	public AbstractProcessorSubmitIssueForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	public final void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextPropertyDefinitions.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		SubmittedIssue submittedIssue = submitIssue(context, map); 
		if ( submittedIssue != null ) {
			issueSubmittedListener.issueSubmitted(context, getBugTrackerName(), submittedIssue, currentGroup);
			LOG.info(String.format("[%s] Submitted %d vulnerabilities to %s", getBugTrackerName(), currentGroup.size(), submittedIssue.getDeepLink()));
		}
		return true;
	}
	
	protected abstract SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData);

	public IIssueSubmittedListener getIssueSubmittedListener() {
		return issueSubmittedListener;
	}

	/* (non-Javadoc)
	 * @see com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities#setIssueSubmittedListener(com.fortify.processrunner.common.issue.IIssueSubmittedListener)
	 */
	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener) {
		this.issueSubmittedListener = issueSubmittedListener;
		return true;
	}
	
	public boolean isIgnorePreviouslySubmittedIssues() {
		return true;
	}
	
	/** 
	 * Indicate that we want all bug tracker fields to be configured on
	 * this instance for the initial bug submission.
	 */
	@Override
	protected boolean includeOnlyFieldsToBeUpdated() {
		return false;
	}
}
