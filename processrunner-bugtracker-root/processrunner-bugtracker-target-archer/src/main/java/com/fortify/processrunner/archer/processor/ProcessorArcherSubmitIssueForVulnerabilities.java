package com.fortify.processrunner.archer.processor;

import java.util.LinkedHashMap;

import com.fortify.processrunner.archer.connection.ArcherConnectionFactory;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;

/**
 * This {@link AbstractProcessorSubmitJSONObjectFromGroupedObjects} implementation
 * submits issues to Archer.
 */
public class ProcessorArcherSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		ArcherConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public String getBugTrackerName() {
		return "Archer";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		return ArcherConnectionFactory.getConnection(context).submitIssue(issueData);
	}
}
