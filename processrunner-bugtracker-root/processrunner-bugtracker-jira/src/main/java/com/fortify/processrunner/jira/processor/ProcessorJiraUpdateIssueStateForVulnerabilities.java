package com.fortify.processrunner.jira.processor;

import java.util.LinkedHashMap;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.jira.util.JiraJSONObjectFromObjectMapBuilder;

public class ProcessorJiraUpdateIssueStateForVulnerabilities extends AbstractProcessorUpdateIssueStateForVulnerabilities {
	private static final JiraJSONObjectFromObjectMapBuilder MAP_TO_JSON = new JiraJSONObjectFromObjectMapBuilder();
	
	@Override
	protected String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		IContextJira contextJira = context.as(IContextJira.class);
		JiraRestConnection conn = contextJira.getJiraConnectionRetriever().getConnection();
		conn.updateIssueData(submittedIssue, MAP_TO_JSON.getJSONObject(issueData));
		return true;
	}
	
	@Override
	protected boolean closeIssueIfOpen(Context context, SubmittedIssue submittedIssue) {
		// TODO Implement this by stealing state transitioning code from SSC JIRA 7 bug tracker plugin
		return false;
	}
	
	@Override
	protected boolean openIssueIfClosed(Context context, SubmittedIssue submittedIssue) {
		// TODO Implement this by stealing state transitioning code from SSC JIRA 7 bug tracker plugin
		return false;
	}

}
