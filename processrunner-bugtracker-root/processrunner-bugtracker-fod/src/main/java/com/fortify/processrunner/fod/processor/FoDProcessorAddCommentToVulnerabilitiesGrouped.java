package com.fortify.processrunner.fod.processor;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This {@link FoDProcessorAddCommentToVulnerabilities} implementation sets the
 * iterable expression to 'CurrentGroup' to allow comments to be added to all
 * vulnerabilities contained in the group that is currently being processed.
 */
public class FoDProcessorAddCommentToVulnerabilitiesGrouped extends FoDProcessorAddCommentToVulnerabilities {
	private static final SimpleExpression EXPR_ITERABLE = SpringExpressionUtil.parseSimpleExpression("CurrentGroup");
	
	public FoDProcessorAddCommentToVulnerabilitiesGrouped() {
		setIterableExpression(EXPR_ITERABLE);
	}

}
