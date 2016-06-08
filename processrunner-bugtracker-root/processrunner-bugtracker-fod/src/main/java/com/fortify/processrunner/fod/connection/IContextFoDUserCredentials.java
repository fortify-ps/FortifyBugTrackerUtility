package com.fortify.processrunner.fod.connection;


public interface IContextFoDUserCredentials extends IContextFoDBaseUrl {
	public static final String PRP_TENANT = "FoDTenant";
	public static final String PRP_USER_NAME = "FoDUserName";
	public static final String PRP_PASSWORD = "FoDPassword";
	
	public void setFoDTenant(String tenant);
	public String getFoDTenant();
	public void setFoDUserName(String userName);
	public String getFoDUserName();
	public void setFoDPassword(String password);
	public String getFoDPassword();
}
