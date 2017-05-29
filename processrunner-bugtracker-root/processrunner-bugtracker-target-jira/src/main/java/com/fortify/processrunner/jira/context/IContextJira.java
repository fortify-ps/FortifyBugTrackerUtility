package com.fortify.processrunner.jira.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to JIRA properties like project key and issue type.
 */
public interface IContextJira extends IContextBugTracker {
	public static final String PRP_BASE_URL = "JiraBaseUrl";
	public static final String PRP_USER_NAME = "JiraUserName";
	public static final String PRP_PASSWORD = "JiraPassword";
	
	public void setJiraBaseUrl(String baseUrl);
	public String getJiraBaseUrl();
	public void setJiraUserName(String userName);
	public String getJiraUserName();
	public void setJiraPassword(String password);
	public String getJiraPassword();
	
	public void setJiraProjectKey(String projectKey);
	public String getJiraProjectKey();
	
	public void setJiraIssueType(String issueTypeName);
	public String getJiraIssueType();
}
