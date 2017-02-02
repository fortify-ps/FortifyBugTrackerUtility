package com.fortify.processrunner.tfs.connection;


public interface IContextTFSConnectionProperties {
	public static final String PRP_BASE_URL = "TFSBaseUrl";
	public static final String PRP_USER_NAME = "TFSUserName";
	public static final String PRP_PASSWORD = "TFSPassword";
	
	public void setTFSBaseUrl(String baseUrl);
	public String getTFSBaseUrl();
	public void setTFSUserName(String userName);
	public String getTFSUserName();
	public void setTFSPassword(String password);
	public String getTFSPassword();
}
