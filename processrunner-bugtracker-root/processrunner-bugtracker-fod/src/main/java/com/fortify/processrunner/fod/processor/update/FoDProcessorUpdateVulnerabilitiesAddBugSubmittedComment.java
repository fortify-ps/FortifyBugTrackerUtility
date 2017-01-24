package com.fortify.processrunner.fod.processor.update;

import com.fortify.processrunner.fod.util.FoDSubmittedIssueCommentHelper;

public class FoDProcessorUpdateVulnerabilitiesAddBugSubmittedComment extends FoDProcessorUpdateVulnerabilitiesAddComment {
	public FoDProcessorUpdateVulnerabilitiesAddBugSubmittedComment() {
		setCommentTemplateExpression(FoDSubmittedIssueCommentHelper.COMMENT_TEMPLATE_EXPRESSION);
	}
}
