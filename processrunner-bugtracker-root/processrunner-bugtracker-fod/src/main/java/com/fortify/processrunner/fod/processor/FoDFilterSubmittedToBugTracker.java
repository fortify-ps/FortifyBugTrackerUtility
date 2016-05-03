package com.fortify.processrunner.fod.processor;

import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.expression.Expression;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation will filter any FoD vulnerability that
 * has already been submitted to a bug tracker before. Currently this will check
 * whether the FoD vulnerability currently being processed contains a comment
 * starting with '--- Vulnerability submitted to'.
 */
public class FoDFilterSubmittedToBugTracker extends AbstractProcessor {
	private static final Expression EXPR_COMMENTS = SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability.summary.comments");
	private static final Expression EXPR_COMMENT = SpringExpressionUtil.parseSimpleExpression("comment");
	
	@Override
	protected boolean process(Context context) {
		Pattern filterPattern = Pattern.compile("--- Vulnerability submitted to .*");
		JSONArray array = SpringExpressionUtil.evaluateExpression(context, EXPR_COMMENTS, JSONArray.class);
		if ( array != null ) {
			for ( int i = 0 ; i < array.length() ; i++ ) {
				String expressionValue = SpringExpressionUtil.evaluateExpression(array.opt(i), EXPR_COMMENT, String.class);
				if ( filterPattern.matcher(expressionValue).matches() ) {
					return false;
				}
			}
		}
		return true;
	}
}
