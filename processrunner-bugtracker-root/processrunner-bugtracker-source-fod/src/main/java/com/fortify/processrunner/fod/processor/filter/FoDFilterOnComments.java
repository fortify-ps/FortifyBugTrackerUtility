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
package com.fortify.processrunner.fod.processor.filter;

import java.util.regex.Pattern;

import org.springframework.expression.Expression;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.json.JSONList;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link IProcessor} implementation will filter vulnerabilities based on
 * FoD comments. Depending on the excludeVulnerabilityWithMatchingComment flag, 
 * vulnerabilities for which a comment matches the configured filterPattern will 
 * either be excluded from further processing (flag set to true) or included for
 * further processing (flag set to false, default).</p>
 * 
 * <p>In addition, the first comment matching the configured filterPattern will be
 * added to the context property 'FoDCommentMatchedByFilter', accessible via 
 * {@link IContextMatchingComment#getFoDCommentMatchedByFilter()}.</p>
 */
public class FoDFilterOnComments extends AbstractProcessor {
	private static final Expression EXPR_COMMENTS = SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability.summary.comments");
	private TemplateExpression filterPatternTemplateExpression = null;
	private boolean excludeVulnerabilityWithMatchingComment = false;
	
	@Override
	protected boolean process(Context context) {
		String filterPatternString = SpringExpressionUtil.evaluateExpression(context, getFilterPatternTemplateExpression(), String.class);
		Pattern filterPattern = Pattern.compile(filterPatternString);
		JSONList comments = SpringExpressionUtil.evaluateExpression(context, EXPR_COMMENTS, JSONList.class);
		if ( comments != null ) {
			for ( String comment : comments.getValues("comment", String.class) ) {
				if ( filterPattern.matcher(comment).matches() ) {
					context.as(IContextMatchingComment.class).setFoDCommentMatchedByFilter(comment);
					return !isExcludeVulnerabilityWithMatchingComment();
				}
			}
		}
		return isExcludeVulnerabilityWithMatchingComment();
	}

	public boolean isExcludeVulnerabilityWithMatchingComment() {
		return excludeVulnerabilityWithMatchingComment;
	}

	public void setExcludeVulnerabilityWithMatchingComment(boolean excludeVulnerabilityWithMatchingComment) {
		this.excludeVulnerabilityWithMatchingComment = excludeVulnerabilityWithMatchingComment;
	}

	public TemplateExpression getFilterPatternTemplateExpression() {
		return filterPatternTemplateExpression;
	}

	public void setFilterPatternTemplateExpression(TemplateExpression filterPatternTemplateExpression) {
		this.filterPatternTemplateExpression = filterPatternTemplateExpression;
	}
	
	public void setFilterPatternTemplateExpression(String filterPatternTemplateExpression) {
		this.filterPatternTemplateExpression = SpringExpressionUtil.parseTemplateExpression(filterPatternTemplateExpression);
	}
	
	public interface IContextMatchingComment {
		public void setFoDCommentMatchedByFilter(String comment);
		public String getFoDCommentMatchedByFilter();
	}
	
}
