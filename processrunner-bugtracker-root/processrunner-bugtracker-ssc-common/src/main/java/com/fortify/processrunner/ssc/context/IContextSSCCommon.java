package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to SSC data like connection properties and application version id.
 * 
 * @author Ruud Senden
 */
public interface IContextSSCCommon {
	public static final String PRP_SSC_BASE_URL = "SSCBaseUrl";
	public static final String PRP_SSC_AUTH_TOKEN = "SSCAuthToken";
	public static final String PRP_SSC_USER_NAME = "SSCUserName";
	public static final String PRP_SSC_PASSWORD = "SSCPassword";
	public static final String PRP_SSC_APPLICATION_VERSION_ID = "SSCApplicationVersionId";
	public static final String PRP_SSC_APPLICATION_VERSIONS = "SSCApplicationVersions";
	
	public void setSSCBaseUrl(String baseUrl);
	public String getSSCBaseUrl();
	
	public void setSSCAuthToken(String authToken);
	public String getSSCAuthToken();
	
	public void setSSCUserName(String userName);
	public String getSSCUserName();
	public void setSSCPassword(String password);
	public String getSSCPassword();
	
	public void setSSCApplicationVersionId(String applicationVersionId);
	public String getSSCApplicationVersionId();
	
	public void setSSCApplicationVersions(String applicationVersionIdsOrNames);
	public String getSSCApplicationVersions();
	
	public void setAppVersion(JSONMap applicationVersion);
	public JSONMap getAppVersion();
}
