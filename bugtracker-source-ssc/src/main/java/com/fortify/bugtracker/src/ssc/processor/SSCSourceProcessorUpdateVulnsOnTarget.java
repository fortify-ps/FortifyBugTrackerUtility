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
package com.fortify.bugtracker.src.ssc.processor;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.processor.ISourceProcessorUpdateVulnsOnTarget;
import com.fortify.bugtracker.common.src.updater.IExistingIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.ssc.cli.ICLIOptionsSSC;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.helper.SSCHelperFactory;
import com.fortify.bugtracker.common.ssc.json.preprocessor.enrich.SSCJSONMapEnrichWithRevisionFromDetails;
import com.fortify.bugtracker.common.ssc.json.preprocessor.filter.SSCJSONMapFilterHasBugURL;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorUpdateIssues;
import com.fortify.bugtracker.src.ssc.config.SSCSourceVulnerabilitiesConfiguration;
import com.fortify.client.ssc.api.SSCCustomTagAPI;
import com.fortify.client.ssc.api.json.embed.SSCEmbedConfig;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.applier.ifblank.IfBlank;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;

/**
 * TODO Update JavaDoc?
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link SSCSourceProcessorRetrieveVulnerabilities}, {@link SSCSourceVulnerabilitiesConfiguration} 
 * and {@link AbstractTargetProcessorUpdateIssues} (provided by the bug 
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
public class SSCSourceProcessorUpdateVulnsOnTarget extends AbstractSSCSourceVulnerabilityProcessor implements ISourceProcessorUpdateVulnsOnTarget, IExistingIssueVulnerabilityUpdater {
	private static final Log LOG = LogFactory.getLog(SSCSourceProcessorSubmitVulnsToTarget.class);
	
	@Override
	protected SourceVulnerabilityProcessorHelper getSourceVulnerabilityProcessorHelper() {
		return new SSCSourceVulnerabilityProcessorHelperUpdate();
	}
	
	private class SSCSourceVulnerabilityProcessorHelperUpdate extends SourceVulnerabilityProcessorHelperUpdate {
		@Override
		public AbstractRestConnectionQueryBuilder<?, ?> createBaseVulnerabilityQueryBuilder(Context context) {
			SSCApplicationVersionIssuesQueryBuilder builder = createSSCVulnerabilityBaseQueryBuilder(context)
					.paramQm(IfBlank.ERROR(), QueryMode.issues)
					// For updating state we want to include all vulnerabilities, including hidden, removed and suppressed
					.paramShowHidden(true)
					.paramShowRemoved(true)
					.paramShowSuppressed(true)
					.paramQ(IfBlank.SKIP(), getBugLinkCustomTagQuery())
					.preProcessor(new SSCJSONMapFilterHasBugURL(MatchMode.INCLUDE));
			if ( getConfiguration().isEnableRevisionWorkAround() ) {
				builder.preProcessor(new SSCJSONMapEnrichWithRevisionFromDetails());
			}
			return builder;
		}

		private String getBugLinkCustomTagQuery() {
			String bugLinkCustomTagName = getConfiguration().getBugLinkCustomTagName();
			return StringUtils.isBlank(bugLinkCustomTagName) ? null : ("["+bugLinkCustomTagName+"]:!<none>");
		}
		
		@Override
		protected void addOnDemandProperty(AbstractRestConnectionQueryBuilder<?, ?> queryBuilder, String propertyName, String uriString) {
			queryBuilder.embed(SSCEmbedConfig.builder().propertyName(propertyName).uri(uriString).build());
		}
	}

	public void updateVulnerabilityStateForExistingIssue(Context context, String bugTrackerName, TargetIssueLocatorAndFields targetIssueLocatorAndFields, Collection<Object> vulnerabilities) {
		Map<String,String> customTagValues = getExtraCustomTagValues(context, targetIssueLocatorAndFields, vulnerabilities);
		if ( !customTagValues.isEmpty() ) {
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ICLIOptionsSSC.CLI_SSC_APPLICATION_VERSION_ID.getValue(context);
			conn.api(SSCCustomTagAPI.class).updateCustomTags(applicationVersionId)
				.withCustomTagHelper(SSCHelperFactory.getSSCCustomTagHelper(context))
				.byName(customTagValues)
				.forVulnerabilities(vulnerabilities)
				.execute();
			LOG.info("[SSC] Updated custom tag values for "+vulnerabilities.size()+" SSC vulnerabilities");
		}
	}
}
