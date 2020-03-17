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

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.jira.cli.ICLIOptionsJira;
import com.fortify.bugtracker.tgt.jira.config.JiraTargetConfiguration;
import com.fortify.bugtracker.tgt.jira.config.JiraTargetNestedParentIssueConfiguration;
import com.fortify.bugtracker.tgt.jira.connection.JiraConnectionFactory;
import com.fortify.bugtracker.tgt.jira.connection.JiraRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link AbstractTargetProcessorSubmitIssues} implementation
 * submits issues to Jira.
 * 
 * TODO Can the {@link #setIssueType(String)} method be called externally?
 *      If not, we can simply store the injected {@link JiraTargetConfiguration}
 *      and get issue type and parent issue from this configuration directly,
 *      instead of duplicating the fields.
 */
@Component
public class JiraTargetProcessorSubmitIssues extends AbstractTargetProcessorSubmitIssues {
	private String issueType;
	private JiraTargetNestedParentIssueConfiguration parentIssue;
	
	@Override
	public void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		JiraConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsJira.CLI_JIRA_PROJECT_KEY);
	}
	
	public String getTargetName() {
		return "Jira";
	}
	
	@Override
	protected TargetIssueLocator submitIssue(Context context, String groupName, List<Object> currentGroup, LinkedHashMap<String, Object> issueFields) {
		JiraRestConnection conn = JiraConnectionFactory.getConnection(context);
		addOptionalParentIssueToIssueFields(context, currentGroup, issueFields, getParentIssue());
		issueFields.put("project.key", ICLIOptionsJira.CLI_JIRA_PROJECT_KEY.getValue(context));
		issueFields.put("issuetype.name", getIssueType());
		issueFields.put("summary", StringUtils.abbreviate((String)issueFields.get("summary"), 254));
		return conn.submitIssue(issueFields);
	}

	private void addOptionalParentIssueToIssueFields(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> issueFields, JiraTargetNestedParentIssueConfiguration parentIssue) {
		if ( parentIssue!=null ) {
			String parentIssueKey = getOrCreateParentIssue(context, currentGroup, parentIssue);
			issueFields.put("parent.key", parentIssueKey);
		}
	}
	
	private String getOrCreateParentIssue(Context context, List<Object> currentGroup, JiraTargetNestedParentIssueConfiguration parentIssue) {
		StandardEvaluationContext sec = ContextSpringExpressionUtil.createStandardEvaluationContext(context);
		LinkedHashMap<String, Object> issueFields = new MapBuilder()
				.addMapUpdater(new MapUpdaterPutValuesFromExpressionMap(sec, currentGroup.get(0), parentIssue.getFields()))
				.build(new LinkedHashMap<String, Object>());
		addOptionalParentIssueToIssueFields(context, currentGroup, issueFields, parentIssue.getParentIssue());
		issueFields.put("project.key", ICLIOptionsJira.CLI_JIRA_PROJECT_KEY.getValue(context));
		issueFields.put("issuetype.name", parentIssue.getIssueType());
		
		return getOrCreateParentIssue(context, parentIssue.getJqlExpression(), issueFields);
	}

	private String getOrCreateParentIssue(Context context, TemplateExpression jqlExpression, LinkedHashMap<String, Object> issueFields) {
		JiraRestConnection conn = JiraConnectionFactory.getConnection(context);
		String jql = ContextSpringExpressionUtil.evaluateExpression(context, issueFields, jqlExpression, String.class);
		String parentIssueKey = conn.getIssueKeyForJql(jql);
		if ( parentIssueKey==null ) {
			parentIssueKey = conn.submitIssue(issueFields).getId();
		}
		return parentIssueKey;
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
	
	public JiraTargetNestedParentIssueConfiguration getParentIssue() {
		return parentIssue;
	}

	public void setParentIssue(JiraTargetNestedParentIssueConfiguration parentIssue) {
		this.parentIssue = parentIssue;
	}

	@Autowired
	public void setConfiguration(JiraTargetConfiguration config) {
		setIssueType(config.getIssueType());
		setParentIssue(config.getParentIssue());
	}
}
