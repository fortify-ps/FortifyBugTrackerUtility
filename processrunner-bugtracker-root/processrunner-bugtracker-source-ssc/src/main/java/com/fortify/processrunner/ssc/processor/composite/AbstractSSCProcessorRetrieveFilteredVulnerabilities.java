package com.fortify.processrunner.ssc.processor.composite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithIssueDetails;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnTopLevelField;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;

/**
 * <p>This composite {@link IProcessor} implementation combines various
 * {@link IProcessor} implementations for retrieving and filtering
 * FoD vulnerabilities, and processing each vulnerability using the 
 * {@link IProcessor} implementation returned by the 
 * {@link #createVulnerabilityProcessor()} that needs to be implemented
 * by subclasses.</p> 
 * 
 * <p>Various filters can be defined using the {@link #setTopLevelFieldRegExFilters(Map)},
 * {@link #setTopLevelFieldRegExFilters(Map)} and {@link #setAllFieldRegExFilters(Map)} 
 * methods.</p>
 * 
 * <p>The configured vulnerability processor can access the current
 * vulnerability using {@link IContextCurrentVulnerability#getCurrentVulnerability()}.</p>
 */
public abstract class AbstractSSCProcessorRetrieveFilteredVulnerabilities extends AbstractCompositeProcessor {
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	private boolean includeIssueDetails;
	
	@Override
	protected void addCompositeContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		return new SSCProcessorRetrieveVulnerabilities(
			createTopLevelFieldFilters(),
			createAddVulnDeepLinkProcessor(),
			createAddJSONDataProcessor(),
			createSubLevelFieldFilters(),
			getVulnerabilityProcessor()
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
	
	protected abstract IProcessor getVulnerabilityProcessor();

	protected SSCProcessorEnrichWithIssueDetails createAddJSONDataProcessor() {
		SSCProcessorEnrichWithIssueDetails result = null;
		if ( isIncludeIssueDetails() ) {
			result = new SSCProcessorEnrichWithIssueDetails();
		}
		return result;
	}
	
	protected IProcessor createAddVulnDeepLinkProcessor() {
		return new SSCProcessorEnrichWithVulnDeepLink();
	}

	protected IProcessor createSubLevelFieldRegExFilter() {
		return new FilterRegEx("CurrentVulnerability", getAllFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldRegExFilter() {
		return new FilterRegEx("CurrentVulnerability", getTopLevelFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldSimpleFilter() {
		CompositeProcessor result = new CompositeProcessor();
		for ( Map.Entry<String, String> entry : getTopLevelFieldSimpleFilters().entrySet() ) {
			result.getProcessors().add(new SSCFilterOnTopLevelField(entry.getKey(), entry.getValue(), false));
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

	public boolean isIncludeIssueDetails() {
		return includeIssueDetails;
	}

	public void setIncludeIssueDetails(boolean includeIssueDetails) {
		this.includeIssueDetails = includeIssueDetails;
	}
}
