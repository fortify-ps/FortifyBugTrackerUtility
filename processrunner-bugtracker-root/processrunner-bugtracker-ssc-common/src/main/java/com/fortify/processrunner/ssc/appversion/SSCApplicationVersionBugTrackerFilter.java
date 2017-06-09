package com.fortify.processrunner.ssc.appversion;

import java.util.Set;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;

/**
 * Filter SSC application versions based on the SSC bug tracker plugin id/name configured
 * for each application version.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionBugTrackerFilter extends AbstractSSCApplicationVersionFilter {
	private Set<String> bugTrackerPluginIds = null;
	private Set<String> bugTrackerPluginNames = null;
	
	public int getOrder() {
		return bugTrackerPluginIds==null?1:0;
	}

	@Override
	public boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONObject applicationVersion) {
		Set<String> pluginIds = getBugTrackerPluginIds();
		if ( pluginIds == null ) {
			pluginIds = getBugTrackerPluginIdsForNames(context, getBugTrackerPluginNames());
		}
		return pluginIds!=null && pluginIds.contains(applicationVersion.opt("bugTrackerPluginId"));
	}

	private Set<String> getBugTrackerPluginIdsForNames(Context context, Set<String> bugTrackerPluginNames) {
		return SSCConnectionFactory.getConnection(context).getBugTrackerPluginIdsForNames(bugTrackerPluginNames);
	}

	public Set<String> getBugTrackerPluginIds() {
		return bugTrackerPluginIds;
	}

	public void setBugTrackerPluginIds(Set<String> bugTrackerPluginIds) {
		this.bugTrackerPluginIds = bugTrackerPluginIds;
	}

	public Set<String> getBugTrackerPluginNames() {
		return bugTrackerPluginNames;
	}

	public void setBugTrackerPluginNames(Set<String> bugTrackerPluginNames) {
		this.bugTrackerPluginNames = bugTrackerPluginNames;
	}
}
