package com.fortify.util.spring.propertyaccessor;

import java.util.Map;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;

/**
 * A SpEL {@link PropertyAccessor} that knows how to read on Map objects.
 * This allows Map keys to be treated as regular object properties.
 */
@Component
public class MapPropertyAccessor implements PropertyAccessor {
	/**
	 * Return the types that are supported by this {@link PropertyAccessor}
	 */
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[]{Map.class};
	}

	/**
	 * Indicate whether the given property name can be read from the given target object.
	 */
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return (target instanceof Map);
	}

	/**
	 * Read the given property name from the given target object.
	 */
	@SuppressWarnings("rawtypes")
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		return new TypedValue( ((Map)target).get(name) );
	}

	/**
	 * Indicate whether the given property name can be written to the given target object.
	 * As we don't support writes, this method always returns false.
	 */
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return (target instanceof Map);
	}

	/**
	 * Write the given value to the given property name on the given target object.
	 * As we don't support writes, this method always throws an {@link UnsupportedOperationException}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		((Map)target).put(name, newValue);
	}

}
