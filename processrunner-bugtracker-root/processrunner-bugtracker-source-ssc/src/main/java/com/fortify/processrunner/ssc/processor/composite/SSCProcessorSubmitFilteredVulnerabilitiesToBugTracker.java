package com.fortify.processrunner.ssc.processor.composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCSource;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnTopLevelField;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONUtil;

/**
 * This class extends {@link AbstractSSCProcessorRetrieveFilteredVulnerabilities} with the following
 * functionality:
 * <ul>
 *  <li>Add filters to exclude vulnerabilities already submitted to the bug tracker</li>
 *  <li>After submitting a group of vulnerabilities to the bug tracker, set the corresponding
 *      bug link field on the SSC vulnerabilities</li>
 * </ul>
 * Apart from the configuration as documented for {@link AbstractSSCProcessorRetrieveFilteredVulnerabilities},
 * this class can be configured with an additional {@link #customTagName} property identifying the SSC
 * custom field name in which the bug link should be stored.
 */
@Component
public class SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker extends AbstractSSCProcessorRetrieveFilteredVulnerabilities {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private String customTagName = null;
	private IProcessorSubmitIssueForVulnerabilities submitIssueProcessor;
	private boolean issueSubmittedListenerSupported;
	
	@Override
	protected boolean preProcess(Context context) {
		if ( issueSubmittedListenerSupported ) {
			if ( getCustomTagName()==null ) {
				IContextSSCSource ctx = context.as(IContextSSCSource.class);
				SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
				String applicationVersionId = ctx.getSSCApplicationVersionId();
				String bugTrackerName = conn.getBugTrackerShortName(applicationVersionId);
				if ( !"Add Existing Bugs".equals(bugTrackerName) ) {
					throw new IllegalStateException("Either custom tag name or the 'Add Existing Bugs' SSC bug tracker needs to be configured");
				}
			}
		}
		return super.preProcess(context);
	}
	
	@Override
	protected CompositeProcessor createTopLevelFieldFilters() {
		CompositeProcessor result = super.createTopLevelFieldFilters();
		if ( getCustomTagName()!=null ) {
			result.getProcessors().add(new SSCFilterOnTopLevelField(getCustomTagName(), "<none>", false));
		} else {
			// TODO Check whether SSC allows to filter on whether a bug has been submitted via a native integration
			Map<String, Pattern> filters = new HashMap<String, Pattern>(1);
			filters.put("bugURL", Pattern.compile("^$"));
			result.getProcessors().add(new FilterRegEx("CurrentVulnerability", filters));
		}
		return result;
	}
	
	@Override
	protected IProcessor getVulnerabilityProcessor() {
		return new CompositeProcessor(getVulnState(), getSubmitIssueProcessor());
	}

	public IProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return submitIssueProcessor;
	}

	@Autowired
	public void setSubmitIssueProcessor(IProcessorSubmitIssueForVulnerabilities submitIssueProcessor) {
		issueSubmittedListenerSupported = submitIssueProcessor.setIssueSubmittedListener(new SSCIssueSubmittedListener());
		this.submitIssueProcessor = submitIssueProcessor;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	public String getCustomTagName() {
		return customTagName;
	}

	public void setCustomTagName(String customTagName) {
		this.customTagName = customTagName;
	}
	
	@Autowired(required=false)
	public void setConfiguration(SSCBugTrackerProcessorConfiguration config) {
		setAllFieldRegExFilters(config.getAllFieldRegExFilters());
		setCustomTagName(config.getCustomTagName());
		setIncludeIssueDetails(config.isIncludeIssueDetails());
		setTopLevelFieldRegExFilters(config.getTopLevelFieldRegExFilters());
		setTopLevelFieldSimpleFilters(config.getTopLevelFieldSimpleFilters());
		getVulnState().setIsVulnerabilityOpenExpression(config.getIsVulnerabilityOpenExpression());
	}

	private class SSCIssueSubmittedListener implements IIssueSubmittedListener {
		public void issueSubmitted(Context context, String bugTrackerName, SubmittedIssue submittedIssue, Collection<Object> vulnerabilities) {
			IContextSSCSource ctx = context.as(IContextSSCSource.class);
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ctx.getSSCApplicationVersionId();
			if ( getCustomTagName()!=null ) {
				conn.addBugLinkToCustomTag(applicationVersionId, getCustomTagName(), submittedIssue.getDeepLink(), vulnerabilities);
			} else {
				Map<String, Object> issueDetails = new HashMap<String, Object>();
				issueDetails.put("existingBugLink", submittedIssue.getDeepLink());
				List<String> issueInstanceIds = JSONUtil.jsonObjectArrayToList(new JSONArray(vulnerabilities), "issueInstanceId", String.class);
				conn.fileBug(applicationVersionId, issueDetails, issueInstanceIds, null, null);
			}
		}
	}
}
