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
package com.fortify.bugtracker.tgt.jira.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.jira.cli.ICLIOptionsJira;
import com.fortify.bugtracker.tgt.jira.config.JiraTargetConfiguration;
import com.fortify.bugtracker.tgt.jira.connection.JiraConnectionFactory;
import com.fortify.bugtracker.tgt.jira.connection.JiraRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

/**
 * This {@link AbstractTargetProcessorSubmitIssues} implementation
 * submits issues to Jira.
 */
@Component
public class JiraTargetProcessorSubmitIssues extends AbstractTargetProcessorSubmitIssues {
	private String issueType;
	
	@Override
	public void addBugTrackerCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		JiraConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsJira.CLI_JIRA_PROJECT_KEY);
	}
	
	public String getTargetName() {
		return "Jira";
	}
	
	@Override
	protected TargetIssueLocator submitIssue(Context context, LinkedHashMap<String, Object> issueFields) {
		JiraRestConnection conn = JiraConnectionFactory.getConnection(context);
		issueFields.put("project.key", ICLIOptionsJira.CLI_JIRA_PROJECT_KEY.getValue(context));
		issueFields.put("issuetype.name", getIssueType());
		issueFields.put("summary", StringUtils.abbreviate((String)issueFields.get("summary"), 254));
		return conn.submitIssue(issueFields);
	}
	
	@Override
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() {
		return new ITargetIssueFieldsRetriever() {
			public JSONMap getIssueFieldsFromTarget(Context context, TargetIssueLocator targetIssueLocator) {
				return JiraConnectionFactory.getConnection(context).getIssueDetails(targetIssueLocator);
			}
		};
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	
	@Autowired
	public void setConfiguration(JiraTargetConfiguration config) {
		setIssueType(config.getIssueType());
	}
}
