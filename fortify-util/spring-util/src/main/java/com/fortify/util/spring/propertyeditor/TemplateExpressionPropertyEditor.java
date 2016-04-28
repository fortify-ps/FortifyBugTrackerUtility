package com.fortify.util.spring.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.springframework.stereotype.Component;

import com.fortify.util.spring.SpringContextUtil.PropertyEditorWithTargetClass;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link PropertyEditor} allows parsing String values into a 
 * TemplateExpression object.
 */
@Component
public final class TemplateExpressionPropertyEditor extends PropertyEditorSupport implements PropertyEditorWithTargetClass {
	public void setAsText(String text) {
        TemplateExpression expression = SpringExpressionUtil.parseTemplateExpression(text);
        setValue(expression);
    }
    
    public Class<?> getTargetClass() {
    	return TemplateExpression.class;
    }

}