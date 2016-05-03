package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * <p>This {@link FoDProcessorAddOnDemandJSONData} implementation adds
 * on-demand JSON data to the vulnerability that is currently
 * being processed for all vulnerability data that is available through 
 * the current FoD REST API, like summary, details, recommendations, ...
 * Each detail object is retrieved separately when it is being accessed
 * for the first time, using the corresponding FoD API call.</p> 
 * 
 * <p>If a configuration requires access to multiple of
 * these detail objects, it may be better to use 
 * {@link FoDProcessorAddOnDemandJSONDataSingleLargeRequest}
 * instead, as this will retrieve all available data in a single
 * (large) request.</p>
 */
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
