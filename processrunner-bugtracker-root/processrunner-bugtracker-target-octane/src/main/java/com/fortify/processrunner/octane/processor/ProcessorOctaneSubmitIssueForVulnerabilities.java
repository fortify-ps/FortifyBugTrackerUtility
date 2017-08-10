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
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_OCTANE_SHARED_SPACE_UID, "Octane Shared Space UID", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_OCTANE_WORKSPACE_ID, "Octane Workspace ID", true));
	}
	
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
