package com.fortify.processrunner.octane.processor;

import java.util.LinkedHashMap;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneSharedSpaceAndWorkspaceId;
import com.fortify.processrunner.octane.connection.OctaneConnectionFactory;
import com.fortify.processrunner.octane.context.IContextOctane;

/**
 * This {@link AbstractProcessorSubmitJSONObjectFromGroupedObjects} implementation
 * submits issues to Octane.
 */
public class ProcessorOctaneSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		OctaneConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_OCTANE_SHARED_SPACE_UID, "Octane Shared Space UID", context, null, true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_OCTANE_WORKSPACE_ID, "Octane Workspace ID", context, null, true));
	}
	
	@Override
	public String getBugTrackerName() {
		return "Octane";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		IContextOctane contextOctane = context.as(IContextOctane.class);
		OctaneAuthenticatingRestConnection conn = OctaneConnectionFactory.getConnection(context);
		return conn.submitIssue(new OctaneSharedSpaceAndWorkspaceId(contextOctane.getOctaneSharedSpaceUid(), contextOctane.getOctaneWorkspaceId()), issueData);
	}
}
