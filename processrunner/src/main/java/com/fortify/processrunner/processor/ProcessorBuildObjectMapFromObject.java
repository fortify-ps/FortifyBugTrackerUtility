package com.fortify.processrunner.processor;

import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractProcessorBuildStringMap} implementation will
 * evaluate all configured {@link TemplateExpression} instances on some root
 * object, and add these values together under the corresponding key to the
 * target object map.</p>
 * 
 * <p>If a root expression has been configured, this will be evaluated on the
 * current {@link Context} instance and used as the root object on which the
 * {@link TemplateExpression} instances will be evaluated. Otherwise, the 
 * current {@link Context} instance will be used as the root object.</p> 
 */
public class ProcessorBuildObjectMapFromObject extends AbstractProcessorBuildObjectMap {
	private boolean append = false;
	
	@Override
	protected boolean updateMap(Context context, Map<String,Object> map) {
		Map<String,TemplateExpression> templateExpressions = getTemplateExpressions();
		if ( templateExpressions != null ) {
			SimpleExpression rootExpression = getRootExpression();
			Object root = rootExpression==null ? context : 
				SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
			for ( Map.Entry<String, TemplateExpression> entry : templateExpressions.entrySet() ) {
				String key = entry.getKey();
				Object value = SpringExpressionUtil.evaluateExpression(root, entry.getValue(), Object.class);
				if ( !isAppend() ) {
					map.put(key, value);
				} else {
					Object oldValue = map.getOrDefault(key, "");
					if ( !(oldValue instanceof String) || !(value instanceof String) ) {
						throw new RuntimeException("Cannot append non-String values");
					}
					map.put(key, ""+oldValue+value);
				}
			}
		}
		return true;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
	
	
}
