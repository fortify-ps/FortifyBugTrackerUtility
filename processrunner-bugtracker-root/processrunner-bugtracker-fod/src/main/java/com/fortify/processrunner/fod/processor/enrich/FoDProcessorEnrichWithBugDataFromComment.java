package com.fortify.processrunner.fod.processor.enrich;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnComments;
import com.fortify.processrunner.fod.util.FoDSubmittedIssueCommentHelper;
import com.fortify.processrunner.processor.AbstractProcessor;

public class FoDProcessorEnrichWithBugDataFromComment extends AbstractProcessor {
	@Override
	protected boolean process(Context context) {
		String bugComment = context.as(FoDFilterOnComments.IContextMatchingComment.class).getFoDCommentMatchedByFilter();
		if ( StringUtils.isBlank(bugComment) ) {
			throw new RuntimeException(this.getClass().getCanonicalName()+" can only be used after a vulnerability has been matched by "+FoDFilterOnComments.class.getName());
		}
		SubmittedIssue submittedIssue = FoDSubmittedIssueCommentHelper.getSubmittedIssueFromComment(bugComment);
		JSONObject vuln = context.as(IContextFoD.class).getFoDCurrentVulnerability();
		try {
			vuln.putOpt("bugId", submittedIssue.getId());
			vuln.putOpt("bugLink", submittedIssue.getDeepLink());
		} catch (JSONException e) {
			throw new RuntimeException("Error enriching vulnerability data with submitted bug information", e);
		}
		return true;
	}

}
