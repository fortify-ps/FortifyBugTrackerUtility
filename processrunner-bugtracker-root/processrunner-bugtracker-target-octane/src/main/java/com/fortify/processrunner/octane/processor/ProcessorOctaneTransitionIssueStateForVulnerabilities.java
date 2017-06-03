package com.fortify.processrunner.octane.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorTransitionIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection;
import com.fortify.processrunner.octane.connection.OctaneConnectionFactory;

public class ProcessorOctaneTransitionIssueStateForVulnerabilities extends AbstractProcessorTransitionIssueStateForVulnerabilities<JSONObject> {
	@Override
	protected void addBugTrackerContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		OctaneConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public String getBugTrackerName() {
		return "Octane";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		getOctaneConnection(context).updateIssue(submittedIssue, issueData);
		return true;
	}

	protected OctaneAuthenticatingRestConnection getOctaneConnection(Context context) {
		return OctaneConnectionFactory.getConnection(context);
	}
	
	@Override
	protected JSONObject getCurrentIssueState(Context context, SubmittedIssue submittedIssue) {
		return getOctaneConnection(context).getIssueState(submittedIssue);
	}
	
	@Override
	protected boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment) {
		return getOctaneConnection(context).transition(submittedIssue, transitionName, comment);
	}

}
