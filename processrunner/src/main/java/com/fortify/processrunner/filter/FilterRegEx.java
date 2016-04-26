package com.fortify.processrunner.filter;

import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * <p>This {@link IProcessor} implementation provides filtering functionality
 * based on regular expressions. Given a mapping between field expressions
 * and patterns, this processor will retrieve the value for the given field
 * expression from the context, and then match this value against the
 * corresponding pattern. If a root expression has been configured, the 
 * given field expressions will be evaluated relative to this root expression
 * instead of relative to the context.</p>
 * 
 * <p>If a current value doesn't match the corresponding pattern, the 
 * {@link #process(Context)} method will return false to indicate that 
 * the current entry should not be further processed. If all current
 * values match the corresponding pattern, then true is returned to 
 * indicate that the current entry can be processed further.</p>
 */
public class FilterRegEx extends AbstractProcessor {
	private String rootExpression;
	private Map<String, Pattern> filterPatterns;
	
	public FilterRegEx() {}
	
	public FilterRegEx(String rootExpression, Map<String, Pattern> filterPatterns) {
		this.rootExpression = rootExpression;
		this.filterPatterns = filterPatterns;
	}
	
	@Override
	protected boolean process(Context context) {
		if ( getFilterPatterns() != null ) {
			Object root = getRootExpression()==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
			for ( Map.Entry<String, Pattern> filterPattern : getFilterPatterns().entrySet() ) {
				String expressionValue = SpringExpressionUtil.evaluateExpression(root, filterPattern.getKey(), String.class);
				if ( !filterPattern.getValue().matcher(expressionValue).matches() ) {
					return false;
				}
			}
		}
		return true;
	}

	public String getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(String rootExpression) {
		this.rootExpression = rootExpression;
	}

	public Map<String, Pattern> getFilterPatterns() {
		return filterPatterns;
	}

	public void setFilterPatterns(Map<String, Pattern> filterPatterns) {
		this.filterPatterns = filterPatterns;
	}
}
