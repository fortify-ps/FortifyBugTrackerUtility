/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
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
package com.fortify.bugtracker.common.src.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.bugtracker.common.src.context.AbstractSourceContextGenerator;
import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * Configuration class for {@link AbstractSourceContextGenerator}.
 * 
 * @author Ruud Senden
 *
 */
public class AbstractSourceContextGeneratorConfiguration implements ISourceContextGeneratorConfiguration {
	private LinkedHashMap<String, String> extraData = new LinkedHashMap<>();
	private SimpleExpression filterExpression = null;
	private LinkedHashMap<SimpleExpression, Context> expressionToContextMap = new LinkedHashMap<>();
	private Map<String, String> attributeMappings = null;
	private LinkedHashMap<Pattern, Context> namePatternToContextMap = null;

	@Override
	public SimpleExpression getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(SimpleExpression filterExpression) {
		this.filterExpression = filterExpression;
	}

	@Override
	public LinkedHashMap<SimpleExpression, Context> getExpressionToContextMap() {
		return expressionToContextMap;
	}

	public void setExpressionToContextMap(LinkedHashMap<SimpleExpression, Context> expressionToContextMap) {
		this.expressionToContextMap = expressionToContextMap;
	}

	@Override
	public LinkedHashMap<String, String> getExtraData() {
		return extraData;
	}

	public void setExtraData(LinkedHashMap<String, String> extraData) {
		this.extraData = extraData;
	}
	
	@Override
	public Map<String, String> getAttributeMappings() {
		return attributeMappings;
	}

	public void setAttributeMappings(Map<String, String> attributeMappings) {
		this.attributeMappings = attributeMappings;
	}

	@Override
	public LinkedHashMap<Pattern, Context> getNamePatternToContextMap() {
		return namePatternToContextMap;
	}

	public void setNamePatternToContextMap(LinkedHashMap<Pattern, Context> namePatternToContextMap) {
		this.namePatternToContextMap = namePatternToContextMap;
	}
}
