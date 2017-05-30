package com.fortify.processrunner.ssc.processrunner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.ProcessRunner;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.ssc.processor.composite.SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker;
import com.fortify.processrunner.ssc.processor.composite.SSCProcessorUpdateBugTrackerState;

public class SSCBugTrackerProcessRunner extends ProcessRunner {
	private final ProcessRunnerType type;
	
	public enum ProcessRunnerType {
		SUBMIT, UPDATE, SUBMIT_AND_UPDATE
	}
	
	public SSCBugTrackerProcessRunner(ProcessRunnerType type) {
		this.type = type;
	}
	
	private SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker submitVulnerabilitiesProcessor;
	private SSCProcessorUpdateBugTrackerState updateBugTrackerStateProcessor;
	
	@PostConstruct
	public void postConstruct() {
		switch (type) {
		case SUBMIT:
			super.setProcessor(getSubmitVulnerabilitiesProcessor());
			super.setEnabled(isSubmitVulnerabilitiesProcessorEnabled());
			super.setDescription("Submit SSC vulnerabilities to "+getBugTrackerName());
			super.setDefault(isSubmitVulnerabilitiesProcessorEnabled() && !isUpdateBugTrackerStateProcessorEnabled());
			break;
		case SUBMIT_AND_UPDATE:
			super.setProcessor(new CompositeProcessor(getSubmitVulnerabilitiesProcessor(), getUpdateBugTrackerStateProcessor()));
			super.setEnabled(isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			super.setDescription("Submit SSC vulnerabilities to "+getBugTrackerName()+" and update issue state");
			super.setDefault(isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			break;
		case UPDATE:
			super.setProcessor(getUpdateBugTrackerStateProcessor());
			super.setEnabled(isUpdateBugTrackerStateProcessorEnabled());
			super.setDescription("Update "+getBugTrackerName()+" issue state based on SSC vulnerability state");
			super.setDefault(!isSubmitVulnerabilitiesProcessorEnabled() && isUpdateBugTrackerStateProcessorEnabled());
			break;
		}
	}
	

	@Autowired
	public final void setProcessors(SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker submitVulnerabilitiesProcessor, SSCProcessorUpdateBugTrackerState updateBugTrackerStateProcessor) {
		this.submitVulnerabilitiesProcessor = submitVulnerabilitiesProcessor;
		this.updateBugTrackerStateProcessor = updateBugTrackerStateProcessor;
		
		CompositeProcessor processor = new CompositeProcessor();
		String description = "";
		if ( submitVulnerabilitiesProcessor.getSubmitIssueProcessor()!=null ) {
			processor.getProcessors().add(submitVulnerabilitiesProcessor);
			description += "Submit vulnerabilities from SSC to "+submitVulnerabilitiesProcessor.getSubmitIssueProcessor().getBugTrackerName();
		}
		if ( updateBugTrackerStateProcessor.getUpdateIssueStateProcessor()!=null ) {
			processor.getProcessors().add(updateBugTrackerStateProcessor);
			description += "\nand update "+submitVulnerabilitiesProcessor.getSubmitIssueProcessor().getBugTrackerName()+" issue state";
		}
		super.setProcessor(processor);
		super.setDescription(description);
	}
	
	protected final SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker getSubmitVulnerabilitiesProcessor() {
		return submitVulnerabilitiesProcessor;
	}
	
	protected final SSCProcessorUpdateBugTrackerState getUpdateBugTrackerStateProcessor() {
		return updateBugTrackerStateProcessor;
	}
	
	protected final boolean isSubmitVulnerabilitiesProcessorEnabled() {
		return getSubmitIssueProcessor()!=null;
	}

	protected AbstractProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return getSubmitVulnerabilitiesProcessor().getSubmitIssueProcessor();
	}
	
	protected final boolean isUpdateBugTrackerStateProcessorEnabled() {
		return getUpdateIssueStateProcessor()!=null;
	}

	protected AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return getUpdateBugTrackerStateProcessor().getUpdateIssueStateProcessor();
	}
	
	protected final String getBugTrackerName() {
		if ( getSubmitIssueProcessor()!=null ) {
			return getSubmitIssueProcessor().getBugTrackerName();
		}
		if ( getUpdateIssueStateProcessor()!=null ) {
			return getUpdateIssueStateProcessor().getBugTrackerName();
		}
		throw new IllegalStateException("Cannot determine bug tracker name");
	}
	
	@Override
	@Resource(name="contextProperties")
	public void setContext(Context context) {
		super.setContext(context);
	}
}
