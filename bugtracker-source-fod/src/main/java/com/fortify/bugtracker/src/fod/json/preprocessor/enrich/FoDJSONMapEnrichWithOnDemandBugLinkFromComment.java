/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.bugtracker.src.fod.json.preprocessor.enrich;

import org.apache.commons.lang.StringUtils;

import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorCommentHelper;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoader;
import com.fortify.util.rest.json.preprocessor.enrich.AbstractJSONMapEnrich;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link AbstractJSONMapEnrich} implementation adds an on-demand bugLink property
 * to the current vulnerability, which retrieves the bug link from the vulnerability 
 * comments (if available). We use an on-demand loader because the actual comments are 
 * loaded on-demand as well.
 * 
 * @author Ruud Senden
 *
 */
public class FoDJSONMapEnrichWithOnDemandBugLinkFromComment extends AbstractJSONMapEnrich {
	private final String targetName;
	public FoDJSONMapEnrichWithOnDemandBugLinkFromComment(String targetName) {
		this.targetName = targetName;
	}
	
	@Override
	protected void enrich(JSONMap json) {
		json.put("bugLink", new FoDJSONMapOnDemandLoaderBugLinkFromComment(targetName));
	}
	
	private static class FoDJSONMapOnDemandLoaderBugLinkFromComment extends AbstractJSONMapOnDemandLoader {
		private static final long serialVersionUID = 1L;
		private final String matchExpression;
		
		public FoDJSONMapOnDemandLoaderBugLinkFromComment(String targetName) {
			super(true);
			this.matchExpression = "--- Vulnerability submitted to "+targetName+":.*";
		}

		@Override
		public Object getOnDemand(String propertyName, JSONMap parent) {
			String spel = "summary?.comments?.$[comment matches '"+matchExpression+"']?.comment";
			String bugComment = SpringExpressionUtil.evaluateExpression(parent, spel, String.class);
			if ( StringUtils.isNotBlank(bugComment) ) {
				return TargetIssueLocatorCommentHelper.getSubmittedIssueFromComment(bugComment).getDeepLink();
			} else {
				return null;
			}
		}
	}

}
