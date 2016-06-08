package com.fortify.processrunner.jira.connection;


public interface IContextJiraConnectionProperties {
	public static final String PRP_BASE_URL = "JiraBaseUrl";
	public static final String PRP_USER_NAME = "JiraUserName";
	public static final String PRP_PASSWORD = "JiraPassword";
	
	public void setJiraBaseUrl(String baseUrl);
	public String getJiraBaseUrl();
	public void setJiraUserName(String userName);
	public String getJiraUserName();
	public void setJiraPassword(String password);
	public String getJiraPassword();
}
