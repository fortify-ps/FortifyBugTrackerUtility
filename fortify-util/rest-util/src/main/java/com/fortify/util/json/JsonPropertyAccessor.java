package com.fortify.util.json;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;

import com.fortify.util.json.ondemand.IOnDemandJSONData;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * A SpEL {@link PropertyAccessor} that knows how to read on Jettison JSON objects.
 * It can optionally gather additional data when a given property value implements 
 * the {@link IOnDemandJSONData}.
 * TODO: Fix support for indexed JSONArray properties (if possible)
 */
@Component
public class JsonPropertyAccessor implements PropertyAccessor {
	public JsonPropertyAccessor() {
		// Register ourselves with the standard evaluation context in SpringUtil
		SpringExpressionUtil.getStandardEvaluationContext().addPropertyAccessor(this);
	}
	
	/**
	 * The kind of types this can work with.
	 */
	private static final Class<?>[] SUPPORTED_CLASSES = new Class[] { JSONObject.class, JSONArray.class };

	/**
	 * Return the types that are supported by this {@link PropertyAccessor}
	 */
	public Class<?>[] getSpecificTargetClasses() {
		return SUPPORTED_CLASSES;
	}

	/**
	 * Indicate whether the given property name can be read from the given target object.
	 */
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		Integer index = getIndex(name);
		return (target instanceof JSONObject) || 
			   (target instanceof JSONArray && index != null && ((JSONArray)target).length() > index);
	}

	/**
	 * Return an integer if the String property name can be parsed as an int, or null otherwise.
	 */
	private Integer getIndex(String name) {
		try {
			return Integer.valueOf(name);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Read the given property name from the given target object.
	 */
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Object value = null;
		if ( target instanceof JSONObject ) {
			JSONObject targetJSONObject = (JSONObject)target; 
			if ( !targetJSONObject.isNull(name) ) {
				value = targetJSONObject.opt(name);
				if ( value != null && value instanceof IOnDemandJSONData ) {
					value = ((IOnDemandJSONData)value).replaceOnDemandJSONData(context, targetJSONObject, name);
				}
			}
		} else if ( target instanceof JSONArray && getIndex(name) != null ) {
			JSONArray targetJSONArray = (JSONArray)target;
			int index = getIndex(name);
			if ( !targetJSONArray.isNull(index) ) {
				value = targetJSONArray.opt(index);
			}
		}
		// For some reason the JSONObject|JSONArray.isNull() checks do not work, so we manually compare the class name
		value = value!=null && value.getClass().getName().equals("org.codehaus.jettison.json.JSONObject$Null") ? null : value; 
		return new TypedValue(value);
	}

	/**
	 * Indicate whether the given property name can be written to the given target object.
	 * As we don't support writes, this method always returns false.
	 */
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	/**
	 * Write the given value to the given property name on the given target object.
	 * As we don't support writes, this method always throws an {@link UnsupportedOperationException}.
	 */
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		throw new UnsupportedOperationException("Write is not supported");
	}

}
