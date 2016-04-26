package com.fortify.processrunner.fod.context;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.rest.IRestConnection;

// As a naming convention, every method name includes 'FoD' to avoid
// naming conflicts with other types of contexts in the backing map.
public interface IContextFoD {
	public void setFoDConnection(IRestConnection conn);
	public IRestConnection getFoDConnection();
	
	public void setFoDReleaseId(String releaseId);
	public String getFoDReleaseId();
	
	public void setFoDCurrentVulnerability(JSONObject vulnerability);
	public JSONObject getFoDCurrentVulnerability();
	
	public void setFoDTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getFoDTopLevelFilterParamValue();
}
