package com.fortify.processrunner.fod.processor.filter;

import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.expression.Expression;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
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
	private static final Expression EXPR_COMMENTS = SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability.summary.comments");
	private static final Expression EXPR_COMMENT = SpringExpressionUtil.parseSimpleExpression("comment");
	private TemplateExpression filterPatternTemplateExpression = null;
	private boolean excludeVulnerabilityWithMatchingComment = false;
	
	@Override
	protected boolean process(Context context) {
		String filterPatternString = SpringExpressionUtil.evaluateExpression(context, getFilterPatternTemplateExpression(), String.class);
		Pattern filterPattern = Pattern.compile(filterPatternString);
		JSONArray array = SpringExpressionUtil.evaluateExpression(context, EXPR_COMMENTS, JSONArray.class);
		if ( array != null ) {
			for ( int i = 0 ; i < array.length() ; i++ ) {
				String comment = SpringExpressionUtil.evaluateExpression(array.opt(i), EXPR_COMMENT, String.class);
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
