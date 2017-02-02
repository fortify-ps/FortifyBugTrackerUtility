package com.fortify.processrunner.tfs.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.TFSWorkItemJSONObjectBuilder;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

public class ProcessorTFSUpdateIssueStateForVulnerabilities extends AbstractProcessorUpdateIssueStateForVulnerabilities<JSONObject> {
	private static final TFSWorkItemJSONObjectBuilder MAP_TO_JSON = new TFSWorkItemJSONObjectBuilder("add");
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", context, null, true));
	}
	
	@Override
	protected String getBugTrackerName() {
		return "TFS";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = contextTFS.getTFSConnectionRetriever().getConnection();
		String collection = contextTFS.getTFSCollection();
		String workItemType = conn.getWorkItemType(collection, submittedIssue);
		if ( workItemType == null ) {
			return false;
		} else {
			fieldRenamer.renameFields(workItemType, issueData);
			conn.updateIssueData(collection, submittedIssue, MAP_TO_JSON.getJSONArray(issueData));
			return true;
		}
	}

	public WorkItemTypeToFieldRenamer getFieldRenamer() {
		return fieldRenamer;
	}

	@Autowired(required=false)
	public void setFieldRenamer(WorkItemTypeToFieldRenamer fieldRenamer) {
		this.fieldRenamer = fieldRenamer;
	}

}
