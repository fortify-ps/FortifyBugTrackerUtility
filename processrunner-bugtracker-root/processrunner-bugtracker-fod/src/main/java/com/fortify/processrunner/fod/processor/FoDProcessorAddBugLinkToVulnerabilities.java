package com.fortify.processrunner.fod.processor;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractFoDProcessorBulkEditVulnerabilities} implementation
 * will add a bug link to a set of FoD vulnerabilities.</p>
 */
public class FoDProcessorAddBugLinkToVulnerabilities extends AbstractFoDProcessorBulkEditVulnerabilities {
	private TemplateExpression bugLinkExpression;
	
	public FoDProcessorAddBugLinkToVulnerabilities() {
		setUriTemplateExpression(SpringExpressionUtil.parseTemplateExpression("/api/v3/releases/${[FoDReleaseId]}/vulnerabilities/bug-link"));
	}

	@Override
	protected JSONObject getBulkEditJSONObject(Context context, JSONArray vulnIds) {
		String bugLink = SpringExpressionUtil.evaluateExpression(context, getBugLinkExpression(), String.class);
		try {
			JSONObject root = new JSONObject();
			JSONObject data = new JSONObject();
			root.put("requestModel", data);
			data.put("bugLink", bugLink);
			data.put("vulnerabilityIds", vulnIds);
			return data;
		} catch ( JSONException e ) {
			throw new RuntimeException("Cannot create FoD bulk edit request", e);
		}
	}

	public TemplateExpression getBugLinkExpression() {
		return bugLinkExpression;
	}

	public void setBugLinkExpression(TemplateExpression bugLinkExpression) {
		this.bugLinkExpression = bugLinkExpression;
	}

	
}
