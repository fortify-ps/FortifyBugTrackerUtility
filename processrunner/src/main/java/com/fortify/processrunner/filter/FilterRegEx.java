package com.fortify.processrunner.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

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
	private SimpleExpression rootExpression;
	private Map<SimpleExpression, Pattern> filterPatterns;
	
	public FilterRegEx() {}
	
	public FilterRegEx(String rootExpression, Map<String, Pattern> filterPatterns) {
		this.rootExpression = SpringExpressionUtil.parseSimpleExpression(rootExpression);
		this.filterPatterns = getFilterPatterns(filterPatterns);
	}
	
	private static final Map<SimpleExpression, Pattern> getFilterPatterns(Map<String, Pattern> map) {
		Map<SimpleExpression, Pattern> result = new HashMap<SimpleExpression, Pattern>(map.size());
		for ( Map.Entry<String, Pattern> entry : map.entrySet() ) {
			result.put(SpringExpressionUtil.parseSimpleExpression(entry.getKey()), entry.getValue());
		}
		return result;
	}

	@Override
	protected boolean process(Context context) {
		if ( getFilterPatterns() != null ) {
			Object root = getRootExpression()==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
			for ( Map.Entry<SimpleExpression, Pattern> filterPattern : getFilterPatterns().entrySet() ) {
				String expressionValue = SpringExpressionUtil.evaluateExpression(root, filterPattern.getKey(), String.class);
				if ( !filterPattern.getValue().matcher(expressionValue).matches() ) {
					return false;
				}
			}
		}
		return true;
	}

	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public Map<SimpleExpression, Pattern> getFilterPatterns() {
		return filterPatterns;
	}

	public void setFilterPatterns(Map<SimpleExpression, Pattern> filterPatterns) {
		this.filterPatterns = filterPatterns;
	}
}
