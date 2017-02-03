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
			result.put(getOperation(op, "/fields/"+field.getKey(), field.getValue()));
		}
		return result;
	}
	
	public JSONObject getOperation(String op, String path, Object value) {
		JSONObject result = updateJSONObjectWithPropertyPath(new JSONObject(), "op", op);
		result = updateJSONObjectWithPropertyPath(result, "path", path);
		result = updateJSONObjectWithPropertyPath(result, "value", value);
		return result;
	}
}
