package com.fortify.processrunner.common.issue;

import java.util.Collection;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used to get notified after an issue has been submitted
 * to an external system/bug tracker. It can for example be used to update the
 * source system with information about the submitted issue.
 * 
 * @author Ruud Senden
 *
 */
public interface IIssueSubmittedListener {
	/**
	 * Notification that the given {@link SubmittedIssue} has been submitted to
	 * the bug tracker identified by bugTrackerName. The issue was submitted for
	 * all vulnerabilities identified by the vulnerabilities parameter.
	 * 
	 * @param context
	 * @param bugTrackerName
	 * @param submittedIssue
	 * @param vulnerabilities
	 */
	public void issueSubmitted(Context context, String bugTrackerName, SubmittedIssue submittedIssue, Collection<Object> vulnerabilities);
}
