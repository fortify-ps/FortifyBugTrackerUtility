/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.bugtracker.issue.IIssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
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
	protected IIssueStateDetailsRetriever<TFSIssueState> getIssueStateDetailsRetriever() {
		return new IIssueStateDetailsRetriever<TFSIssueState>() {
			public TFSIssueState getIssueStateDetails(Context context, SubmittedIssue submittedIssue) {
				return TFSConnectionFactory.getConnection(context).getIssueState(context.as(IContextTFS.class).getTFSCollection(), submittedIssue);
			}
		};
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
