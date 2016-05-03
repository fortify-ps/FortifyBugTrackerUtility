package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.Map;

import com.fortify.util.spring.SpringExpressionUtil;

/**
 * <p>This {@link FoDProcessorAddOnDemandJSONData} implementation adds
 * on-demand JSON data to the vulnerability that is currently
 * being processed for all vulnerability data that is available through 
 * the current FoD REST API, like summary, details, recommendations, ...
 * All detail objects are retrieved using a single FoD API call whenever
 * one of the detail objects is first accessed.</p> 
 * 
 * <p>If a configuration requires access to only a small number of detail
 * objects, it may be better to use 
 * {@link FoDProcessorAddOnDemandJSONDataMultiSmallRequest}
 * instead, as this will retrieve the necessary data in smaller,
 * individual API requests.</p>
 */
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
