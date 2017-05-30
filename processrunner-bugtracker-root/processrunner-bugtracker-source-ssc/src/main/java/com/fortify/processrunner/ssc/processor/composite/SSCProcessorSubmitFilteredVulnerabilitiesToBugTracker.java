package com.fortify.processrunner.ssc.processor.composite;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSC;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnTopLevelField;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;

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
	private String customTagName = "BugLink";
	private AbstractProcessorSubmitIssueForVulnerabilities submitIssueProcessor;
	
	@Override
	protected CompositeProcessor createTopLevelFieldFilters() {
		CompositeProcessor result = super.createTopLevelFieldFilters();
		result.getProcessors().add(new SSCFilterOnTopLevelField(getCustomTagName(), "<none>", false));
		return result;
	}
	
	@Override
	protected IProcessor getVulnerabilityProcessor() {
		return new CompositeProcessor(getVulnState(), getSubmitIssueProcessor());
	}

	public AbstractProcessorSubmitIssueForVulnerabilities getSubmitIssueProcessor() {
		return submitIssueProcessor;
	}

	@Autowired
	public void setSubmitIssueProcessor(AbstractProcessorSubmitIssueForVulnerabilities submitIssueProcessor) {
		submitIssueProcessor.setIssueSubmittedListener(new SSCIssueSubmittedListener());
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
			IContextSSC ctx = context.as(IContextSSC.class);
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ctx.getSSCApplicationVersionId();
			conn.addBugLinkToCustomTag(applicationVersionId, getCustomTagName(), submittedIssue.getDeepLink(), vulnerabilities);
		}
	}
}
