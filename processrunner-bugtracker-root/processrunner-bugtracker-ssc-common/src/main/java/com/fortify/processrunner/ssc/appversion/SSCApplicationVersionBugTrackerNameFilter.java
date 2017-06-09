package com.fortify.processrunner.ssc.appversion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;

/**
 * Filter SSC application versions based on the SSC bug tracker plugin name configured
 * for each application version.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionBugTrackerNameFilter extends AbstractSSCApplicationVersionFilter {
	private Set<String> bugTrackerPluginNames = null;
	
	public SSCApplicationVersionBugTrackerNameFilter() {}
	
	public SSCApplicationVersionBugTrackerNameFilter(String... bugTrackerPluginNames) {
		this.bugTrackerPluginNames = bugTrackerPluginNames==null ? null : new HashSet<String>(Arrays.asList(bugTrackerPluginNames));
	}
	
	public int getOrder() {
		return 1;
	}

	@Override
	public boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONObject applicationVersion) {
		Set<String> pluginIds = getBugTrackerPluginIdsForNames(context, getBugTrackerPluginNames());
		return pluginIds!=null && pluginIds.contains(applicationVersion.opt("bugTrackerPluginId"));
	}

	private Set<String> getBugTrackerPluginIdsForNames(Context context, Set<String> bugTrackerPluginNames) {
		return bugTrackerPluginNames==null ? null : SSCConnectionFactory.getConnection(context).getBugTrackerPluginIdsForNames(bugTrackerPluginNames);
	}

	public Set<String> getBugTrackerPluginNames() {
		return bugTrackerPluginNames;
	}

	public void setBugTrackerPluginNames(Set<String> bugTrackerPluginNames) {
		this.bugTrackerPluginNames = bugTrackerPluginNames;
	}
}
