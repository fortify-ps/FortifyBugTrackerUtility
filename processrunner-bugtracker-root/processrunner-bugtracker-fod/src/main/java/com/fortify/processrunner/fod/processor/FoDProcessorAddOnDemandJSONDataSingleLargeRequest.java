package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.Map;

import com.fortify.util.spring.SpringExpressionUtil;

public class FoDProcessorAddOnDemandJSONDataSingleLargeRequest extends FoDProcessorAddOnDemandJSONData {
	public FoDProcessorAddOnDemandJSONDataSingleLargeRequest() {
		Map<String, RootExpressionAndUri> nameToRootExpressionAndUriMap = new HashMap<String, RootExpressionAndUri>();
		add(nameToRootExpressionAndUriMap, "summary");
		add(nameToRootExpressionAndUriMap, "details");
		add(nameToRootExpressionAndUriMap, "recommendations");
		add(nameToRootExpressionAndUriMap, "screenshots");
		add(nameToRootExpressionAndUriMap, "history");
		add(nameToRootExpressionAndUriMap, "requestResponse");
		add(nameToRootExpressionAndUriMap, "headers");
		add(nameToRootExpressionAndUriMap, "parameters");
		add(nameToRootExpressionAndUriMap, "traces");
		setNameToRootExpressionAndUriMap(nameToRootExpressionAndUriMap);
	}
	
	private void add(Map<String, RootExpressionAndUri> map, String name) {
		map.put(name, new RootExpressionAndUri(
				SpringExpressionUtil.parseSimpleExpression("data.data."+name), 
				"/api/v2/Releases/${[FoDReleaseId]}/Vulnerabilities/${[FoDCurrentVulnerability].vulnId}/AllData"));
	}
}
