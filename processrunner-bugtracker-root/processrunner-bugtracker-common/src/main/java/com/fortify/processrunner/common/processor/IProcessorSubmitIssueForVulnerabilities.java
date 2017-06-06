package com.fortify.processrunner.common.processor;

import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This interface provides the methods necessary for submitting issues for
 * vulnerabilities.
 * 
 * @author Ruud Senden
 *
 */
public interface IProcessorSubmitIssueForVulnerabilities extends IProcessor {
	/**
	 * Get the bug tracker name for this implementation
	 * @return
	 */
	public String getBugTrackerName();
	
	/**
	 * Set the {@link IIssueSubmittedListener} to be called when the bug tracker
	 * implementation has submitted an issue. This method should return true if
	 * the bug tracker implementation supports calling the {@link IIssueSubmittedListener},
	 * or false if it doesn't support this. The latter is usually only the case
	 * if the bug tracker implementation itself updates the source system with the
	 * bug details.
	 * @param issueSubmittedListener
	 * @return true if {@link IIssueSubmittedListener} is supported, false otherwise
	 */
	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener);
}