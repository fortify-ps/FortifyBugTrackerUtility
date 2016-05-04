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
public final class SpringExpressionUtil {
	private static final Log LOG = LogFactory.getLog(SpringContextUtil.class);
	private static final Collection<PropertyAccessor> PROPERTY_ACCESSORS = getPropertyAccessors();
	private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
	private static final StandardEvaluationContext SPEL_CONTEXT = createStandardEvaluationContext();
	
	private SpringExpressionUtil() {}
	
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
			LOG.info("Loaded PropertyAccessors: "+result);
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
		return evaluateExpression(getStandardEvaluationContext(), input, expression, returnType);
	}

	public static final <T> T evaluateExpression(EvaluationContext context, Object input, Expression expression, Class<T> returnType) {
		if ( input==null || expression==null ) { return null; }
		return expression.getValue(context, input, returnType);
	}
	
	public static final <T> T evaluateExpression(Object input, String expression, Class<T> returnType) {
		return evaluateExpression(getStandardEvaluationContext(), input, expression, returnType);
	}

	public static final <T> T evaluateExpression(EvaluationContext context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, parseSimpleExpression(expression), returnType);
	}
	
	public static final <T> T evaluateTemplateExpression(Object input, String expression, Class<T> returnType) {
		return evaluateTemplateExpression(getStandardEvaluationContext(), input, expression, returnType);
	}

	public static final <T> T evaluateTemplateExpression(EvaluationContext context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, parseTemplateExpression(expression), returnType);
	}
}
