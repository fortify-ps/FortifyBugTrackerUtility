package com.fortify.processrunner.ssc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.ssc.appversion.ISSCApplicationVersionFilter;
import com.fortify.processrunner.ssc.appversion.ISSCApplicationVersionFilterFactory;
import com.fortify.processrunner.ssc.appversion.SSCApplicationVersionBugTrackerNameFilter;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCTarget;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class submits a set of vulnerabilities through a native SSC bug tracker integration.
 * The fields to be submitted are configured through our {@link AbstractProcessorBuildObjectMapFromGroupedObjects}
 * superclass.
 * 
 * @author Ruud Senden
 *
 */
public class ProcessorSSCSubmitIssueForVulnerabilities extends AbstractProcessorBuildObjectMapFromGroupedObjects implements IProcessorSubmitIssueForVulnerabilities, ISSCApplicationVersionFilterFactory {
	private static final Log LOG = LogFactory.getLog(ProcessorSSCSubmitIssueForVulnerabilities.class);
	private String sscBugTrackerName;
	
	public ProcessorSSCSubmitIssueForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	public String getBugTrackerName() {
		return getSscBugTrackerName()+" through SSC";
	}
	
	public Collection<ISSCApplicationVersionFilter> getSSCApplicationVersionFilters(Context context) {
		return Arrays.asList((ISSCApplicationVersionFilter)new SSCApplicationVersionBugTrackerNameFilter(getSscBugTrackerName()));
	}

	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener) {
		// We ignore the issueSubmittedListener since we don't need to update SSC state
		// after submitting a bug through SSC. We return false to indicate that we don't
		// support an issue submitted listener.
		return false;
	}
	
	
	@Override
	protected void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCBugTracker.PRP_USER_NAME, getSscBugTrackerName()+" user name (required if SSC bug tracker requires authentication)", false).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCBugTracker.PRP_PASSWORD, getSscBugTrackerName()+" password", true).readFromConsole(true).isPassword(true).ignoreIfPropertyNotSet(IContextSSCBugTracker.PRP_USER_NAME));
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		if ( conn.isBugTrackerAuthenticationRequired(applicationVersionId) ) {
			conn.authenticateForBugFiling(applicationVersionId, ctx.getSSCBugTrackerUserName(), ctx.getSSCBugTrackerPassword());
		}
		List<String> issueInstanceIds = new ArrayList<String>();
		for ( Object issue : currentGroup ) {
			issueInstanceIds.add(SpringExpressionUtil.evaluateExpression(issue, "issueInstanceId", String.class));
		}
		JSONMap result = conn.fileBug(ctx.getSSCApplicationVersionId(), map, issueInstanceIds);
		String bugLink = SpringExpressionUtil.evaluateExpression(result, "data?.values?.externalBugDeepLink", String.class);
		LOG.info(String.format("[SSC] Submitted %d vulnerabilities via SSC to %s", currentGroup.size(), bugLink));
		return true;
	}
	
	public String getSscBugTrackerName() {
		return sscBugTrackerName;
	}

	public void setSscBugTrackerName(String sscBugTrackerName) {
		this.sscBugTrackerName = sscBugTrackerName;
	}
	
	private interface IContextSSCBugTracker {
		public static final String PRP_USER_NAME = "SSCBugTrackerUserName";
		public static final String PRP_PASSWORD = "SSCBugTrackerUserName";
		
		public void setSSCBugTrackerUserName(String userName);
		public String getSSCBugTrackerUserName();
		public void setSSCBugTrackerPassword(String password);
		public String getSSCBugTrackerPassword();
		
	}
}
