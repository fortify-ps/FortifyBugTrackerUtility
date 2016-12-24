package com.fortify.processrunner.common.processor;

import com.fortify.processrunner.processor.ProcessorPrintMessage;

public class ProcessorPrintMessageGroupedVulnerabilitiesSubmitted extends ProcessorPrintMessage {
	public ProcessorPrintMessageGroupedVulnerabilitiesSubmitted() {
		super(null, "Submitted ${CurrentGroup.size()} vulnerabilities to ${BugTrackerName}: ${SubmittedIssue.deepLink}", null);
	}
}
