package com.fortify.processrunner.ssc.connection;

public interface IContextSSCTokenCredentials extends IContextSSCBaseUrl {
	public static final String PRP_SSC_AUTH_TOKEN = "SSCAuthToken";
	public void setSSCAuthToken(String authToken);
	public String getSSCAuthToken();
}
