package com.fortify.processrunner.fod.processor.composite;

import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.fod.processor.FoDFilterOnTopLevelFields;
import com.fortify.processrunner.fod.processor.FoDFilterSubmittedToBugTracker;
import com.fortify.processrunner.fod.processor.FoDProcessorAddConnectionToContext;
import com.fortify.processrunner.fod.processor.FoDProcessorAddOnDemandJSONDataMultiSmallRequest;
import com.fortify.processrunner.fod.processor.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;
import com.fortify.util.rest.IRestConnectionFactory;

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
	private IRestConnectionFactory connectionFactory;
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public IProcessor[] getProcessors() {
		return new IProcessor[] {
			new ProcessorPrintMessage(null, "Retrieving vulnerabilities for release ${FoDReleaseId} from ${FoDConnection.baseUrl}\n", null),
			createAddConnectionToContextProcessor(),
			createRootVulnerabilityArrayProcessor()
		};
	}
	
	protected FoDProcessorAddConnectionToContext createAddConnectionToContextProcessor() {
		return new FoDProcessorAddConnectionToContext(getConnectionFactory());
	}
	
	protected FoDProcessorRetrieveVulnerabilities createRootVulnerabilityArrayProcessor() {
		return new FoDProcessorRetrieveVulnerabilities(
			createTopLevelFieldSimpleFilter(),
			createTopLevelFieldRegExFilter(),
			createAddOnDemandJSONDataProcessor(),
			new FoDFilterSubmittedToBugTracker(),
			createSubLevelFieldRegExFilter(),
			getVulnerabilityProcessor()
		);
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

	protected IProcessor createAddOnDemandJSONDataProcessor() {
		// TODO return new FoDProcessorAddOnDemandJSONDataSingleLargeRequest(); if boolean property set
		return new FoDProcessorAddOnDemandJSONDataMultiSmallRequest();
	}

	public IRestConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(IRestConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public Map<String, String> getTopLevelFieldSimpleFilters() {
		return topLevelFieldSimpleFilters;
	}

	public void setTopLevelFieldSimpleFilters(
			Map<String, String> topLevelFieldSimpleFilters) {
		this.topLevelFieldSimpleFilters = topLevelFieldSimpleFilters;
	}

	public Map<String, Pattern> getTopLevelFieldRegExFilters() {
		return topLevelFieldRegExFilters;
	}

	public void setTopLevelFieldRegExFilters(
			Map<String, Pattern> topLevelFieldRegExFilters) {
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
	
	
	
}
