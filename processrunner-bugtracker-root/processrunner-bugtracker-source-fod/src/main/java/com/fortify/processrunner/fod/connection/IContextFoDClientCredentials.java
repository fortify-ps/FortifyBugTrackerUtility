package com.fortify.processrunner.fod.connection;

public interface IContextFoDClientCredentials extends IContextFoDBaseUrl {
	public static final String PRP_CLIENT_ID = "FoDClientId";
	public static final String PRP_CLIENT_SECRET = "FoDClientSecret";
	public void setFoDClientId(String clientId);
	public String getFoDClientId();
	public void setFoDClientSecret(String clientSecret);
	public String getFoDClientSecret();
}
