package com.fortify.processrunner.jira.processor;

import java.util.LinkedHashMap;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorTransitionIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.jira.connection.JiraConnectionFactory;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.util.json.JSONMap;

public class ProcessorJiraTransitionIssueStateForVulnerabilities extends AbstractProcessorTransitionIssueStateForVulnerabilities<JSONMap> {
	@Override
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		JiraConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueFields) {
		getJiraConnection(context).updateIssueData(submittedIssue, issueFields);
		return true;
	}

	protected JiraRestConnection getJiraConnection(Context context) {
		return JiraConnectionFactory.getConnection(context);
	}
	
	@Override
	protected JSONMap getCurrentIssueState(Context context, SubmittedIssue submittedIssue) {
		return getJiraConnection(context).getIssueState(submittedIssue);
	}
	
	@Override
	protected boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment) {
		return getJiraConnection(context).transition(submittedIssue, transitionName, comment);
	}

}
