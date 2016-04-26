package com.fortify.processrunner.fod.jira;

import com.fortify.processrunner.fod.processor.composite.AbstractFoDProcessorSubmitVulnerabilities;
import com.fortify.processrunner.jira.processor.ProcessorJiraSubmitIssueFromStringMap;

public class FoDProcessorSubmitVulnerabilitiesToJira extends AbstractFoDProcessorSubmitVulnerabilities {
	private final ProcessorJiraSubmitIssueFromStringMap jira = new ProcessorJiraSubmitIssueFromStringMap();
	
	public FoDProcessorSubmitVulnerabilitiesToJira() {
		init(getJira());
	}

	public ProcessorJiraSubmitIssueFromStringMap getJira() {
		return jira;
	}

}
