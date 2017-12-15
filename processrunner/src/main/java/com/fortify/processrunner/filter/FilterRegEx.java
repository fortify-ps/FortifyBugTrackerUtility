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

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.api.util.rest.json.preprocessor.JSONMapFilterRegEx;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.api.util.spring.expression.SimpleExpression;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.processor.IProcessor;

/**
 * <p>This {@link IProcessor} implementation can be configured with a field expression and
 * corresponding regular expression pattern. The configured field expression will be
 * evaluated on the current root object, and matched against the configured pattern.</p>
 * 
 * <p>If you want to match multiple fields against multiple patterns, you can use the 
 * {@link #createFromMap(String, Map, boolean)} method to generate multiple instances of
 * this class based on a {@link Map} of field expressions and corresponding {@link Pattern}
 * instances.</p> 
 * 
 * TODO Replace with {@link JSONMapFilterRegEx}?
 * 
 * @author Ruud Senden
 */
public class FilterRegEx extends AbstractFilteringProcessor {
	private final SimpleExpression fieldExpression;
	private final Pattern matchPattern;

	/**
	 * Constructor for configuring all relevant properties
	 * @param rootExpression
	 * @param fieldExpression
	 * @param matchPattern
	 * @param excludeMatched
	 */
	public FilterRegEx(String rootExpression, String fieldExpression, Pattern matchPattern, boolean excludeMatched) {
		this(rootExpression, SpringExpressionUtil.parseSimpleExpression(fieldExpression), matchPattern, excludeMatched);
	}
	
	/**
	 * Constructor for configuring all relevant properties
	 * @param rootExpression
	 * @param fieldExpression
	 * @param matchPattern
	 * @param excludeMatched
	 */
	public FilterRegEx(String rootExpression, SimpleExpression fieldExpression, Pattern matchPattern, boolean excludeMatched) {
		super(SpringExpressionUtil.parseSimpleExpression(rootExpression), excludeMatched);
		this.fieldExpression = fieldExpression;
		this.matchPattern = matchPattern;
	}
	
	/**
	 * Evaluate the configured field expression on the given root object, and match the resulting value against
	 * the configured regular expression {@link Pattern}.
	 */
	@Override
	protected boolean isMatching(Context context, Object rootObject) {
		String expressionValue = ContextSpringExpressionUtil.evaluateExpression(context, rootObject, getFieldExpression(), String.class);
		if ( expressionValue==null ) { expressionValue=""; }
		return getMatchPattern().matcher(expressionValue).matches();
	}

	public SimpleExpression getFieldExpression() {
		return fieldExpression;
	}

	public Pattern getMatchPattern() {
		return matchPattern;
	}
	
	/**
	 * Create multiple {@link FilterRegEx} instances from a {@link Map} containing field expressions
	 * and corresponding {@link Pattern} instances.
	 * @param rootExpression
	 * @param patternMap
	 * @param excludeMatched
	 * @return
	 */
	public static final FilterRegEx[] createFromMap(String rootExpression, Map<SimpleExpression, Pattern> patternMap, boolean excludeMatched) {
		ArrayList<FilterRegEx> result = new ArrayList<FilterRegEx>(patternMap.size());
		for ( Map.Entry<SimpleExpression, Pattern> entry : patternMap.entrySet() ) {
			result.add(new FilterRegEx(rootExpression, entry.getKey(), entry.getValue(), excludeMatched));
		}
		return result.toArray(new FilterRegEx[]{});
	}
}
