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
package com.fortify.bugtracker.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.bugtracker.common.src.processor.ISourceProcessorSubmitVulnsToTarget;
import com.fortify.bugtracker.common.src.processor.ISourceProcessorUpdateVulnsOnTarget;
import com.fortify.processrunner.ProcessRunner;

/**
 * This {@link ProcessRunner} implementation can be configured to
 * submit vulnerabilities to a bug tracker, update issue state,
 * or both. Depending on available processors in the Spring configuration,
 * this implementation will determine whether the current instance
 * is enabled and whether it should be the default process runner:
 * <ul>
 *  <li>Only {@link ISourceProcessorSubmitVulnsToTarget} available: Submit ProcessRunner enabled and default runner, others disabled</li>
 *  <li>Only {@link ISourceProcessorUpdateVulnsOnTarget} available: Update ProcessRunner enabled and default runner, others disabled</li>
 *  <li>Both {@link ISourceProcessorSubmitVulnsToTarget} and {@link ISourceProcessorUpdateVulnsOnTarget} available: Submit, Update and Submit&Update ProcessRunners enabled, Submit&Update is default ProcessRunner</li>
 * </ul>
 * 
 * @author Ruud Senden
 *
 */
public class BugTrackerProcessRunner extends ProcessRunner {
	private final String sourceSystemName;
	private final ProcessRunnerType type;
	protected ISourceProcessorSubmitVulnsToTarget submitVulnerabilitiesProcessor;
	protected ISourceProcessorUpdateVulnsOnTarget updateStateProcessor;

	public enum ProcessRunnerType {
		SUBMIT, UPDATE, SUBMIT_AND_UPDATE
	}

	public BugTrackerProcessRunner(String sourceSystemName, ProcessRunnerType type) {
		this.sourceSystemName = sourceSystemName;
		this.type = type;
	}

	@PostConstruct
	public void postConstruct() {
		switch (type) {
		case SUBMIT:
			this.setProcessors(getSubmitVulnerabilitiesProcessor());
			this.setEnabled(isSubmitVulnerabilitiesProcessorEnabled());
			this.setDescription("Submit "+sourceSystemName+" vulnerabilities to "+getBugTrackerName());
			this.setDefault(isSubmitVulnerabilitiesProcessorEnabled() && !isUpdateBugTrackerStateProcessorEnabled());
			break;
		case SUBMIT_AND_UPDATE:
			this.setProcessors(getUpdateStateProcessor(), getSubmitVulnerabilitiesProcessor());
			this.setEnabled(isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			this.setDescription("Submit "+sourceSystemName+" vulnerabilities to "+getBugTrackerName()+" and update issue state");
			this.setDefault(isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			break;
		case UPDATE:
			this.setProcessors(getUpdateStateProcessor());
			this.setEnabled(isUpdateBugTrackerStateProcessorEnabled());
			this.setDescription("Update "+getBugTrackerName()+" issue state based on "+sourceSystemName+" vulnerability state");
			this.setDefault(!isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			break;
		}
	}

	private final boolean isSubmitVulnerabilitiesProcessorEnabled() {
		return getSubmitVulnerabilitiesProcessor()!=null && getSubmitVulnerabilitiesProcessor().isEnabled();
	}

	private final boolean isUpdateBugTrackerStateProcessorEnabled() {
		return getUpdateStateProcessor()!=null && getUpdateStateProcessor().isEnabled();
	}

	public ISourceProcessorSubmitVulnsToTarget getSubmitVulnerabilitiesProcessor() {
		return submitVulnerabilitiesProcessor;
	}

	@Autowired(required=false)
	public void setSubmitVulnerabilitiesProcessor(ISourceProcessorSubmitVulnsToTarget submitVulnerabilitiesProcessor) {
		this.submitVulnerabilitiesProcessor = submitVulnerabilitiesProcessor;
	}

	public ISourceProcessorUpdateVulnsOnTarget getUpdateStateProcessor() {
		return updateStateProcessor;
	}

	@Autowired(required=false)
	public void setUpdateStateProcessor(ISourceProcessorUpdateVulnsOnTarget updateStateProcessor) {
		this.updateStateProcessor = updateStateProcessor;
	}

	protected final String getBugTrackerName() {
		if ( getSubmitVulnerabilitiesProcessor()!=null ) {
			return getSubmitVulnerabilitiesProcessor().getTargetName();
		}
		if ( getUpdateStateProcessor()!=null ) {
			return getUpdateStateProcessor().getTargetName();
		}
		throw new IllegalStateException("Cannot determine bug tracker name");
	}

}
