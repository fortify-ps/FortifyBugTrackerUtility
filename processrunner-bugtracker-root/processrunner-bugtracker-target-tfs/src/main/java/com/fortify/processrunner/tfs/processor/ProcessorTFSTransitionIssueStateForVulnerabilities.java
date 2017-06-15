package com.fortify.processrunner.tfs.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorTransitionIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.tfs.connection.TFSConnectionFactory;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.connection.TFSRestConnection.TFSIssueState;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

public class ProcessorTFSTransitionIssueStateForVulnerabilities extends AbstractProcessorTransitionIssueStateForVulnerabilities<TFSIssueState> {
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		TFSConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", true));
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
			return conn.updateIssueData(collection, submittedIssue, issueData);
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
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		if ( comment != null ) {
			fields.put("System.History", comment);
		}
		fields.put("System.State", transitionName);
		return conn.updateIssueData(collection, submittedIssue, fields);
	}

	public WorkItemTypeToFieldRenamer getFieldRenamer() {
		return fieldRenamer;
	}

	@Autowired(required=false)
	public void setFieldRenamer(WorkItemTypeToFieldRenamer fieldRenamer) {
		this.fieldRenamer = fieldRenamer;
	}

}
