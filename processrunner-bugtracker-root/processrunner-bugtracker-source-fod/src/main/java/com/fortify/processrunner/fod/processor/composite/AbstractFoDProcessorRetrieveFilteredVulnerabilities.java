package com.fortify.processrunner.fod.processor.composite;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithExtraFoDData;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnTopLevelField;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;

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
 * vulnerability using {@link IContextFoD#getFoDCurrentVulnerability()}.</p>
 */
public abstract class AbstractFoDProcessorRetrieveFilteredVulnerabilities extends AbstractCompositeProcessor {
	private Set<String> extraFields = new HashSet<String>();
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	
	@Override
	protected void addCompositeContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		FoDConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
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

	protected FoDProcessorEnrichWithExtraFoDData createAddJSONDataProcessor() {
		FoDProcessorEnrichWithExtraFoDData result = new FoDProcessorEnrichWithExtraFoDData();
		result.setFields(getExtraFields());
		return result;
	}
	
	protected IProcessor createAddVulnDeepLinkProcessor() {
		return new FoDProcessorEnrichWithVulnDeepLink();
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
}
