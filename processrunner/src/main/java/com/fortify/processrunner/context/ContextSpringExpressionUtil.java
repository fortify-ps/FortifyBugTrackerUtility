package com.fortify.processrunner.context;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.util.spring.SpringExpressionUtil;

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
	
	public static final <T> T evaluateExpression(Context context, Object input, Expression expression, Class<T> returnType) {
		return evaluateExpression(createStandardEvaluationContext(context), input, expression, returnType);
	}
	
	public static final <T> T evaluateExpression(Context context, Object input, String expression, Class<T> returnType) {
		return evaluateExpression(createStandardEvaluationContext(context), input, expression, returnType);
	}

	public static final <T> T evaluateTemplateExpression(Context context, Object input, String expression, Class<T> returnType) {
		return evaluateTemplateExpression(createStandardEvaluationContext(context), input, expression, returnType);
	}
}
