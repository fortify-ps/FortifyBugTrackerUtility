package com.fortify.processrunner.ssc.processor.enrich;

import com.fortify.processrunner.common.issue.IssueState;
import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This class enriches the current vulnerability with the vulnerability open/closed state.
 * 
 * @author Ruud Senden
 *
 */
public class SSCProcessorEnrichWithVulnState extends AbstractSSCProcessorEnrich {
	public static final String NAME_VULN_STATE = "vulnState";
	public static final SimpleExpression DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION = SpringExpressionUtil.parseSimpleExpression("removed==false && suppressed==false");
	private SimpleExpression isVulnerabilityOpenExpression = DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	
	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		boolean isOpen = SpringExpressionUtil.evaluateExpression(currentVulnerability, isVulnerabilityOpenExpression, Boolean.class);
		currentVulnerability.put(NAME_VULN_STATE, isOpen?IssueState.OPEN.name():IssueState.CLOSED.name());
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
