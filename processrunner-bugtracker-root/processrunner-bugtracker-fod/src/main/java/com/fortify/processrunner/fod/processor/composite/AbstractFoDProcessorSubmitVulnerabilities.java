package com.fortify.processrunner.fod.processor.composite;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

public abstract class AbstractFoDProcessorSubmitVulnerabilities extends AbstractCompositeProcessor {
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	private final FoDProcessorBuildIssueStringMap issue = new FoDProcessorBuildIssueStringMap();
	private FoDProcessorRetrieveFilteredVulnerabilities initializedProcessor;

	@Override
	public IProcessor[] getProcessors() {
		if ( initializedProcessor == null ) {
			initializedProcessor = getFod();
			initializedProcessor.setVulnerabilityProcessor(getIssue());
			getIssue().setIssueProcessor(new CompositeProcessor(
				getSubmitVulnerabilityProcessor(), 
				createPrintMessageProcessor(),
				createAddCommentToVulnerabilitiesProcessor()));
		}
		return new IProcessor[]{initializedProcessor};
	}

	private ProcessorPrintMessage createPrintMessageProcessor() {
		if ( StringUtils.isBlank(getIssue().getGrouping()) ) {
			return new ProcessorPrintMessage(null, "Submitted vulnerability ${FoDCurrentVulnerability.vulnId} to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}\n", null);
		} else {
			return new ProcessorPrintMessage(null, "Submitted ${CurrentGroup.size()} vulnerabilities to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}\n", null);
		}
	}
	
	protected IProcessor createAddCommentToVulnerabilitiesProcessor() {
		FoDProcessorAddCommentToVulnerabilities result = new FoDProcessorAddCommentToVulnerabilities();
		if ( StringUtils.isBlank(getIssue().getGrouping()) ) {
			result.setRootExpression("FoDCurrentVulnerability");
			result.setVulnIdExpression("vulnId");
		} else {
			result.setIterableExpression("CurrentGroup");
			result.setVulnIdExpression("vulnId");
		}
		result.setCommentTemplate("--- Vulnerability submitted to ${SubmittedIssueBugTrackerName}: ID ${SubmittedIssueId} URL ${SubmittedIssueBrowserURL}");
		return result;
	}
	
	protected abstract IProcessor getSubmitVulnerabilityProcessor();


	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}

	public FoDProcessorBuildIssueStringMap getIssue() {
		return issue;
	}
}
