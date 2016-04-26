package com.fortify.processrunner.fod.processor;

import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

public class FoDFilterSubmittedToBugTracker extends AbstractProcessor {
	@Override
	protected boolean process(Context context) {
		Pattern filterPattern = Pattern.compile("--- Vulnerability submitted to .*");
		JSONArray array = SpringExpressionUtil.evaluateExpression(context, "FoDCurrentVulnerability.summary.comments", JSONArray.class);
		if ( array != null ) {
			for ( int i = 0 ; i < array.length() ; i++ ) {
				String expressionValue = SpringExpressionUtil.evaluateExpression(array.opt(i), "comment", String.class);
				if ( filterPattern.matcher(expressionValue).matches() ) {
					return false;
				}
			}
		}
		return true;
	}
	
	

}
