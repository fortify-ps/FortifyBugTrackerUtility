package com.fortify.processrunner.octane.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to Octane properties.
 */
public interface IContextOctane extends IContextBugTracker {
	public static final String PRP_BASE_URL = "OctaneBaseUrl";
	public static final String PRP_USER_NAME = "OctaneUserName";
	public static final String PRP_PASSWORD = "OctanePassword";
	public static final String PRP_CLIENT_ID = "OctaneClientId";
	public static final String PRP_CLIENT_SECRET = "OctaneClientSecret";
	public static final String PRP_OCTANE_WORKSPACE_ID = "OctaneWorkspaceId";
	public static final String PRP_OCTANE_SHARED_SPACE_UID = "OctaneSharedSpaceUid";
	
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
	
	public void setOctaneWorkspaceId(String workspaceId);
	public String getOctaneWorkspaceId();
	
	public void setOctaneSharedSpaceUid(String sharedSpaceUid);
	public String getOctaneSharedSpaceUid();
}
