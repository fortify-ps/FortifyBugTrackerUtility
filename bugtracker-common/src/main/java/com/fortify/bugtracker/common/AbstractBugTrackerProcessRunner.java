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
package com.fortify.bugtracker.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.src.processor.ISourceProcessorSubmitVulnsToTarget;
import com.fortify.bugtracker.common.src.processor.ISourceProcessorUpdateVulnsOnTarget;
import com.fortify.processrunner.AbstractProcessRunner;
import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;

/**
 * <p>This {@link AbstractProcessRunner} implementation can be configured to
 * submit vulnerabilities to a bug tracker, update issue state,
 * or both. Depending on available processors in the Spring configuration,
 * this implementation will determine the available actions and default action:</p>
 * <ul>
 *  <li>Only {@link ISourceProcessorSubmitVulnsToTarget} available: submitVulnerabilities action enabled and default, others disabled</li>
 *  <li>Only {@link ISourceProcessorUpdateVulnsOnTarget} available: updateIssueState action enabled and default, others disabled</li>
 *  <li>Both {@link ISourceProcessorSubmitVulnsToTarget} and {@link ISourceProcessorUpdateVulnsOnTarget} available: submitVulnerabilities, updateIssueState and submitVulnerabilitiesAndUpdateIssueState actions enabled, submitVulnerabilitiesAndUpdateIssueState is default action</li>
 * </ul>
 * 
 * <p>Source system implementations must extend this class, providing the source system name
 * in the constructor, and annotated with the {@link Component} annotation.</p>
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractBugTrackerProcessRunner extends AbstractProcessRunner {
	private final CLIOptionDefinition cliOptionDefinitionAction = new CLIOptionDefinition("processing", "Action", "Action to be performed", true).extraInfo("Used for", "Submit", "Update");
	private ISourceProcessorSubmitVulnsToTarget submitVulnerabilitiesProcessor;
	private ISourceProcessorUpdateVulnsOnTarget updateStateProcessor;
	private IProcessor submitVulnerabilitiesAndUpdateIssueStateProcessor;
	
	
	@Override
	public void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		cliOptionDefinitions.add(cliOptionDefinitionAction); 
		cliOptionDefinitions
				.addAll("updateIssueState", getUpdateStateProcessor())
				.addAll("submitVulnerabilities", getSubmitVulnerabilitiesProcessor());
		cliOptionDefinitions.getCLIOptionDefinitionsBySource("submitVulnerabilities").forEach(def->def.addExtraInfo("Used for", "Submit"));
		cliOptionDefinitions.getCLIOptionDefinitionsBySource("updateIssueState").forEach(def->def.addExtraInfo("Used for", "Update"));
	}

	@PostConstruct
	public void postConstruct() {
		if ( isUpdateBugTrackerStateProcessorEnabled() ) {
			cliOptionDefinitionAction.allowedValue("updateIssueState", "Update "+getTargetName()+" issue state based on "+getSourceName()+" vulnerability state");
			cliOptionDefinitionAction.defaultValue("updateIssueState");
		}
		if ( isSubmitVulnerabilitiesProcessorEnabled() ) {
			cliOptionDefinitionAction.allowedValue("submitVulnerabilities", "Submit "+getSourceName()+" vulnerabilities to "+getTargetName());
			cliOptionDefinitionAction.defaultValue("submitVulnerabilities");
		}
		if ( isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled() ) {
			cliOptionDefinitionAction.allowedValue("submitVulnerabilitiesAndUpdateIssueState", "Submit "+getSourceName()+" vulnerabilities to "+getTargetName()+" and update issue state");
			cliOptionDefinitionAction.defaultValue("submitVulnerabilitiesAndUpdateIssueState");
		}
	}

	@Override
	public IProcessor getProcessor(Context context) {
		switch ( cliOptionDefinitionAction.getValue(context) ) {
		case "updateIssueState": 
			return getUpdateStateProcessor();
		case "submitVulnerabilities": 
			return getSubmitVulnerabilitiesProcessor();
		case "submitVulnerabilitiesAndUpdateIssueState": 
			return getSubmitVulnerabilitiesAndUpdateIssueStateProcessor();
		default:
			throw new IllegalArgumentException("Unknown action specified");
		}
	}

	private IProcessor getSubmitVulnerabilitiesAndUpdateIssueStateProcessor() {
		if ( submitVulnerabilitiesAndUpdateIssueStateProcessor==null ) {
			submitVulnerabilitiesAndUpdateIssueStateProcessor = new CompositeProcessor(getUpdateStateProcessor(), getSubmitVulnerabilitiesProcessor());
		}
		return submitVulnerabilitiesAndUpdateIssueStateProcessor;
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

	protected final String getTargetName() {
		if ( getSubmitVulnerabilitiesProcessor()!=null ) {
			return getSubmitVulnerabilitiesProcessor().getTargetName();
		}
		if ( getUpdateStateProcessor()!=null ) {
			return getUpdateStateProcessor().getTargetName();
		}
		throw new IllegalStateException("Cannot determine target name");
	}
	
	protected abstract String getSourceName();

}
