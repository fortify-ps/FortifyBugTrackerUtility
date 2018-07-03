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
package com.fortify.bugtracker.src.ssc.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.fortify.processrunner.context.Context;

public class SSCSourceApplicationVersionsConfigurationTest {

	@Test
	public void testGetExpressionToContextMap() {
		SSCSourceApplicationVersionsConfiguration config = new SSCSourceApplicationVersionsConfiguration();
		config.setExpressionToContextMap(getExpressionToContextMap());
		config.setOptionalAttributeMappings(getOptionalAttributeMappings());
		config.setRequiredAttributeMappings(getRequiredAttributeMappings());
		config.setNamePatternToContextMap(getNamePatternToContextMap());
		config.postConstruct();
		// TODO Check expressionToContextMap contents, instead of just printing it
		System.out.println(config.getExpressionToContextMap());
	}

	private LinkedHashMap<String, Context> getNamePatternToContextMap() {
		LinkedHashMap<String, Context> result = new LinkedHashMap<>();
		result.put(".*", new Context().chainedPut("ContextAttrFromNamePattern", "ContextAttrFromNamePatternValue"));
		return result;
	}

	private Map<String, String> getRequiredAttributeMappings() {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		result.put("SSCRequiredAttrName","ContextAttrNameForReqAttr");
		return result;
	}

	private Map<String, String> getOptionalAttributeMappings() {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		result.put("SSCOptionalAttrName","ContextAttrForOptAttr");
		return result;
	}

	private LinkedHashMap<String, Context> getExpressionToContextMap() {
		LinkedHashMap<String, Context> result = new LinkedHashMap<>();
		result.put("", new Context().chainedPut("ContextAttrMatchingEmpty", "ContextAttrMatchingEmptyValue"));
		result.put("true", new Context().chainedPut("ContextAttrMatchingTrue", "ContextAttrMatchingTrueValue"));
		result.put("someAttr=='test'", new Context().chainedPut("ContextAttrMatchingTest", "ContextAttrMatchingTestValue"));
		return result;
	}

}
