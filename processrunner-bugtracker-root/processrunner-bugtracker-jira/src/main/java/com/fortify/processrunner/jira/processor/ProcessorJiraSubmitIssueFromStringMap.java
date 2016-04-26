package com.fortify.processrunner.jira.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.processor.ProcessorBuildStringMap.IContextStringMap;

public class ProcessorJiraSubmitIssueFromStringMap extends AbstractProcessorJiraSubmitIssue {
	private String defaultIssueType = "Task";
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>(2);
		result.add(new ContextProperty("JiraProjectKey", "JIRA project key identifying the JIRA project to submit vulnerabilities to", !context.containsKey("JiraProjectKey")));
		result.add(new ContextProperty("JiraIssueType", "JIRA issue type", false));
		return result;
	}
	
	@Override
	protected JSONObject getIssueToBeSubmitted(Context context) {
		JSONObject result = new JSONObject();
		IContextJira contextJira = context.as(IContextJira.class);
		Map<String, String> fields = context.as(IContextStringMap.class).getStringMap();
		fields.put("project.key", contextJira.getJiraProjectKey());
		fields.put("issuetype.name", getIssueType(contextJira));
		for ( Map.Entry<String,String> field : fields.entrySet() ) {
			addField(result, "fields."+field.getKey(), field.getValue());
		}
		return result;
	}
	
	private void addField(JSONObject json, String key, String value) {
		try {
			String[] propertyPath = StringUtils.split(key, ".");
			for ( int i = 0 ; i<propertyPath.length-1 ; i++ ) {
				String property = propertyPath[i]; 
				if ( !json.has(property) ) {
					json.put(property, new JSONObject());
				}
				json = json.optJSONObject(property);
			}
			json.put(propertyPath[propertyPath.length-1], value);
		} catch ( JSONException e ) {
			throw new RuntimeException("Error creating JSON object for issue to be submitted", e);
		}
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
