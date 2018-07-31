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

import java.util.Collection;
import java.util.HashMap;
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
	private LinkedHashMap<SimpleExpression, Context> expressionToOptionsMap = new LinkedHashMap<>();
	private Map<String, String> attributeMappings = null;
	private LinkedHashMap<Pattern, Context> namePatternToOptionsMap = null;
	private final Map<String, String> mappingDescriptions = new HashMap<String, String>();

	@Override
	public SimpleExpression getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(SimpleExpression filterExpression) {
		this.filterExpression = filterExpression;
	}

	@Override
	public LinkedHashMap<SimpleExpression, Context> getExpressionToOptionsMap() {
		return expressionToOptionsMap;
	}

	public void setExpressionToOptionsMap(LinkedHashMap<SimpleExpression, Context> expressionToOptionsMap) {
		this.expressionToOptionsMap = expressionToOptionsMap;
		updateMappingDescriptions(expressionToOptionsMap.values(), "Mapped from expressionToContextMap");
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
		for ( Map.Entry<String, String> entry : attributeMappings.entrySet() ) {
			mappingDescriptions.put(entry.getValue(), "Mapped from attribute '"+entry.getKey()+"'");
		}
	}

	@Override
	public LinkedHashMap<Pattern, Context> getNamePatternToOptionsMap() {
		return namePatternToOptionsMap;
	}

	public void setNamePatternToOptionsMap(LinkedHashMap<Pattern, Context> namePatternToOptionsMap) {
		this.namePatternToOptionsMap = namePatternToOptionsMap;
		updateMappingDescriptions(namePatternToOptionsMap.values(), "Mapped from namePatternToContextMap");
	}
	
	private void updateMappingDescriptions(Collection<Context> contexts, String description) {
		for ( Context context : contexts ) {
			for ( String key : context.keySet() ) {
				mappingDescriptions.put(key, description);
			}
		}
	}
	
	public Map<String, String> getMappingDescriptions() {
		return mappingDescriptions;
	}
	
	
}
