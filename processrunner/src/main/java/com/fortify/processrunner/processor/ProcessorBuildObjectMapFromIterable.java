package com.fortify.processrunner.processor;

import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractProcessorBuildObjectMap} implementation will
 * evaluate the configured root expression on the current {@link Context}
 * instance to retrieve an {@link Iterable} instance.</p>
 * 
 * <p>On each object returned by the {@link Iterable} instance, the
 * configured {@link TemplateExpression} instances will be evaluated,
 * and appended to the current value for the corresponding map key.</p> 
 */
public class ProcessorBuildObjectMapFromIterable extends AbstractProcessorBuildObjectMap {
	@Override
	protected boolean updateMap(Context context, Map<String,Object> map) {
		Map<String,TemplateExpression> templateExpressions = getTemplateExpressions();
		if ( templateExpressions != null ) {
			Iterable<?> iterable = SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Iterable.class);
			for ( Object object : iterable ) {
				for ( Map.Entry<String, TemplateExpression> entry : templateExpressions.entrySet() ) {
					String key = entry.getKey();
					Object value = map.get(key);
					if ( value == null ) { value = ""; }
					if ( !(value instanceof String) ) {
						throw new RuntimeException("Cannot append value to non-String value");
					}
					value = value+""+SpringExpressionUtil.evaluateExpression(object, entry.getValue(), String.class);
					map.put(key, value);
				}
			}
		}
		return true;
	}
}
