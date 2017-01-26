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

public abstract class AbstractProcessorUpdateIssueStateForVulnerabilities<IssueStateType> extends AbstractProcessorBuildObjectMapFromGroupedObjects {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorUpdateIssueStateForVulnerabilities.class);
	private SimpleExpression isVulnStateOpenExpression;
	private SimpleExpression vulnBugIdExpression;
	private SimpleExpression vulnBugLinkExpression;
	private SimpleExpression isIssueOpenableExpression;
	private SimpleExpression isIssueCloseableExpression;
	
	
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
		result.setId(SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugIdExpression, String.class));
		result.setDeepLink(SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugLinkExpression, String.class));
		return result;
	}
	
	protected void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {}
	
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		return false;
	}
	
	protected boolean openIssueIfClosed(Context context, SubmittedIssue submittedIssue) {
		IssueStateType currentIssueState = getCurrentIssueStateIfExpressionMatchesIssueState(context, submittedIssue, getIsIssueOpenableExpression()); 
		if ( currentIssueState!=null ) {
			return openIssue(context, submittedIssue, currentIssueState);
		}
		return false;
	}
	
	protected boolean closeIssueIfOpen(Context context, SubmittedIssue submittedIssue) {
		IssueStateType currentIssueState = getCurrentIssueStateIfExpressionMatchesIssueState(context, submittedIssue, getIsIssueCloseableExpression()); 
		if ( currentIssueState!=null ) {
			return closeIssue(context, submittedIssue, currentIssueState);
		}
		return false;
	}
	
	protected boolean openIssue(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState) {
		return false;
	}
	
	protected boolean closeIssue(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState) {
		return false;
	}
	
	protected IssueStateType getCurrentIssueState(Context context, SubmittedIssue submittedIssue) {
		return null;
	}
	
	protected abstract String getBugTrackerName();
	
	private IssueStateType getCurrentIssueStateIfExpressionMatchesIssueState(Context context, SubmittedIssue submittedIssue, SimpleExpression expression) {
		if ( expression != null ) {
			IssueStateType currentIssueState = getCurrentIssueState(context, submittedIssue);
			if ( currentIssueState != null && SpringExpressionUtil.evaluateExpression(currentIssueState, expression, Boolean.class) ) {
				return currentIssueState;
			}
		}
		return null;
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
	
	
}
