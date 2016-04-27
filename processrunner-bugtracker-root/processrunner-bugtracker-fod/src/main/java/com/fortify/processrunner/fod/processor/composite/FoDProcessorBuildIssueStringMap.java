package com.fortify.processrunner.fod.processor.composite;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorBuildObjectMap;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

public class FoDProcessorBuildIssueStringMap extends AbstractCompositeProcessor {
	private String grouping;
	private Map<String,String> fields;
	private Map<String,String> appendedFields;
	private IProcessor issueProcessor;
	
	@Override
	public IProcessor[] getProcessors() {
		if ( StringUtils.isNotBlank(getGrouping()) ) {
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
		result.setRootExpression("FoDCurrentVulnerability");
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
		result.setRootExpression("CurrentGroup[0]");
		result.setRootExpressionTemplates(getFields());
		result.setAppenderExpression("CurrentGroup");
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
		result.setRootExpression("FoDCurrentVulnerability");
		result.setRootExpressionTemplates(getFields());
		return result;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public Map<String, String> getAppendedFields() {
		return appendedFields;
	}

	public void setAppendedFields(Map<String, String> appendedFields) {
		this.appendedFields = appendedFields;
	}

	public IProcessor getIssueProcessor() {
		return issueProcessor;
	}

	public void setIssueProcessor(IProcessor issueProcessor) {
		this.issueProcessor = issueProcessor;
	}
	
	

}
