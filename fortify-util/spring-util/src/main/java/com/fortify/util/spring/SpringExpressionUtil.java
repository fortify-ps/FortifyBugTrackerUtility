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
package com.fortify.util.spring;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class provides several utility methods related to 
 * Spring Expression Language, for example for evaluating
 * (template) expressions on input objects.
 */
public class SpringExpressionUtil {
	private static final Log LOG = LogFactory.getLog(SpringContextUtil.class);
	private static final Collection<PropertyAccessor> PROPERTY_ACCESSORS = getPropertyAccessors();
	private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
	private static final StandardEvaluationContext SPEL_CONTEXT = createStandardEvaluationContext();
	
	protected SpringExpressionUtil() {}
	
	/**
	 * Automatically load all PropertyAccessor implementations
	 * (annotated with {@link Component}) from 
	 * com.fortify.util.spring.propertyaccessor (sub-)packages. 
	 * @return
	 */
	private static final Collection<PropertyAccessor> getPropertyAccessors() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("com.fortify.util.spring.propertyaccessor");
		try {
			Collection<PropertyAccessor> result = ctx.getBeansOfType(PropertyAccessor.class).values();
			LOG.info("[Process] Loaded PropertyAccessors: "+result);
			return result;
		} finally {
			ctx.close();
		}
	}
	
	public static final StandardEvaluationContext createStandardEvaluationContext() {
		StandardEvaluationContext result = new StandardEvaluationContext();
		for ( PropertyAccessor propertyAccessor : PROPERTY_ACCESSORS ) {
			result.addPropertyAccessor(propertyAccessor);
		}
		return result;
	}

	public static final StandardEvaluationContext getStandardEvaluationContext() {
		return SPEL_CONTEXT;
	}

	/**
	 * Parse the given string as a SpEL expression.
	 * @param exprStr
	 * @return The SpEL {@link Expression} object for the given expression string 
	 */
	public static final SimpleExpression parseSimpleExpression(String exprStr) {
		return new SimpleExpression(SPEL_PARSER.parseExpression(exprStr));
	}
	
	/**
	 * Parse the given string as a SpEL template expression.
	 * @param exprStr
	 * @return The SpEL {@link Expression} object for the given expression string 
	 */
	public static final TemplateExpression parseTemplateExpression(String exprStr) {
		return new TemplateExpression(SPEL_PARSER.parseExpression(exprStr.replace("\\n", "\n"), new TemplateParserContext("${","}")));
	}
	
	public static final <T> T evaluateExpression(Object input, Expression expression, Class<T> returnType) {
		return evaluateExpression(null, input, expression, returnType);
	}

	public static final <T> T evaluateExpression(EvaluationContext context, Object input, Expression expression, Class<T> returnType) {
		if ( input==null || expression==null ) { return null; }
		context = context!=null ? context : getStandardEvaluationContext(); 
		return expression.getValue(context, input, returnType);
	}
	
	public static final <T> T evaluateExpression(Object input, String expression, Class<T> returnType) {
		return evaluateExpression(null, input, expression, returnType);
	}

	public static final <T> T evaluateExpression(EvaluationContext context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, parseSimpleExpression(expression), returnType);
	}
	
	public static final <T> T evaluateTemplateExpression(Object input, String expression, Class<T> returnType) {
		return evaluateTemplateExpression(null, input, expression, returnType);
	}

	public static final <T> T evaluateTemplateExpression(EvaluationContext context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, parseTemplateExpression(expression), returnType);
	}
}
