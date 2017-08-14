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
package com.fortify.util.spring.propertyaccessor.ondemand;

import java.util.Map;

import org.springframework.beans.PropertyAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.ondemand.IOnDemandPropertyLoader;
import com.fortify.util.spring.propertyaccessor.MapPropertyAccessor;

/**
 * This class extends {@link MapPropertyAccessor} by adding support for
 * dynamically loading map values from {@link IOnDemandPropertyLoader}
 * instances.
 * 
 * @author Ruud Senden
 */
@Component
public class OnDemandMapPropertyAccessor extends MapPropertyAccessor {
	/**
	 * Retrieve the value for the given name from the target map. If the value
	 * does not implement the {@link IOnDemandPropertyLoader} interface, it will
	 * be returned as-is. If the value does implement this interface, its
	 * {@link IOnDemandPropertyLoader#getValue(Context, Map)} method will
	 * be called to retrieve the actual value. In the target map, the original 
	 * {@link IOnDemandPropertyLoader} will be replaced with the actual value,
	 * and the actual value will be returned.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public TypedValue read(EvaluationContext evaluationContext, Object target, String name) throws AccessException {
		Map<Object,Object> targetMap = (Map<Object,Object>)target;
		Object value = targetMap.get(name);
		if ( value instanceof IOnDemandPropertyLoader ) {
			Context ctx = getContext(evaluationContext);
			value = ((IOnDemandPropertyLoader<?>)value).getValue(ctx, targetMap);
			targetMap.put(name, value);
		}
		return new TypedValue(value);
	}
	
	/**
	 * Get the current {@link Context} from the given {@link EvaluationContext}.
	 * This method will throw an exception if no {@link Context} instance is
	 * available.
	 * @param evaluationContext
	 * @return
	 */
	protected Context getContext(EvaluationContext evaluationContext) {
		Object result = evaluationContext.lookupVariable("ctx");
		if ( result != null && (result instanceof Context) ) {
			return (Context)result;
		} else {
			TypedValue root = evaluationContext.getRootObject();
			if ( root != null && (root.getValue() instanceof Context) ) {
				return (Context)root.getValue();
			}
		}
		throw new IllegalStateException("Context is not available");
	}

	/**
	 * Get the order for this {@link PropertyAccessor}. Since we want this {@link OnDemandMapPropertyAccessor}
	 * to be used instead of the original {@link MapPropertyAccessor}, we return a lower number than our
	 * superclass.
	 */
	@Override
	public int getOrder() {
		return 5; // Lower order than our parent, to be loaded first
	}
}
