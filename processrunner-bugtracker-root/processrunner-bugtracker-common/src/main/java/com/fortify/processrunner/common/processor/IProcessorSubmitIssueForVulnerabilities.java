package com.fortify.processrunner.common.processor;

import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.processor.IProcessor;

public interface IProcessorSubmitIssueForVulnerabilities extends IProcessor {
	public String getBugTrackerName();
	public void setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener);
}