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
package com.fortify.processrunner.context;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.api.util.spring.SpringExpressionUtil;

/**
 * This class extends {@link SpringExpressionUtil} with methods
 * that allow for storing the {@link Context} as a variable in 
 * the Spring {@link EvaluationContext}.
 * 
 * @author Ruud Senden
 *
 */
public class ContextSpringExpressionUtil extends SpringExpressionUtil {
	
	/**
	 * Create a Spring {@link StandardEvaluationContext} instance based
	 * on {@link SpringExpressionUtil#createStandardEvaluationContext()},
	 * adding the current {@link Context} as variable 'ctx'.
	 * 
	 * @param context
	 * @return
	 */
	public static final StandardEvaluationContext createStandardEvaluationContext(Context context) {
		StandardEvaluationContext result = createStandardEvaluationContext();
		result.setVariable("ctx", context);
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
		return evaluateExpression(createStandardEvaluationContext(context), input, expression, returnType);
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
		return evaluateExpression(createStandardEvaluationContext(context), input, expression, returnType);
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
		return evaluateTemplateExpression(createStandardEvaluationContext(context), input, expression, returnType);
	}
}
