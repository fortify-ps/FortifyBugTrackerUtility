package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public class ProcessorBuildObjectMapFromGroupedObjects extends AbstractCompositeProcessor {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	@Override
	public List<IProcessor> getProcessors() {
		ProcessorBuildObjectMapFromObject fieldsProcessor = new ProcessorBuildObjectMapFromObject();
		fieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup[0]"));
		fieldsProcessor.setTemplateExpressions(getFields());
		
		ProcessorBuildObjectMapFromIterable appendedFieldsProcessor = new ProcessorBuildObjectMapFromIterable();
		appendedFieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
		appendedFieldsProcessor.setTemplateExpressions(getAppendedFields());
		return new ArrayList<IProcessor>(Arrays.asList(new IProcessor[]{fieldsProcessor, appendedFieldsProcessor}));
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
