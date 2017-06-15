package com.fortify.processrunner.fod.processor.enrich;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class allows for loading additional vulnerability details from FoD and adding them to the 
 * current FoD vulnerability JSON object. The following additional vulnerability details are
 * supported by this class:
 * <ul>
 *  <li>summary</li>
 *  <li>details</li>
 *  <li>recommendations</li>
 *  <li>screenshots</li>
 *  <li>history</li>
 *  <li>request-response</li>
 *  <li>headers</li>
 *  <li>parameters</li>
 *  <li>traces</li>
 * </ul>
 * The extra data from the list above to be added to the current vulnerability can be specified either 
 * via the constructor or the {@link #setFields(Set)} methods. The data will be added as a JSON object
 * to the current vulnerability under the corresponding property name.
 */
public class FoDProcessorEnrichWithExtraFoDData extends AbstractFoDProcessorEnrich {
	private static final Map<String, TemplateExpression> FIELD_TO_URI_EXPRESSION_MAP = getUriExpressionMap();
	private Set<String> fields = new HashSet<String>();
	
	public FoDProcessorEnrichWithExtraFoDData() {}
	
	public FoDProcessorEnrichWithExtraFoDData(String... fields) {
		getFields().addAll(Arrays.asList(fields));
	}

	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		for ( String field : fields ) {
			currentVulnerability.put(field, getJSONMap(context, field));
		}
		return true;
	}

	private JSONMap getJSONMap(Context context, String field) {
		IRestConnection conn = FoDConnectionFactory.getConnection(context);
		TemplateExpression uriExpr = FIELD_TO_URI_EXPRESSION_MAP.get(field); 
		if ( uriExpr == null ) {
			throw new RuntimeException("Unknown FoD field "+field);
		}
		String uri = SpringExpressionUtil.evaluateExpression(context, uriExpr, String.class);
		JSONMap data = conn.executeRequest(HttpMethod.GET, 
				conn.getBaseResource().path(uri), JSONMap.class);
		return data;
	}
	
	private static final Map<String, TemplateExpression> getUriExpressionMap() {
		Map<String, TemplateExpression> result = new HashMap<String, TemplateExpression>();
		add(result, "summary");
		add(result, "details");
		add(result, "recommendations");
		add(result, "screenshots");
		add(result, "history");
		add(result, "request-response");
		add(result, "headers");
		add(result, "parameters");
		add(result, "traces");
		return result;
	}
	
	private static final void add(Map<String, TemplateExpression> map, String name) {
		map.put(name, SpringExpressionUtil.parseTemplateExpression("/api/v3/releases/${[FoDReleaseId]}/vulnerabilities/${[CurrentVulnerability].vulnId}/"+name));
	}

	/**
	 * @return the fields
	 */
	public Set<String> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(Set<String> fields) {
		this.fields = fields;
	}
}
