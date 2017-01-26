package com.fortify.util.json;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * <p>This class allows for building and updating JSONObject 
 * and JSONArray instances based on composite property paths.
 * Property paths may use the '.' separator to build nested objects.
 * Non-existing objects in the property path will be created on the fly. 
 * Each nested object will either be another JSONObject, or a
 * JSONArray if the property name ends with '[]'. New objects will
 * be appended to a JSONArray if it already exists.</p>
 *  
 * <p>At the end of the chain, an optional value can be inserted. 
 * If this value is a regular Java array or collection, it will 
 * be converted to a JSONArray instance. Other values are inserted as-is.</p>
 * 
 * <p>This is best explained by the following example. Suppose we
 * start with an empty JSONObject, and then invoke
 * {@link #updateJSONObjectWithPropertyPath(JSONObject, String, Object)}
 * twice. For both invocations we use the same property path 
 * "root.myArray[].value'; for the first invocation we supply a
 * Java int array with values [1,2,3] as the optional value, on the
 * second invocation an int array with values [4,5,6]. 
 * The resulting JSON will then look as follows:
 * <code>{"root":{"myArray":[{"value":[1,2,3]},{"value":[4,5,6]}]}}</code>
 * 
 * <p>Subclasses may override the various methods in this class to
 * provide more specialized behavior.</p>
 *  
 */
public class JSONObjectBuilder {
	/**
	 * Build a new JSONObject from an object map. The map keys
	 * define the property paths, the map values define the 
	 * corresponding values that will be inserted at the end
	 * of the property path.
	 * @param data
	 * @return
	 */
	public JSONObject getJSONObject(Map<String, Object> data) {
		JSONObject result = new JSONObject();
		for ( Map.Entry<String,Object> field : data.entrySet() ) {
			updateJSONObjectWithPropertyPath(result, field.getKey(), field.getValue());
		}
		return result;
	}
	
	/**
	 * Update the given JSONObject with the given value under the given property path.
	 * @param parent
	 * @param propertyPath
	 * @param value
	 * @return
	 */
	public JSONObject updateJSONObjectWithPropertyPath(JSONObject parent, String propertyPath, Object value) {
		return updateJSONObjectWithPropertyPath(parent, StringUtils.split(propertyPath, "."), value);
	}
	
	/**
	 * Update the given JSONArray with the given value under the given property path.
	 * @param parent
	 * @param propertyPath
	 * @param value
	 * @return
	 */
	public JSONArray updateJSONArrayWithPropertyPath(JSONArray parent, String propertyPath, Object value) {
		return updateJSONArrayWithPropertyPath(parent, StringUtils.split(propertyPath, "."), value);
	}
	
	protected JSONObject updateJSONObjectWithPropertyPath(JSONObject parent, String[] propertyPath, Object value) {
		try {
			String currentProperty = propertyPath[0];
			String[] remainingPropertyPath = getPropertyPathWithoutRoot(propertyPath);
			Object currentPropertyValue = getPropertyValue(currentProperty, parent.opt(getSimpleName(currentProperty)), remainingPropertyPath!=null, value);
			if ( currentPropertyValue != null ) {
				parent.put(getSimpleName(currentProperty), currentPropertyValue);
				updateJSONObjectOrArrayWithPropertyPath(currentPropertyValue, remainingPropertyPath, value);
			}
			return parent;
		} catch ( JSONException e ) {
			throw new RuntimeException("Error creating JSONObject", e);
		}
	}
	
	/**
	 * Get the simple name for the given property name. This will remove any special
	 * property attributes like the '[]' arry indicator.
	 * @param currentProperty
	 * @return
	 */
	protected String getSimpleName(String currentProperty) {
		return StringUtils.removeEnd(currentProperty, "[]");
	}

	protected JSONArray updateJSONArrayWithPropertyPath(JSONArray parent, String[] propertyPath, Object value) {
		String currentProperty = propertyPath[0];
		Object currentPropertyValue = getPropertyValue(currentProperty, null, true, value);
		if ( currentPropertyValue != null ) {
			parent.put(currentPropertyValue);
			updateJSONObjectOrArrayWithPropertyPath(currentPropertyValue, propertyPath, value);
		}
		return parent;
	}

	protected void updateJSONObjectOrArrayWithPropertyPath(Object parent, String[] propertyPath, Object value) {
		if ( propertyPath!=null && propertyPath.length>0 && parent!=null ) {
			if ( parent instanceof JSONObject ) {
				updateJSONObjectWithPropertyPath((JSONObject)parent, propertyPath, value);
			} else if ( parent instanceof JSONArray ) {
				updateJSONArrayWithPropertyPath((JSONArray)parent, propertyPath, value);
			} else {
				throw new RuntimeException("Unexpected property value type "+parent.getClass().getCanonicalName());
			}
		}
	}
	
	protected String[] getPropertyPathWithoutRoot(String[] propertyPath) {
		return propertyPath.length<2 ? null : Arrays.copyOfRange(propertyPath, 1, propertyPath.length);
	}

	protected Object getPropertyValue(String currentProperty, Object currentPropertyValue, boolean hasRemainingPropertyPath, Object value) {
		if ( !hasRemainingPropertyPath ) { return convertValue(value); }
		if ( currentProperty.endsWith("[]") ) {
			if ( currentPropertyValue == null ) {
				return new JSONArray(); 
			} else if ( currentPropertyValue instanceof JSONArray ) {
				return currentPropertyValue;
			} else {
				throw getIllegalTypeException(currentProperty, JSONArray.class, currentPropertyValue.getClass());
			}
		}
		if ( currentPropertyValue == null ) { 
			return new JSONObject(); 
		} else if ( currentPropertyValue instanceof JSONObject ) {
			return currentPropertyValue;
		} else {
			throw getIllegalTypeException(currentProperty, JSONObject.class, currentPropertyValue.getClass());
		}
	}
	
	protected IllegalArgumentException getIllegalTypeException(String currentProperty, Class<?> expectedClass, Class<?> currentClass) {
		return new IllegalArgumentException("Property "+currentProperty+" is expected as "+expectedClass.getCanonicalName()+", but is "+currentClass.getCanonicalName());
	}
	

	protected Object convertValue(Object value) {
		if ( value != null ) {
			if ( value.getClass().isArray() ) {
				value = convertArrayToJSONArray(value);
			} else if ( value instanceof Collection ) {
				return convertCollectionToJSONArray((Collection<?>)value);
			}
		}
		return value;
	}

	protected Object convertArrayToJSONArray(Object array) {
		JSONArray result = new JSONArray();
		int length = Array.getLength(array);
		for ( int i = 0 ; i < length ; i++ ) {
			result.put(Array.get(array, i));
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
	
	// TODO Move these tests to a test class
	public static void main(String[] args) {
		JSONObject test = new JSONObject();
		new JSONObjectBuilder().updateJSONObjectWithPropertyPath(test, "root.myArray[].value", new int[]{1,2,3});
		new JSONObjectBuilder().updateJSONObjectWithPropertyPath(test, "root.myArray[].value", new int[]{4,5,6});
		System.out.println(test.toString());
		
		// TODO This doesn't work
		//      Expected output: {"root":{"myArray":[1,2]}}
		//      Actual output: {"root":{"myArray":2}}
		test = new JSONObject();
		new JSONObjectBuilder().updateJSONObjectWithPropertyPath(test, "root.myArray[]", 1);
		new JSONObjectBuilder().updateJSONObjectWithPropertyPath(test, "root.myArray[]", 2);
		System.out.println(test.toString());
		
		JSONArray testArray = new JSONArray();
		new JSONObjectBuilder().updateJSONArrayWithPropertyPath(testArray, "value", 1);
		new JSONObjectBuilder().updateJSONArrayWithPropertyPath(testArray, "value", 2);
		System.out.println(testArray.toString());
	}
}
