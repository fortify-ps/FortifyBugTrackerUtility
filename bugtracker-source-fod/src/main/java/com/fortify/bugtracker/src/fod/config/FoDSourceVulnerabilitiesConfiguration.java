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
package com.fortify.bugtracker.src.fod.config;

import org.apache.commons.lang.StringUtils;

import com.fortify.bugtracker.common.src.config.AbstractSourceVulnerabilitiesConfiguration;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorCommentHelper;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class holds all FoD-related configuration properties used to submit vulnerabilities
 * to a bug tracker or other external system, and performing issue state management.
 * 
 * @author Ruud Senden
 *
 */
public class FoDSourceVulnerabilitiesConfiguration extends AbstractSourceVulnerabilitiesConfiguration {
	private static final SimpleExpression DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION =
			SpringExpressionUtil.parseSimpleExpression("closedStatus==false && isSuppressed==false && status!=4");
	private String filterStringForVulnerabilitiesToBeSubmitted = null;
	private boolean addBugDataAsComment = false;
	private String commentTargetName = null;
	private TemplateExpression commentTemplateExpression = null;
	private boolean addNativeBugLink = false;
	
	@Override
	protected SimpleExpression getDefaultIsVulnerabilityOpenExpression() {
		return DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	}
	
	public String getFilterStringForVulnerabilitiesToBeSubmitted() {
		return filterStringForVulnerabilitiesToBeSubmitted;
	}
	public void setFilterStringForVulnerabilitiesToBeSubmitted(String filterStringForVulnerabilitiesToBeSubmitted) {
		this.filterStringForVulnerabilitiesToBeSubmitted = filterStringForVulnerabilitiesToBeSubmitted;
	}
	public boolean isAddBugDataAsComment() {
		return addBugDataAsComment;
	}
	public void setAddBugDataAsComment(boolean addBugDataAsComment) {
		this.addBugDataAsComment = addBugDataAsComment;
	}
	public String getCommentTargetName() {
		return commentTargetName;
	}
	public void setCommentTargetName(String commentTargetName) {
		this.commentTargetName = commentTargetName;
	}
	public TemplateExpression getCommentTemplateExpression() {
		return commentTemplateExpression;
	}
	public void setCommentTemplateExpression(TemplateExpression commentTemplateExpression) {
		this.commentTemplateExpression = commentTemplateExpression;
	}
	public boolean isAddNativeBugLink() {
		return addNativeBugLink;
	}
	public void setAddNativeBugLink(boolean addNativeBugLink) {
		this.addNativeBugLink = addNativeBugLink;
	}
	
	public TargetIssueLocatorCommentHelper getTargetIssueLocatorCommentHelper(String defaultTargetName) {
		if ( commentTemplateExpression != null ) {
			return TargetIssueLocatorCommentHelper.fromTemplateExpression(commentTemplateExpression);
		} else if ( StringUtils.isNotBlank(commentTargetName) ) {
			return TargetIssueLocatorCommentHelper.fromTargetName(commentTargetName);
		} else {
			return TargetIssueLocatorCommentHelper.fromTargetName(defaultTargetName);
		}
	}
}
