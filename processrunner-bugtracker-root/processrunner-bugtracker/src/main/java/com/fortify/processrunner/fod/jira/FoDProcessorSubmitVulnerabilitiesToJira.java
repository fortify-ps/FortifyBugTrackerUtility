package com.fortify.processrunner.fod.jira;

import com.fortify.processrunner.fod.processor.composite.AbstractFoDProcessorSubmitVulnerabilities;
import com.fortify.processrunner.jira.processor.ProcessorJiraSubmitIssueFromObjectMap;
import com.fortify.processrunner.processor.IProcessor;

public class FoDProcessorSubmitVulnerabilitiesToJira extends AbstractFoDProcessorSubmitVulnerabilities {
	private final ProcessorJiraSubmitIssueFromObjectMap jira = new ProcessorJiraSubmitIssueFromObjectMap();
	
	public ProcessorJiraSubmitIssueFromObjectMap getJira() {
		return jira;
	}
	
	@Override
	protected IProcessor getSubmitVulnerabilityProcessor() {
		return getJira();
	}

}
