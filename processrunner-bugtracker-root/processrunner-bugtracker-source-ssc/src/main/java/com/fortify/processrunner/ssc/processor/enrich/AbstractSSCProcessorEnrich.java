package com.fortify.processrunner.ssc.processor.enrich;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;

/**
 * Abstract base class for enriching SSC vulnerability data with additional details
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractSSCProcessorEnrich extends AbstractProcessor {
	@Override
	protected boolean process(Context context) {
		JSONObject currentVulnerability = (JSONObject) context.as(IContextCurrentVulnerability.class).getCurrentVulnerability();
		try {
			return enrich(context, currentVulnerability);
		} catch (JSONException e) {
			throw new RuntimeException("Error enriching vulnerability data", e);
		}
	}

	protected abstract boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException;
}
