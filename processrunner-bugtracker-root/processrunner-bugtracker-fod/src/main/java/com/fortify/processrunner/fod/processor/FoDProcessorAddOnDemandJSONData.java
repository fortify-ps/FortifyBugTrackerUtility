package com.fortify.processrunner.fod.processor;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.json.AbstractProcessorAddOnDemandJSONData;
import com.fortify.util.rest.IRestConnection;

/**
 * This {@link AbstractProcessorAddOnDemandJSONData} implementation provides
 * the FoD {@link IRestConnection} used to retrieve additional JSON data,
 * and the FoD vulnerability that is currently being processed as the
 * root object to which on-demand data needs to be added. Usually this 
 * class is not used directly, but instead either 
 * {@link FoDProcessorAddOnDemandJSONDataMultiSmallRequest} or
 * {@link FoDProcessorAddOnDemandJSONDataSingleLargeRequest} is used.
 */
public class FoDProcessorAddOnDemandJSONData extends AbstractProcessorAddOnDemandJSONData {

	@Override
	protected IRestConnection getRestconnection(Context context) {
		return context.as(IContextFoD.class).getFoDConnection();
	}
	
	@Override
	protected JSONObject getRootObject(Context context) {
		return context.as(IContextFoD.class).getFoDCurrentVulnerability();
	}

}
