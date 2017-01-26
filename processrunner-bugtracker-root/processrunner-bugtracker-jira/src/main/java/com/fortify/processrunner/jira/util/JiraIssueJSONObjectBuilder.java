package com.fortify.processrunner.jira.util;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectBuilder;

public class JiraIssueJSONObjectBuilder extends JSONObjectBuilder {
	@Override
	public JSONObject updateJSONObjectWithPropertyPath(JSONObject parent, String propertyPath, Object value) {
		return super.updateJSONObjectWithPropertyPath(parent, "fields."+propertyPath, value);
	}
}
