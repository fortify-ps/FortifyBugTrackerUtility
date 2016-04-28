package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public class FoDProcessorAddOnDemandJSONDataMultiSmallRequest extends FoDProcessorAddOnDemandJSONData {
	private static final SimpleExpression EXPR_DATA = SpringExpressionUtil.parseSimpleExpression("data.data");
	
	public FoDProcessorAddOnDemandJSONDataMultiSmallRequest() {
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
		map.put(name, new RootExpressionAndUri(EXPR_DATA, "/api/v2/Releases/${[FoDReleaseId]}/Vulnerabilities/${[FoDCurrentVulnerability].vulnId}/"+StringUtils.capitalize(name)));
	}
}
