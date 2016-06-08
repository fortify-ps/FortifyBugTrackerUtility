package com.fortify.processrunner.jira.processor;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.context.IContextSubmittedIssueData;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.jira.connection.JiraConnectionRetriever;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.IRestConnection;
import com.sun.jersey.api.client.WebResource;

/**
 * <p>This abstract {@link IProcessor} implementation allows for submitting
 * an issue to JIRA. It allows for setting up a JIRA connection by 
 * configuring a {@link JiraConnectionRetriever} instance, optionally
 * setting/overriding the JIRA user name and password based on context
 * properties.</p>
 * 
 * <p>Concrete implementations must implement the {@link #getIssueToBeSubmitted(Context)}
 * method to return the JSONObject with issue data to be posted to JIRA.</p>
 * 
 * <p>Once an issue has been submitted, the {@link Context} will be updated
 * with information as specified in the {@link IContextSubmittedIssueData}
 * interface.</p>
 */
// TODO (Low) Make this a concrete class and retrieve JSONObject from context?
public abstract class AbstractProcessorJiraSubmitIssue extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorJiraSubmitIssue.class);

	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		result.add(new ContextProperty("JiraUserName", "Jira user name", false));
		result.add(new ContextProperty("JiraPassword", "Jira password", false));
		return result;
	}
	
	@Override
	protected boolean process(Context context) {
		IContextJira contextJira = context.as(IContextJira.class);
		IRestConnection conn = contextJira.getJiraConnectionRetriever().getConnection();
		JSONObject issueToBeSubmitted = getIssueToBeSubmitted(context);
		LOG.trace("Submitting issue to JIRA: "+issueToBeSubmitted);
		WebResource.Builder resource = conn.getBaseResource().path("/rest/api/latest/issue").entity(issueToBeSubmitted);
		JSONObject submitResult = conn.executeRequest(HttpMethod.POST, resource, JSONObject.class);
		String submittedIssueKey = submitResult.optString("key");
		
		String submittedIssueBrowserURL = conn.getBaseResource()
				.path("/browse/").path(submittedIssueKey).getURI().toString();
		contextJira.setSubmittedIssueId(submittedIssueKey);
		contextJira.setSubmittedIssueBugTrackerName("JIRA");
		contextJira.setSubmittedIssueLocation(submittedIssueBrowserURL);
		return true;
	}
	
	protected abstract JSONObject getIssueToBeSubmitted(Context context);
}
