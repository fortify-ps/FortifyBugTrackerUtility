package com.fortify.processrunner.common.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.context.Context;

public abstract class AbstractProcessorSubmitJSONObjectFromGroupedObjects extends AbstractProcessorSubmitObjectMapFromGroupedObjects {

	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		return submitIssue(context, getJSONObject(context, issueData));
	}
	
	protected abstract SubmittedIssue submitIssue(Context context, JSONObject jsonObject);

	protected JSONObject getJSONObject(Context context, LinkedHashMap<String, Object> issueData) {
		JSONObject result = new JSONObject();
		for ( Map.Entry<String,Object> field : issueData.entrySet() ) {
			addField(result, field.getKey(), field.getValue());
		}
		return result;
	}

	protected void addField(JSONObject json, String key, Object value) {
		try {
			String[] propertyPath = StringUtils.split(key, ".");
			for ( int i = 0 ; i<propertyPath.length-1 ; i++ ) {
				String property = propertyPath[i]; 
				if ( !json.has(property) ) {
					json.put(property, new JSONObject());
				}
				json = json.optJSONObject(property);
			}
			json.put(propertyPath[propertyPath.length-1], convertValue(value));
		} catch ( JSONException e ) {
			throw new RuntimeException("Error creating JSON object for issue to be submitted", e);
		}
	}

	protected Object convertValue(Object value) {
		if ( value != null ) {
			if ( value.getClass().isArray() ) {
				return convertArrayToJSONArray((Object[])value);
			} else if ( value instanceof Collection ) {
				return convertCollectionToJSONArray((Collection<?>)value);
			}
		}
		return value;
	}

	protected Object convertArrayToJSONArray(Object[] array) {
		JSONArray result = new JSONArray();
		for ( Object entry : array ) {
			result.put(entry);
		}
		return result;
	}
	
	protected Object convertCollectionToJSONArray(Collection<?> collection) {
		JSONArray result = new JSONArray();
		for ( Object entry : collection ) {
			result.put(entry);
		}
		return result;
	}

}
