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
package com.fortify.processrunner.common.bugtracker.issue;

import java.util.LinkedHashMap;

import com.fortify.api.util.spring.expression.TemplateExpression;

/** 
 * This class describes the bug tracker fields and their contents to be submitted
 * or updated by FortifyBugTrackerUtility.
 *  
 * @author Ruud Senden
 */
public class BugTrackerFieldConfiguration {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	private String[] fieldsToUpdateDuringStateManagement;
	
	public LinkedHashMap<String, TemplateExpression> getFields() {
		return fields;
	}
	public void setFields(LinkedHashMap<String, TemplateExpression> fields) {
		this.fields = fields;
	}
	public LinkedHashMap<String, TemplateExpression> getAppendedFields() {
		return appendedFields;
	}
	public void setAppendedFields(LinkedHashMap<String, TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}
	public String[] getFieldsToUpdateDuringStateManagement() {
		return fieldsToUpdateDuringStateManagement;
	}
	public void setFieldsToUpdateDuringStateManagement(String[] fieldsToUpdateDuringStateManagement) {
		this.fieldsToUpdateDuringStateManagement = fieldsToUpdateDuringStateManagement;
	}
	
	
}
