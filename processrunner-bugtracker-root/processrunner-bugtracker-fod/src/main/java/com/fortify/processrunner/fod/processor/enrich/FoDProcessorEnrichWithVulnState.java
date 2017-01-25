package com.fortify.processrunner.fod.processor.enrich;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.IssueState;
import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public class FoDProcessorEnrichWithVulnState extends AbstractFoDProcessorEnrich {
	public static final String NAME_VULN_STATE = "vulnState";
	private SimpleExpression isVulnerabilityOpenExpression = SpringExpressionUtil.parseSimpleExpression("closedStatus==false");
	
	@Override
	protected boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException {
		boolean isOpen = SpringExpressionUtil.evaluateExpression(currentVulnerability, isVulnerabilityOpenExpression, Boolean.class);
		currentVulnerability.putOpt(NAME_VULN_STATE, isOpen?IssueState.OPEN.name():IssueState.CLOSED.name());
		return true;
	}

	/**
	 * @return the isVulnerabilityOpenExpression
	 */
	public SimpleExpression getIsVulnerabilityOpenExpression() {
		return isVulnerabilityOpenExpression;
	}

	/**
	 * @param isVulnerabilityOpenExpression the isVulnerabilityOpenExpression to set
	 */
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.isVulnerabilityOpenExpression = isVulnerabilityOpenExpression;
	}
}
