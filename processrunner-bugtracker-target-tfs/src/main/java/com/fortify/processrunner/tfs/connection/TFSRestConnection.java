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
package com.fortify.processrunner.tfs.connection;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.fortify.processrunner.bugtracker.common.target.issue.SubmittedIssue;
import com.fortify.util.rest.connection.AbstractRestConnection;
import com.fortify.util.rest.connection.AbstractRestConnectionConfig;
import com.fortify.util.rest.connection.IRestConnectionBuilder;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

public final class TFSRestConnection extends AbstractRestConnection {
	private static final Log LOG = LogFactory.getLog(TFSRestConnection.class);
	
	public TFSRestConnection(TFSRestConnectionConfig<?> config) {
		super(config);
	}
	
	@Override
	protected boolean doPreemptiveBasicAuthentication() {
		return true;
	}
	
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder).accept("application/json; api-version=1.0", "application/json-patch+json; api-version=1.0");
	}
	
	public SubmittedIssue submitIssue(String collection, String project, String workItemType, Map<String, Object> fields) {
		LOG.trace(String.format("[TFS] Submitting issue: %s", fields));
		WebTarget target = getBaseResource()
				.path(collection).path(project).path("/_apis/wit/workitems").path("$"+workItemType);
		
		JSONMap submitResult = executeRequest("PATCH", target, Entity.entity(getOperations(fields), "application/json-patch+json"), JSONMap.class);
		
		String submittedIssueId = submitResult.get("id", String.class);
		String submittedIssueBrowserURL = SpringExpressionUtil.evaluateExpression(submitResult, "_links.html.href", String.class);
		return new SubmittedIssue(submittedIssueId, submittedIssueBrowserURL);
	}
	
	public TFSIssueState getIssueState(String collection, SubmittedIssue submittedIssue) {
		JSONMap json = getWorkItemDetails(collection, submittedIssue, "System.WorkItemType", "System.State", "System.Reason");
		return new TFSIssueState(
				SpringExpressionUtil.evaluateExpression(json, "fields['System.WorkItemType']", String.class),
				SpringExpressionUtil.evaluateExpression(json, "fields['System.State']", String.class),
				SpringExpressionUtil.evaluateExpression(json, "fields['System.Reason']", String.class)
		);
	}
	
	public boolean updateIssueData(String collection, SubmittedIssue submittedIssue, Map<String, Object> fields) {
		String issueId = getIssueId(submittedIssue);
		if ( issueId == null ) {
			return false;
		} else {
			LOG.trace(String.format("[TFS] Updating issue data for %s: %s", submittedIssue.getDeepLink(), fields)); 
			WebTarget target = getBaseResource()
					.path(collection).path("/_apis/wit/workitems").path(issueId);
			executeRequest("PATCH", target, Entity.entity(getOperations(fields), "application/json-patch+json"), null);
			return true;
		}
	}
	
	private JSONList getOperations(Map<String, Object> fields) {
		JSONList result = new JSONList();
		for ( Map.Entry<String, Object> field : fields.entrySet() ) {
			JSONMap op = new JSONMap();
			op.put("op", "add");
			op.put("path", "/fields/"+field.getKey());
			op.put("value", field.getValue());
			result.add(op);
		}
		return result;
	}

	public String getWorkItemType(String collection, SubmittedIssue submittedIssue) {
		JSONMap workItemDetails = getWorkItemDetails(collection, submittedIssue, "System.WorkItemType");
		return workItemDetails==null?null:SpringExpressionUtil.evaluateExpression(workItemDetails, "fields['System.WorkItemType']", String.class);
	}
	
	private JSONMap getWorkItemDetails(String collection, SubmittedIssue submittedIssue, String... fields) {
		LOG.trace(String.format("[TFS] Retrieving issue data for %s", submittedIssue.getDeepLink())); 
		String issueId = getIssueId(submittedIssue);
		if ( issueId == null ) {
			LOG.warn(String.format("[TFS] Cannot get work item id from URL %s", submittedIssue.getDeepLink()));
			return null;
		} else {
			WebTarget target = getBaseResource()
					.path(collection).path("/_apis/wit/workitems").path(issueId);
			if ( fields!=null ) {
				target = target.queryParam("fields", StringUtils.join(fields, ","));
			}
			return executeRequest(HttpMethod.GET, target, JSONMap.class);
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
	
	/**
	 * This method returns an {@link TFSRestConnectionBuilder} instance
	 * that allows for building {@link TFSRestConnection} instances.
	 * @return
	 */
	public static final TFSRestConnectionBuilder builder() {
		return new TFSRestConnectionBuilder();
	}
	
	/**
	 * This class provides a builder pattern for configuring an {@link TFSRestConnection} instance.
	 * It re-uses builder functionality from {@link AbstractRestConnectionConfig}, and adds a
	 * {@link #build()} method to build an {@link TFSRestConnection} instance.
	 * 
	 * @author Ruud Senden
	 */
	public static final class TFSRestConnectionBuilder extends TFSRestConnectionConfig<TFSRestConnectionBuilder> implements IRestConnectionBuilder<TFSRestConnection> {
		@Override
		public TFSRestConnection build() {
			return new TFSRestConnection(this);
		}
	}
	
}
