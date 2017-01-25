package com.fortify.util.json;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * <p>This class allows for building a JSONObject from an object map
 * with JSON property names as map keys, and property values as
 * map values.</p>
 * 
 * <p>Map keys may use the '.' separator to build nested objects.
 * Map values may contain regular Java arrays and collections, which
 * will be converted to JSONArray instances.</p>
 * 
 * <p>For example, a map entry with key "root.sub.value' and value 
 * [1,2,3] (Java array or collection) will be converted into the 
 * following JSONObject tree:</p>
 * <pre>
 * JSONObject
 *   "root": JSONObject
 *           "sub": JSONObject
 *                  "value": JSONArray
 *                           [1,2,3]
 * </pre>
 * 
 * <p>Subclasses may override the various methods in this class to
 * provide more specialized behavior.</p>
 *  
 */
public class JSONObjectFromObjectMapBuilder {
	public JSONObject getJSONObject(Map<String, Object> data) {
		JSONObject result = new JSONObject();
		for ( Map.Entry<String,Object> field : data.entrySet() ) {
			addField(result, field.getKey(), field.getValue());
		}
		return result;
	}

	protected void addField(JSONObject json, String key, Object value) {
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
			throw new RuntimeException("Error creating JSONObject", e);
		}
	}

	protected Object convertValue(Object value) {
		if ( value != null ) {
			if ( value.getClass().isArray() ) {
				return convertArrayToJSONArray((Object[])value);
			} else if ( value instanceof Collection ) {
				return convertCollectionToJSONArray((Collection<?>)value);
			}
		}
		return value;
	}

	protected Object convertArrayToJSONArray(Object[] array) {
		JSONArray result = new JSONArray();
		for ( Object entry : array ) {
			result.put(entry);
		}
		return result;
	}
	
	protected Object convertCollectionToJSONArray(Collection<?> collection) {
		JSONArray result = new JSONArray();
		for ( Object entry : collection ) {
			result.put(entry);
		}
		return result;
	}
}
