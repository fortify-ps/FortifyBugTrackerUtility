package com.fortify.processrunner.fod.processor;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will filter any FoD vulnerability that
 * has already been submitted to a bug tracker before, based on the FoD
 * comments.
 */
public class FoDFilterOnBugSubmittedComment extends FoDFilterOnComments {
	public FoDFilterOnBugSubmittedComment() {
		setFilterPattern("--- Vulnerability submitted to .*");
	}
}
