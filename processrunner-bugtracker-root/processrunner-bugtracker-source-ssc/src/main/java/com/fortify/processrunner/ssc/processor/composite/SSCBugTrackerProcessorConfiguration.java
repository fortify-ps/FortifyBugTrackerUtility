package com.fortify.processrunner.ssc.processor.composite;

import java.util.Map;
import java.util.regex.Pattern;

import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This class holds all configuration properties for 
 * {@link SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker} and
 * {@link SSCProcessorUpdateBugTrackerState} to allow for
 * easy Spring-based configuration.
 * 
 * @author Ruud Senden
 *
 */
public class SSCBugTrackerProcessorConfiguration {
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	private boolean includeIssueDetails;
	private String customTagName = "BugLink";
	private SimpleExpression isVulnerabilityOpenExpression = SSCProcessorEnrichWithVulnState.DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	
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
	public String getCustomTagName() {
		return customTagName;
	}
	public void setCustomTagName(String customTagName) {
		this.customTagName = customTagName;
	}
	public SimpleExpression getIsVulnerabilityOpenExpression() {
		return isVulnerabilityOpenExpression;
	}
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.isVulnerabilityOpenExpression = isVulnerabilityOpenExpression;
	}
}
