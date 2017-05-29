package com.fortify.processrunner.archer.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to Archer properties.
 */
public interface IContextArcher extends IContextBugTracker {
	public static final String PRP_BASE_URL = "ArcherBaseUrl";
	public static final String PRP_INSTANCE_NAME = "ArcherInstanceName";
	public static final String PRP_USER_NAME = "ArcherUserName";
	public static final String PRP_USER_DOMAIN = "ArcherUserDomain";
	public static final String PRP_PASSWORD = "ArcherPassword";
	public static final String PRP_APPLICATION_NAME = "ArcherApplicationName";
	
	public void setArcherBaseUrl(String baseUrl);
	public String getArcherBaseUrl();
	public void setArcherApplicationName(String applicationName);
	public String getArcherApplicationName();
	public void setArcherInstanceName(String instanceName);
	public String getArcherInstanceName();
	public void setArcherUserName(String userName);
	public String getArcherUserName();
	public void setArcherUserDomain(String userDomain);
	public String getArcherUserDomain();
	public void setArcherPassword(String password);
	public String getArcherPassword();
}
