package com.fortify.processrunner.jira.connection;

import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;

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
	
	public SubmittedIssue submitIssue(Map<String, Object> issueFields) {
		LOG.trace(String.format("[Jira] Submitting issue: %s", issueFields));
		WebTarget target = getBaseResource().path("/rest/api/latest/issue");
		JSONMap request = getIssueRequestData(issueFields);
		JSONMap submitResult = executeRequest(HttpMethod.POST, target, Entity.entity(request, "application/json"), JSONMap.class);
		
		String submittedIssueKey = submitResult.get("key", String.class);
		String submittedIssueBrowserURL = getBaseResource()
				.path("/browse/").path(submittedIssueKey).getUri().toString();
		return new SubmittedIssue(submittedIssueKey, submittedIssueBrowserURL);
	}

	private JSONMap getIssueRequestData(Map<String, Object> issueFields) {
		JSONMap request = new JSONMap();
		request.getOrCreateJSONMap("fields").putPaths(issueFields);
		return request;
	}
	
	public void updateIssueData(SubmittedIssue submittedIssue, Map<String, Object> issueFields) {
		LOG.trace(String.format("[Jira] Updating issue data for %s: %s", submittedIssue.getDeepLink(), issueFields)); 
		String issueId = getIssueId(submittedIssue);
		WebTarget target = getBaseResource().path("/rest/api/latest/issue").path(issueId);
		executeRequest(HttpMethod.PUT, target, Entity.entity(getIssueRequestData(issueFields), "application/json"), null);
	}

	public String getTransitionId(String issueId, String transitionName) {
		if ( transitionName==null ) { return null; }
		WebTarget target = getBaseResource().path("/rest/api/latest/issue").path(issueId).path("transitions");
		JSONMap result = executeRequest(HttpMethod.GET, target, JSONMap.class);
		JSONList transitions = result.get("transitions", JSONList.class);
		String transitionId = transitions.mapValue("name", transitionName, "id", String.class);
		if ( transitionId==null ) {
			LOG.warn(String.format("[Jira] Transition %s does not exist in list of available transitions %s", transitionName, transitions.getValues("name", String.class)));
		}
		return transitionId;
	}
	
	public boolean transition(SubmittedIssue submittedIssue, String transitionName, String comment) {
		String issueId = getIssueId(submittedIssue);
		String transitionId = getTransitionId(issueId, transitionName);
		if ( transitionId == null ) { return false; }
		
		WebTarget target = getBaseResource().path("/rest/api/latest/issue").path(issueId).path("transitions");

		// { "transition" : { "id" : <transitionId> } }
		JSONMap request = new JSONMap();
		request.putPath("transition.id", transitionId);
		
		if ( StringUtils.isNotBlank(comment)) {
			// { "update" : { "comment" : [ { "add" : { "body" : <comment> } } ] }
			request.putPath("update.comment[].add.body", comment);
		}

		executeRequest(HttpMethod.POST, target, Entity.entity(request, "application/json"), JSONMap.class);
		return true;
	}
	
	public JSONMap getIssueDetails(SubmittedIssue submittedIssue, String... fields) {
		String issueId = getIssueId(submittedIssue);
		WebTarget target = getBaseResource().path("/rest/api/latest/issue").path(issueId);
		if ( fields!=null ) {
			target = target.queryParam("fields", StringUtils.join(fields, ","));
		}
		return executeRequest(HttpMethod.GET, target, JSONMap.class);
	}
	
	public JSONMap getIssueState(SubmittedIssue submittedIssue) {
		JSONMap issueDetails = getIssueDetails(submittedIssue, "status", "resolution");
		return issueDetails.get("fields", JSONMap.class);
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