package com.fortify.processrunner.tfs.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.tfs.connection.TFSConnectionFactory;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

/**
 * This {@link AbstractProcessorSubmitIssueForVulnerabilities} implementation
 * submits issues to TFS.
 */
public class ProcessorTFSSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private String defaultWorkItemType = "Bug";
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		TFSConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSProject", "TFS project to submit vulnerabilities to", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSWorkItemType", "TFS work item type", true).defaultValue(getDefaultWorkItemType()));
	}
	
	@Override
	public String getBugTrackerName() {
		return "TFS";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = TFSConnectionFactory.getConnection(context);
		issueData.put("System.Title", StringUtils.abbreviate((String)issueData.get("System.Title"), 254));
		String workItemType = getWorkItemType(contextTFS);
		fieldRenamer.renameFields(workItemType, issueData);
		return conn.submitIssue(contextTFS.getTFSCollection(), contextTFS.getTFSProject(), workItemType, issueData);
	}
	
	protected String getWorkItemType(IContextTFS context) {
		String issueType = context.getTFSWorkItemType();
		return issueType!=null?issueType:getDefaultWorkItemType();
	}

	public String getDefaultWorkItemType() {
		return defaultWorkItemType;
	}

	public void setDefaultWorkItemType(String defaultWorkItemType) {
		this.defaultWorkItemType = defaultWorkItemType;
	}

	public WorkItemTypeToFieldRenamer getFieldRenamer() {
		return fieldRenamer;
	}

	@Autowired(required=false)
	public void setFieldRenamer(WorkItemTypeToFieldRenamer fieldRenamer) {
		this.fieldRenamer = fieldRenamer;
	}
}
