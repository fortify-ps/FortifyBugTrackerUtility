package com.fortify.processrunner.octane.util;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectBuilder;

public class OctaneDefectJSONObjectBuilder extends JSONObjectBuilder {
	@Override
	public JSONObject updateJSONObjectWithPropertyPath(JSONObject parent, String propertyPath, Object value) {
		return super.updateJSONObjectWithPropertyPath(parent, "data."+propertyPath, value);
	}
}
