/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.ssc.processor.composite;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.api.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.api.util.spring.expression.SimpleExpression;
import com.fortify.api.util.spring.expression.TemplateExpression;
import com.fortify.processrunner.common.bugtracker.issue.IIssueStateDetailsRetriever;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssueAndIssueStateDetailsRetriever;
import com.fortify.processrunner.common.source.vulnerability.IVulnerabilityUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.processor.CompositeOrderedProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.appversion.ISSCApplicationVersionFilter;
import com.fortify.processrunner.ssc.appversion.ISSCApplicationVersionFilterFactory;
import com.fortify.processrunner.ssc.appversion.SSCApplicationVersionBugTrackerNameFilter;
import com.fortify.processrunner.ssc.appversion.SSCApplicationVersionCustomTagFilter;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;
import com.fortify.processrunner.ssc.json.preprocessor.SSCFilterOnBugURL;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithOnDemandBugURLFromCustomTag;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithOnDemandIssueDetails;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;

/**
 * This class holds all SSC-related configuration properties used to submit vulnerabilities
 * to a bug tracker or other external system, and performing issue state management.
 * Based on these configuration properties, this class provides various functionalities
 * like checking the current {@link Context}, generating filters and vulnerability enrichers,
 * and updating SSC vulnerabilities with information about submitted bug tracker issues. 
 * 
 * @author Ruud Senden
 *
 */
public class SSCBugTrackerProcessorConfiguration implements IVulnerabilityUpdater, ISSCApplicationVersionFilterFactory {
	private static final Log LOG = LogFactory.getLog(SSCBugTrackerProcessorConfiguration.class);
	private String filterStringForVulnerabilitiesToBeSubmitted = null;
	private Map<SimpleExpression,Pattern> regExFiltersForVulnerabilitiesToBeSubmitted = null;
	private String bugLinkCustomTagName = null;
	private boolean addNativeBugLink = false;
	private Map<String,TemplateExpression> extraCustomTags = null;
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState();
	
	/**
	 * Check whether current application version has the correct configuration
	 * for updating vulnerability state based on our configuration.
	 * @param context
	 * @return
	 */
	public boolean checkContext(Context context) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		if ( StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			List<String> customTagNames = conn.api().customTag().getApplicationVersionCustomTagNames(applicationVersionId);
			if ( customTagNames==null || !customTagNames.contains(getBugLinkCustomTagName()) ) {
				throw new IllegalStateException("Configured custom tag "+getBugLinkCustomTagName()+" is not available for application version "+applicationVersionId);
			}
		} else if ( isAddNativeBugLink() ) {
			String bugTrackerName = conn.api().bugTracker().getApplicationVersionBugTrackerShortName(applicationVersionId);
			if ( !"Add Existing Bugs".equals(bugTrackerName) ) {
				throw new IllegalStateException("Either custom tag name or the 'Add Existing Bugs' SSC bug tracker needs to be configured");
			}
		}
		return true;
	}
	
	/**
	 * Generate SSC application version filter based on either {@link #bugLinkCustomTagName}
	 * or availability of the 'Add Existing Bugs' bug tracker integration, if either is configured.
	 */
	public Collection<ISSCApplicationVersionFilter> getSSCApplicationVersionFilters(Context context) {
		if ( StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			SSCApplicationVersionCustomTagFilter filter = new SSCApplicationVersionCustomTagFilter();
			filter.setCustomTagNames(new HashSet<String>(Arrays.asList(getBugLinkCustomTagName())));
			return Arrays.asList((ISSCApplicationVersionFilter)filter);
		} else if ( isAddNativeBugLink() ) {
			SSCApplicationVersionBugTrackerNameFilter filter = new SSCApplicationVersionBugTrackerNameFilter();
			filter.setBugTrackerPluginNames(new HashSet<String>(Arrays.asList("Add Existing Bugs")));
			return Arrays.asList((ISSCApplicationVersionFilter)filter);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the full SSC filter string for vulnerabilities that need to be submitted to the bug tracker
	 * @return
	 */
	public String getFullSSCFilterStringForVulnerabilitiesToBeSubmitted(boolean ignorePreviouslySubmittedIssues) {
		String result = getFilterStringForVulnerabilitiesToBeSubmitted();
		if ( ignorePreviouslySubmittedIssues && StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			result = StringUtils.isBlank(result) ? "" : (result+" ");
			result += getBugLinkCustomTagName()+":<none>";
		}
		// SSC doesn't allow filtering on bugURL, so this is handled in createFilterForVulnerabilitiesToBeSubmitted
		return result;
	}
	
	/**
	 * Get the full SSC filter string for vulnerabilities that have been previously submitted
	 * @return
	 */
	public String getFullSSCFilterStringForVulnerabilitiesAlreadySubmitted() {
		if ( StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			return getBugLinkCustomTagName()+":!<none>";
		} else {
			return null;
		}
		// SSC doesn't allow filtering on bugURL, so this is handled in createFilterForVulnerabilitiesAlreadySubmitted
	}
	
	/**
	 * Get filters to include only vulnerabilities that need to be submitted to the bug tracker
	 */
	public IProcessor getFiltersForVulnerabilitiesToBeSubmitted(boolean ignorePreviouslySubmittedIssues) {
		CompositeOrderedProcessor result = new CompositeOrderedProcessor();
		if ( ignorePreviouslySubmittedIssues && isAddNativeBugLink() ) {
			result.addProcessors(new SSCFilterOnBugURL(true));
		}
		if ( getRegExFiltersForVulnerabilitiesToBeSubmitted()!=null ) {
			result.addProcessors(FilterRegEx.createFromMap("CurrentVulnerability", getRegExFiltersForVulnerabilitiesToBeSubmitted(), false));
		}
		return result;
	}
	
	/**
	 * Get filters to include only vulnerabilities that have been previously submitted
	 */
	public IProcessor getFiltersForVulnerabilitiesAlreadySubmitted() {
		CompositeOrderedProcessor result = new CompositeOrderedProcessor();
		if ( isAddNativeBugLink() ) {
			result.addProcessors(new SSCFilterOnBugURL(false));
		}
		return result;
	}
	
	public IProcessor getEnrichersForVulnerabilitiesToBeSubmitted() {
		return getDefaultEnrichers();
	}
	
	public IProcessor getEnrichersForVulnerabilitiesAlreadySubmitted() {
		CompositeProcessor result = new CompositeProcessor(getDefaultEnrichers());
		result.addProcessors(new SSCProcessorEnrichWithOnDemandBugURLFromCustomTag(getBugLinkCustomTagName()));
		return result;
	}
	
	public IProcessor getDefaultEnrichers() {
		return new CompositeProcessor(
				new SSCProcessorEnrichWithOnDemandIssueDetails(),
				new SSCProcessorEnrichWithVulnDeepLink(),
				getEnrichWithVulnStateProcessor());
	}

	@SuppressWarnings("unchecked")
	public void updateVulnerabilityStateForNewIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		Map<String,String> customTagValues = getExtraCustomTagValues(context, submittedIssue, issueStateDetailsRetriever, vulnerabilities);
		
		if ( StringUtils.isNotBlank(getBugLinkCustomTagName()) ) {
			customTagValues.put(getBugLinkCustomTagName(), submittedIssue.getDeepLink());
		} 
		if ( !customTagValues.isEmpty() ) {
			conn.api().customTag().setCustomTagValues(applicationVersionId, customTagValues, vulnerabilities);
			LOG.info("[SSC] Updated custom tag values for SSC vulnerabilities");
		}
		if ( isAddNativeBugLink() ) {
			Map<String, Object> issueDetails = new HashMap<String, Object>();
			issueDetails.put("existingBugLink", submittedIssue.getDeepLink());
			List<String> issueInstanceIds = ContextSpringExpressionUtil.evaluateExpression(context, vulnerabilities, "#root.![issueInstanceId]", List.class);
			conn.api().bugTracker().fileBug(applicationVersionId, issueDetails, issueInstanceIds);
			LOG.info("[SSC] Added bug link for SSC vulnerabilities using 'Add Existing Bugs' bug tracker");
		}
	}

	public void updateVulnerabilityStateForExistingIssue(Context context, String bugTrackerName, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		Map<String,String> customTagValues = getExtraCustomTagValues(context, submittedIssue, issueStateDetailsRetriever, vulnerabilities);
		if ( !customTagValues.isEmpty() ) {
			IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ctx.getSSCApplicationVersionId();
			conn.api().customTag().setCustomTagValues(applicationVersionId, customTagValues, vulnerabilities);
			LOG.info("[SSC] Updated custom tag values for "+vulnerabilities.size()+" SSC vulnerabilities");
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" }) // TODO Can we refactor such that we don't need to suppress these warnings?
	private Map<String,String> getExtraCustomTagValues(Context context, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<?> issueStateDetailsRetriever, Collection<Object> vulnerabilities) {
		Map<String,String> result = new HashMap<String,String>();
		if ( getExtraCustomTags()!=null ) {
			List<String> availableCustomTagNames = getCustomTagNames(context);
			SubmittedIssueAndIssueStateDetailsRetriever<?> data = new SubmittedIssueAndIssueStateDetailsRetriever(context, submittedIssue, issueStateDetailsRetriever);
			for ( Map.Entry<String, TemplateExpression> entry : getExtraCustomTags().entrySet() ) {
				if ( availableCustomTagNames.contains(entry.getKey()) ) {
					String customTagName = entry.getKey();
					String newCustomTagValue = ContextSpringExpressionUtil.evaluateExpression(context, data, entry.getValue(), String.class);
					if ( needCustomTagUpdate(context, customTagName, newCustomTagValue, vulnerabilities) ) {
						result.put(entry.getKey(), newCustomTagValue);
					}
				}
			}
		}
		return result;
	}
	
	private boolean needCustomTagUpdate(Context context, String customTagName, String newCustomTagValue, Collection<Object> vulnerabilities) {
		return ContextSpringExpressionUtil.evaluateExpression(context, vulnerabilities, 
				"#this.?[" // Filter list of vulnerabilities 
				+"details.customTagValues.?[customTagName=='"+customTagName+"'].size()==0" // Where custom tag is not yet set
				+" || " // Or
				+"details.customTagValues.?[customTagName=='"+customTagName+"' && textValue!='"+newCustomTagValue+"'].size()>0" // Where current value is different than new value
				+"].size()>0" // Return true if there are any such vulnerabilities, false if there are none
				, Boolean.class);
	}

	private List<String> getCustomTagNames(Context context) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		return conn.api().customTag().getApplicationVersionCustomTagNames(applicationVersionId);
	}
	
	
	public void setIsVulnerabilityOpenExpression(SimpleExpression isVulnerabilityOpenExpression) {
		this.enrichWithVulnStateProcessor.setIsVulnerabilityOpenExpression(isVulnerabilityOpenExpression);
	}

	public String getFilterStringForVulnerabilitiesToBeSubmitted() {
		return filterStringForVulnerabilitiesToBeSubmitted;
	}

	public void setFilterStringForVulnerabilitiesToBeSubmitted(String sscFilterStringForVulnerabilitiesToBeSubmitted) {
		this.filterStringForVulnerabilitiesToBeSubmitted = sscFilterStringForVulnerabilitiesToBeSubmitted;
	}

	public Map<SimpleExpression, Pattern> getRegExFiltersForVulnerabilitiesToBeSubmitted() {
		return regExFiltersForVulnerabilitiesToBeSubmitted;
	}

	public void setRegExFiltersForVulnerabilitiesToBeSubmitted(Map<SimpleExpression, Pattern> regExFiltersForVulnerabilitiesToBeSubmitted) {
		this.regExFiltersForVulnerabilitiesToBeSubmitted = regExFiltersForVulnerabilitiesToBeSubmitted;
	}

	public String getBugLinkCustomTagName() {
		return bugLinkCustomTagName;
	}

	public void setBugLinkCustomTagName(String bugLinkCustomTagName) {
		this.bugLinkCustomTagName = bugLinkCustomTagName;
	}

	public boolean isAddNativeBugLink() {
		return addNativeBugLink;
	}

	public void setAddNativeBugLink(boolean addNativeBugLink) {
		this.addNativeBugLink = addNativeBugLink;
	}

	public Map<String, TemplateExpression> getExtraCustomTags() {
		return extraCustomTags;
	}

	public void setExtraCustomTags(Map<String, TemplateExpression> extraCustomTags) {
		this.extraCustomTags = extraCustomTags;
	}

	public SSCProcessorEnrichWithVulnState getEnrichWithVulnStateProcessor() {
		return enrichWithVulnStateProcessor;
	}
	
	
}
