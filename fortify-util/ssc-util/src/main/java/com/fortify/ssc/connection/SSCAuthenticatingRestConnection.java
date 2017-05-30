package com.fortify.ssc.connection;

import java.util.Collection;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for SSC.
 */
public class SSCAuthenticatingRestConnection extends SSCBasicRestConnection {
	private final ISSCTokenFactory tokenFactory;

	public SSCAuthenticatingRestConnection(String baseUrl, String token, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		if ( StringUtils.isBlank(token) ) {
			throw new RuntimeException("SSC authentication token cannot be blank");
		}
		this.tokenFactory = new SSCTokenFactoryTokenCredentials(token);
	}
	
	public SSCAuthenticatingRestConnection(String baseUrl, String userName, String password, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		if ( StringUtils.isBlank(userName) ) {
			throw new RuntimeException("SSC username cannot be blank");
		}
		if ( StringUtils.isBlank(password) ) {
			throw new RuntimeException("SSC password cannot be blank");
		}
		this.tokenFactory = new SSCTokenFactoryUserCredentials(baseUrl, userName, password, proxyConfig);
	}
	
	/**
	 * Update the {@link Builder} to add the Authorization header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.header("Authorization", "FortifyToken "+tokenFactory.getToken());
	}

	public void addBugLinkToCustomTag(String applicationVersionId, String customTagName, String deepLink, Collection<Object> vulns) {
		// TODO Simplify this code
		JSONObject request = new JSONObject();
		JSONObject customTagAudit = new JSONObject();
		JSONObjectBuilder builder = new JSONObjectBuilder();
		builder.updateJSONObjectWithPropertyPath(request, "type", "AUDIT_ISSUE");

		JSONArray issues = new JSONArray();
		for ( Object vuln : vulns ) {
			JSONObject issue = new JSONObject();
			builder.updateJSONObjectWithPropertyPath(issue, "id", SpringExpressionUtil.evaluateExpression(vuln, "id", Long.class));
			builder.updateJSONObjectWithPropertyPath(issue, "revision", SpringExpressionUtil.evaluateExpression(vuln, "revision", Long.class));
			issues.put(issue);
		}
		builder.updateJSONObjectWithPropertyPath(request, "values.issues", issues);
		builder.updateJSONObjectWithPropertyPath(customTagAudit, "customTagGuid", getCustomTagGuid(customTagName));
		builder.updateJSONObjectWithPropertyPath(customTagAudit, "textValue", deepLink);
		builder.updateJSONObjectWithPropertyPath(request, "values.customTagAudit", new Object[]{customTagAudit});
		executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issues/action")
				.entity(request, "application/json"), JSONObject.class);
		
	}

	private String getCustomTagGuid(String customTagName) {
		JSONObject data = executeRequest(HttpMethod.GET, getBaseResource().path("/api/v1/customTags").queryParam("fields", "guid,name"), JSONObject.class);
		return JSONUtil.mapValue(SpringExpressionUtil.evaluateExpression(data, "data", JSONArray.class), "name.toLowerCase()", customTagName.toLowerCase(), "guid", String.class);
	}

	public void updateIssueSearchOptions(String applicationVersionId, IssueSearchOptions issueSearchOptions) {
		executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issueSearchOptions")
				.entity(issueSearchOptions.getJSONRequestData(), "application/json"), JSONObject.class);
	}
}