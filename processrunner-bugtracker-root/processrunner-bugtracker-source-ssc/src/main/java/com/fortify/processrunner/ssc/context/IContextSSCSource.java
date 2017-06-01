package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to SSC connection properties, application version id and SSC bug tracker
 * username/password.
 */
public interface IContextSSCSource extends IContextSSCCommon {	
	public void setSSCTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getSSCTopLevelFilterParamValue();
}
