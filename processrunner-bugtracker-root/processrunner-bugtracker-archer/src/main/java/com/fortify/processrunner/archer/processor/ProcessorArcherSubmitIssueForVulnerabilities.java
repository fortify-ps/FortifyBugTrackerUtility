package com.fortify.processrunner.archer.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.fortify.processrunner.archer.connection.ArcherAuthenticatingRestConnection;
import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.util.json.JSONObjectFromObjectMapBuilder;

/**
 * This {@link AbstractProcessorSubmitJSONObjectFromGroupedObjects} implementation
 * submits issues to Archer.
 */
public class ProcessorArcherSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private static final JSONObjectFromObjectMapBuilder MAP_TO_JSON = new JSONObjectFromObjectMapBuilder();
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO
	}
	
	@Override
	protected String getBugTrackerName() {
		return "Archer";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		IContextArcher contextArcher = context.as(IContextArcher.class);
		ArcherAuthenticatingRestConnection conn = contextArcher.getArcherConnectionRetriever().getConnection();
		return conn.submitIssue(MAP_TO_JSON.getJSONObject(issueData));
	}
}
