package com.fortify.processrunner.processor;

import java.util.HashMap;
import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for building a 
 * String {@link Map} based on the configured expressions.
 */
public class ProcessorBuildObjectMap extends AbstractProcessor {
	private SimpleExpression rootExpression;
	private SimpleExpression appenderExpression;
	private Map<String,TemplateExpression> rootExpressionTemplates;
	private Map<String,TemplateExpression> appenderExpressionTemplates;
	
	@Override
	protected boolean process(Context context) {
		Map<String,Object> map = new HashMap<String,Object>(rootExpressionTemplates.size());
		if ( getRootExpressionTemplates() != null ) {
			Object rootObject = SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
			for ( Map.Entry<String, TemplateExpression> rootExpressionTemplate : getRootExpressionTemplates().entrySet() ) {
				map.put(rootExpressionTemplate.getKey(), SpringExpressionUtil.evaluateExpression(rootObject, rootExpressionTemplate.getValue(), Object.class));
			}
		}
		if ( getAppenderExpressionTemplates() != null ) {
			Iterable<?> appenderValues = SpringExpressionUtil.evaluateExpression(context, appenderExpression, Iterable.class);
			for ( Object appenderValue : appenderValues ) {
				for ( Map.Entry<String, TemplateExpression> appenderExpressionTemplate : getAppenderExpressionTemplates().entrySet() ) {
					String key = appenderExpressionTemplate.getKey();
					Object value = map.get(key);
					if ( value == null ) { value = ""; }
					if ( !(value instanceof String) ) {
						throw new RuntimeException("Cannot append value to non-String value");
					}
					value = value+""+SpringExpressionUtil.evaluateExpression(appenderValue, appenderExpressionTemplate.getValue(), Object.class);
					map.put(key, value);
				}
			}
		}
		
		context.as(IContextStringMap.class).setObjectMap(map);
		
		return true;
	}

	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public SimpleExpression getAppenderExpression() {
		return appenderExpression;
	}

	public void setAppenderExpression(SimpleExpression appenderExpression) {
		this.appenderExpression = appenderExpression;
	}

	public Map<String, TemplateExpression> getRootExpressionTemplates() {
		return rootExpressionTemplates;
	}

	public void setRootExpressionTemplates(Map<String, TemplateExpression> rootExpressionTemplates) {
		this.rootExpressionTemplates = rootExpressionTemplates;
	}

	public Map<String, TemplateExpression> getAppenderExpressionTemplates() {
		return appenderExpressionTemplates;
	}

	public void setAppenderExpressionTemplates(Map<String, TemplateExpression> appenderExpressionTemplates) {
		this.appenderExpressionTemplates = appenderExpressionTemplates;
	}
	
	public static interface IContextStringMap {
		public void setObjectMap(Map<String, Object> map);
		public Map<String, Object> getObjectMap();
	}
}
