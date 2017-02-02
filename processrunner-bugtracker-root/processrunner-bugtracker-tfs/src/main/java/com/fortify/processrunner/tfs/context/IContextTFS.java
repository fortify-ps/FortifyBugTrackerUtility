package com.fortify.processrunner.tfs.context;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.tfs.connection.ITFSConnectionRetriever;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to TFS properties like collection, project and work item type.
 */
public interface IContextTFS extends IContextBugTracker {
	public void setTFSConnectionRetriever(ITFSConnectionRetriever connectionRetriever);
	public ITFSConnectionRetriever getTFSConnectionRetriever();
	
	public void setTFSCollection(String collection);
	public String getTFSCollection();
	
	public void setTFSProject(String project);
	public String getTFSProject();
	
	public void setTFSWorkItemType(String workItemType);
	public String getTFSWorkItemType();
}
