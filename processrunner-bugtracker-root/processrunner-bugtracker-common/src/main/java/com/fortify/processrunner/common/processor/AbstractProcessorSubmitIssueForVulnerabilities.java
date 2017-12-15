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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.common.bugtracker.issue.IIssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.source.vulnerability.IVulnerabilityUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;

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
public abstract class AbstractProcessorSubmitIssueForVulnerabilities<IssueStateDetailsType> extends AbstractBugTrackerFieldsBasedProcessor implements IProcessorSubmitIssueForVulnerabilities {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorSubmitIssueForVulnerabilities.class);
	private IVulnerabilityUpdater vulnerabilityUpdater;
	
	/**
	 * This constructor sets the root expression on our parent to 'CurrentVulnerability'
	 */
	public AbstractProcessorSubmitIssueForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	/**
	 * Add the bug tracker name to the current context, and call 
	 * {@link #addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions, Context)}
	 * to allow subclasses to add additional context property definitions
	 */
	@Override
	public final void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextPropertyDefinitions.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	/**
	 * Subclasses can override this method to add additional bug tracker related {@link ContextPropertyDefinitions}
	 * @param contextPropertyDefinitions
	 * @param context
	 */
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}
	
	/**
	 * This method calls {@link #submitIssue(Context, LinkedHashMap)} to actually submit an issue to the 
	 * bug tracker for the current group of vulnerabilities. If an {@link IVulnerabilityUpdater} instance
	 * has been configured, it will be called to update vulnerability state in the source system, for example
	 * to store the bug id or deep link.
	 */
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		SubmittedIssue submittedIssue = submitIssue(context, map); 
		if ( submittedIssue != null ) {
			LOG.info(String.format("[%s] Submitted %d vulnerabilities to %s", getBugTrackerName(), currentGroup.size(), submittedIssue.getDeepLink()));
			if ( vulnerabilityUpdater != null ) {
				vulnerabilityUpdater.updateVulnerabilityStateForNewIssue(context, getBugTrackerName(), submittedIssue, getIssueStateDetailsRetriever(), currentGroup);
			}
		}
		return true;
	}
	
	/**
	 * Subclasses must implement this method to actually submit a {@link Map} with issue data
	 * to the bug tracker.
	 * @param context
	 * @param issueData
	 * @return
	 */
	protected abstract SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData);
	
	/**
	 * Subclasses may override this method to return an {@link IIssueStateDetailsRetriever} instance that
	 * can be used to retrieve details about the current state of the issue in the bug tracker.
	 * @return
	 */
	protected IIssueStateDetailsRetriever<IssueStateDetailsType> getIssueStateDetailsRetriever() { 
		return null;
	}

	/**
	 * Set the {@link IVulnerabilityUpdater} instance used to update vulnerabilities in the source system
	 * based on submitted issue data. This is optional and usually auto-wired by Spring.
	 * @param vulnerabilityUpdater
	 */
	@Autowired(required=false)
	public void setVulnerabilityUpdater(IVulnerabilityUpdater vulnerabilityUpdater) {
		this.vulnerabilityUpdater = vulnerabilityUpdater;
	}
	
	/**
	 * Subclasses can override this method to re-submit any previously submitted issues, for example
	 * when exporting all vulnerabilities to a file. The default implementation returns true, indicating
	 * that previously submitted issues should be ignored.
	 */
	public boolean isIgnorePreviouslySubmittedIssues() {
		return true;
	}
	
	/** 
	 * Indicate that we want all bug tracker fields to be configured on
	 * this instance for the initial bug submission.
	 */
	@Override
	protected final boolean includeOnlyFieldsToBeUpdated() {
		return false;
	}
}
