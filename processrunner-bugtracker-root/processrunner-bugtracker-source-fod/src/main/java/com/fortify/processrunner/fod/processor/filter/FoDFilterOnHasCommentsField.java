package com.fortify.processrunner.fod.processor.filter;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will perform filtering based on the
 * FoD hasComments field. Only if the hasComments field matches the configured
 * filterValue ("true" or "false"), the vulnerability will be processed.
 */
public class FoDFilterOnHasCommentsField extends FoDFilterOnTopLevelField {
	private static final String FIELD_NAME = "hasComments"; 
	public FoDFilterOnHasCommentsField() {
		super(FIELD_NAME);
	}
	
	public FoDFilterOnHasCommentsField(String filterValue) {
		super(FIELD_NAME, filterValue);
	}
	
}
