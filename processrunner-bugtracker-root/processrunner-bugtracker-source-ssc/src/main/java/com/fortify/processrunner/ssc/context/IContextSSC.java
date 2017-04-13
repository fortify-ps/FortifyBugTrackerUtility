package com.fortify.processrunner.ssc.context;

import com.fortify.processrunner.context.Context;
import com.fortify.ssc.connection.ISSCConnectionRetriever;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to FoD data like REST connection, release id and top level filter parameter
 * values.
 */
public interface IContextSSC {
	public void setSSCConnectionRetriever(ISSCConnectionRetriever connectionRetriever);
	public ISSCConnectionRetriever getSSCConnectionRetriever();
	
	public void setSSCApplicationVersionId(String applicationVersionId);
	public String getSSCApplicationVersionId();
	
	public void setSSCTopLevelFilterParamValue(String topLevelFilterParamValue);
	public String getSSCTopLevelFilterParamValue();
}
