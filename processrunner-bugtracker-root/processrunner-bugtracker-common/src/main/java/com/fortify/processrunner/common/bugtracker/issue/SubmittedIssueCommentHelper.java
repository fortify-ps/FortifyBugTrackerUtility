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

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

/**
 * This helper class allows for generating and parsing comments with information about submitted issues.
 * 
 * @author Ruud Senden
 */
public class SubmittedIssueCommentHelper {
	private static final String FMT_COMMENT_STR = "--- Vulnerability submitted to {0}:{1} Location {2}";
	private static final MessageFormat FMT_COMMENT = new MessageFormat(FMT_COMMENT_STR);
	
	private SubmittedIssueCommentHelper() {}
	
	/**
	 * Get a comment string that describes the given {@link SubmittedIssue} for the given bug tracker name
	 * @param bugTrackerName
	 * @param submittedIssue
	 * @return
	 */
	public static final String getCommentForSubmittedIssue(String bugTrackerName, SubmittedIssue submittedIssue) {
		StringBuffer sb = new StringBuffer("--- Vulnerability submitted to ").append(bugTrackerName). append(": ");
		if ( submittedIssue.getId()!=null ) {
			sb.append("ID ").append(submittedIssue.getId()).append(" ");
		}
		sb.append("Location ").append(submittedIssue.getDeepLink());
		return sb.toString();
	}
	
	/**
	 * Parse a {@link SubmittedIssue} from the given comment string that was previously generated using
	 * {@link #getCommentForSubmittedIssue(String, SubmittedIssue)}
	 * @param comment
	 * @return
	 */
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
