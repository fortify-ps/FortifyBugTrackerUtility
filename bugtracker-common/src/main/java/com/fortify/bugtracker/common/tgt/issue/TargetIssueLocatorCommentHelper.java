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
package com.fortify.bugtracker.common.tgt.issue;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.regex.Pattern;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This helper class allows for generating and parsing comments with information about submitted issues.
 * As this may be used in serializable on-demand objects, this class is {@link Serializable}.
 * 
 * @author Ruud Senden
 */
public class TargetIssueLocatorCommentHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String templateExpressionString;
	private final String matchPatternString;
	private final String parserFormatString;
	
	private TargetIssueLocatorCommentHelper(String templateExpressionString) {
		this.templateExpressionString = templateExpressionString;
		this.matchPatternString = createMatchPatternString(templateExpressionString);
		this.parserFormatString = createParserFormatString(templateExpressionString);
	}
	
	public static final TargetIssueLocatorCommentHelper fromTargetName(String targetName) {
		return new TargetIssueLocatorCommentHelper(createTemplateExpressionStringForTargetName(targetName));
	}
	
	public static final TargetIssueLocatorCommentHelper fromTemplateExpression(TemplateExpression templateExpression) {
		return new TargetIssueLocatorCommentHelper(templateExpression.getExpressionString());
	}
	
	private static final String createTemplateExpressionStringForTargetName(String targetName) {
		return "--- Vulnerability submitted to "+targetName+": ID ${id} Location ${deepLink}";
	}
	
	private String createMatchPatternString(String templateExpressionString) {
		return "\\Q"+
				templateExpressionString.replaceAll(Pattern.quote("${id}"), "\\\\E.*\\\\Q")
		           .replaceAll(Pattern.quote("${deepLink}"), "\\\\E.*\\\\Q")
				+"\\E";
	}
	
	private String createParserFormatString(String templateExpressionString) {
		return templateExpressionString
				.replaceAll("'", "''") // Quote any single quotes
				.replaceAll(Pattern.quote("${id}"), "::0::")
				.replaceAll(Pattern.quote("${deepLink}"), "::1::")
				.replaceAll(Pattern.quote("{"), "'{'")
				.replaceAll(Pattern.quote("}"), "'}'")
				.replaceAll(Pattern.quote("::0::"), "{0}")
				.replaceAll(Pattern.quote("::1::"), "{1}"); 
	}

	
	/**
	 * Get a comment string that describes the given {@link TargetIssueLocator} using the configured template expression
	 * @param targetIssueLocator
	 * @return
	 */
	public final String getCommentForSubmittedIssue(TargetIssueLocator targetIssueLocator) {
		return SpringExpressionUtil.evaluateExpression(targetIssueLocator, getTemplateExpression(), String.class);
	}
	
	/**
	 * Parse a {@link TargetIssueLocator} from the given comment string that was previously generated using
	 * {@link #getCommentForSubmittedIssue(TargetIssueLocator)}
	 * @param comment
	 * @return
	 */
	public final TargetIssueLocator getTargetIssueLocatorFromComment(String comment) {
		try {
			Object[] fields = getParser().parse(comment);
			String id = fields.length<1 ? null : (String)fields[0];
			String deepLink = fields.length<2 ? null : (String)fields[1];
			return new TargetIssueLocator(id, deepLink);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing comment "+comment, e);
		}
	}

	// TODO Cache in transient variable
	public final TemplateExpression getTemplateExpression() {
		return SpringExpressionUtil.parseTemplateExpression(templateExpressionString);
	}

	// TODO Cache in transient variable
	public final Pattern getMatchPattern() {
		return Pattern.compile(matchPatternString);
	}

	// TODO Cache in transient variable
	public final MessageFormat getParser() {
		return new MessageFormat(parserFormatString);
	}
}
