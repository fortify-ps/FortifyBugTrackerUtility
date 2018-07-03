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
package com.fortify.bugtracker.common.src.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.bugtracker.common.src.config.ISourceContextGeneratorConfiguration;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;
import com.fortify.util.rest.query.IRestConnectionQuery;
import com.fortify.util.spring.SpringExpressionUtil;

public abstract class AbstractSourceContextGenerator implements IContextGenerator {
	private LinkedHashMap<String, String> extraData = null;
	private LinkedHashMap<String, Context> expressionToContextMap = null;
	
	protected abstract AbstractRestConnectionQueryBuilder<?, ?> createBaseQueryBuilder(Context context, boolean hasFilterExpressions);
	protected abstract Context updateContextFromJSONMap(Context newContext, JSONMap json);
	
	@Override
	public Collection<Context> generateContexts(Context initialContext) {
		List<Context> result = new ArrayList<>();
		JSONList queryResult = getQuery(initialContext).getAll();
		if ( !CollectionUtils.isEmpty(queryResult) ) {
			for ( JSONMap json : queryResult.asValueType(JSONMap.class) ) {
				addContextsFromJSONMap(initialContext, json, result);
			}
		}
		return result;
	}

	private void addContextsFromJSONMap(Context initialContext, JSONMap json, List<Context> result) {
		for ( Map.Entry<String, Context> entry : getExpressionToContextMap().entrySet() ) {
			String exprString = entry.getKey();
			Context newContext = updateContextFromJSONMap(new Context(initialContext), json);
			// TODO Log info message with information why json was not matched
			if ( StringUtils.isBlank(exprString) || ContextSpringExpressionUtil.evaluateExpression(newContext, json, exprString, Boolean.class) ) {
				addMappedContext(newContext, json, entry.getValue());
				result.add(newContext);
			}
		}
	}

	private void addMappedContext(Context context, JSONMap json, Context mappedContext) {
		if ( mappedContext != null ) {
			for ( Entry<String, Object> entry : mappedContext.entrySet() ) {
				String ctxPropertyName = entry.getKey();
				Object ctxPropertyValue = SpringExpressionUtil.evaluateTemplateExpression(json, (String)entry.getValue(), Object.class);
				if ( isNotBlankString(ctxPropertyValue) ) {
					context.put(ctxPropertyName, ctxPropertyValue);
				}
			}
		}
	}

	/**
	 * @param value to be checked for null or blank
	 * @return false if value is null, or if value is a string that is blank, true otherwise
	 */
	private boolean isNotBlankString(Object value) {
		return value!=null && (!(value instanceof String) || StringUtils.isNotBlank((String)value));
	}

	private final IRestConnectionQuery getQuery(Context context) {
		AbstractRestConnectionQueryBuilder<?,?> queryBuilder = createBaseQueryBuilder(context, MapUtils.isNotEmpty(getExpressionToContextMap()));
		addOnDemandData(queryBuilder);
		return queryBuilder.build();
	}
	
	private final void addOnDemandData(AbstractRestConnectionQueryBuilder<?,?> queryBuilder) {
		// TODO Remove code duplication with SourceVulnerabilityProcessorHelper
		Map<String, String> extraData = getExtraData();
		if ( extraData != null ) {
			for ( Map.Entry<String, String> entry : extraData.entrySet() ) {
				String propertyName = entry.getKey();
				String uriString = StringUtils.substringBeforeLast(entry.getValue(), ";");
				// TODO Parse properly as properties, to allow additional properties if ever necessary
				boolean useCache = "useCache=true".equals(StringUtils.substringAfterLast(entry.getValue(), ";"));
				queryBuilder.onDemand(propertyName, uriString, useCache?propertyName:null);
			}
		}
	}

	public LinkedHashMap<String, String> getExtraData() {
		return extraData;
	}

	public void setExtraData(LinkedHashMap<String, String> extraData) {
		this.extraData = extraData;
	}

	public LinkedHashMap<String, Context> getExpressionToContextMap() {
		return expressionToContextMap;
	}

	public void setExpressionToContextMap(LinkedHashMap<String, Context> expressionToContextMap) {
		this.expressionToContextMap = expressionToContextMap;
	}
	
	@Autowired(required=false)
	public void setConfiguration(ISourceContextGeneratorConfiguration config) {
		setExtraData(config.getExtraData());
		setExpressionToContextMap(config.getExpressionToContextMap());
	}
}
