package com.fortify.processrunner.common.issue;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

/**
 * This helper class allows for generating and parsing comments with submitted issue information.
 */
public class SubmittedIssueCommentHelper {
	private static final String FMT_COMMENT_STR = "--- Vulnerability submitted to {0}:{1} Location {2}";
	private static final MessageFormat FMT_COMMENT = new MessageFormat(FMT_COMMENT_STR);
	
	private SubmittedIssueCommentHelper() {}
	
	public static final String getCommentForSubmittedIssue(String bugTrackerName, SubmittedIssue submittedIssue) {
		StringBuffer sb = new StringBuffer("--- Vulnerability submitted to ").append(bugTrackerName). append(": ");
		if ( submittedIssue.getId()!=null ) {
			sb.append("ID ").append(submittedIssue.getId()).append(" ");
		}
		sb.append("Location ").append(submittedIssue.getDeepLink());
		return sb.toString();
	}
	
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
