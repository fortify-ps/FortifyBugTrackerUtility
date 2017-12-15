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
package com.fortify.processrunner.fod.processor.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.common.bugtracker.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.common.processor.IProcessorUpdateState;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnState;
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
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	@Override
	protected IProcessor createFoDProcessorRetrieveAndProcessVulnerabilities() {
		FoDProcessorRetrieveVulnerabilities result = new FoDProcessorRetrieveVulnerabilities(
			getConfiguration().getEnrichersForVulnerabilitiesAlreadySubmitted(),
			getConfiguration().getFiltersForVulnerabilitiesAlreadySubmitted(),
			getUpdateIssueStateProcessor()
		);
		result.setIncludeFixed(true);
		result.setIncludeSuppressed(true);
		result.setSearchString(getConfiguration().getFullFoDFilterStringForVulnerabilitiesAlreadySubmitted());
		result.setPurpose("updating state");
		return result;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(FoDProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugIdExpression(SpringExpressionUtil.parseSimpleExpression("bugId"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugLink"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	
	public boolean isEnabled() {
		return getUpdateIssueStateProcessor() != null;
	}

	public String getBugTrackerName() {
		return getUpdateIssueStateProcessor() == null ? null : getUpdateIssueStateProcessor().getBugTrackerName();
	}
}
