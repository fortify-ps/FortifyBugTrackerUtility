package com.fortify.processrunner.jira.processor;

import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link AbstractProcessorSubmitIssueFromGroupedObjects} implementation
 * submits issues to Jira.
 */
public class ProcessorSubmitIssueToJiraFromGroupedObjects extends AbstractProcessorSubmitIssueFromGroupedObjects {
	private final ProcessorSubmitIssueToJiraFromObjectMap jira = new ProcessorSubmitIssueToJiraFromObjectMap();
	
	@Override
	protected IProcessor getSubmitIssueProcessor() {
		return jira;
	}
	
	public ProcessorSubmitIssueToJiraFromObjectMap getJira() {
		return jira;
	}
}
