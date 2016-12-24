package com.fortify.processrunner.fod.processor;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractFoDProcessorBulkEditVulnerabilities} implementation
 * will add a comment to a set of FoD vulnerabilities. The comment to be added
 * is defined via {@link #setCommentTemplate(TemplateExpression)}.</p>
 */
public class FoDProcessorAddCommentToVulnerabilities extends AbstractFoDProcessorBulkEditVulnerabilities {
	private TemplateExpression commentTemplateExpression;
	
	public FoDProcessorAddCommentToVulnerabilities() {
		setUriTemplateExpression(SpringExpressionUtil.parseTemplateExpression("/api/v3/releases/${[FoDReleaseId]}/vulnerabilities/bulk-edit"));
	}

	@Override
	protected JSONObject getBulkEditJSONObject(Context context, JSONArray vulnIds) {
		String comment = SpringExpressionUtil.evaluateExpression(context, getCommentTemplateExpression(), String.class);
		try {
			JSONObject root = new JSONObject();
			JSONObject data = new JSONObject();
			root.put("requestModel", data);
			data.put("comment", comment);
			data.put("vulnerabilityIds", vulnIds);
			return data;
		} catch ( JSONException e ) {
			throw new RuntimeException("Cannot create FoD bulk edit request", e);
		}
	}

	public TemplateExpression getCommentTemplateExpression() {
		return commentTemplateExpression;
	}

	public void setCommentTemplateExpression(TemplateExpression commentTemplateExpression) {
		this.commentTemplateExpression = commentTemplateExpression;
	}
	
	public void setCommentTemplateExpression(String commentTemplateExpression) {
		this.commentTemplateExpression = SpringExpressionUtil.parseTemplateExpression(commentTemplateExpression);
	}
}
