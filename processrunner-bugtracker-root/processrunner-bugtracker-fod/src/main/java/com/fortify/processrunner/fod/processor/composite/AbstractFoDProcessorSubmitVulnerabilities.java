package com.fortify.processrunner.fod.processor.composite;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

// TODO Current (template) expressions will not work if issues are submitted without grouping
public abstract class AbstractFoDProcessorSubmitVulnerabilities extends AbstractCompositeProcessor {
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	private final FoDProcessorBuildIssueStringMap issue = new FoDProcessorBuildIssueStringMap();
	
	protected void init(IProcessor submitVulnerabilityProcessor) {
		getFod().setVulnerabilityProcessor(getIssue());
		getIssue().setIssueProcessor(new CompositeProcessor(
				submitVulnerabilityProcessor, 
				new ProcessorPrintMessage(null, "Submitted ${CurrentGroup.size()} vulnerabilities to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}\n", null),
				createAddCommentToVulnerabilitiesProcessor()));
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

	@Override
	public IProcessor[] getProcessors() {
		return new IProcessor[]{getFod()};
	}


	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}

	public FoDProcessorBuildIssueStringMap getIssue() {
		return issue;
	}
}
