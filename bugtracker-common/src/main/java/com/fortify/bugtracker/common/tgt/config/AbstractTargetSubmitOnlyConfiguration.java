/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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

import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * Configuration class for {@link AbstractTargetProcessorSubmitIssues}.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractTargetSubmitOnlyConfiguration extends AbstractTargetBasicIssueFieldsConfiguration implements ITargetSubmitIssuesConfiguration {
	private TemplateExpression groupTemplateExpression;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}
	public void setGroupTemplateExpression(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}
	public LinkedHashMap<String, TemplateExpression> getAppendedFields() {
		return appendedFields;
	}
	public void setAppendedFields(LinkedHashMap<String, TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}
	
	@Override
	public TemplateExpression getGroupTemplateExpressionForSubmit() {
		return getGroupTemplateExpression();
	}
	@Override
	public LinkedHashMap<String, TemplateExpression> getFieldsForSubmit() {
		return getFields();
	}
	@Override
	public LinkedHashMap<String, TemplateExpression> getAppendedFieldsForSubmit() {
		return getAppendedFields();
	}
	
	
}
