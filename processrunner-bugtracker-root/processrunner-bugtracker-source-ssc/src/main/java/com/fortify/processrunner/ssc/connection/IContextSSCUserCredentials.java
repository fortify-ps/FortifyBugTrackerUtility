package com.fortify.processrunner.ssc.connection;


public interface IContextSSCUserCredentials extends IContextSSCBaseUrl {
	public static final String PRP_SSC_USER_NAME = "SSCUserName";
	public static final String PRP_SSC_PASSWORD = "SSCPassword";
	
	public void setSSCUserName(String userName);
	public String getSSCUserName();
	public void setSSCPassword(String password);
	public String getSSCPassword();
}
