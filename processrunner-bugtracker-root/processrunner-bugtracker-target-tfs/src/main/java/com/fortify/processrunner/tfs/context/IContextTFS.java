package com.fortify.processrunner.tfs.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to TFS properties like collection, project and work item type.
 */
public interface IContextTFS extends IContextBugTracker {
	public static final String PRP_BASE_URL = "TFSBaseUrl";
	public static final String PRP_USER_NAME = "TFSUserName";
	public static final String PRP_PASSWORD = "TFSPassword";
	
	public void setTFSBaseUrl(String baseUrl);
	public String getTFSBaseUrl();
	public void setTFSUserName(String userName);
	public String getTFSUserName();
	public void setTFSPassword(String password);
	public String getTFSPassword();
	
	public void setTFSCollection(String collection);
	public String getTFSCollection();
	
	public void setTFSProject(String project);
	public String getTFSProject();
	
	public void setTFSWorkItemType(String workItemType);
	public String getTFSWorkItemType();
}
