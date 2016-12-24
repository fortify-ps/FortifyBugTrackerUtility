package com.fortify.processrunner.archer.processor;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.context.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.IRestConnection;
import com.sun.jersey.api.client.WebResource;

/**
 * <p>This abstract {@link IProcessor} implementation allows for submitting
 * an issue to Archer. Concrete implementations must implement the 
 * {@link #getIssueToBeSubmitted(Context)} method to return the JSONObject 
 * with issue data to be posted to Archer.</p>
 * 
 * <p>Once an issue has been submitted, the {@link Context} will be updated
 * with information as specified in the {@link IContextBugTracker}
 * interface.</p>
 */
// TODO (Low) Make this a concrete class and retrieve JSONObject from context?
// TODO (Crit) Update this method to call the correct Archer API, and get the issue id and deep link
public abstract class AbstractProcessorSubmitIssueToArcher extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorSubmitIssueToArcher.class);
	
	@Override
	protected boolean process(Context context) {
		IContextArcher contextArcher = context.as(IContextArcher.class);
		IRestConnection conn = contextArcher.getArcherConnectionRetriever().getConnection();
		JSONObject issueToBeSubmitted = getIssueToBeSubmitted(context);
		LOG.trace("Submitting issue to Archer: "+issueToBeSubmitted);
		WebResource.Builder resource = conn.getBaseResource().path("/api/core/content").entity(issueToBeSubmitted);
		JSONObject submitResult = conn.executeRequest(HttpMethod.POST, resource, JSONObject.class);
		String submittedIssueId = submitResult.optString("ContentId");
		
		String submittedIssueBrowserURL = conn.getBaseResource()
				.path("/browse/").path(submittedIssueId).getURI().toString();
		contextArcher.setSubmittedIssue(new SubmittedIssue(submittedIssueId, submittedIssueBrowserURL));
		return true;
	}
	
	protected abstract JSONObject getIssueToBeSubmitted(Context context);
}
