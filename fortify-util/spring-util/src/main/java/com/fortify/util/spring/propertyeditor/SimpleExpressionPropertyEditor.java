package com.fortify.util.spring.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.springframework.stereotype.Component;

import com.fortify.util.spring.SpringContextUtil.PropertyEditorWithTargetClass;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This {@link PropertyEditor} allows parsing String values into an 
 * SPeL Expression object.
 */
@Component
public final class SimpleExpressionPropertyEditor extends PropertyEditorSupport implements PropertyEditorWithTargetClass {
	public void setAsText(String text) {
        SimpleExpression expression = SpringExpressionUtil.parseSimpleExpression(text);
        setValue(expression);
    }
    
    public Class<?> getTargetClass() {
    	return SimpleExpression.class;
    }
}