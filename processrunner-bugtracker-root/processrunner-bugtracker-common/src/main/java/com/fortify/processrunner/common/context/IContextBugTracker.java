package com.fortify.processrunner.common.context;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method 
 * to allow easy access to bugtracker-related information.
 */
public interface IContextBugTracker {
	public static final String PRP_BUG_TRACKER_NAME = "BugTrackerName";
	public void setBugTrackerName(String bugTrackerName);
	public String getBugTrackerName();
	
	public void setSubmittedIssue(SubmittedIssue submittedIssue);
	public SubmittedIssue getSubmittedIssue();
}
