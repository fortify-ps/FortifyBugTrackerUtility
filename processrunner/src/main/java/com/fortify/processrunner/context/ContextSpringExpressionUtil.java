/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
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
package com.fortify.processrunner.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.util.spring.context.expression.MapAccessorIgnoreNonExistingProperties;
import com.fortify.util.spring.expression.helper.DefaultExpressionHelper;

/**
 * This class extends {@link SpringExpressionUtil} with methods
 * that allow for storing the {@link Context} as a variable in 
 * the Spring {@link EvaluationContext}.
 * 
 * @author Ruud Senden
 * 
 * TODO Do we still need this, now that (most) on-demand loaders no longer need access to the context?
 *
 */
public class ContextSpringExpressionUtil {
	public static final StandardEvaluationContext createStandardEvaluationContext(Context context) {
		StandardEvaluationContext result = new StandardEvaluationContext();
		result.setPropertyAccessors(createPropertyAccessors());
		result.setVariable("ctx", context);
		return result;
	}
	
	protected static final List<PropertyAccessor> createPropertyAccessors() {
		List<PropertyAccessor> result = new ArrayList<PropertyAccessor>();
		result.add(new ReflectivePropertyAccessor());
		result.add(new MapAccessorIgnoreNonExistingProperties());
		return result;
	}
	
	/**
	 * Evaluate the given expression on the given input Object, allowing access to the current
	 * {@link Context} using the '#ctx' variable. The expression result will be converted into
	 * the given returnType if possible.
	 * @param context
	 * @param input
	 * @param expression
	 * @param returnType
	 * @return
	 */
	public static final <T> T evaluateExpression(Context context, Object input, Expression expression, Class<T> returnType) {
		if ( input==null || expression==null ) { return null; }
		return expression.getValue(createStandardEvaluationContext(context), input, returnType);
	}
	
	/**
	 * Evaluate the given expression on the given input Object, allowing access to the current
	 * {@link Context} using the '#ctx' variable. The expression result will be converted into
	 * the given returnType if possible.
	 * @param context
	 * @param input
	 * @param expression
	 * @param returnType
	 * @return
	 */
	public static final <T> T evaluateExpression(Context context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, DefaultExpressionHelper.get().parseSimpleExpression(expression), returnType);
	}

	/**
	 * Evaluate the given expression on the given input Object, allowing access to the current
	 * {@link Context} using the '#ctx' variable. The expression result will be converted into
	 * the given returnType if possible.
	 * @param context
	 * @param input
	 * @param expression
	 * @param returnType
	 * @return
	 */
	public static final <T> T evaluateTemplateExpression(Context context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, DefaultExpressionHelper.get().parseTemplateExpression(expression), returnType);
	}
}
