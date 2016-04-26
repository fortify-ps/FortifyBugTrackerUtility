package com.fortify.processrunner.fod.processor;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.json.AbstractProcessorAddOnDemandJSONData;
import com.fortify.util.rest.IRestConnection;

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
