package com.fortify.processrunner.ssc.appversion;

import java.util.List;
import java.util.Set;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.util.json.JSONMap;

/**
 * Filter SSC application versions based on whether the SSC application version contains
 * the configured custom tag name(s).
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionCustomTagFilter extends AbstractSSCApplicationVersionFilter {
	private Set<String> customTagNames = null;
	
	public int getOrder() {
		return 2;
	}

	@Override
	public boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONMap applicationVersion) {
		Set<String> namesToCheck = getCustomTagNames();
		List<String> avCustomTagNames = SSCConnectionFactory.getConnection(context).getApplicationVersionCustomTagNames(applicationVersionId);
		boolean result = true;
		for (String nameToCheck : namesToCheck) {
			result &= avCustomTagNames.contains(nameToCheck);
		}
		return result;
	}

	public Set<String> getCustomTagNames() {
		return customTagNames;
	}

	public void setCustomTagNames(Set<String> customTagNames) {
		this.customTagNames = customTagNames;
	}
}
