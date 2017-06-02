package com.fortify.processrunner.common.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.util.spring.SpringExpressionUtil;

public abstract class AbstractProcessorSubmitIssueForVulnerabilities extends AbstractProcessorBuildObjectMapFromGroupedObjects implements IProcessorSubmitIssueForVulnerabilities {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorSubmitIssueForVulnerabilities.class);
	private IIssueSubmittedListener issueSubmittedListener;
	
	public AbstractProcessorSubmitIssueForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	public final void addExtraContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextProperties.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextProperties(contextProperties, context);
	}
	
	protected void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {}
	
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
	/* (non-Javadoc)
	 * @see com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities#getBugTrackerName()
	 */
	public abstract String getBugTrackerName();

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
}
