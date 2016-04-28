package com.fortify.util.spring.expression;

import org.springframework.expression.Expression;

import com.fortify.util.spring.propertyeditor.SimpleExpressionPropertyEditor;
import com.fortify.util.spring.propertyeditor.TemplateExpressionPropertyEditor;

/**
 * <p>This is a simple wrapper class for a Spring {@link Expression}
 * instance. It's main use is in combination with 
 * {@link SimpleExpressionPropertyEditor} to allow automatic
 * conversion from String values to simple {@link Expression}
 * instances.</p>
 * 
 * <p>The reason for needing this wrapper class is to differentiate
 * with templated {@link Expression} instances that are handled 
 * by {@link TemplateExpressionPropertyEditor}.</p>
 */
public class SimpleExpression extends WrappedExpression {
	public SimpleExpression(Expression target) {
		super(target);
	}
}
