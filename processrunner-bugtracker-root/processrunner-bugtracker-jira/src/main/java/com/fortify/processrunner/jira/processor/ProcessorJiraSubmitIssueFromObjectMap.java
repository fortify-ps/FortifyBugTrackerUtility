package com.fortify.processrunner.jira.processor;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMap.IContextObjectMap;

/**
 * This concrete {@link AbstractProcessorJiraSubmitIssue} implementation 
 * generates the {@link JSONObject} to be posted to JIRA, based on {@link Context}
 * data provided via the {@link IContextObjectMap} interface. 
 */
public class ProcessorJiraSubmitIssueFromObjectMap extends AbstractProcessorJiraSubmitIssue {
	private String defaultIssueType = "Task";
	
	@Override
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("JiraProjectKey", "JIRA project key identifying the JIRA project to submit vulnerabilities to", context, null, true));
		contextProperties.add(new ContextProperty("JiraIssueType", "JIRA issue type", context, getDefaultIssueType(), false));
	}
	
	@Override
	protected JSONObject getIssueToBeSubmitted(Context context) {
		JSONObject result = new JSONObject();
		IContextJira contextJira = context.as(IContextJira.class);
		Map<String, Object> fields = context.as(IContextObjectMap.class).getObjectMap();
		fields.put("project.key", contextJira.getJiraProjectKey());
		fields.put("issuetype.name", getIssueType(contextJira));
		fields.put("summary", StringUtils.abbreviate((String)fields.get("summary"), 254));
		for ( Map.Entry<String,Object> field : fields.entrySet() ) {
			addField(result, "fields."+field.getKey(), field.getValue());
		}
		return result;
	}
	
	private void addField(JSONObject json, String key, Object value) {
		try {
			String[] propertyPath = StringUtils.split(key, ".");
			for ( int i = 0 ; i<propertyPath.length-1 ; i++ ) {
				String property = propertyPath[i]; 
				if ( !json.has(property) ) {
					json.put(property, new JSONObject());
				}
				json = json.optJSONObject(property);
			}
			json.put(propertyPath[propertyPath.length-1], convertValue(value));
		} catch ( JSONException e ) {
			throw new RuntimeException("Error creating JSON object for issue to be submitted", e);
		}
	}

	private Object convertValue(Object value) {
		if ( value != null ) {
			if ( value.getClass().isArray() ) {
				return convertArrayToJSONArray((Object[])value);
			} else if ( value instanceof Collection ) {
				return convertCollectionToJSONArray((Collection<?>)value);
			}
		}
		return value;
	}

	private Object convertArrayToJSONArray(Object[] array) {
		JSONArray result = new JSONArray();
		for ( Object entry : array ) {
			result.put(entry);
		}
		return result;
	}
	
	private Object convertCollectionToJSONArray(Collection<?> collection) {
		JSONArray result = new JSONArray();
		for ( Object entry : collection ) {
			result.put(entry);
		}
		return result;
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
