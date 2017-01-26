package com.fortify.processrunner.jira.processor;

import java.util.LinkedHashMap;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorTransitionIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.jira.util.JiraIssueJSONObjectBuilder;

public class ProcessorJiraTransitionIssueStateForVulnerabilities extends AbstractProcessorTransitionIssueStateForVulnerabilities<JSONObject> {
	private static final JiraIssueJSONObjectBuilder MAP_TO_JSON = new JiraIssueJSONObjectBuilder();
	
	@Override
	protected String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		getJiraConnection(context).updateIssueData(submittedIssue, MAP_TO_JSON.getJSONObject(issueData));
		return true;
	}

	protected JiraRestConnection getJiraConnection(Context context) {
		IContextJira contextJira = context.as(IContextJira.class);
		JiraRestConnection conn = contextJira.getJiraConnectionRetriever().getConnection();
		return conn;
	}
	
	@Override
	protected JSONObject getCurrentIssueState(Context context, SubmittedIssue submittedIssue) {
		return getJiraConnection(context).getIssueState(submittedIssue);
	}
	
	@Override
	protected boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment) {
		return getJiraConnection(context).transition(submittedIssue, transitionName, comment);
	}

}
