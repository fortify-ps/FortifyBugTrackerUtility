package com.fortify.processrunner.fod.processor;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public class FoDProcessorAddCommentToVulnerabilitiesGrouped extends FoDProcessorAddCommentToVulnerabilities {
	private static final SimpleExpression EXPR_ITERABLE = SpringExpressionUtil.parseSimpleExpression("CurrentGroup");
	
	public FoDProcessorAddCommentToVulnerabilitiesGrouped() {
		setIterableExpression(EXPR_ITERABLE);
	}

}
