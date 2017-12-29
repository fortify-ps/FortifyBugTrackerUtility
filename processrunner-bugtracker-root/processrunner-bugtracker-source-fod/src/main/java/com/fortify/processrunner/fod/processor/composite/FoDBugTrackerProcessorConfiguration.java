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
package com.fortify.processrunner.fod.processor.composite;

import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This class holds all FoD-related configuration properties used to submit vulnerabilities
 * to a bug tracker or other external system, and performing issue state management.
 * 
 * @author Ruud Senden
 *
 */
public class FoDBugTrackerProcessorConfiguration {
	private static final SimpleExpression DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION =
			SpringExpressionUtil.parseSimpleExpression("closedStatus==false && isSuppressed==false && status!=4");
	private String filterStringForVulnerabilitiesToBeSubmitted = null;
	private Map<String,Pattern> regExFiltersForVulnerabilitiesToBeSubmitted = null;
	private boolean addBugDataAsComment = false;
	private boolean addNativeBugLink = false;
	private SimpleExpression isVulnerabilityOpenExpression = DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	
	public String getFilterStringForVulnerabilitiesToBeSubmitted() {
		return filterStringForVulnerabilitiesToBeSubmitted;
	}
	public void setFilterStringForVulnerabilitiesToBeSubmitted(String filterStringForVulnerabilitiesToBeSubmitted) {
		this.filterStringForVulnerabilitiesToBeSubmitted = filterStringForVulnerabilitiesToBeSubmitted;
	}
	public Map<String, Pattern> getRegExFiltersForVulnerabilitiesToBeSubmitted() {
		return regExFiltersForVulnerabilitiesToBeSubmitted;
	}
	public void setRegExFiltersForVulnerabilitiesToBeSubmitted(Map<String, Pattern> regExFiltersForVulnerabilitiesToBeSubmitted) {
		this.regExFiltersForVulnerabilitiesToBeSubmitted = regExFiltersForVulnerabilitiesToBeSubmitted;
	}
	public boolean isAddBugDataAsComment() {
		return addBugDataAsComment;
	}
	public void setAddBugDataAsComment(boolean addBugDataAsComment) {
		this.addBugDataAsComment = addBugDataAsComment;
	}
	public boolean isAddNativeBugLink() {
		return addNativeBugLink;
	}
	public void setAddNativeBugLink(boolean addNativeBugLink) {
		this.addNativeBugLink = addNativeBugLink;
	}
	public SimpleExpression getIsVulnerabilityOpenExpression() {
		return isVulnerabilityOpenExpression;
	}
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.isVulnerabilityOpenExpression = isVulnerabilityOpenExpression;
	}
}
