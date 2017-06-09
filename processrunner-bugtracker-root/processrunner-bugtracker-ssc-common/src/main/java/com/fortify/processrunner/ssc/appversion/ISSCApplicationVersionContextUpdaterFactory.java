package com.fortify.processrunner.ssc.appversion;

import java.util.Collection;

import com.fortify.processrunner.context.Context;

/**
 * This interface allows for dynamically generating zero, one or more 
 * {@link ISSCApplicationVersionContextUpdater} instances.
 * 
 * @author Ruud Senden
 *
 */
public interface ISSCApplicationVersionContextUpdaterFactory {
	public Collection<ISSCApplicationVersionContextUpdater> getSSCApplicationVersionContextUpdaters(Context context);
}
