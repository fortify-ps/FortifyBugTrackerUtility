package com.fortify.processrunner.jira.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.jira.connection.IJiraConnectionRetriever;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to JIRA properties like project key and issue type.
 */
public interface IContextJira extends IContextBugTracker {
	public void setJiraConnectionRetriever(IJiraConnectionRetriever connectionRetriever);
	public IJiraConnectionRetriever getJiraConnectionRetriever();
	
	public void setJiraProjectKey(String projectKey);
	public String getJiraProjectKey();
	
	public void setJiraIssueType(String issueTypeName);
	public String getJiraIssueType();
}
