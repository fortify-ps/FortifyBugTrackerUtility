package com.fortify.processrunner.jira.context;

import com.fortify.processrunner.common.context.IContextSubmittedIssueData;

public interface IContextJira extends IContextSubmittedIssueData {
	public void setJiraProjectKey(String projectKey);
	public String getJiraProjectKey();
	
	public void setJiraIssueType(String issueTypeName);
	public String getJiraIssueType();
}
