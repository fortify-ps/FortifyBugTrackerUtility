package com.fortify.processrunner.fod.processor.filter;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will perform filtering based on the
 * FoD bugSubmitted field. Only if the bugSubmitted field matches the configured
 * filterValue ("true" or "false"), the vulnerability will be processed.
 */
public class FoDFilterOnBugSubmittedField extends FoDFilterOnTopLevelField {
	private static final String FIELD_NAME = "bugSubmitted"; 
	public FoDFilterOnBugSubmittedField() {
		super(FIELD_NAME);
	}
	
	public FoDFilterOnBugSubmittedField(String filterValue) {
		super(FIELD_NAME, filterValue);
	}
}
