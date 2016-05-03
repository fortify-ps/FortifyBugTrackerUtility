package com.fortify.processrunner.fod.processor.composite;

import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilitiesGrouped;
import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilitiesNonGrouped;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

/**
 * <p>This abstract {@link IProcessor} implementation combines various processors
 * for retrieving and filtering FoD vulnerability data, building bug tracker 
 * issue data, and actually submitting the issue data to an arbitrary bug tracker.
 * Concrete implementations must implement the {@link #getSubmitVulnerabilityProcessor()}
 * method to return an {@link IProcessor} implementation that can actually submit
 * an issue to a bug tracker.</p> 
 */
public abstract class AbstractFoDProcessorSubmitVulnerabilities extends AbstractCompositeProcessor {
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	private final FoDProcessorBuildIssueObjectMap issue = new FoDProcessorBuildIssueObjectMap();
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
		if ( getIssue().getGrouping()==null ) {
			return new ProcessorPrintMessage(null, "Submitted vulnerability ${FoDCurrentVulnerability.vulnId} to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}\n", null);
		} else {
			return new ProcessorPrintMessage(null, "Submitted ${CurrentGroup.size()} vulnerabilities to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}\n", null);
		}
	}
	
	protected IProcessor createAddCommentToVulnerabilitiesProcessor() {
		if ( getIssue().getGrouping()==null ) {
			return new FoDProcessorAddCommentToVulnerabilitiesNonGrouped();
		} else {
			return new FoDProcessorAddCommentToVulnerabilitiesGrouped();
		}
	}
	
	protected abstract IProcessor getSubmitVulnerabilityProcessor();


	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}

	public FoDProcessorBuildIssueObjectMap getIssue() {
		return issue;
	}
}
