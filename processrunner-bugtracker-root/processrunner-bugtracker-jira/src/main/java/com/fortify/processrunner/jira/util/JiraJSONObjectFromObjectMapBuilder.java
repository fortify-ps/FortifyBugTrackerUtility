package com.fortify.processrunner.jira.util;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectFromObjectMapBuilder;

public class JiraJSONObjectFromObjectMapBuilder extends JSONObjectFromObjectMapBuilder {
	@Override
	protected void addField(JSONObject json, String key, Object value) {
		super.addField(json, "fields."+key, value);
	}
}
