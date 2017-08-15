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
package com.fortify.processrunner.fod.processor.enrich;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssueCommentHelper;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.ondemand.IOnDemandPropertyLoader;
import com.fortify.util.json.JSONMap;

public class FoDProcessorEnrichWithOnDemandBugLinkFromComment extends AbstractFoDProcessorEnrich {
	@Override
	protected boolean enrich(Context context, JSONMap vuln) {
		vuln.put("bugLink", new IOnDemandPropertyLoader<String>() {
			private static final long serialVersionUID = 1L;
			public String getValue(Context ctx, Map<?, ?> targetMap) {
				String matchExpression = ContextSpringExpressionUtil.evaluateTemplateExpression(ctx, ctx, "--- Vulnerability submitted to ${BugTrackerName}.*", String.class);
				String spel = "CurrentVulnerability.summary.comments.$[comment matches '"+matchExpression+"']?.comment";
				String bugComment = ContextSpringExpressionUtil.evaluateExpression(ctx, ctx, spel, String.class);
				if ( StringUtils.isNotBlank(bugComment) ) {
					return SubmittedIssueCommentHelper.getSubmittedIssueFromComment(bugComment).getDeepLink();
				} else {
					return null;
				}
			}
		});
		return true;
	}

}
