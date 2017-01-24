package com.fortify.processrunner.common.processor;

import java.util.Collection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fortify.processrunner.common.IssueState;
import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions.IContextGrouping;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public abstract class AbstractProcessorUpdateBugStateFromGroupedObjects extends AbstractProcessor {
	private SimpleExpression isVulnerabilityOpenExpression; 
	
	@Override
	protected boolean process(Context context) {
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		Collection<Object> currentGroup = contextGrouping.getCurrentGroup();
		SubmittedIssue submittedIssue = getSubmittedIssue(context, currentGroup);
		MultiValueMap<IssueState, Object> vulnsByState = getVulnsByIssueState(context, currentGroup);
		updateBugStateIfNecessary(context, submittedIssue, vulnsByState);
		return true;
	}

	protected SubmittedIssue getSubmittedIssue(Context context, Collection<Object> currentGroup) {
		SubmittedIssue result = new SubmittedIssue();
		result.setId(SpringExpressionUtil.evaluateExpression(currentGroup, "[0].?bugId", String.class));
		result.setDeepLink(SpringExpressionUtil.evaluateExpression(currentGroup, "[0].?bugLink", String.class));
		return result;
	}
	
	protected MultiValueMap<IssueState, Object> getVulnsByIssueState(Context context, Collection<Object> currentGroup) {
		MultiValueMap<IssueState, Object> result = new LinkedMultiValueMap<IssueState, Object>(2);
		for ( Object o : currentGroup ) {
			if ( SpringExpressionUtil.evaluateExpression(o, isVulnerabilityOpenExpression, Boolean.class) ) {
				result.add(IssueState.OPEN, o);
			} else {
				result.add(IssueState.CLOSED, o);
			}
		}
		return result;
	}
	
	protected abstract void updateBugStateIfNecessary(Context context, SubmittedIssue submittedIssue, MultiValueMap<IssueState, Object> vulnsByState);
	
	
}
