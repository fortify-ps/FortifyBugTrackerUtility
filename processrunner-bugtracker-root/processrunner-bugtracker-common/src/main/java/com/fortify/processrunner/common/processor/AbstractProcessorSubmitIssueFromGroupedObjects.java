package com.fortify.processrunner.common.processor;

import java.util.List;

import com.fortify.processrunner.common.processor.ProcessorPrintMessageGroupedVulnerabilitiesSubmitted;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorBuildObjectMapFromGroupedObjects;

/**
 * This abstract class builds a string map using {@link ProcessorBuildObjectMapFromGroupedObjects},
 * submits the issue to the bug tracker, and prints a status message. Subclasses need
 * to implement the {@link #getSubmitIssueProcessor()} method to actually submit the issue.
 */
public abstract class AbstractProcessorSubmitIssueFromGroupedObjects extends ProcessorBuildObjectMapFromGroupedObjects {
	@Override
	public List<IProcessor> getProcessors() {
		List<IProcessor> result = super.getProcessors();
		result.add(getSubmitIssueProcessor());
		result.add(new ProcessorPrintMessageGroupedVulnerabilitiesSubmitted());
		return result;
	}
	
	protected abstract IProcessor getSubmitIssueProcessor();
}
