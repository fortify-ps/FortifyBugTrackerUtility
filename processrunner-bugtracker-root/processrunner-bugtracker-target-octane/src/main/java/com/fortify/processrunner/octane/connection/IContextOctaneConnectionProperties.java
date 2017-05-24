package com.fortify.processrunner.octane.connection;

public interface IContextOctaneConnectionProperties {
	public static final String PRP_BASE_URL = "OctaneBaseUrl";
	public static final String PRP_USER_NAME = "OctaneUserName";
	public static final String PRP_PASSWORD = "OctanePassword";
	public static final String PRP_CLIENT_ID = "OctaneClientId";
	public static final String PRP_CLIENT_SECRET = "OctaneClientSecret";
	
	public void setOctaneBaseUrl(String baseUrl);
	public String getOctaneBaseUrl();
	
	public void setOctaneUserName(String userName);
	public String getOctaneUserName();
	public void setOctanePassword(String password);
	public String getOctanePassword();
	
	public void setOctaneClientId(String clientId);
	public String getOctaneClientId();
	public void setOctaneClientSecret(String clientSecret);
	public String getOctaneClientSecret();
}
