package com.fortify.processrunner.ssc.appversion;

import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;

/**
 * This interface allows for updating {@link Context} instances based on
 * the provided application version {@link JSONObject}.
 * 
 * @author Ruud Senden
 *
 */
public interface ISSCApplicationVersionContextUpdater {
	public void updateContext(Context context, JSONMap applicationVersion);
}
