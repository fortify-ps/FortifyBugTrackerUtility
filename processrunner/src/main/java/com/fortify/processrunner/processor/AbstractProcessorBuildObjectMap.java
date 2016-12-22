package com.fortify.processrunner.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This abstract {@link IProcessor} implementation allows for building a 
 * {@link String} {@link Map} based on template expressions. Multiple concrete 
 * implementations can be combined to add or modify {@link Map} entries.</p>
 */
public abstract class AbstractProcessorBuildObjectMap extends AbstractProcessor {
	private SimpleExpression rootExpression;
	private LinkedHashMap<String,TemplateExpression> templateExpressions;
	
	/**
	 * This method retrieves the current object map, creating a new one
	 * if it does not yet exist in the current {@link Context}. It will
	 * then invoke the {@link #updateMap(Context, Map)} method to update
	 * the target map. 
	 */
	@Override
	protected final boolean process(Context context) {
		IContextObjectMap ctx = context.as(IContextObjectMap.class);
		LinkedHashMap<String,Object> map = ctx.getObjectMap();
		if ( map == null ) {
			map = new LinkedHashMap<String,Object>(templateExpressions.size());
			ctx.setObjectMap(map);
		}
		return updateMap(context, map);
	}

	/**
	 * Concrete implementations must implement this method in order to update
	 * the given map based on the current {@link Context} and configured
	 * expressions.
	 * @param context
	 * @param map
	 * @return
	 */
	protected abstract boolean updateMap(Context context, Map<String,Object> map);

	/**
	 * Get the configured root expression.
	 * @return
	 */
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	/**
	 * Configure the root expression.
	 * @param rootExpression
	 */
	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	/**
	 * Get the configured {@link Map} with {@link TemplateExpression} instances
	 * @return
	 */
	public Map<String, TemplateExpression> getTemplateExpressions() {
		return templateExpressions;
	}

	/**
	 * Configure the {@link Map} with {@link TemplateExpression} instances
	 * @return
	 */
	public void setTemplateExpressions(LinkedHashMap<String, TemplateExpression> templateExpressions) {
		this.templateExpressions = templateExpressions;
	}
	
	/**
	 * This interface can be used with the {@link Context#as(Class)} method to allow
	 * access to the current object map.
	 */
	public static interface IContextObjectMap {
		public void setObjectMap(LinkedHashMap<String, Object> map);
		public LinkedHashMap<String, Object> getObjectMap();
	}
}
