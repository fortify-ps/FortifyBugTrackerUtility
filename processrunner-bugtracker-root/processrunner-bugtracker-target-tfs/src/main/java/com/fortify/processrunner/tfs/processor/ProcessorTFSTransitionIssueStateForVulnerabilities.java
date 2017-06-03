package com.fortify.processrunner.tfs.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorTransitionIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.tfs.connection.TFSConnectionFactory;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.connection.TFSRestConnection.TFSIssueState;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.TFSWorkItemJSONObjectBuilder;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

public class ProcessorTFSTransitionIssueStateForVulnerabilities extends AbstractProcessorTransitionIssueStateForVulnerabilities<TFSIssueState> {
	private static final TFSWorkItemJSONObjectBuilder MAP_TO_JSON = new TFSWorkItemJSONObjectBuilder("add");
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		TFSConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", context, null, true));
	}
	
	@Override
	public String getBugTrackerName() {
		return "TFS";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = TFSConnectionFactory.getConnection(context);
		String collection = contextTFS.getTFSCollection();
		String workItemType = conn.getWorkItemType(collection, submittedIssue);
		if ( workItemType == null ) {
			return false;
		} else {
			fieldRenamer.renameFields(workItemType, issueData);
			return conn.updateIssueData(collection, submittedIssue, MAP_TO_JSON.getJSONArray(issueData));
		}
	}
	
	@Override
	protected TFSIssueState getCurrentIssueState(Context context, SubmittedIssue submittedIssue) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = TFSConnectionFactory.getConnection(context);
		String collection = contextTFS.getTFSCollection();
		return conn.getIssueState(collection, submittedIssue);
	}
	
	@Override
	protected boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = TFSConnectionFactory.getConnection(context);
		String collection = contextTFS.getTFSCollection();
		JSONArray ops = new JSONArray();
		if ( comment != null ) {
			ops.put(MAP_TO_JSON.getOperation("add", "/fields/System.History", comment));
		}
		ops.put(MAP_TO_JSON.getOperation("replace", "/fields/System.State", transitionName));
		return conn.updateIssueData(collection, submittedIssue, ops);
	}

	public WorkItemTypeToFieldRenamer getFieldRenamer() {
		return fieldRenamer;
	}

	@Autowired(required=false)
	public void setFieldRenamer(WorkItemTypeToFieldRenamer fieldRenamer) {
		this.fieldRenamer = fieldRenamer;
	}

}
