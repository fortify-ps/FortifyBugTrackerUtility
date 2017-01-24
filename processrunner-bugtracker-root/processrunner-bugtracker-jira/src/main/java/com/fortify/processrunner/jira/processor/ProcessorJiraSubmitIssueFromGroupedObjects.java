package com.fortify.processrunner.jira.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitJSONObjectFromGroupedObjects;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.jira.connection.JiraRestConnection;
import com.fortify.processrunner.jira.context.IContextJira;

/**
 * This {@link AbstractProcessorSubmitJSONObjectFromGroupedObjects} implementation
 * submits issues to Jira.
 */
public class ProcessorJiraSubmitIssueFromGroupedObjects extends AbstractProcessorSubmitJSONObjectFromGroupedObjects {
	private String defaultIssueType = "Task";
	
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("JiraProjectKey", "JIRA project key identifying the JIRA project to submit vulnerabilities to", context, null, true));
		contextProperties.add(new ContextProperty("JiraIssueType", "JIRA issue type", context, getDefaultIssueType(), false));
	}
	
	@Override
	protected String getBugTrackerName() {
		return "Jira";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, JSONObject jsonObject) {
		IContextJira contextJira = context.as(IContextJira.class);
		JiraRestConnection conn = contextJira.getJiraConnectionRetriever().getConnection();
		return conn.submitIssue(jsonObject);
	}
	
	@Override
	protected JSONObject getJSONObject(Context context, LinkedHashMap<String, Object> issueData) {
		IContextJira contextJira = context.as(IContextJira.class);
		issueData.put("project.key", contextJira.getJiraProjectKey());
		issueData.put("issuetype.name", getIssueType(contextJira));
		issueData.put("summary", StringUtils.abbreviate((String)issueData.get("summary"), 254));
		return super.getJSONObject(context, issueData);
	}
	
	@Override
	protected void addField(JSONObject json, String key, Object value) {
		super.addField(json, "fields."+key, value);
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
