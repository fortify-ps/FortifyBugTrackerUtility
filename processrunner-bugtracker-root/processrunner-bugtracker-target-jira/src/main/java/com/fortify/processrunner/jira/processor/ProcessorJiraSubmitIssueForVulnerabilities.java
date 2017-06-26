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
package com.fortify.processrunner.jira.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.jira.connection.JiraConnectionFactory;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.processrunner.jira.context.IContextJira;

/**
 * This {@link AbstractProcessorSubmitIssueForVulnerabilities} implementation
 * submits issues to Jira.
 */
public class ProcessorJiraSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private String defaultIssueType = "Task";
	
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		JiraConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		contextPropertyDefinitions.add(new ContextPropertyDefinition("JiraProjectKey", "JIRA project key identifying the JIRA project to submit vulnerabilities to", true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition("JiraIssueType", "JIRA issue type", true).defaultValue(getDefaultIssueType()));
	}
	
	@Override
	public String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueFields) {
		IContextJira contextJira = context.as(IContextJira.class);
		JiraRestConnection conn = JiraConnectionFactory.getConnection(context);
		issueFields.put("project.key", contextJira.getJiraProjectKey());
		issueFields.put("issuetype.name", getIssueType(contextJira));
		issueFields.put("summary", StringUtils.abbreviate((String)issueFields.get("summary"), 254));
		return conn.submitIssue(issueFields);
	}
	
	protected String getIssueType(IContextJira context) {
		String issueType = context.getJiraIssueType();
		return issueType!=null?issueType:getDefaultIssueType();
	}

	public String getDefaultIssueType() {
		return defaultIssueType;
	}

	public void setDefaultIssueType(String defaultIssueType) {
		this.defaultIssueType = defaultIssueType;
	}
}
