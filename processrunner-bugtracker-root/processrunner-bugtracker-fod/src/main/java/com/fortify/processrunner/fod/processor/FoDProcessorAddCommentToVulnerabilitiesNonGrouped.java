package com.fortify.processrunner.fod.processor;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public class FoDProcessorAddCommentToVulnerabilitiesNonGrouped extends FoDProcessorAddCommentToVulnerabilities {
	private static final SimpleExpression EXPR_ROOT = SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability");
	
	public FoDProcessorAddCommentToVulnerabilitiesNonGrouped() {
		setRootExpression(EXPR_ROOT);
	}

}
