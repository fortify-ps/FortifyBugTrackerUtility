package com.fortify.processrunner.ssc.appversion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;

/**
 * Filter SSC application versions based on the SSC bug tracker plugin id configured
 * for each application version.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionBugTrackerIdFilter extends AbstractSSCApplicationVersionFilter {
	private Set<String> bugTrackerPluginIds = null;
	
	public SSCApplicationVersionBugTrackerIdFilter() {}
	
	public SSCApplicationVersionBugTrackerIdFilter(String... bugTrackerPluginIds) {
		this.bugTrackerPluginIds = bugTrackerPluginIds==null ? null : new HashSet<String>(Arrays.asList(bugTrackerPluginIds));
	}
	
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONObject applicationVersion) {
		Set<String> pluginIds = getBugTrackerPluginIds();
		return pluginIds!=null && pluginIds.contains(applicationVersion.optString("bugTrackerPluginId"));
	}

	public Set<String> getBugTrackerPluginIds() {
		return bugTrackerPluginIds;
	}

	public void setBugTrackerPluginIds(Set<String> bugTrackerPluginIds) {
		this.bugTrackerPluginIds = bugTrackerPluginIds;
	}
}
