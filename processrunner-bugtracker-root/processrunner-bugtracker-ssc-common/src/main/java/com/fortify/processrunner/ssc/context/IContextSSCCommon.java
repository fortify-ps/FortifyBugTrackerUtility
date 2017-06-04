package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to SSC data like connection properties, SSC REST connection, application
 * version id and top level filter parameter values.
 */
public interface IContextSSCCommon {
	public static final String PRP_SSC_BASE_URL = "SSCBaseUrl";
	public static final String PRP_SSC_AUTH_TOKEN = "SSCAuthToken";
	public static final String PRP_SSC_USER_NAME = "SSCUserName";
	public static final String PRP_SSC_PASSWORD = "SSCPassword";
	public static final String PRP_SSC_APPLICATION_VERSION_ID = "SSCApplicationVersionId";
	
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
}
