package com.fortify.processrunner.fod.processor.composite;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilitiesGrouped;
import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilitiesNonGrouped;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorBuildObjectMapFromIterable;
import com.fortify.processrunner.processor.ProcessorBuildObjectMapFromObject;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions.IContextGrouping;
import com.fortify.processrunner.processor.ProcessorPrintMessage;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link IProcessor} implementation allows mapping FoD 
 * vulnerabilities to an object map based on configurable 
 * {@link TemplateExpression} instances. Basically this allows 
 * for extracting FoD vulnerability data into field data to be 
 * submitted to a bug tracker.</p>
 * 
 * <p>If no {@link #groupTemplateExpression} expression is configured, a single field 
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
 * <p>The {@link #appendedFields} expressions will be evaluated on either
 * the current vulnerability (if grouping is disabled), or every 
 * vulnerability within the current group (if grouping is enabled). 
 * The resulting value will be appended to the current field value.</p>
 */
public abstract class AbstractFoDProcessorSubmitVulnerabilities extends AbstractProcessor {
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	
	private TemplateExpression groupTemplateExpression;
	private Map<String,TemplateExpression> fields;
	private Map<String,TemplateExpression> appendedFields;
	private IProcessor initializedProcessor;
	
	@Override
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		getFod().addContextProperties(contextProperties, context);
		getSubmitVulnerabilityProcessor().addContextProperties(contextProperties, context);
		String defaultExpr = getGroupTemplateExpression()==null?null:getGroupTemplateExpression().getExpressionString(); 
		contextProperties.add(new ContextProperty(IContextGrouping.PRP_GROUP_TEMPLATE_EXPRESSION, "Expression used to define issue groups", context, defaultExpr, false));
	}
	
	@Override
	protected boolean preProcess(Context context) {
		return _process(Phase.PRE_PROCESS, context);
	}
	
	@Override
	protected boolean process(Context context) {
		return _process(Phase.PROCESS, context);
	}
	
	@Override
	protected boolean postProcess(Context context) {
		return _process(Phase.POST_PROCESS, context);
	}
	
	private final boolean _process(Phase phase, Context context) {
		return getProcessor(context).process(phase, context);
	}

	protected IProcessor getProcessor(Context context) {
		if ( initializedProcessor == null ) {
			TemplateExpression groupTemplateExpression = getGroupTemplateExpression(context);
			getFod().setVulnerabilityProcessor(
					groupTemplateExpression == null 
					? createNonGroupedIssueProcessor()
					: createGroupedIssueProcessor(groupTemplateExpression));
			initializedProcessor = getFod();
		}
		return initializedProcessor;
	}

	protected TemplateExpression getGroupTemplateExpression(Context context) {
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		String groupTemplateExpressionString = context.as(IContextGrouping.class).getGroupTemplateExpression();
		if ( StringUtils.isNotBlank(groupTemplateExpressionString) ) {
			groupTemplateExpression = SpringExpressionUtil.parseTemplateExpression(groupTemplateExpressionString);
		}
		return groupTemplateExpression;
	}

	protected IProcessor createGroupedIssueProcessor(TemplateExpression groupTemplateExpression) {
		ProcessorGroupByExpressions result = new ProcessorGroupByExpressions();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setGroupTemplateExpression(groupTemplateExpression);
		result.setGroupProcessor(new CompositeProcessor(
				createGroupedBuildStringMapProcessor(), 
				getSubmitVulnerabilityProcessor(),
				new FoDProcessorAddCommentToVulnerabilitiesGrouped(),
				new ProcessorPrintMessage(null, "Submitted ${CurrentGroup.size()} vulnerabilities to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}", null)
			));
		return result;
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

	protected IProcessor createNonGroupedIssueProcessor() {
		return new CompositeProcessor(
			createNonGroupedBuildStringMapProcessor(),
			getSubmitVulnerabilityProcessor(),
			new FoDProcessorAddCommentToVulnerabilitiesNonGrouped(),
			new ProcessorPrintMessage(null, "Submitted vulnerability ${FoDCurrentVulnerability.vulnId} to ${SubmittedIssueBugTrackerName} issue ${SubmittedIssueId}", null)
		);
	}

	protected IProcessor createNonGroupedBuildStringMapProcessor() {
		ProcessorBuildObjectMapFromObject fieldsProcessor = new ProcessorBuildObjectMapFromObject();
		fieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		fieldsProcessor.setTemplateExpressions(getFields());
		
		ProcessorBuildObjectMapFromObject appendedFieldsProcessor = new ProcessorBuildObjectMapFromObject();
		appendedFieldsProcessor.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		appendedFieldsProcessor.setTemplateExpressions(getAppendedFields());
		appendedFieldsProcessor.setAppend(true);
		return new CompositeProcessor(fieldsProcessor, appendedFieldsProcessor);
	}
	
	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	public void setGroupTemplateExpresison(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
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
	
	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}
	
	protected abstract IProcessor getSubmitVulnerabilityProcessor();
}
