package com.fortify.ssc.connection;

import java.util.HashMap;
import java.util.Map;

import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;

/**
 * This class describes the SSC issue search options, allowing to either 
 * include or exclude removed, hidden and suppressed issues.
 * @author Ruud Senden
 *
 */
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
	
	JSONList getJSONRequestData() {
		JSONList result = new JSONList();
		result.add(getOption("REMOVED"));
		result.add(getOption("SUPPRESSED"));
		result.add(getOption("HIDDEN"));
		return result;
	}
	private JSONMap getOption(String optionType) {
		JSONMap result = new JSONMap();
		result.put("optionType", optionType);
		result.put("optionValue", searchOptions.getOrDefault(optionType, false));
		return result;
	}
}
