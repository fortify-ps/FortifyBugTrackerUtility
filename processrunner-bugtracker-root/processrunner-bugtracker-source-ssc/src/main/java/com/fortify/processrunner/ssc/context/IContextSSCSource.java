package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to the context rpoperties provided by {@link IContextSSCCommon}, as
 * well as the SSC top level filter parameter value.
 * 
 * @author Ruud Senden
 */
public interface IContextSSCSource extends IContextSSCCommon {	
	public void setSSCTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getSSCTopLevelFilterParamValue();
}
