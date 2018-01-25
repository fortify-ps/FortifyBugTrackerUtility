/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.jira.connection;

import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.bugtracker.issue.SubmittedIssue;
import com.fortify.util.rest.connection.AbstractRestConnection;
import com.fortify.util.rest.connection.AbstractRestConnectionConfig;
import com.fortify.util.rest.connection.IRestConnectionBuilder;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;

public final class JiraRestConnection extends AbstractRestConnection {
	private static final Log LOG = LogFactory.getLog(JiraRestConnection.class);
	
	public JiraRestConnection(JiraRestConnectionConfig<?> config) {
		super(config);
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
	
	/**
	 * This method returns an {@link JiraRestConnectionBuilder} instance
	 * that allows for building {@link JiraRestConnection} instances.
	 * @return
	 */
	public static final JiraRestConnectionBuilder builder() {
		return new JiraRestConnectionBuilder();
	}
	
	/**
	 * This class provides a builder pattern for configuring an {@link JiraRestConnection} instance.
	 * It re-uses builder functionality from {@link AbstractRestConnectionConfig}, and adds a
	 * {@link #build()} method to build an {@link JiraRestConnection} instance.
	 * 
	 * @author Ruud Senden
	 */
	public static final class JiraRestConnectionBuilder extends JiraRestConnectionConfig<JiraRestConnectionBuilder> implements IRestConnectionBuilder<JiraRestConnection> {
		@Override
		public JiraRestConnection build() {
			return new JiraRestConnection(this);
		}
	}
	
}
