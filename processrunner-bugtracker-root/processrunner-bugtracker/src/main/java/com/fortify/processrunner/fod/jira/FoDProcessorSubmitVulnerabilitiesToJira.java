package com.fortify.processrunner.fod.jira;

import com.fortify.processrunner.fod.processor.composite.AbstractFoDProcessorSubmitVulnerabilities;
import com.fortify.processrunner.jira.processor.ProcessorJiraSubmitIssueFromStringMap;
import com.fortify.processrunner.processor.IProcessor;

public class FoDProcessorSubmitVulnerabilitiesToJira extends AbstractFoDProcessorSubmitVulnerabilities {
	private final ProcessorJiraSubmitIssueFromStringMap jira = new ProcessorJiraSubmitIssueFromStringMap();
	
	public ProcessorJiraSubmitIssueFromStringMap getJira() {
		return jira;
	}
	
	@Override
	protected IProcessor getSubmitVulnerabilityProcessor() {
		return getJira();
	}

}
