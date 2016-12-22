package com.fortify.processrunner.file;

import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link AbstractProcessorSubmitIssueFromGroupedObjects} implementation
 * submits issues to a file.
 */
public class ProcessorSubmitIssueToFileFromGroupedObjects extends AbstractProcessorSubmitIssueFromGroupedObjects {
	private final ProcessorSubmitIssueToFileFromObjectMap file = new ProcessorSubmitIssueToFileFromObjectMap();

	@Override
	protected IProcessor getSubmitIssueProcessor() {
		return file;
	}
	
	public ProcessorSubmitIssueToFileFromObjectMap getFile() {
		return file;
	}
}
