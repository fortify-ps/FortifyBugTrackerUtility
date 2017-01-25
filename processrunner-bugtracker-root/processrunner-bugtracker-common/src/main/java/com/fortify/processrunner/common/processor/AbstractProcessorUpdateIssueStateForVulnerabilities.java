package com.fortify.processrunner.common.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This abstract class allows for updating an existing issue based on vulnerabilities grouped by
 * issue id or issue link. TODO 
 * 
 * builds a string map using {@link AbstractProcessorBuildObjectMapFromGroupedObjects},
 * submits the issue to the bug tracker, and logs a status message. Subclasses need
 * to implement the {@link #getBugTrackerName()} method to return the bug tracker name, and the 
 * {@link #submitIssue(Context, LinkedHashMap)} method to actually submit the issue.
 */
public abstract class AbstractProcessorUpdateIssueStateForVulnerabilities extends AbstractProcessorBuildObjectMapFromGroupedObjects {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorUpdateIssueStateForVulnerabilities.class);
	private SimpleExpression isVulnStateOpenExpression;
	private SimpleExpression bugIdExpression;
	private SimpleExpression bugLinkExpression;
	
	public AbstractProcessorUpdateIssueStateForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	public final void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextProperties.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextProperties(contextProperties, context);
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		SubmittedIssue submittedIssue = getSubmittedIssue(currentGroup.get(0));
		String fieldNames = map.keySet().toString();
		if ( updateIssueFields(context, submittedIssue, map) ) {
			LOG.info(String.format("[%s] Updated field(s) %s for issue %s", getBugTrackerName(), fieldNames, submittedIssue.getDeepLink()));
		}
		if ( hasOpenVulnerabilities(currentGroup) ) {
			if ( openIssueIfClosed(context, submittedIssue) ) {
				LOG.info(String.format("[%s] Re-opened issue %s", getBugTrackerName(), submittedIssue.getDeepLink()));
			}
		} else {
			if ( closeIssueIfOpen(context, submittedIssue) ) {
				LOG.info(String.format("[%s] Closed issue %s", getBugTrackerName(), submittedIssue.getDeepLink()));
			}
		}
		
		return true;
	}
	
	protected SubmittedIssue getSubmittedIssue(Object vulnerability) {
		SubmittedIssue result = new SubmittedIssue();
		result.setId(SpringExpressionUtil.evaluateExpression(vulnerability, bugIdExpression, String.class));
		result.setDeepLink(SpringExpressionUtil.evaluateExpression(vulnerability, bugLinkExpression, String.class));
		return result;
	}
	
	protected void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {}
	
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		return false;
	}
	
	protected boolean openIssueIfClosed(Context context, SubmittedIssue submittedIssue) {
		return false;
	}
	
	protected boolean closeIssueIfOpen(Context context, SubmittedIssue submittedIssue) {
		return false;
	}
	
	protected abstract String getBugTrackerName();
	
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
	public SimpleExpression getBugIdExpression() {
		return bugIdExpression;
	}

	/**
	 * @param bugIdExpression the bugIdExpression to set
	 */
	public void setBugIdExpression(SimpleExpression bugIdExpression) {
		this.bugIdExpression = bugIdExpression;
	}

	/**
	 * @return the bugLinkExpression
	 */
	public SimpleExpression getBugLinkExpression() {
		return bugLinkExpression;
	}

	/**
	 * @param bugLinkExpression the bugLinkExpression to set
	 */
	public void setBugLinkExpression(SimpleExpression bugLinkExpression) {
		this.bugLinkExpression = bugLinkExpression;
	}
	
	
}
