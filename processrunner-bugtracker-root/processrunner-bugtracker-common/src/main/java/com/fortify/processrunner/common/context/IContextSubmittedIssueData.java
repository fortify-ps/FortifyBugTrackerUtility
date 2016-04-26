package com.fortify.processrunner.common.context;

public interface IContextSubmittedIssueData {
	public void setSubmittedIssueBugTrackerName(String bugTrackerName);
	public String getSubmittedIssueBugTrackerName();
	
	public void setSubmittedIssueId(String id);
	public String getSubmittedIssueId();
	
	public void setSubmittedIssueBrowserURL(String url);
	public String getSubmittedIssueBrowserURL();
}
