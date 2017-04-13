package com.fortify.processrunner.fod.processor.filter;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will perform filtering based on FoD 
 * comments that indicate that a vulnerability has been submitted to a bug 
 * tracker. If the excludeVulnerabilityWithMatchingComment flag is set to 
 * false (default), vulnerabilities will only be processed if they have a 
 * comment indicating that the vulnerability has been previously submitted to 
 * a bug tracker. If the flag is set to true, vulnerabilities will only be 
 * processed if they do not have have a comment indicating that the vulnerability 
 * has been previously submitted to a bug tracker.
 */
public class FoDFilterOnBugSubmittedComment extends FoDFilterOnComments {
	public FoDFilterOnBugSubmittedComment() {
		setFilterPatternTemplateExpression("--- Vulnerability submitted to ${BugTrackerName}.*");
	}
	
	public FoDFilterOnBugSubmittedComment(boolean excludeVulnerabilityWithMatchingComment) {
		this();
		setExcludeVulnerabilityWithMatchingComment(excludeVulnerabilityWithMatchingComment);
	}
}
