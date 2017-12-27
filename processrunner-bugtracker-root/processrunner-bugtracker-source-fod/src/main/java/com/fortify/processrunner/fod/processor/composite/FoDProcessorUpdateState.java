/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.processrunner.fod.processor.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.api.fod.connection.api.query.builder.FoDReleaseVulnerabilitiesQueryBuilder;
import com.fortify.api.util.rest.json.preprocessor.AbstractJSONMapFilter.MatchMode;
import com.fortify.api.util.rest.query.IRestConnectionQuery;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.common.bugtracker.issue.IssueState;
import com.fortify.processrunner.common.json.preprocessor.JSONMapEnrichWithVulnState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.common.processor.IProcessorUpdateState;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.json.preprocessor.FoDJSONMapFilterHasBugLink;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.IProcessor;

/**
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link FoDProcessorRetrieveVulnerabilities}, {@link FoDBugTrackerProcessorConfiguration} 
 * and {@link AbstractProcessorUpdateIssueStateForVulnerabilities} (provided by the bug 
 * tracker implementation) to allow for updating bug tracker issue state based on FoD 
 * vulnerability state, and vice versa.</p> 
 * 
 * <p>This combined configuration will retrieve all FoD vulnerabilities (both open and closed) 
 * that have been previously submitted to the bug tracker, group them by external bug link/id, 
 * and then allow the bug tracker implementation to update the bug tracker issue with updated 
 * vulnerability state, like updating issue fields and automatically re-opening or closing the 
 * bug tracker issue based on corresponding vulnerability states. Generic functionality for 
 * updating FoD vulnerability state based on bug tracker issue state is provided by 
 * {@link FoDBugTrackerProcessorConfiguration}, but has not yet been implemented for FoD. 
 * 
 * @author Ruud Senden
 */
@Component
public class FoDProcessorUpdateState extends AbstractFoDVulnerabilityProcessor implements IProcessorUpdateState {
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> vulnerabilityProcessor;
	
	public IRestConnectionQuery getVulnerabilityQuery(Context context) {
		FoDReleaseVulnerabilitiesQueryBuilder builder = createVulnerabilityBaseQueryBuilder(context)
				.paramIncludeFixed(true)
				.paramIncludeSuppressed(true)
				.preProcessor(new FoDJSONMapFilterHasBugLink(MatchMode.INCLUDE));
		if ( getConfiguration().isAddNativeBugLink() ) {
			builder.paramFilterAnd("bugSubmitted","true");
		}
		if ( getConfiguration().isAddBugDataAsComment() ) {
			builder.paramFilterAnd("hasComments","true");
		}
		return builder.build();
	}
	
	@Override
	protected String getPurpose() {
		return "updating state";
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setVulnerabilityProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> vulnerabilityProcessor) {
		vulnerabilityProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		vulnerabilityProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(JSONMapEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		vulnerabilityProcessor.setVulnBugIdExpression(SpringExpressionUtil.parseSimpleExpression("bugId"));
		vulnerabilityProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugLink"));
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}
}
