package com.fortify.processrunner.ssc.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCTarget;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for writing data from the 
 * current {@link Context} instance to a file based on a list of configured 
 * {@link TemplateExpression} instances.
 * 
 * TODO Improve class structure such that we don''t have to implement a method that is not being called,
 *      and the {@link #setIssueSubmittedListener(com.fortify.processrunner.common.issue.IIssueSubmittedListener)}
 *      is not being called (as the issueSubmittedListener will not be called by this implementation)
 */
public class ProcessorSSCSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		SSCConnectionFactory.addContextProperties(contextProperties, context);
		contextProperties.add(new ContextProperty(IContextSSCTarget.PRP_SSC_BUG_TRACKER_USER_NAME, "User name for SSC bug tracker", context, null, false));
		contextProperties.add(new ContextProperty(IContextSSCTarget.PRP_SSC_BUG_TRACKER_PASSWORD, "Password for SSC bug tracker", context, null, false));
	}
	
	@Override
	public String getBugTrackerName() {
		return "SSC";
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		List<String> issueInstanceIds = new ArrayList<String>();
		for ( Object issue : currentGroup ) {
			issueInstanceIds.add(SpringExpressionUtil.evaluateExpression(issue, "issueInstanceId", String.class));
		}
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		conn.authenticateForBugFiling(ctx.getSSCApplicationVersionId(), ctx.getSSCBugTrackerUserName(), ctx.getSSCBugTrackerPassword());
		conn.fileBug(ctx.getSSCApplicationVersionId(), map, issueInstanceIds);
		return true;
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> fields) {
		// We override processMap, so this method won't be called
		return null;
	}
}
