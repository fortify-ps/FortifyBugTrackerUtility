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

import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;

/**
 * TODO Update class comment
 */
@Component
public class SSCProcessorSubmitVulnerabilities extends AbstractSSCVulnerabilityProcessor {
	private IProcessorSubmitIssueForVulnerabilities submitIssueProcessor;
	
	@Override
	protected IProcessor createSSCProcessorRetrieveAndProcessVulnerabilities() {
		IProcessorSubmitIssueForVulnerabilities submitIssueProcessor = getSubmitIssueProcessor();
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			getConfiguration().getEnrichersForVulnerabilitiesToBeSubmitted(),
			getConfiguration().getFiltersForVulnerabilitiesToBeSubmitted(submitIssueProcessor.isIgnorePreviouslySubmittedIssues()),
			submitIssueProcessor
		);
		result.getIssueSearchOptions().setIncludeHidden(false);
		result.getIssueSearchOptions().setIncludeRemoved(false);
		result.getIssueSearchOptions().setIncludeSuppressed(false);
		result.setSearchString(getConfiguration().getFullSSCFilterStringForVulnerabilitiesToBeSubmitted(submitIssueProcessor.isIgnorePreviouslySubmittedIssues()));
		return result;
	}

	public IProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return submitIssueProcessor;
	}

	@Autowired
	public void setSubmitIssueProcessor(IProcessorSubmitIssueForVulnerabilities submitIssueProcessor) {
		this.submitIssueProcessor = submitIssueProcessor;
	}
	
	
}
