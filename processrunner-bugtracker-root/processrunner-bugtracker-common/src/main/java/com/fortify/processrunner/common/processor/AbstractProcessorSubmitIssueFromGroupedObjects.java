package com.fortify.processrunner.common.processor;

import java.util.Collection;
import java.util.List;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
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
	
	@Override
	protected void addCompositeContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextProperties.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
	}
	
	protected abstract IProcessor getSubmitIssueProcessor();
	protected abstract String getBugTrackerName();
}
