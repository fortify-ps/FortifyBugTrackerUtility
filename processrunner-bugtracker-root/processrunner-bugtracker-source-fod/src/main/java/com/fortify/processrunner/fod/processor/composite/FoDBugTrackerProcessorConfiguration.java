package com.fortify.processrunner.fod.processor.composite;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithVulnState;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This class holds all configuration properties for 
 * {@link FoDProcessorSubmitFilteredVulnerabilitiesToBugTracker} and
 * {@link FoDProcessorUpdateBugTrackerState} to allow for
 * easy Spring-based configuration.
 * 
 * @author Ruud Senden
 *
 */
public class FoDBugTrackerProcessorConfiguration {
	private Set<String> extraFields = new HashSet<String>();
	private Map<String,String> topLevelFieldSimpleFilters;
	private Map<String,Pattern> topLevelFieldRegExFilters;
	private Map<String,Pattern> allFieldRegExFilters;
	private boolean useFoDCommentForSubmittedBugs = false;
	private SimpleExpression isVulnerabilityOpenExpression = FoDProcessorEnrichWithVulnState.DEFAULT_IS_VULNERABILITY_OPEN_EXPRESSION;
	
	public Set<String> getExtraFields() {
		return extraFields;
	}
	public void setExtraFields(Set<String> extraFields) {
		this.extraFields = extraFields;
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
	public boolean isUseFoDCommentForSubmittedBugs() {
		return useFoDCommentForSubmittedBugs;
	}
	public void setUseFoDCommentForSubmittedBugs(boolean useFoDCommentForSubmittedBugs) {
		this.useFoDCommentForSubmittedBugs = useFoDCommentForSubmittedBugs;
	}
	public SimpleExpression getIsVulnerabilityOpenExpression() {
		return isVulnerabilityOpenExpression;
	}
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.isVulnerabilityOpenExpression = isVulnerabilityOpenExpression;
	}
}
