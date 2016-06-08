package com.fortify.processrunner.fod.processor.composite;

import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorBuildObjectMapFromIterable;
import com.fortify.processrunner.processor.ProcessorBuildObjectMapFromObject;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions.IContextGrouping;
import com.fortify.processrunner.processor.ProcessorPrintMessage;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;
import com.javamex.classmexer.MemoryUtil;

/**
 * <p>This {@link IProcessor} implementation allows mapping FoD 
 * vulnerabilities to an object map based on configurable 
 * {@link TemplateExpression} instances. Basically this allows 
 * for extracting FoD vulnerability data into field data to be 
 * submitted to a bug tracker.</p>
 * 
 * <p>If no {@link #grouping} expression is configured, a single field 
 * {@link Map} will be generated for each individual vulnerability.
 * If a grouping expression is configured, then a single field
 * {@link Map} will be generated for each vulnerability group.</p>
 * 
 * <p>The {@link #fields} and {@link #appendedFields} mappings define
 * field names/keys together with a corresponding {@link TemplateExpression} 
 * instance used to retrieve information from FoD vulnerabilities.</p>
 * 
 * <p>The {@link #fields} expressions will be evaluated on the current 
 * FoD vulnerability (if grouping is disabled) or the first available 
 * FoD vulnerability within a group (if grouping is enabled).</p>
 * 
 * <p>The {@link #appendedFields} expressions are only applicable if 
 * grouping is enabled, and will be evaluated on every vulnerability 
 * within the current group. The resulting value will be appended
 * to the current field value.</p>
 */
public class FoDProcessorBuildIssueObjectMap extends AbstractCompositeProcessor {
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
				createGroupedMemoryUsageProcessor(),
				createGroupedBuildStringMapProcessor(), 
				getIssueProcessor()));
		return result;
	}

	protected IProcessor createGroupedStatusMessageProcessor() {
		// TODO (Low) Make this processor a top-level class in processrunner project so it can be re-used by non-FoD processor chains?
		return new ProcessorPrintMessage("Grouped ${TotalCount} vulnerabilities in ${Groups==null?'0':Groups.size()} issues",null,null);
	}
	
	protected IProcessor createGroupedMemoryUsageProcessor() {
		// TODO (Low) Make this processor a top-level class in processrunner project so it can be re-used by non-FoD processor chains?
		return new ProcessorPrintMessage() {
			@Override
			protected boolean preProcess(Context context) {
				try {
					MultiValueMap<String, Object> groups = context.as(IContextGrouping.class).getGroups();
					if ( groups != null ) {
						printAndLog("Grouped vulnerabilities memory usage: "+MemoryUtil.deepMemoryUsageOf(groups)+" bytes");
					}
				} catch ( IllegalStateException e ) {
					LOG.debug("Agent unavailable; memory information cannot be displayed.\n"
							+"To enable memory information, add -javaagent:path/to/classmexer-0.03.jar to Java command line.\n"
							+"Classmexer can be downloaded from http://www.javamex.com/classmexer/classmexer-0_03.zip");
				}
				return true;
			}
		};
	}

	protected IProcessor createGroupedBuildStringMapProcessor() {
		ProcessorBuildObjectMapFromObject fieldsProcessor = new ProcessorBuildObjectMapFromObject();
		fieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup[0]"));
		fieldsProcessor.setTemplateExpressions(getFields());
		
		ProcessorBuildObjectMapFromIterable appendedFieldsProcessor = new ProcessorBuildObjectMapFromIterable();
		appendedFieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
		appendedFieldsProcessor.setTemplateExpressions(getAppendedFields());
		return new CompositeProcessor(fieldsProcessor, appendedFieldsProcessor);
	}

	protected IProcessor[] createNonGroupedIssueProcessor() {
		return new IProcessor[]{
			createNonGroupedBuildStringMapProcessor(),
			getIssueProcessor()
		};
	}

	protected IProcessor createNonGroupedBuildStringMapProcessor() {
		ProcessorBuildObjectMapFromObject result = new ProcessorBuildObjectMapFromObject();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setTemplateExpressions(getFields());
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
