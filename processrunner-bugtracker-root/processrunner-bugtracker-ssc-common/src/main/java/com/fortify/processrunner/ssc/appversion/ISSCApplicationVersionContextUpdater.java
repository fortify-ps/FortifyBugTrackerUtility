package com.fortify.processrunner.ssc.appversion;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;

/**
 * This interface allows for updating {@link Context} instances based on
 * the provided application version {@link JSONObject}.
 * 
 * @author Ruud Senden
 *
 */
public interface ISSCApplicationVersionContextUpdater {
	public void updateContext(Context context, JSONObject applicationVersion);
}
