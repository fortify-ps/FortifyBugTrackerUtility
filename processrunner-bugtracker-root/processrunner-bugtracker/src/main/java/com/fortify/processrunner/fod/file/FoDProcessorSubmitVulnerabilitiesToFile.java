package com.fortify.processrunner.fod.file;

import com.fortify.processrunner.file.FileProcessorSubmitIssue;
import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilities;
import com.fortify.processrunner.fod.processor.composite.FoDProcessorRetrieveFilteredVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;

public class FoDProcessorSubmitVulnerabilitiesToFile extends AbstractCompositeProcessor {
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	private final FileProcessorSubmitIssue file = new FileProcessorSubmitIssue();
	
	public FoDProcessorSubmitVulnerabilitiesToFile() {
		fod.setVulnerabilityProcessor(new CompositeProcessor(file, createAddCommentToVulnerabilitiesProcessor()));
		file.setRootExpression("FoDCurrentVulnerability");
	}
	
	protected IProcessor createAddCommentToVulnerabilitiesProcessor() {
		FoDProcessorAddCommentToVulnerabilities result = new FoDProcessorAddCommentToVulnerabilities();
		result.setRootExpression("FoDCurrentVulnerability");
		result.setVulnIdExpression("vulnId");
		result.setCommentTemplate("--- Vulnerability submitted to ${SubmittedIssueBugTrackerName}");
		return result;
	}

	@Override
	public IProcessor[] getProcessors() {
		return new IProcessor[]{getFod()};
	}


	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}
	
	public FileProcessorSubmitIssue getFile() {
		return file;
	}
}
