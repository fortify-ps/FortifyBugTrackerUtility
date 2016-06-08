package com.fortify.processrunner.fod.context;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.fod.connection.IFoDConnectionRetriever;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to FoD data like REST connection, release id and vulnerability currently
 * being processed.
 */
public interface IContextFoD {
	public void setFoDConnectionRetriever(IFoDConnectionRetriever connectionRetriever);
	public IFoDConnectionRetriever getFoDConnectionRetriever();
	
	public void setFoDReleaseId(String releaseId);
	public String getFoDReleaseId();
	
	public void setFoDCurrentVulnerability(JSONObject vulnerability);
	public JSONObject getFoDCurrentVulnerability();
	
	public void setFoDTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getFoDTopLevelFilterParamValue();
}
