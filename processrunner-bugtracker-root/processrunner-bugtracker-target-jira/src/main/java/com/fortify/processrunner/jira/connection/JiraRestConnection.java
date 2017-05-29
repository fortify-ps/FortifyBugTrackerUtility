package com.fortify.processrunner.jira.connection;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;
import com.sun.jersey.api.client.WebResource;

public final class JiraRestConnection extends RestConnection {
	private static final Log LOG = LogFactory.getLog(JiraRestConnection.class);
	
	public JiraRestConnection(String baseUrl, Credentials credentials, ProxyConfiguration proxy) {
		super(baseUrl, credentials);
		setProxy(proxy);
	}
	
	@Override
	protected boolean doPreemptiveBasicAuthentication() {
		return true;
	}
	
	public SubmittedIssue submitIssue(JSONObject issueData) {
		LOG.trace(String.format("[Jira] Submitting issue: %s", issueData));
		WebResource.Builder resource = getBaseResource().path("/rest/api/latest/issue").entity(issueData);
		JSONObject submitResult = executeRequest(HttpMethod.POST, resource, JSONObject.class);
		
		String submittedIssueKey = submitResult.optString("key");
		String submittedIssueBrowserURL = getBaseResource()
				.path("/browse/").path(submittedIssueKey).getURI().toString();
		return new SubmittedIssue(submittedIssueKey, submittedIssueBrowserURL);
	}
	
	public void updateIssueData(SubmittedIssue submittedIssue, JSONObject issueData) {
		LOG.trace(String.format("[Jira] Updating issue data for %s: %s", submittedIssue.getDeepLink(), issueData)); 
		String issueId = getIssueId(submittedIssue);
		WebResource.Builder resource = getBaseResource().path("/rest/api/latest/issue").path(issueId).entity(issueData);
		executeRequest(HttpMethod.PUT, resource, null);
	}

	public String getTransitionId(String issueId, String transitionName) {
		if ( transitionName==null ) { return null; }
		WebResource resource = getBaseResource().path("/rest/api/latest/issue").path(issueId).path("transitions");
		JSONObject result = executeRequest(HttpMethod.GET, resource, JSONObject.class);
		JSONArray transitions = result.optJSONArray("transitions");
		String transitionId = JSONUtil.mapValue(transitions, "name", transitionName, "id", String.class);
		if ( transitionId==null ) {
			LOG.warn(String.format("[Jira] Transition %s does not exist in list of available transitions %s", transitionName, JSONUtil.jsonObjectArrayToList(transitions, "name", String.class)));
		}
		return transitionId;
	}
	
	public boolean transition(SubmittedIssue submittedIssue, String transitionName, String comment) {
		String issueId = getIssueId(submittedIssue);
		String transitionId = getTransitionId(issueId, transitionName);
		if ( transitionId == null ) { return false; }
		
		final JSONObjectBuilder builder = new JSONObjectBuilder();
		WebResource resource = getBaseResource().path("/rest/api/latest/issue").path(issueId).path("transitions");

		// { "transition" : { "id" : <transitionId> } }
		JSONObject jsonRequest = builder.updateJSONObjectWithPropertyPath(new JSONObject(), "transition.id", transitionId);
		
		if ( StringUtils.isNotBlank(comment)) {
			// { "update" : { "comment" : [ { "add" : { "body" : <comment> } } ] }
			new JSONObjectBuilder().updateJSONObjectWithPropertyPath(jsonRequest, "update.comment[].add.body", comment);
		}

		executeRequest(HttpMethod.POST, resource.entity(jsonRequest), JSONObject.class);
		return true;
	}
	
	public JSONObject getIssueDetails(SubmittedIssue submittedIssue, String... fields) {
		String issueId = getIssueId(submittedIssue);
		WebResource resource = getBaseResource().path("/rest/api/latest/issue").path(issueId);
		if ( fields!=null ) {
			resource = resource.queryParam("fields", StringUtils.join(fields, ","));
		}
		return executeRequest(HttpMethod.GET, resource, JSONObject.class);
	}
	
	public JSONObject getIssueState(SubmittedIssue submittedIssue) {
		JSONObject issueDetails = getIssueDetails(submittedIssue, "status", "resolution");
		return issueDetails.optJSONObject("fields");
		// status.name, resolution
	}
	
	private String getIssueId(SubmittedIssue submittedIssue) {
		String id = submittedIssue.getId();
		if ( StringUtils.isBlank(id) ) {
			// TODO (Low) Check whether link indeed looks like a JIRA URL?
			String deepLink = submittedIssue.getDeepLink();
			id = deepLink.substring(deepLink.lastIndexOf('/'));
		}
		return id;
	}
	
}