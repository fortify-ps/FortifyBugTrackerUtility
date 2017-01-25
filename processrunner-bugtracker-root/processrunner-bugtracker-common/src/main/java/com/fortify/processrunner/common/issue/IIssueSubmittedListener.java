package com.fortify.processrunner.common.issue;

import java.util.Collection;

import com.fortify.processrunner.context.Context;

public interface IIssueSubmittedListener {
	public void issueSubmitted(Context context, String bugTrackerName, SubmittedIssue submittedIssue, Collection<Object> vulnerabilities);
}
