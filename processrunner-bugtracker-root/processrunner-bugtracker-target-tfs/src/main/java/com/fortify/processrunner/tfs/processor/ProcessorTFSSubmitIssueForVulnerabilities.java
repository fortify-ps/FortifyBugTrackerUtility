/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.tfs.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.bugtracker.issue.IssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.tfs.connection.TFSConnectionFactory;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.connection.TFSRestConnection.TFSIssueState;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

/**
 * This {@link AbstractProcessorSubmitIssueForVulnerabilities} implementation
 * submits issues to TFS.
 */
public class ProcessorTFSSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities<TFSIssueState> {
	private String defaultWorkItemType = "Bug";
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		TFSConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSProject", "TFS project to submit vulnerabilities to", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("TFSWorkItemType", "TFS work item type", true).defaultValue(getDefaultWorkItemType()));
	}
	
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
	
	@Override
	protected IssueStateDetailsRetriever<TFSIssueState> getIssueStateDetailsRetriever() {
		return new IssueStateDetailsRetriever<TFSIssueState>() {
			public TFSIssueState getIssueStateDetails(Context context, SubmittedIssue submittedIssue) {
				return TFSConnectionFactory.getConnection(context).getIssueState(context.as(IContextTFS.class).getTFSCollection(), submittedIssue);
			}
		};
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
