package com.fortify.processrunner.processor;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterAppendValuesFromExpressionMap;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This abstract class allows for building an object map from grouped objects.
 * Objects are grouped by our parent {@link AbstractProcessorGroupByExpressions}
 * class. In our {@link #processGroup(Context, List)} method we build an object 
 * map for the current group, and then call {@link #processMap(Context, List, LinkedHashMap)}
 * to allow concrete implementation to further process the object map.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessorBuildObjectMapFromGroupedObjects extends AbstractProcessorGroupByExpressions {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	@Override
	protected boolean processGroup(Context context, String groupName, List<Object> currentGroup) {
		StandardEvaluationContext sec = ContextSpringExpressionUtil.createStandardEvaluationContext(context);
		LinkedHashMap<String, Object> map = new MapBuilder()
				.addMapUpdater(new MapUpdaterPutValuesFromExpressionMap(sec, currentGroup.get(0), getFields()))
				.addMapUpdater(new MapUpdaterAppendValuesFromExpressionMap(sec, currentGroup, getAppendedFields()))
				.build(new LinkedHashMap<String, Object>());
		return processMap(context, currentGroup, map);
	}
	
	protected abstract boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map);

	public LinkedHashMap<String,TemplateExpression> getFields() {
		return fields;
	}

	public void setFields(LinkedHashMap<String,TemplateExpression> fields) {
		this.fields = fields;
	}

	public LinkedHashMap<String,TemplateExpression> getAppendedFields() {
		return appendedFields;
	}

	public void setAppendedFields(LinkedHashMap<String,TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}

}
