package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.Map;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will filter any FoD vulnerability that
 * has already been submitted to a bug tracker before, based on the FoD
 * bugSubmitted field.
 */
public class FoDFilterOnBugSubmittedField extends FoDFilterOnTopLevelFields {
	private static final Map<String, String> FILTERS = createFilters();
	public FoDFilterOnBugSubmittedField() {
		super(FILTERS);
	}
	
	private static Map<String, String> createFilters() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("bugSubmitted", "false");
		return result;
	}
	
	
}
