package com.fortify.processrunner.ssc.appversion;

import java.util.Collection;

import com.fortify.processrunner.context.Context;

/**
 * This interface allows for dynamically generating zero, one or more 
 * {@link ISSCApplicationVersionFilter} instances.
 * 
 * @author Ruud Senden
 *
 */
public interface ISSCApplicationVersionFilterFactory {
	public Collection<ISSCApplicationVersionFilter> getSSCApplicationVersionFilters(Context context);
}
