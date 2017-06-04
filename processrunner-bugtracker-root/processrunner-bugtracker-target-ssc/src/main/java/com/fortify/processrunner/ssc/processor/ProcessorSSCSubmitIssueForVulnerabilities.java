package com.fortify.processrunner.ssc.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCTarget;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;

public class ProcessorSSCSubmitIssueForVulnerabilities extends AbstractProcessor implements IProcessorSubmitIssueForVulnerabilities {
	private Map<String, SSCIssueSubmitter> bugTrackers = new HashMap<String, SSCIssueSubmitter>();
	
	@Override
	public void addContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCTarget.PRP_SSC_BUG_TRACKER_USER_NAME, "User name for SSC bug tracker", context, null, false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCTarget.PRP_SSC_BUG_TRACKER_PASSWORD, "Password for SSC bug tracker", context, null, false));
		for ( SSCIssueSubmitter issueSubmitter : bugTrackers.values() ) {
			issueSubmitter.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		}
	}
	
	// @Override
	public String getBugTrackerName() {
		return "SSC";
	}
	
	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener) {
		// We ignore the issueSubmittedListener since we don't need to update SSC state
		// after submitting a bug through SSC. We return false to indicate that we don't
		// support an issue submitted listener.
		return false;
	}
	
	@Override
	protected boolean preProcess(Context context) {
		return callIssueSubmitter(Phase.PRE_PROCESS, context);
	}
	
	@Override
	protected boolean process(Context context) {
		return callIssueSubmitter(Phase.PROCESS, context);
	}
	
	@Override
	protected boolean postProcess(Context context) {
		return callIssueSubmitter(Phase.POST_PROCESS, context);
	}

	public Map<String, SSCIssueSubmitter> getBugTrackers() {
		return bugTrackers;
	}

	public void setBugTrackers(Map<String, SSCIssueSubmitter> bugTrackers) {
		this.bugTrackers = bugTrackers;
	}
	
	private final boolean callIssueSubmitter(Phase phase, Context context) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String bugTrackerName = conn.getBugTrackerShortName(ctx.getSSCApplicationVersionId());
		SSCIssueSubmitter submitter = getBugTrackers().get(bugTrackerName);
		if ( submitter == null ) {
			throw new IllegalStateException("No configuration found for bug tracker "+bugTrackerName);
		}
		return submitter.process(phase, context);
	}
}