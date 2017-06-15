package com.fortify.processrunner.ssc.appversion;

import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;

/**
 * This abstract base class allows for either including or excluding SSC application versions
 * based on the configured {@link #includeMatched} property. Concrete implementations need to
 * implement the {@link #isApplicationVersionMatching(Context, String, JSONObject)} method
 * to actually match the SSC application version against some implementation-dependent criteria. 
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractSSCApplicationVersionFilter implements ISSCApplicationVersionFilter {
	private boolean includeMatched = true;
	
	public final boolean isApplicationVersionIncluded(Context context, JSONMap applicationVersion) {
		return isApplicationVersionMatching(context, applicationVersion.get("id", String.class), applicationVersion) == isIncludeMatched();
	}
	
	public abstract boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONMap applicationVersion);

	public boolean isIncludeMatched() {
		return includeMatched;
	}

	public void setIncludeMatched(boolean includeMatched) {
		this.includeMatched = includeMatched;
	}

}
