package com.fortify.processrunner.fod.connection;

public interface IContextFoDBaseUrl {
	public static final String PRP_BASE_URL = "FoDBaseUrl";
	public void setFoDBaseUrl(String baseUrl);
	public String getFoDBaseUrl();
}
