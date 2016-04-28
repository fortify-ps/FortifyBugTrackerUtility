package com.fortify.util.spring.propertyaccessor;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;

import com.fortify.util.json.ondemand.IOnDemandJSONData;

/**
 * A SpEL {@link PropertyAccessor} that knows how to read on {@link JSONObject} instances.
 * It can optionally gather additional data when a given property value implements 
 * the {@link IOnDemandJSONData}.
 */
@Component
public class JsonObjectPropertyAccessor implements PropertyAccessor {
	/**
	 * The kind of types this can work with.
	 */
	private static final Class<?>[] SUPPORTED_CLASSES = new Class[] { JSONObject.class };

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
		return (target instanceof JSONObject);
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
		}
		// For some reason the JSONObject.isNull() checks do not work, so we manually compare the class name
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
