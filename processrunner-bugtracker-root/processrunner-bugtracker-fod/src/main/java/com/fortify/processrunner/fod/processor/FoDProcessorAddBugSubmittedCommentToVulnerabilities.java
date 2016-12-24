package com.fortify.processrunner.fod.processor;

public class FoDProcessorAddBugSubmittedCommentToVulnerabilities extends FoDProcessorAddCommentToVulnerabilities {
	public FoDProcessorAddBugSubmittedCommentToVulnerabilities() {
		setCommentTemplateExpression("--- Vulnerability submitted to ${BugTrackerName}: ${SubmittedIssue.id==null?'':'ID '+SubmittedIssue.id} Location ${SubmittedIssue.deepLink}");
	}
}
