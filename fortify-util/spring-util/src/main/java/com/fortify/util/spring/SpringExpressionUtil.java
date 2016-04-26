package com.fortify.util.spring;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.util.spring.propertyaccessor.MapPropertyAccessor;

/**
 * This class provides several utility methods related to 
 * Spring Expression Language, for example for evaluating
 * (template) expressions on input objects.
 */
public final class SpringExpressionUtil {
	private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
	private static final StandardEvaluationContext SPEL_CONTEXT = createStandardEvaluationContext();
	
	private SpringExpressionUtil() {}
	
	private static final StandardEvaluationContext createStandardEvaluationContext() {
		StandardEvaluationContext result = new StandardEvaluationContext();
		result.addPropertyAccessor(new MapPropertyAccessor());
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
	public static final Expression parseExpression(String exprStr) {
		return SPEL_PARSER.parseExpression(exprStr);
	}
	
	/**
	 * Parse the given string as a SpEL template expression.
	 * @param exprStr
	 * @return The SpEL {@link Expression} object for the given expression string 
	 */
	public static final Expression parseTemplateExpression(String exprStr) {
		return SPEL_PARSER.parseExpression(exprStr, new TemplateParserContext("${","}"));
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
		return evaluateExpression(context, input, parseExpression(expression), returnType);
	}
	
	public static final <T> T evaluateTemplateExpression(Object input, String expression, Class<T> returnType) {
		return evaluateTemplateExpression(getStandardEvaluationContext(), input, expression, returnType);
	}

	public static final <T> T evaluateTemplateExpression(EvaluationContext context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(context, input, parseTemplateExpression(expression.replace("\\n", "\n")), returnType);
	}
}
