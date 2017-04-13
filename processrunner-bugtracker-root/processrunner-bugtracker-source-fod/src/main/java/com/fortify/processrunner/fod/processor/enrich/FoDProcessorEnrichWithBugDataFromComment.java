package com.fortify.processrunner.fod.processor.enrich;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.issue.SubmittedIssueCommentHelper;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnComments;

public class FoDProcessorEnrichWithBugDataFromComment extends AbstractFoDProcessorEnrich {
	@Override
	protected boolean enrich(Context context, JSONObject vuln) throws JSONException {
		String bugComment = context.as(FoDFilterOnComments.IContextMatchingComment.class).getFoDCommentMatchedByFilter();
		if ( StringUtils.isBlank(bugComment) ) {
			throw new RuntimeException(this.getClass().getCanonicalName()+" can only be used after a vulnerability has been matched by "+FoDFilterOnComments.class.getName());
		}
		SubmittedIssue submittedIssue = SubmittedIssueCommentHelper.getSubmittedIssueFromComment(bugComment);
		vuln.putOpt("bugId", submittedIssue.getId());
		vuln.putOpt("bugLink", submittedIssue.getDeepLink());
		return true;
	}

}
