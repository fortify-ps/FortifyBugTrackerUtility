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
package com.fortify.bugtracker.common.tgt.config;

import java.util.LinkedHashMap;

import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorUpdateIssues;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * Configuration class for {@link AbstractTargetProcessorUpdateIssues}.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractTargetSubmitAndUpdateConfiguration extends AbstractTargetSubmitOnlyConfiguration implements ITargetUpdateIssuesConfiguration {
	private String[] fieldsToUpdateDuringStateManagement;
	private SimpleExpression isIssueOpenableExpression;
	private SimpleExpression isIssueCloseableExpression;
	
	public String[] getFieldsToUpdateDuringStateManagement() {
		return fieldsToUpdateDuringStateManagement;
	}
	public void setFieldsToUpdateDuringStateManagement(String[] fieldsToUpdateDuringStateManagement) {
		this.fieldsToUpdateDuringStateManagement = fieldsToUpdateDuringStateManagement;
	}
	public SimpleExpression getIsIssueOpenableExpression() {
		return isIssueOpenableExpression;
	}
	public void setIsIssueOpenableExpression(SimpleExpression isIssueOpenableExpression) {
		this.isIssueOpenableExpression = isIssueOpenableExpression;
	}
	public SimpleExpression getIsIssueCloseableExpression() {
		return isIssueCloseableExpression;
	}
	public void setIsIssueCloseableExpression(SimpleExpression isIssueCloseableExpression) {
		this.isIssueCloseableExpression = isIssueCloseableExpression;
	}
	@Override
	public LinkedHashMap<String, TemplateExpression> getFieldsForUpdate() {
		return getFilteredMap(getFields(), getFieldsToUpdateDuringStateManagement());
	}
	
	@Override
	public LinkedHashMap<String, TemplateExpression> getAppendedFieldsForUpdate() {
		return getFilteredMap(getAppendedFields(), getFieldsToUpdateDuringStateManagement());
	}
	
	/**
	 * Utility method for filtering an input map based on the given array of keys.
	 * @param inputMap
	 * @param keys
	 * @return
	 */
	private static final LinkedHashMap<String, TemplateExpression> getFilteredMap(LinkedHashMap<String, TemplateExpression> inputMap, String[] keys) {
		if ( keys == null || keys.length < 1 ) { return null; }
		LinkedHashMap<String, TemplateExpression> result = new LinkedHashMap<String, TemplateExpression>(keys.length);
		for ( String key : keys ) {
			result.put(key, inputMap.get(key));
		}
		return result;
	}
	
}
