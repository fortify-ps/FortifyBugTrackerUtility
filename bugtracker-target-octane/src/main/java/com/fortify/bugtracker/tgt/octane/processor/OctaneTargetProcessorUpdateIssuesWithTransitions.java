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
package com.fortify.bugtracker.tgt.octane.processor;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.IIssueStateDetailsRetriever;
import com.fortify.bugtracker.common.tgt.issue.SubmittedIssue;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorUpdateIssuesWithTransitions;
import com.fortify.bugtracker.tgt.octane.connection.OctaneAuthenticatingRestConnection;
import com.fortify.bugtracker.tgt.octane.connection.OctaneConnectionFactory;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.util.rest.json.JSONMap;

@Component
public class OctaneTargetProcessorUpdateIssuesWithTransitions extends AbstractTargetProcessorUpdateIssuesWithTransitions<JSONMap> {
	@Override
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		OctaneConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public String getTargetName() {
		return "Octane";
	}
	
	@Override
	protected boolean updateIssueFields(Context context, SubmittedIssue submittedIssue, LinkedHashMap<String, Object> issueData) {
		getOctaneConnection(context).updateIssue(submittedIssue, issueData);
		return true;
	}

	protected OctaneAuthenticatingRestConnection getOctaneConnection(Context context) {
		return OctaneConnectionFactory.getConnection(context);
	}
	
	@Override
	protected IIssueStateDetailsRetriever<JSONMap> getIssueStateDetailsRetriever() {
		return new IIssueStateDetailsRetriever<JSONMap>() {
			public JSONMap getIssueStateDetails(Context context, SubmittedIssue submittedIssue) {
				return OctaneConnectionFactory.getConnection(context).getIssueState(submittedIssue);
			}
		};
	}
	
	@Override
	protected boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment) {
		return getOctaneConnection(context).transition(submittedIssue, transitionName, comment);
	}

}
