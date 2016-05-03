package com.fortify.processrunner.common.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method 
 * to allow easy access to information about a submitted bug tracker issue
 * like bug tracker name, issue id and location (usually a URL).
 */
public interface IContextSubmittedIssueData {
	public void setSubmittedIssueBugTrackerName(String bugTrackerName);
	public String getSubmittedIssueBugTrackerName();
	
	public void setSubmittedIssueId(String id);
	public String getSubmittedIssueId();
	
	public void setSubmittedIssueLocation(String url);
	public String getSubmittedIssueBrowserURL();
}
