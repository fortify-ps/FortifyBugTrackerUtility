package com.fortify.processrunner.archer.processor;

import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link AbstractProcessorSubmitIssueFromGroupedObjects} implementation
 * submits issues to Jira.
 */
public class ProcessorSubmitIssueToArcherFromGroupedObjects extends AbstractProcessorSubmitIssueFromGroupedObjects {
	private final ProcessorSubmitIssueToArcherFromObjectMap jira = new ProcessorSubmitIssueToArcherFromObjectMap();
	
	@Override
	protected IProcessor getSubmitIssueProcessor() {
		return jira;
	}
	
	public ProcessorSubmitIssueToArcherFromObjectMap getJira() {
		return jira;
	}
	
	@Override
	protected String getBugTrackerName() {
		return "Jira";
	}
}
