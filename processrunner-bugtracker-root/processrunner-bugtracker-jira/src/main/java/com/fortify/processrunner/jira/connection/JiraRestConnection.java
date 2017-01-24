package com.fortify.processrunner.jira.connection;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.util.rest.RestConnection;
import com.sun.jersey.api.client.WebResource;

public final class JiraRestConnection extends RestConnection {
	private static final Log LOG = LogFactory.getLog(JiraRestConnection.class);
	
	public JiraRestConnection(String baseUrl, Credentials credentials) {
		super(baseUrl, credentials);
	}
	
	@Override
	protected boolean doPreemptiveBasicAuthentication() {
		return true;
	}
	
	public SubmittedIssue submitIssue(JSONObject issueData) {
		LOG.trace("Submitting issue to JIRA: "+issueData);
		WebResource.Builder resource = getBaseResource().path("/rest/api/latest/issue").entity(issueData);
		JSONObject submitResult = executeRequest(HttpMethod.POST, resource, JSONObject.class);
		String submittedIssueKey = submitResult.optString("key");
		
		String submittedIssueBrowserURL = getBaseResource()
				.path("/browse/").path(submittedIssueKey).getURI().toString();
		return new SubmittedIssue(submittedIssueKey, submittedIssueBrowserURL);
	}
	
}