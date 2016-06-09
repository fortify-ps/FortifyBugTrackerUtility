package com.fortify.processrunner.fod.processor.composite;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.fod.processor.FoDFilterOnTopLevelFields;
import com.fortify.processrunner.fod.processor.FoDFilterSubmittedToBugTracker;
import com.fortify.processrunner.fod.processor.FoDProcessorAddJSONData;
import com.fortify.processrunner.fod.processor.FoDProcessorAddVulnDeepLink;
import com.fortify.processrunner.fod.processor.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This composite {@link IProcessor} implementation combines various
 * {@link IProcessor} implementations for retrieving and filtering
 * FoD vulnerabilities, and processing each vulnerability using the
 * {@link IProcessor} implementation configured via 
 * {@link #setVulnerabilityProcessor(IProcessor)}. Various filters
 * can be defined using the {@link #setTopLevelFieldRegExFilters(Map)},
 * {@link #setTopLevelFieldRegExFilters(Map)} and 
 * {@link #setAllFieldRegExFilters(Map)} methods.
 */
public class FoDProcessorRetrieveFilteredVulnerabilities extends AbstractCompositeProcessor {
	private Set<String> extraFields = new HashSet<String>();
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public IProcessor[] getProcessors() {
		return new IProcessor[] {
			createRootVulnerabilityArrayProcessor()
		};
	}
	
	protected FoDProcessorRetrieveVulnerabilities createRootVulnerabilityArrayProcessor() {
		return new FoDProcessorRetrieveVulnerabilities(
			createTopLevelFieldSimpleFilter(),
			createTopLevelFieldRegExFilter(),
			createAddVulnDeepLinkProcessor(),
			createAddJSONDataProcessor(),
			new FoDFilterSubmittedToBugTracker(),
			createSubLevelFieldRegExFilter(),
			getVulnerabilityProcessor()
		);
	}

	protected IProcessor createAddJSONDataProcessor() {
		FoDProcessorAddJSONData result = new FoDProcessorAddJSONData();
		result.setFields(getExtraFields());
		// Always add summary, as it is required by FoDFilterSubmittedToBugTracker
		result.getFields().add("summary");
		return result;
	}
	
	protected IProcessor createAddVulnDeepLinkProcessor() {
		return new FoDProcessorAddVulnDeepLink();
	}

	protected IProcessor createSubLevelFieldRegExFilter() {
		return new FilterRegEx("FoDCurrentVulnerability", getAllFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldRegExFilter() {
		return new FilterRegEx("FoDCurrentVulnerability", getTopLevelFieldRegExFilters());
	}

	protected IProcessor createTopLevelFieldSimpleFilter() {
		return new FoDFilterOnTopLevelFields(getTopLevelFieldSimpleFilters());
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
	
	
}
