/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.bugtracker.tgt.jira.processor;

import java.net.URI;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsUpdater;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorUpdateIssuesWithTransitions;
import com.fortify.bugtracker.tgt.jira.connection.JiraConnectionFactory;
import com.fortify.bugtracker.tgt.jira.connection.JiraRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

@Component
public class JiraTargetProcessorUpdateIssuesWithTransitions extends AbstractTargetProcessorUpdateIssuesWithTransitions {
	@Override
	protected void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		JiraConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
	}
	
	@Override
	public String getTargetName() {
		return "Jira";
	}
	
	@Override
	protected URI getTargetURI(Context context) {
		return getJiraConnection(context).getBaseUrl();
	}
	
	@Override
	protected ITargetIssueFieldsUpdater getTargetIssueFieldsUpdater() {
		return new ITargetIssueFieldsUpdater() {
			@Override
			public boolean updateIssueFields(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields, LinkedHashMap<String, Object> issueFields) {
				getJiraConnection(context).updateIssueData(targetIssueLocatorAndFields.getLocator(), issueFields);
				return true;
			}
		};
	}

	protected JiraRestConnection getJiraConnection(Context context) {
		return JiraConnectionFactory.getConnection(context);
	}
	
	@Override
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() {
		return new ITargetIssueFieldsRetriever() {
			public JSONMap getIssueFieldsFromTarget(Context context, TargetIssueLocator targetIssueLocator) {
				return getJiraConnection(context).getIssueDetails(targetIssueLocator);
			}
		};
	}
	
	@Override
	protected boolean transition(Context context, TargetIssueLocator targetIssueLocator, String transitionName, String comment) {
		return getJiraConnection(context).transition(targetIssueLocator, transitionName, comment);
	}

}
