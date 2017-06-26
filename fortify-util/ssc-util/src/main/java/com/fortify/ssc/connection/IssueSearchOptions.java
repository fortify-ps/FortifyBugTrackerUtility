/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
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
