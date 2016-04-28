package com.fortify.processrunner.fod.processor.composite;

import java.util.Map;

import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorBuildObjectMap;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.processrunner.processor.ProcessorPrintMessage;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public class FoDProcessorBuildIssueStringMap extends AbstractCompositeProcessor {
	private TemplateExpression grouping;
	private Map<String,TemplateExpression> fields;
	private Map<String,TemplateExpression> appendedFields;
	private IProcessor issueProcessor;
	
	@Override
	public IProcessor[] getProcessors() {
		if ( getGrouping()!=null ) {
			return createGroupedIssueProcessor();
		} else {
			return createNonGroupedIssueProcessor();
		}
	}

	protected IProcessor[] createGroupedIssueProcessor() {
		return new IProcessor[]{
			createGroupingProcessor()
		};
	}

	protected IProcessor createGroupingProcessor() {
		ProcessorGroupByExpressions result = new ProcessorGroupByExpressions();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setGroupTemplateExpression(getGrouping());
		result.setGroupProcessor(new CompositeProcessor(
				createGroupedStatusMessageProcessor(), 
				createGroupedBuildStringMapProcessor(), 
				getIssueProcessor()));
		return result;
	}

	protected IProcessor createGroupedStatusMessageProcessor() {
		return new ProcessorPrintMessage("Grouped ${TotalCount} vulnerabilities in ${Groups==null?'0':Groups.size()} issues\n",null,null);
	}

	protected IProcessor createGroupedBuildStringMapProcessor() {
		ProcessorBuildObjectMap result = new ProcessorBuildObjectMap();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup[0]"));
		result.setRootExpressionTemplates(getFields());
		result.setAppenderExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
		result.setAppenderExpressionTemplates(getAppendedFields());
		return result;
	}

	protected IProcessor[] createNonGroupedIssueProcessor() {
		return new IProcessor[]{
			createNonGroupedBuildStringMapProcessor(),
			getIssueProcessor()
		};
	}

	protected IProcessor createNonGroupedBuildStringMapProcessor() {
		ProcessorBuildObjectMap result = new ProcessorBuildObjectMap();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setRootExpressionTemplates(getFields());
		return result;
	}

	public TemplateExpression getGrouping() {
		return grouping;
	}

	public void setGrouping(TemplateExpression grouping) {
		this.grouping = grouping;
	}

	public Map<String, TemplateExpression> getFields() {
		return fields;
	}

	public void setFields(Map<String, TemplateExpression> fields) {
		this.fields = fields;
	}

	public Map<String, TemplateExpression> getAppendedFields() {
		return appendedFields;
	}

	public void setAppendedFields(Map<String, TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}

	public IProcessor getIssueProcessor() {
		return issueProcessor;
	}

	public void setIssueProcessor(IProcessor issueProcessor) {
		this.issueProcessor = issueProcessor;
	}
	
	

}
