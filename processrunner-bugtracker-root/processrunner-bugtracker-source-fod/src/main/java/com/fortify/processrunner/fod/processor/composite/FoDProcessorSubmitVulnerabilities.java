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

import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.common.processor.IProcessorSubmitVulnerabilities;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.IProcessor;

/**
 * <p>This {@link IProcessor} implementation combines and configures 
 * {@link FoDProcessorRetrieveVulnerabilities}, {@link FoDBugTrackerProcessorConfiguration} 
 * and {@link IProcessorSubmitIssueForVulnerabilities} (provided by the bug tracker 
 * implementation) to allow for submitting FoD vulnerabilities to bug trackers or
 * other external systems.</p> 
 * 
 * <p>This combined configuration will retrieve all open FoD vulnerabilities based on
 * configured search/filtering criteria, optionally group the vulnerabilities based on
 * a configurable grouping expression (if supported by the bug tracker implementation),
 * and then submit the grouped vulnerabilities to the bug tracker or other external system.
 * Optionally, the bug tracker issue link can be stored in FoD for each submitted 
 * vulnerability, for state management purposes (see {@link FoDProcessorUpdateState}) and
 * to allow the user to navigate back and forth between FoD and bug tracker.</p> 
 * 
 * @author Ruud Senden
 */
@Component
public class FoDProcessorSubmitVulnerabilities extends AbstractFoDVulnerabilityProcessor implements IProcessorSubmitVulnerabilities {
	private IProcessorSubmitIssueForVulnerabilities submitIssueProcessor;
	
	@Override
	protected IProcessor createFoDProcessorRetrieveAndProcessVulnerabilities() {
		IProcessorSubmitIssueForVulnerabilities submitIssueProcessor = getSubmitIssueProcessor();
		FoDProcessorRetrieveVulnerabilities result = new FoDProcessorRetrieveVulnerabilities(
			getConfiguration().getEnrichersForVulnerabilitiesToBeSubmitted(),
			getConfiguration().getFiltersForVulnerabilitiesToBeSubmitted(submitIssueProcessor.isIgnorePreviouslySubmittedIssues()),
			submitIssueProcessor
		);
		result.setIncludeFixed(false);
		result.setIncludeSuppressed(false);
		result.setSearchString(getConfiguration().getFullFoDFilterStringForVulnerabilitiesToBeSubmitted(submitIssueProcessor.isIgnorePreviouslySubmittedIssues()));
		return result;
	}

	public IProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return submitIssueProcessor;
	}

	@Autowired
	public void setSubmitIssueProcessor(IProcessorSubmitIssueForVulnerabilities submitIssueProcessor) {
		this.submitIssueProcessor = submitIssueProcessor;
	}

	public boolean isEnabled() {
		return getSubmitIssueProcessor() != null;
	}

	public String getBugTrackerName() {
		return getSubmitIssueProcessor() == null ? null : getSubmitIssueProcessor().getBugTrackerName();
	}
	
	
}
