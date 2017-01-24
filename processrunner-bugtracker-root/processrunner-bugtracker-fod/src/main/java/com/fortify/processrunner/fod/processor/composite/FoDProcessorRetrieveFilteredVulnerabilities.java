package com.fortify.processrunner.fod.processor.composite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithExtraFoDData;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnTopLevelField;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions.IContextGrouping;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This composite {@link IProcessor} implementation combines various
 * {@link IProcessor} implementations for retrieving, filtering
 * and optionally grouping FoD vulnerabilities, and processing 
 * each vulnerability using the {@link IProcessor} implementation 
 * configured via {@link #setVulnerabilityProcessor(IProcessor)}.</p> 
 * 
 * <p>Various filters can be defined using the {@link #setTopLevelFieldRegExFilters(Map)},
 * {@link #setTopLevelFieldRegExFilters(Map)} and {@link #setAllFieldRegExFilters(Map)} 
 * methods. Optional grouping can be enabled using the 
 * {@link #setGroupTemplateExpression(TemplateExpression)} method.</p>
 * 
 * <p>The configured vulnerability processor can access the current
 * (group of) vulnerabilities using {@link IContextGrouping#getCurrentGroup()}
 * (also if grouping has not been enabled; in that case each current group
 * will only contain a single vulnerability).</p>
 */
public class FoDProcessorRetrieveFilteredVulnerabilities extends AbstractCompositeProcessor {
	private Set<String> extraFields = new HashSet<String>();
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	private TemplateExpression groupTemplateExpression;
	
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		return new FoDProcessorRetrieveVulnerabilities(
			createTopLevelFieldFilters(),
			createAddVulnDeepLinkProcessor(),
			createAddJSONDataProcessor(),
			createSubLevelFieldFilters(),
			createGroupingProcessor()
		);
	}
	
	protected CompositeProcessor createTopLevelFieldFilters() {
		return new CompositeProcessor(
			createTopLevelFieldSimpleFilter(),
			createTopLevelFieldRegExFilter()
		);
	}
	
	protected CompositeProcessor createSubLevelFieldFilters() {
		return new CompositeProcessor(
			createSubLevelFieldRegExFilter()
		);
	}

	protected IProcessor createGroupingProcessor() {
		ProcessorGroupByExpressions result = new ProcessorGroupByExpressions();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setGroupTemplateExpression(getGroupTemplateExpression());
		result.setGroupProcessor(createGroupProcessor());
		return result;
	}
	
	protected IProcessor createGroupProcessor() {
		return getVulnerabilityProcessor();
	}

	protected FoDProcessorEnrichWithExtraFoDData createAddJSONDataProcessor() {
		FoDProcessorEnrichWithExtraFoDData result = new FoDProcessorEnrichWithExtraFoDData();
		result.setFields(getExtraFields());
		return result;
	}
	
	protected IProcessor createAddVulnDeepLinkProcessor() {
		return new FoDProcessorEnrichWithVulnDeepLink();
	}

	protected IProcessor createSubLevelFieldRegExFilter() {
		return new FilterRegEx("FoDCurrentVulnerability", getAllFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldRegExFilter() {
		return new FilterRegEx("FoDCurrentVulnerability", getTopLevelFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldSimpleFilter() {
		CompositeProcessor result = new CompositeProcessor();
		for ( Map.Entry<String, String> entry : getTopLevelFieldSimpleFilters().entrySet() ) {
			result.getProcessors().add(new FoDFilterOnTopLevelField(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	public Map<String, String> getTopLevelFieldSimpleFilters() {
		return topLevelFieldSimpleFilters;
	}

	public void setTopLevelFieldSimpleFilters(Map<String, String> topLevelFieldSimpleFilters) {
		this.topLevelFieldSimpleFilters = topLevelFieldSimpleFilters;
	}

	public Map<String, Pattern> getTopLevelFieldRegExFilters() {
		return topLevelFieldRegExFilters;
	}

	public void setTopLevelFieldRegExFilters(Map<String, Pattern> topLevelFieldRegExFilters) {
		this.topLevelFieldRegExFilters = topLevelFieldRegExFilters;
	}

	public Map<String, Pattern> getAllFieldRegExFilters() {
		return allFieldRegExFilters;
	}

	public void setAllFieldRegExFilters(Map<String, Pattern> allFieldRegExFilters) {
		this.allFieldRegExFilters = allFieldRegExFilters;
	}

	public IProcessor getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	public void setVulnerabilityProcessor(IProcessor vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}

	/**
	 * @return the extraFields
	 */
	public Set<String> getExtraFields() {
		return extraFields;
	}

	/**
	 * @param extraFields the extraFields to set
	 */
	public void setExtraFields(Set<String> extraFields) {
		this.extraFields = extraFields;
	}

	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	public void setGroupTemplateExpression(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}
}
