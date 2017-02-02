package com.fortify.processrunner.tfs.util;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectBuilder;

public class TFSWorkItemJSONObjectBuilder extends JSONObjectBuilder {
	private final String op;
	public TFSWorkItemJSONObjectBuilder(String op) {
		this.op = op;
	}
	public JSONArray getJSONArray(Map<String, Object> data) {
		JSONArray result = new JSONArray();
		for ( Map.Entry<String,Object> field : data.entrySet() ) {
			JSONObject entry = updateJSONObjectWithPropertyPath(new JSONObject(), "op", op);
			entry = updateJSONObjectWithPropertyPath(entry, "path", "/fields/"+field.getKey());
			entry = updateJSONObjectWithPropertyPath(entry, "value", field.getValue());
			result.put(entry);
		}
		return result;
	}
}
