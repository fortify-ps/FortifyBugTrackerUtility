package com.fortify.processrunner.ssc.appversion;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.core.Ordered;

import com.fortify.processrunner.context.Context;

/**
 * This interface allows for filtering SSC application versions by implementing the
 * {@link #isApplicationVersionIncluded(Context, JSONObject)} method.
 * 
 * @author Ruud Senden
 *
 */
public interface ISSCApplicationVersionFilter extends Ordered {
	public boolean isApplicationVersionIncluded(Context context, JSONObject applicationVersion);
	/**
	 * This method defines the filter order, to have less expensive filters
	 * be processed before more expensive filters. As a rule of thumb, this
	 * method should return the following number:
	 * numberOfCachedRESTCalls+10*numberOfNonCachedRESTCalls.
	 */
	int getOrder();
}
