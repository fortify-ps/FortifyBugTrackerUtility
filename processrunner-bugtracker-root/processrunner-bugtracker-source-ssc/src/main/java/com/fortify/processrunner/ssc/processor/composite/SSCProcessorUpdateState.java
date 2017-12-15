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
package com.fortify.processrunner.ssc.processor.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.processrunner.common.bugtracker.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.common.processor.IProcessorUpdateState;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;

/**
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
public class SSCProcessorUpdateState extends AbstractSSCVulnerabilityProcessor implements IProcessorUpdateState {
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	@Override
	protected IProcessor createSSCProcessorRetrieveAndProcessVulnerabilities() {
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			getConfiguration().getEnrichersForVulnerabilitiesAlreadySubmitted(),
			getConfiguration().getFiltersForVulnerabilitiesAlreadySubmitted(),
			getUpdateIssueStateProcessor()
		);
		result.getIssueSearchOptions().setIncludeHidden(false);
		result.getIssueSearchOptions().setIncludeRemoved(true);
		result.getIssueSearchOptions().setIncludeSuppressed(true);
		result.setSearchString(getConfiguration().getFullSSCFilterStringForVulnerabilitiesAlreadySubmitted());
		result.setPurpose("updating state");
		return result;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugURL}"));
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(SSCProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugURL"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	
	public boolean isEnabled() {
		return getUpdateIssueStateProcessor() != null;
	}

	public String getBugTrackerName() {
		return getUpdateIssueStateProcessor() == null ? null : getUpdateIssueStateProcessor().getBugTrackerName();
	}
}
