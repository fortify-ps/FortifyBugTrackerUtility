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
package com.fortify.bugtracker.src.ssc.processor.composite;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.processor.IProcessorUpdateState;
import com.fortify.bugtracker.common.src.updater.IExistingIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.context.IContextSSCCommon;
import com.fortify.bugtracker.common.trgt.issue.IIssueStateDetailsRetriever;
import com.fortify.bugtracker.common.trgt.issue.SubmittedIssue;
import com.fortify.bugtracker.common.trgt.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.bugtracker.src.ssc.json.preprocessor.enrich.SSCJSONMapEnrichWithRevisionFromDetails;
import com.fortify.bugtracker.src.ssc.json.preprocessor.filter.SSCJSONMapFilterHasBugURL;
import com.fortify.bugtracker.src.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;
import com.fortify.client.ssc.api.SSCCustomTagAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;

/**
 * TODO Update JavaDoc?
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link SSCProcessorRetrieveVulnerabilities}, {@link SSCBugTrackerProcessorConfiguration} 
 * and {@link AbstractProcessorUpdateIssueStateForVulnerabilities} (provided by the bug 
 * tracker implementation) to allow for updating bug tracker issue state based on SSC 
 * vulnerability state, and vice versa.</p> 
 * 
 * <p>This combined configuration will retrieve all SSC vulnerabilities (both open and closed) 
 * that have been previously submitted to the bug tracker, group them by external bug link/id, 
 * and then allow the bug tracker implementation to update the bug tracker issue with updated 
 * vulnerability state, like updating issue fields and automatically re-opening or closing the 
 * bug tracker issue based on corresponding vulnerability states. Based on bug tracker issue 
 * state, SSC vulnerability state may be updated as well, for example by setting custom tags 
 * showing current bug tracker issue state. 
 * 
 * @author Ruud Senden
 */
@Component
public class SSCProcessorUpdateState extends AbstractSSCVulnerabilityProcessor implements IProcessorUpdateState, IExistingIssueVulnerabilityUpdater {
	private static final Log LOG = LogFactory.getLog(SSCProcessorSubmitVulnerabilities.class);
	
	@Override
	protected SourceVulnerabilityProcessorHelper getSourceVulnerabilityProcessorHelper() {
		return new SSCSourceVulnerabilityProcessorHelperUpdate();
	}
	
	private class SSCSourceVulnerabilityProcessorHelperUpdate extends SourceVulnerabilityProcessorHelperUpdate {
		@Override
		public AbstractRestConnectionQueryBuilder<?, ?> createBaseVulnerabilityQueryBuilder(Context context) {
			SSCApplicationVersionIssuesQueryBuilder builder = createSSCVulnerabilityBaseQueryBuilder(context)
					.paramQm(QueryMode.issues)
					.includeHidden(false)
					.includeRemoved(true)
					.includeSuppressed(true);
			if ( StringUtils.isNotBlank(getConfiguration().getBugLinkCustomTagName()) ) {
				builder.paramFilter(getConfiguration().getBugLinkCustomTagName()+":!<none>");
			}
			builder.preProcessor(new SSCJSONMapFilterHasBugURL(MatchMode.INCLUDE));
			if ( getConfiguration().isEnableRevisionWorkAround() ) {
				builder.preProcessor(new SSCJSONMapEnrichWithRevisionFromDetails());
			}
			return builder;
		}
	}

	public void updateVulnerabilityStateForExistingIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		Map<String,String> customTagValues = getExtraCustomTagValues(context, submittedIssue, issueStateDetailsRetriever, vulnerabilities);
		if ( !customTagValues.isEmpty() ) {
			IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ctx.getSSCApplicationVersionId();
			conn.api(SSCCustomTagAPI.class).setCustomTagValues(applicationVersionId, customTagValues, vulnerabilities);
			LOG.info("[SSC] Updated custom tag values for "+vulnerabilities.size()+" SSC vulnerabilities");
		}
	}
}
