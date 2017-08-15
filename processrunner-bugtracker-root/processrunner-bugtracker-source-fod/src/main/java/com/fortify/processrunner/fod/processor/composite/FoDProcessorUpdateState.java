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

import com.fortify.processrunner.common.bugtracker.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnState;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on SSC vulnerability state. It will retrieve all SSC vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 * 
 * @author Ruud Senden
 */
@Component
public class FoDProcessorUpdateState extends AbstractFoDVulnerabilityProcessor {
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	@Override
	protected IProcessor createFoDProcessorRetrieveAndProcessVulnerabilities() {
		FoDProcessorRetrieveVulnerabilities result = new FoDProcessorRetrieveVulnerabilities(
			getConfiguration().getEnrichersForVulnerabilitiesAlreadySubmitted(),
			getConfiguration().getFiltersForVulnerabilitiesAlreadySubmitted(),
			getUpdateIssueStateProcessor()
		);
		result.setIncludeRemoved(true);
		result.setSearchString(getConfiguration().getFullFoDFilterStringForVulnerabilitiesAlreadySubmitted());
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
}
