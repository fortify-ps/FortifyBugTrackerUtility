package com.fortify.processrunner.common.context;

public interface IContextSubmittedIssueData {
	public void setSubmittedIssueBugTrackerName(String bugTrackerName);
	public String getSubmittedIssueBugTrackerName();
	
	public void setSubmittedIssueId(String id);
	public String getSubmittedIssueId();
	
	public void setSubmittedIssueLocation(String url);
	public String getSubmittedIssueBrowserURL();
}
