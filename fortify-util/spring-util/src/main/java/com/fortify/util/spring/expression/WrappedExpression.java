package com.fortify.util.spring.expression;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;

import com.fortify.util.spring.propertyeditor.SimpleExpressionPropertyEditor;

/**
 * <p>This is a simple wrapper class for a Spring {@link Expression}
 * instance. It's main use is in combination with 
 * {@link TemplateExpressionPropertyEditor} to allow automatic
 * conversion from String values to templated {@link Expression}
 * instances.</p>
 * 
 * <p>The reason for needing this wrapper class is to differentiate
 * with non-templated {@link Expression} instances that are
 * handled by {@link SimpleExpressionPropertyEditor}.</p>
 */
public class WrappedExpression implements Expression {
	private final Expression target;
	
	/**
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue()
	 */
	public Object getValue() throws EvaluationException {
		return target.getValue();
	}

	/**
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(java.lang.Object)
	 */
	public Object getValue(Object paramObject) throws EvaluationException {
		return target.getValue(paramObject);
	}

	/**
	 * @param paramClass
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(java.lang.Class)
	 */
	public <T> T getValue(Class<T> paramClass) throws EvaluationException {
		return target.getValue(paramClass);
	}

	/**
	 * @param paramObject
	 * @param paramClass
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(java.lang.Object, java.lang.Class)
	 */
	public <T> T getValue(Object paramObject, Class<T> paramClass)
			throws EvaluationException {
		return target.getValue(paramObject, paramClass);
	}

	/**
	 * @param paramEvaluationContext
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(org.springframework.expression.EvaluationContext)
	 */
	public Object getValue(EvaluationContext paramEvaluationContext)
			throws EvaluationException {
		return target.getValue(paramEvaluationContext);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(org.springframework.expression.EvaluationContext, java.lang.Object)
	 */
	public Object getValue(EvaluationContext paramEvaluationContext,
			Object paramObject) throws EvaluationException {
		return target.getValue(paramEvaluationContext, paramObject);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramClass
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(org.springframework.expression.EvaluationContext, java.lang.Class)
	 */
	public <T> T getValue(EvaluationContext paramEvaluationContext,
			Class<T> paramClass) throws EvaluationException {
		return target.getValue(paramEvaluationContext, paramClass);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @param paramClass
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValue(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.Class)
	 */
	public <T> T getValue(EvaluationContext paramEvaluationContext,
			Object paramObject, Class<T> paramClass) throws EvaluationException {
		return target.getValue(paramEvaluationContext, paramObject, paramClass);
	}

	/**
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueType()
	 */
	public Class<?> getValueType() throws EvaluationException {
		return target.getValueType();
	}

	/**
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueType(java.lang.Object)
	 */
	public Class<?> getValueType(Object paramObject) throws EvaluationException {
		return target.getValueType(paramObject);
	}

	/**
	 * @param paramEvaluationContext
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueType(org.springframework.expression.EvaluationContext)
	 */
	public Class<?> getValueType(EvaluationContext paramEvaluationContext)
			throws EvaluationException {
		return target.getValueType(paramEvaluationContext);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueType(org.springframework.expression.EvaluationContext, java.lang.Object)
	 */
	public Class<?> getValueType(EvaluationContext paramEvaluationContext,
			Object paramObject) throws EvaluationException {
		return target.getValueType(paramEvaluationContext, paramObject);
	}

	/**
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueTypeDescriptor()
	 */
	public TypeDescriptor getValueTypeDescriptor() throws EvaluationException {
		return target.getValueTypeDescriptor();
	}

	/**
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueTypeDescriptor(java.lang.Object)
	 */
	public TypeDescriptor getValueTypeDescriptor(Object paramObject)
			throws EvaluationException {
		return target.getValueTypeDescriptor(paramObject);
	}

	/**
	 * @param paramEvaluationContext
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueTypeDescriptor(org.springframework.expression.EvaluationContext)
	 */
	public TypeDescriptor getValueTypeDescriptor(
			EvaluationContext paramEvaluationContext)
			throws EvaluationException {
		return target.getValueTypeDescriptor(paramEvaluationContext);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#getValueTypeDescriptor(org.springframework.expression.EvaluationContext, java.lang.Object)
	 */
	public TypeDescriptor getValueTypeDescriptor(
			EvaluationContext paramEvaluationContext, Object paramObject)
			throws EvaluationException {
		return target.getValueTypeDescriptor(paramEvaluationContext,
				paramObject);
	}

	/**
	 * @param paramEvaluationContext
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#isWritable(org.springframework.expression.EvaluationContext)
	 */
	public boolean isWritable(EvaluationContext paramEvaluationContext)
			throws EvaluationException {
		return target.isWritable(paramEvaluationContext);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#isWritable(org.springframework.expression.EvaluationContext, java.lang.Object)
	 */
	public boolean isWritable(EvaluationContext paramEvaluationContext,
			Object paramObject) throws EvaluationException {
		return target.isWritable(paramEvaluationContext, paramObject);
	}

	/**
	 * @param paramObject
	 * @return
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#isWritable(java.lang.Object)
	 */
	public boolean isWritable(Object paramObject) throws EvaluationException {
		return target.isWritable(paramObject);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#setValue(org.springframework.expression.EvaluationContext, java.lang.Object)
	 */
	public void setValue(EvaluationContext paramEvaluationContext,
			Object paramObject) throws EvaluationException {
		target.setValue(paramEvaluationContext, paramObject);
	}

	/**
	 * @param paramObject1
	 * @param paramObject2
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#setValue(java.lang.Object, java.lang.Object)
	 */
	public void setValue(Object paramObject1, Object paramObject2)
			throws EvaluationException {
		target.setValue(paramObject1, paramObject2);
	}

	/**
	 * @param paramEvaluationContext
	 * @param paramObject1
	 * @param paramObject2
	 * @throws EvaluationException
	 * @see org.springframework.expression.Expression#setValue(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.Object)
	 */
	public void setValue(EvaluationContext paramEvaluationContext,
			Object paramObject1, Object paramObject2)
			throws EvaluationException {
		target.setValue(paramEvaluationContext, paramObject1, paramObject2);
	}

	/**
	 * @return
	 * @see org.springframework.expression.Expression#getExpressionString()
	 */
	public String getExpressionString() {
		return target.getExpressionString();
	}

	public WrappedExpression(Expression target) {
		this.target = target;
	}
	
	

}
