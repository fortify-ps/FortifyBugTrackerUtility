/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.bugtracker.common.ssc.appversion.json.preprocessor.filter;

import com.fortify.client.ssc.json.preprocessor.filter.SSCJSONMapFilterApplicationVersionHasValuesForAllAttributes;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterListenerLogger.LogLevel;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This extension of {@link SSCJSONMapFilterApplicationVersionHasValuesForAllAttributes} adds
 * information logging about excluded applications versions.
 * 
 * @author Ruud Senden
 *
 */
public class SSCJSONMapFilterWithLoggerApplicationVersionSpEL extends JSONMapFilterSpEL {
	public SSCJSONMapFilterWithLoggerApplicationVersionSpEL(MatchMode matchMode, SimpleExpression expression) {
		super(matchMode, expression);
		addFilterListeners(new SSCJSONMapFilterListenerLoggerApplicationVersion(LogLevel.INFO,
				null,
				"${textObjectDoesOrDoesnt} match expression "+expression.getExpressionString()));
	}
}
