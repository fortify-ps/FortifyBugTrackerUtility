/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.util.spring.propertyaccessor;

import java.util.Map;

import org.springframework.core.Ordered;
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
public class MapPropertyAccessor implements PropertyAccessor, Ordered {
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
	 */
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return (target instanceof Map);
	}

	/**
	 * Write the given value to the given property name on the given target object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		((Map)target).put(name, newValue);
	}

	public int getOrder() {
		return 10;
	}

}
