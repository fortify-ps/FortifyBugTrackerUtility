package com.fortify.processrunner.tfs.connection;

import java.net.URI;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.utils.URLEncodedUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.rest.RestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.sun.jersey.api.client.WebResource;

public final class TFSRestConnection extends RestConnection {
	private static final Log LOG = LogFactory.getLog(TFSRestConnection.class);
	
	public TFSRestConnection(String baseUrl, Credentials credentials) {
		super(baseUrl, credentials);
	}
	
	@Override
	protected boolean doPreemptiveBasicAuthentication() {
		return true;
	}
	
	@Override
	public WebResource.Builder updateBuilder(WebResource.Builder builder) {
		return super.updateBuilder(builder).accept("application/json; api-version=1.0", "application/json-patch+json; api-version=1.0");
	}
	
	public SubmittedIssue submitIssue(String collection, String project, String workItemType, JSONArray operations) {
		LOG.trace(String.format("[TFS] Submitting issue: %s", operations));
		WebResource.Builder resource = getBaseResource()
				.path(collection).path(project).path("/_apis/wit/workitems").path("$"+workItemType)
				.type("application/json-patch+json").entity(operations);
		
		JSONObject submitResult = executeRequest("PATCH", resource, JSONObject.class);
		
		String submittedIssueId = submitResult.optString("id");
		String submittedIssueBrowserURL = SpringExpressionUtil.evaluateExpression(submitResult, "_links.html.href", String.class);
		return new SubmittedIssue(submittedIssueId, submittedIssueBrowserURL);
	}
	
	public TFSIssueState getIssueState(String collection, SubmittedIssue submittedIssue) {
		JSONObject json = getWorkItemDetails(collection, submittedIssue, "System.WorkItemType", "System.State", "System.Reason");
		return new TFSIssueState(
				SpringExpressionUtil.evaluateExpression(json, "fields['System.WorkItemType']", String.class),
				SpringExpressionUtil.evaluateExpression(json, "fields['System.State']", String.class),
				SpringExpressionUtil.evaluateExpression(json, "fields['System.Reason']", String.class)
		);
	}
	
	public boolean updateIssueData(String collection, SubmittedIssue submittedIssue, JSONArray operations) {
		String issueId = getIssueId(submittedIssue);
		if ( issueId == null ) {
			return false;
		} else {
			LOG.trace(String.format("[TFS] Updating issue data for %s: %s", submittedIssue.getDeepLink(), operations)); 
			WebResource.Builder resource = getBaseResource()
					.path(collection).path("/_apis/wit/workitems").path(issueId)
					.type("application/json-patch+json").entity(operations);
			executeRequest("PATCH", resource, null);
			return true;
		}
	}
	
	public String getWorkItemType(String collection, SubmittedIssue submittedIssue) {
		JSONObject workItemDetails = getWorkItemDetails(collection, submittedIssue, "System.WorkItemType");
		return workItemDetails==null?null:SpringExpressionUtil.evaluateExpression(workItemDetails, "fields['System.WorkItemType']", String.class);
	}
	
	private JSONObject getWorkItemDetails(String collection, SubmittedIssue submittedIssue, String... fields) {
		LOG.trace(String.format("[TFS] Retrieving issue data for %s", submittedIssue.getDeepLink())); 
		String issueId = getIssueId(submittedIssue);
		if ( issueId == null ) {
			LOG.warn(String.format("[TFS] Cannot get work item id from URL %s", submittedIssue.getDeepLink()));
			return null;
		} else {
			WebResource resource = getBaseResource()
					.path(collection).path("/_apis/wit/workitems").path(issueId);
			if ( fields!=null ) {
				resource = resource.queryParam("fields", StringUtils.join(fields, ","));
			}
			return executeRequest(HttpMethod.GET, resource, JSONObject.class);
		}
	}
	
	private String getIssueId(SubmittedIssue submittedIssue) {
		String id = submittedIssue.getId();
		if ( StringUtils.isBlank(id) ) {
			String deepLink = submittedIssue.getDeepLink();
			List<NameValuePair> params = URLEncodedUtils.parse(URI.create(deepLink), "UTF-8");

			for (NameValuePair param : params) {
			  if ( "id".equals(param.getName()) ) {
				  return param.getValue();
			  }
			}
		}
		return id;
	}
	
	public class TFSIssueState {
		private final String workItemType;
		private final String state;
		private final String reason;
		
		public TFSIssueState(String workItemType, String state, String reason) {
			super();
			this.workItemType = workItemType;
			this.state = state;
			this.reason = reason;
		}
		
		
		public String getWorkItemType() {
			return workItemType;
		}
		
		public String getState() {
			return state;
		}
		
		public String getReason() {
			return reason;
		}
	}
	
}