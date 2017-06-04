package com.fortify.processrunner.jira.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.jira.connection.JiraConnectionFactory;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.jira.util.JiraIssueJSONObjectBuilder;

/**
 * This {@link AbstractProcessorSubmitIssueForVulnerabilities} implementation
 * submits issues to Jira.
 */
public class ProcessorJiraSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private static final JiraIssueJSONObjectBuilder MAP_TO_JSON = new JiraIssueJSONObjectBuilder();
	private String defaultIssueType = "Task";
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		JiraConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("JiraProjectKey", "JIRA project key identifying the JIRA project to submit vulnerabilities to", context, null, true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("JiraIssueType", "JIRA issue type", context, getDefaultIssueType(), false));
	}
	
	@Override
	public String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		IContextJira contextJira = context.as(IContextJira.class);
		JiraRestConnection conn = JiraConnectionFactory.getConnection(context);
		issueData.put("project.key", contextJira.getJiraProjectKey());
		issueData.put("issuetype.name", getIssueType(contextJira));
		issueData.put("summary", StringUtils.abbreviate((String)issueData.get("summary"), 254));
		return conn.submitIssue(MAP_TO_JSON.getJSONObject(issueData));
	}
	
	protected String getIssueType(IContextJira context) {
		String issueType = context.getJiraIssueType();
		return issueType!=null?issueType:getDefaultIssueType();
	}

	public String getDefaultIssueType() {
		return defaultIssueType;
	}

	public void setDefaultIssueType(String defaultIssueType) {
		this.defaultIssueType = defaultIssueType;
	}
}
