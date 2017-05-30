package com.fortify.ssc.connection;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IssueSearchOptions {
	private Map<String, Boolean> searchOptions = new HashMap<String, Boolean>();
	
	public boolean isIncludeRemoved() {
		return searchOptions.getOrDefault("REMOVED", false);
	}
	public void setIncludeRemoved(boolean includeRemoved) {
		searchOptions.put("REMOVED", includeRemoved);
	}
	public boolean isIncludeSuppressed() {
		return searchOptions.getOrDefault("SUPPRESSED", false);
	}
	public void setIncludeSuppressed(boolean includeSuppressed) {
		searchOptions.put("SUPPRESSED", includeSuppressed);
	}
	public boolean isIncludeHidden() {
		return searchOptions.getOrDefault("HIDDEN", false);
	}
	public void setIncludeHidden(boolean includeHidden) {
		searchOptions.put("HIDDEN", includeHidden);
	}
	
	JSONArray getJSONRequestData() {
		JSONArray result = new JSONArray();
		result.put(getOption("REMOVED"));
		result.put(getOption("SUPPRESSED"));
		result.put(getOption("HIDDEN"));
		return result;
	}
	private JSONObject getOption(String optionType) {
		try {
			JSONObject result = new JSONObject();
			result.put("optionType", optionType);
			result.put("optionValue", searchOptions.getOrDefault(optionType, false));
			return result;
		} catch (JSONException e) {
			throw new RuntimeException("Error building JSON Object", e);
		}
	}
	
}
