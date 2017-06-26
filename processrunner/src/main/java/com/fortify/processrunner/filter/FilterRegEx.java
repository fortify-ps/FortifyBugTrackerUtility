/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
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
 * 
 * @author Ruud Senden
 */
public class FilterRegEx extends AbstractProcessor {
	private SimpleExpression rootExpression;
	private Map<SimpleExpression, Pattern> filterPatterns;
	
	/**
	 * Default constructor for manual configuration of this
	 * {@link IProcessor} via setter methods.
	 */
	public FilterRegEx() {}
	
	/**
	 * Constructor that allows configuration of the root
	 * expression and map of filter patterns.
	 * @param rootExpression
	 * @param filterPatterns
	 */
	public FilterRegEx(String rootExpression, Map<String, Pattern> filterPatterns) {
		this.rootExpression = SpringExpressionUtil.parseSimpleExpression(rootExpression);
		this.filterPatterns = getFilterPatterns(filterPatterns);
	}
	
	/**
	 * Convert the {@link String} keys in the given map to {@link SimpleExpression}
	 * instances. 
	 * @param map
	 * @return
	 */
	private static final Map<SimpleExpression, Pattern> getFilterPatterns(Map<String, Pattern> map) {
		Map<SimpleExpression, Pattern> result = new HashMap<SimpleExpression, Pattern>(map.size());
		for ( Map.Entry<String, Pattern> entry : map.entrySet() ) {
			result.put(SpringExpressionUtil.parseSimpleExpression(entry.getKey()), entry.getValue());
		}
		return result;
	}

	/**
	 * Process the current {@link Context}. This will retrieve the current
	 * context property value for each filter pattern mapping, and match
	 * this value against the configured regular expression. If any of the
	 * values do not match the corresponding regular expression, this method
	 * will return false to indicate that the current context should not be
	 * processed any further. Otherwise, true is returned.
	 */
	@Override
	protected boolean process(Context context) {
		if ( getFilterPatterns() != null ) {
			Object root = getRootExpression()==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
			for ( Map.Entry<SimpleExpression, Pattern> filterPattern : getFilterPatterns().entrySet() ) {
				String expressionValue = SpringExpressionUtil.evaluateExpression(root, filterPattern.getKey(), String.class);
				if ( expressionValue==null ) { expressionValue=""; }
				if ( !filterPattern.getValue().matcher(expressionValue).matches() ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the configured root expression. May be null.
	 * @return
	 */
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	/**
	 * Configure the optional root expression.
	 * @param rootExpression
	 */
	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	/**
	 * Get the configured map of filter patterns.
	 * @return
	 */
	public Map<SimpleExpression, Pattern> getFilterPatterns() {
		return filterPatterns;
	}

	/**
	 * Configure the map of filter patterns.
	 * @param filterPatterns
	 */
	public void setFilterPatterns(Map<SimpleExpression, Pattern> filterPatterns) {
		this.filterPatterns = filterPatterns;
	}
}
