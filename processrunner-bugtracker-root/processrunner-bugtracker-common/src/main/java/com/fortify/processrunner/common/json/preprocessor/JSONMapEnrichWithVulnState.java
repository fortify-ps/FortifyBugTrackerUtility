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
package com.fortify.processrunner.common.json.preprocessor;

import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.json.preprocessor.AbstractJSONMapEnrich;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.api.util.spring.expression.SimpleExpression;
import com.fortify.processrunner.common.bugtracker.issue.IssueState;

/**
 * This abstract implementation of {@link AbstractProcessorEnrichCurrentVulnerability} will evaluate a 
 * configurable expression to determine whether the current vulnerability is considered open or closed. 
 * The vulnerability state will then be added to the current vulnerability as the 'vulnState' property.
 * Concrete implementations must implement the {@link #getDefaultIsVulnerabilityOpenExpression()} method
 * to provide a default value for the configurable expression.
 */
public class JSONMapEnrichWithVulnState extends AbstractJSONMapEnrich {
	public static final String NAME_VULN_STATE = "vulnState";
	private final SimpleExpression isVulnerabilityOpenExpression;
	
	public JSONMapEnrichWithVulnState(SimpleExpression isVulnerabilityOpenExpression) {
		this.isVulnerabilityOpenExpression = isVulnerabilityOpenExpression;
	}
	
	@Override
	protected void enrich(JSONMap json) {
		boolean isOpen = SpringExpressionUtil.evaluateExpression(json, isVulnerabilityOpenExpression, Boolean.class);
		json.put(NAME_VULN_STATE, isOpen?IssueState.OPEN.name():IssueState.CLOSED.name());
	}
}