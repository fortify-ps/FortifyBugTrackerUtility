package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public abstract class AbstractProcessorBuildObjectMapFromGroupedObjects extends AbstractProcessorBuildObjectMap {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	@Override
	protected Collection<IMapUpdater> getMapUpdaters(Context context) {
		Collection<IMapUpdater> result = new ArrayList<IMapUpdater>(2);
		result.add(new MapUpdaterPutValuesFromExpressionMap(SpringExpressionUtil.parseSimpleExpression("CurrentGroup[0]"), getFields()));
		result.add(new MapUpdaterAppendValuesFromExpressionMap(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"), getAppendedFields()));
		return result;
	}

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
