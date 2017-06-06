package com.fortify.processrunner.ssc.processor.composite;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.common.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.filter.FilterRegEx;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.appversion.ISSCApplicationVersionFilter;
import com.fortify.processrunner.ssc.appversion.SSCApplicationVersionFilterBugTracker;
import com.fortify.processrunner.ssc.appversion.SSCApplicationVersionFilterCustomTag;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCSource;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithBugDataFromCustomTag;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithIssueDetails;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnTopLevelField;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on SSC vulnerability state. It will retrieve all SSC vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 * 
 * @author Ruud Senden
 */
@Component
public class SSCProcessorUpdateBugTrackerState extends AbstractCompositeProcessor {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private String customTagName = "BugLink";
	
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	/**
	 * Generate SSC application version filter based on either {@link #customTagName} (if configured)
	 * or availability of the 'Add Existing Bugs' bug tracker integration.
	 * 
	 * TODO Remove code duplication between this class and {@link SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker} 
	 */
	public Collection<ISSCApplicationVersionFilter> getSSCApplicationVersionFilters(Context context) {
		if ( getCustomTagName()!=null ) {
			SSCApplicationVersionFilterCustomTag filter = new SSCApplicationVersionFilterCustomTag();
			filter.setCustomTagNames(new HashSet<String>(Arrays.asList(getCustomTagName())));
			return Arrays.asList((ISSCApplicationVersionFilter)filter);
		} else {
			SSCApplicationVersionFilterBugTracker filter = new SSCApplicationVersionFilterBugTracker();
			filter.setBugTrackerPluginNames(new HashSet<String>(Arrays.asList("Add Existing Bugs")));
			return Arrays.asList((ISSCApplicationVersionFilter)filter);
		}
	}
	
	/**
	 * This method checks whether the current application version either has the
	 * {@link #customTagName} custom tag (if configured), or has the 'Add Existing Bugs'
	 * bug tracker integration enabled.
	 * 
	 * TODO Remove code duplication between this class and {@link SSCProcessorSubmitFilteredVulnerabilitiesToBugTracker} 
	 */
	@Override
	protected boolean preProcess(Context context) {
		if ( getCustomTagName()==null ) {
			IContextSSCSource ctx = context.as(IContextSSCSource.class);
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String applicationVersionId = ctx.getSSCApplicationVersionId();
			String bugTrackerName = conn.getApplicationVersionBugTrackerShortName(applicationVersionId);
			if ( !"Add Existing Bugs".equals(bugTrackerName) ) {
				throw new IllegalStateException("Either custom tag name or the 'Add Existing Bugs' SSC bug tracker needs to be configured");
			}
		}
		return super.preProcess(context);
	}
	
	@Override
	protected void addCompositeContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			createFilters(),
			new SSCProcessorEnrichWithIssueDetails(),
			new SSCProcessorEnrichWithVulnDeepLink(),
			new SSCProcessorEnrichWithBugDataFromCustomTag(getCustomTagName()),
			getVulnState(),
			getUpdateIssueStateProcessor()
		);
		result.getIssueSearchOptions().setIncludeHidden(false);
		result.getIssueSearchOptions().setIncludeRemoved(true);
		result.getIssueSearchOptions().setIncludeSuppressed(true);
		return result;
	}

	private IProcessor createFilters() {
		IProcessor result;
		if ( getCustomTagName()!=null ) {
			result = new SSCFilterOnTopLevelField(getCustomTagName(), "<none>", true);
		} else {
			// TODO Check whether SSC allows to filter on whether a bug has been submitted via a native integration
			Map<String, Pattern> filters = new HashMap<String, Pattern>(1);
			filters.put("bugURL", Pattern.compile("^\\S+$"));
			result = new FilterRegEx("CurrentVulnerability", filters);
		}
		return result;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}

	public String getCustomTagName() {
		return customTagName;
	}

	public void setCustomTagName(String customTagName) {
		this.customTagName = customTagName;
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		updateIssueStateProcessor.setForceGrouping(true);
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(SSCProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugLink"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	
	@Autowired(required=false)
	public void setConfiguration(SSCBugTrackerProcessorConfiguration config) {
		setCustomTagName(config.getCustomTagName());
		getVulnState().setIsVulnerabilityOpenExpression(config.getIsVulnerabilityOpenExpression());
	}
}
