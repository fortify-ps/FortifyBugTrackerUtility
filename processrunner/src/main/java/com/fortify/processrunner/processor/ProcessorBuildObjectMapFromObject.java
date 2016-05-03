package com.fortify.processrunner.processor;

import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractProcessorBuildObjectMap} implementation will
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
	@Override
	protected boolean updateMap(Context context, Map<String,Object> map) {
		Map<String,TemplateExpression> templateExpressions = getTemplateExpressions();
		if ( templateExpressions != null ) {
			SimpleExpression rootExpression = getRootExpression();
			Object root = rootExpression==null ? context : 
				SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
			for ( Map.Entry<String, TemplateExpression> rootExpressionTemplate : templateExpressions.entrySet() ) {
				map.put(rootExpressionTemplate.getKey(), SpringExpressionUtil.evaluateExpression(root, rootExpressionTemplate.getValue(), Object.class));
			}
		}
		return true;
	}
}
