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
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnBugURL;
import com.fortify.processrunner.ssc.vulnerability.SSCVulnerabilityUpdater;

/**
 * TODO Update class comment
 */
@Component
public class SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker extends AbstractSSCProcessorRetrieveFilteredVulnerabilities {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private IProcessorSubmitIssueForVulnerabilities submitIssueProcessor;
	private SSCVulnerabilityUpdater vulnerabilityUpdater;
	
	/**
	 * This method calls {@link SSCVulnerabilityUpdater#checkContext(Context)} to check the
	 * current application version configuration
	 */
	@Override
	protected boolean preProcess(Context context) {
		return (vulnerabilityUpdater==null || vulnerabilityUpdater.checkContext(context)) && super.preProcess(context);
	}
	
	@Override
	protected CompositeProcessor createTopLevelFieldFilters() {
		CompositeProcessor result = super.createTopLevelFieldFilters();
		if ( submitIssueProcessor.isIgnorePreviouslySubmittedIssues() ) {
			if ( getVulnerabilityUpdater()!=null ) {
				result.getProcessors().add(getVulnerabilityUpdater().createVulnerabilityNotYetSubmittedFilter());
			} else {
				result.getProcessors().add(new SSCFilterOnBugURL(false));
			}
		}
		return result;
	}
	
	@Override
	protected IProcessor getVulnerabilityProcessor() {
		return new CompositeProcessor(getVulnState(), getSubmitIssueProcessor());
	}

	public IProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return submitIssueProcessor;
	}

	@Autowired
	public void setSubmitIssueProcessor(IProcessorSubmitIssueForVulnerabilities submitIssueProcessor) {
		this.submitIssueProcessor = submitIssueProcessor;
	}
	
	public SSCVulnerabilityUpdater getVulnerabilityUpdater() {
		return vulnerabilityUpdater;
	}

	@Autowired(required=false)
	public void setVulnerabilityUpdater(SSCVulnerabilityUpdater vulnerabilityUpdater) {
		this.vulnerabilityUpdater = vulnerabilityUpdater;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	@Autowired(required=false)
	public void setConfiguration(SSCBugTrackerProcessorConfiguration config) {
		setAllFieldRegExFilters(config.getAllFieldRegExFilters());
		setIncludeIssueDetails(config.isIncludeIssueDetails());
		setTopLevelFieldRegExFilters(config.getTopLevelFieldRegExFilters());
		setTopLevelFieldSimpleFilters(config.getTopLevelFieldSimpleFilters());
		getVulnState().setIsVulnerabilityOpenExpression(config.getIsVulnerabilityOpenExpression());
	}
}
