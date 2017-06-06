package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to the context rpoperties provided by {@link IContextSSCCommon}, as
 * well as the SSC bug tracker user name and password.
 * 
 * @author Ruud Senden
 */
public interface IContextSSCTarget extends IContextSSCCommon {	
	public static final String PRP_SSC_BUG_TRACKER_USER_NAME = "SSCBugTrackerUserName";
	public static final String PRP_SSC_BUG_TRACKER_PASSWORD = "SSCBugTrackerPassword";
	
	public void setSSCBugTrackerUserName(String userName);
	public String getSSCBugTrackerUserName();
	
	public void setSSCBugTrackerPassword(String password);
	public String getSSCBugTrackerPassword();
}
