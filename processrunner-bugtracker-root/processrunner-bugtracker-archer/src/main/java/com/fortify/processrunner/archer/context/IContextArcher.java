package com.fortify.processrunner.archer.context;

import com.fortify.processrunner.archer.connection.IArcherConnectionRetriever;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to Archer properties.
 */
public interface IContextArcher extends IContextBugTracker {
	public void setArcherConnectionRetriever(IArcherConnectionRetriever connectionRetriever);
	public IArcherConnectionRetriever getArcherConnectionRetriever();
}
