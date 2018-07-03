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
package com.fortify.bugtracker.src.fod.config;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.fortify.bugtracker.common.src.config.AbstractSourceContextGeneratorConfiguration;
import com.fortify.processrunner.context.Context;

public class FoDSourceReleasesConfiguration extends AbstractSourceContextGeneratorConfiguration {
	private Map<String, String> optionalAttributeMappings = null;
	private Map<String, String> requiredAttributeMappings = null;
	private LinkedHashMap<String, Context> namePatternToContextMap = null;
	
	/**
	 * This method updates expressionToContextMap defined in our superclass
	 * with information from {@link #getOptionalAttributeMappings()},
	 * {@link #getRequiredAttributeMappings()} and {@link #getNamePatternToContextMap()}
	 * by generating the corresponding expressions and contexts.
	 */
	@PostConstruct
	public void postConstruct() {
		LinkedHashMap<String, Context> expressionToContextMap = getExpressionToContextMap();
		expressionToContextMap = updateMapWithNamePatternToContextMap(expressionToContextMap);
		expressionToContextMap = updateMapWithAttributeMappings(expressionToContextMap);
		setExpressionToContextMap(expressionToContextMap);
	}

	private LinkedHashMap<String, Context> updateMapWithAttributeMappings(LinkedHashMap<String, Context> expressionToContextMap) {
		LinkedHashMap<String, Context> result = new LinkedHashMap<>();
		Context contextToAdd = getAttributesContext(getOptionalAttributeMappings());
		contextToAdd.putAll(getAttributesContext(getRequiredAttributeMappings()));
		
		if ( MapUtils.isEmpty(expressionToContextMap) ) {
			result.put(appendAttributesExpressionString("", getRequiredAttributeMappings()), contextToAdd);
		} else {
			for ( Map.Entry<String, Context> entry : expressionToContextMap.entrySet() ) {
				String exprWithReqAttrs = appendAttributesExpressionString(entry.getKey(), getRequiredAttributeMappings());
				Context contextWithAttrs = mergeContexts(entry.getValue(), contextToAdd);
				result.put(exprWithReqAttrs, contextWithAttrs);
			}
		}
		
		return result;
	}

	private Context mergeContexts(Context context, Context contextToAdd) {
		Context result = new Context(context);
		result.putAll(contextToAdd);
		return result;
	}

	private LinkedHashMap<String, Context> updateMapWithNamePatternToContextMap(LinkedHashMap<String, Context> expressionToContextMap) {
		LinkedHashMap<String, Context> result = new LinkedHashMap<>(expressionToContextMap);
		if ( MapUtils.isNotEmpty(getNamePatternToContextMap()) ) {
			for ( Map.Entry<String, Context> entry : getNamePatternToContextMap().entrySet() ) {
				result.put(getReleaseNameExpressionForPattern(entry.getKey()), entry.getValue());
			}
		}
		return result;
	}
	
	private String getReleaseNameExpressionForPattern(String pattern) {
		return "applicationName+':'+releaseName matches '"+pattern+"'";
	}

	private String appendAttributesExpressionString(String orgExpr, Map<String, String> attributeMappings) {
		String result = orgExpr;
		if ( MapUtils.isNotEmpty(attributeMappings) ) {
			for ( Map.Entry<String, String> entry : attributeMappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxPropertyName = entry.getValue();
				if ( StringUtils.isNotBlank(result) ) { result += " && "; }
				// TODO can this be simplified?
				result += "(#ctx['"+ctxPropertyName+"']!=null || (applicationWithAttributesMap.attributesMap['"+attrName+"']!=null && applicationWithAttributesMap.attributesMap['"+attrName+"'].size()>0))";
			}
		}
		return result;
	}

	private Context getAttributesContext(Map<String, String> attributeMappings) {
		Context result = new Context();
		if ( MapUtils.isNotEmpty(attributeMappings) ) {
			for ( Map.Entry<String, String> entry : attributeMappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxPropertyName = entry.getValue();
				result.put(ctxPropertyName, "${applicationWithAttributesMap.attributesMap['"+attrName+"']}");
			}
		}
		return result;
	}

	public Map<String, String> getOptionalAttributeMappings() {
		return optionalAttributeMappings;
	}
	public void setOptionalAttributeMappings(Map<String, String> optionalAttributeMappings) {
		this.optionalAttributeMappings = optionalAttributeMappings;
	}
	public Map<String, String> getRequiredAttributeMappings() {
		return requiredAttributeMappings;
	}
	public void setRequiredAttributeMappings(Map<String, String> requiredAttributeMappings) {
		this.requiredAttributeMappings = requiredAttributeMappings;
	}
	public LinkedHashMap<String, Context> getNamePatternToContextMap() {
		return namePatternToContextMap;
	}
	public void setNamePatternToContextMap(LinkedHashMap<String, Context> namePatternToContextMap) {
		this.namePatternToContextMap = namePatternToContextMap;
	}
	
	
}
