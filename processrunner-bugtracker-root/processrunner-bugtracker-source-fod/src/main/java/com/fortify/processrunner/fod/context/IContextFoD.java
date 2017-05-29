package com.fortify.processrunner.fod.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to FoD data like FoD connection properties, FoD REST connection, release 
 * id and top level filter parameter values.
 */
public interface IContextFoD {
	public static final String PRP_BASE_URL = "FoDBaseUrl";
	public static final String PRP_CLIENT_ID = "FoDClientId";
	public static final String PRP_CLIENT_SECRET = "FoDClientSecret";
	public static final String PRP_TENANT = "FoDTenant";
	public static final String PRP_USER_NAME = "FoDUserName";
	public static final String PRP_PASSWORD = "FoDPassword";
	
	public void setFoDBaseUrl(String baseUrl);
	public String getFoDBaseUrl();
	
	public void setFoDClientId(String clientId);
	public String getFoDClientId();
	public void setFoDClientSecret(String clientSecret);
	public String getFoDClientSecret();
	
	public void setFoDTenant(String tenant);
	public String getFoDTenant();
	public void setFoDUserName(String userName);
	public String getFoDUserName();
	public void setFoDPassword(String password);
	public String getFoDPassword();
	
	public void setFoDReleaseId(String releaseId);
	public String getFoDReleaseId();
	
	public void setFoDTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getFoDTopLevelFilterParamValue();
}
