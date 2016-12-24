package com.fortify.processrunner.jira.processor;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.context.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.IRestConnection;
import com.sun.jersey.api.client.WebResource;

/**
 * <p>This abstract {@link IProcessor} implementation allows for submitting
 * an issue to JIRA. Concrete implementations must implement the 
 * {@link #getIssueToBeSubmitted(Context)} method to return the JSONObject 
 * with issue data to be posted to JIRA.</p>
 * 
 * <p>Once an issue has been submitted, the {@link Context} will be updated
 * with information as specified in the {@link IContextBugTracker}
 * interface.</p>
 */
// TODO (Low) Make this a concrete class and retrieve JSONObject from context?
public abstract class AbstractProcessorSubmitIssueToJira extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorSubmitIssueToJira.class);
	
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
		contextJira.setSubmittedIssue(new SubmittedIssue(submittedIssueKey, submittedIssueBrowserURL));
		return true;
	}
	
	protected abstract JSONObject getIssueToBeSubmitted(Context context);
}
