package com.fortify.processrunner.fod.util;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.common.SubmittedIssue;

/**
 * This helper class allows for generating and parsing FoD comments with submitted issue information.
 *
 */
public class FoDSubmittedIssueCommentHelper {
	public static final String COMMENT_TEMPLATE_EXPRESSION = "--- Vulnerability submitted to ${BugTrackerName}: ${SubmittedIssue.id==null?'':'ID '+SubmittedIssue.id} Location ${SubmittedIssue.deepLink}";
	private static final String FMT_COMMENT_STR = "--- Vulnerability submitted to {0}:{1} Location {2}";
	private static final MessageFormat FMT_COMMENT = new MessageFormat(FMT_COMMENT_STR);
	
	private FoDSubmittedIssueCommentHelper() {}
	
	public static final SubmittedIssue getSubmittedIssueFromComment(String comment) {
		try {
			Object[] fields = FMT_COMMENT.parse(comment);
			String id = StringUtils.removeStart(StringUtils.trim((String)fields[1]), "ID ");
			String deepLink = StringUtils.trim((String)fields[2]);
			return new SubmittedIssue(id, deepLink);
		} catch (Exception e) {
			throw new RuntimeException("Error parsing comment "+comment);
		}
	}
}
