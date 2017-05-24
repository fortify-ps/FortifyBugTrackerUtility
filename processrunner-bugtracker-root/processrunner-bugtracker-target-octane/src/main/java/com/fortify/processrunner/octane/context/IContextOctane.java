package com.fortify.processrunner.octane.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.octane.connection.IOctaneConnectionRetriever;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to Octane properties.
 */
public interface IContextOctane extends IContextBugTracker {
	public static final String PRP_OCTANE_WORKSPACE_ID = "OctaneWorkspaceId";
	public static final String PRP_OCTANE_SHARED_SPACE_UID = "OctaneSharedSpaceUid";
	
	public void setOctaneConnectionRetriever(IOctaneConnectionRetriever connectionRetriever);
	public IOctaneConnectionRetriever getOctaneConnectionRetriever();
	
	public void setOctaneWorkspaceId(String workspaceId);
	public String getOctaneWorkspaceId();
	
	public void setOctaneSharedSpaceUid(String sharedSpaceUid);
	public String getOctaneSharedSpaceUid();
}
