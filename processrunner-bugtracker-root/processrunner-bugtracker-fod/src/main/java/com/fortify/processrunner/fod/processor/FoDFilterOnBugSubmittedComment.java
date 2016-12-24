package com.fortify.processrunner.fod.processor;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will filter out any FoD 
 * vulnerability that has already been submitted to a bug tracker 
 * before, based on the FoD comments.
 */
public class FoDFilterOnBugSubmittedComment extends FoDFilterOnComments {
	public FoDFilterOnBugSubmittedComment() {
		setExcludePattern(true);
		setFilterPatternTemplateExpression("--- Vulnerability submitted to ${BugTrackerName}.*");
	}
}
