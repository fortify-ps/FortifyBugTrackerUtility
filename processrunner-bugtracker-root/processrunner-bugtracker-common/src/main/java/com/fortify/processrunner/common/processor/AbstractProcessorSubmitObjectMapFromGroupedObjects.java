package com.fortify.processrunner.common.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.processor.CompositeProcessor;

/**
 * This abstract class builds a string map using {@link AbstractProcessorBuildObjectMapFromGroupedObjects},
 * submits the issue to the bug tracker, and prints a status message. Subclasses need
 * to implement the {@link #getSubmitIssueProcessor()} method to actually submit the issue.
 */
public abstract class AbstractProcessorSubmitObjectMapFromGroupedObjects extends CompositeProcessor {
	private final SubmitIssueProcessor submitIssueProcessor = new SubmitIssueProcessor();
	
	public AbstractProcessorSubmitObjectMapFromGroupedObjects() {
		setProcessors(submitIssueProcessor, new ProcessorPrintMessageGroupedVulnerabilitiesSubmitted());
	}
	
	@Override
	protected final void addCompositeContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextProperties.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		addBugTrackerContextProperties(contextProperties, context);
	}
	
	protected void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {}
	
	public SubmitIssueProcessor getIssue() {
		return submitIssueProcessor;
	}
	
	protected abstract SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData);
	protected abstract String getBugTrackerName();
	
	private class SubmitIssueProcessor extends AbstractProcessorBuildObjectMapFromGroupedObjects {
		@Override
		protected boolean processMap(Context context, LinkedHashMap<String, Object> map) {
			SubmittedIssue submittedIssue = submitIssue(context, map); 
			context.as(IContextBugTracker.class).setSubmittedIssue(submittedIssue);
			return true;
		}
	}
}
