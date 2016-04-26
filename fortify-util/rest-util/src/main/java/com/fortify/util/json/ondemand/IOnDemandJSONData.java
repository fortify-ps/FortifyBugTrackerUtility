package com.fortify.util.json.ondemand;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.expression.EvaluationContext;

import com.fortify.util.json.JsonPropertyAccessor;

/**
 * This interface allows for on-demand loading of JSON data. 
 * Any objects in a JSONObject that implement this interface
 * will automatically be replaced with the corresponding
 * on-demand data whenever accessed via {@link JsonPropertyAccessor}
 */
public interface IOnDemandJSONData {
	public Object replaceOnDemandJSONData(EvaluationContext evaluationContext, JSONObject target, String name);
}
