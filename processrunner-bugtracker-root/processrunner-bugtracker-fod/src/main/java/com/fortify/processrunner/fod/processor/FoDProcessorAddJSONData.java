package com.fortify.processrunner.fod.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public class FoDProcessorAddJSONData extends AbstractProcessor {
	private static final Map<String, TemplateExpression> FIELD_TO_URI_EXPRESSION_MAP = getUriExpressionMap();
	private Set<String> fields = new HashSet<String>();

	@Override
	protected boolean process(Context context) {
		IContextFoD ctx = context.as(IContextFoD.class);
		JSONObject currentVulnerability = ctx.getFoDCurrentVulnerability();
		try {
			for ( String field : fields ) {
				currentVulnerability.putOpt(field, getJSONObject(context, field));
			}
		} catch ( JSONException e ) {
			throw new RuntimeException("Error adding extra JSON data for FoD vulnerability", e);
		}
		return true;
	}

	private JSONObject getJSONObject(Context context, String field) {
		IRestConnection conn = context.as(IContextFoD.class).getFoDConnectionRetriever().getConnection();
		TemplateExpression uriExpr = FIELD_TO_URI_EXPRESSION_MAP.get(field); 
		if ( uriExpr == null ) {
			throw new RuntimeException("Unknown FoD field "+field);
		}
		String uri = SpringExpressionUtil.evaluateExpression(context, uriExpr, String.class);
		JSONObject data = conn.executeRequest(HttpMethod.GET, 
				conn.getBaseResource().path(uri), JSONObject.class);
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
		map.put(name, SpringExpressionUtil.parseTemplateExpression("/api/v3/releases/${[FoDReleaseId]}/vulnerabilities/${[FoDCurrentVulnerability].vulnId}/"+name));
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
