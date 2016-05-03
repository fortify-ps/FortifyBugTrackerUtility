package com.fortify.processrunner.fod.processor;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This {@link FoDProcessorAddCommentToVulnerabilities} implementation sets the
 * root expression to 'FoDCurrentVulnerability' to allow a comment to be added to
 * the vulnerability that is currently being processed.
 */
public class FoDProcessorAddCommentToVulnerabilitiesNonGrouped extends FoDProcessorAddCommentToVulnerabilities {
	private static final SimpleExpression EXPR_ROOT = SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability");
	
	public FoDProcessorAddCommentToVulnerabilitiesNonGrouped() {
		setRootExpression(EXPR_ROOT);
	}

}
