package com.fortify.processrunner.octane.connection;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class provides an authenticated REST connection for Octane.
 * 
 */
public class OctaneAuthenticatingRestConnection extends OctaneBasicRestConnection {
	private final IOctaneCredentials credentials;
	public OctaneAuthenticatingRestConnection(String baseUrl, IOctaneCredentials credentials, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		this.credentials = credentials;
	}
	
	/**
	 * Call our superclass to create the {@link HttpClientBuilder}, and
	 * add {@link OctaneUnauthorizedRetryStrategy} to automatically authorize
	 * with Octane if necessary.
	 */
	@Override
	protected HttpClientBuilder createApacheHttpClientBuilder() {
		return super.createApacheHttpClientBuilder()
				.setServiceUnavailableRetryStrategy(new OctaneUnauthorizedRetryStrategy());
	}
	
	public SubmittedIssue submitIssue(String sharedSpaceUid, String workspaceId, Map<String, Object> issueData) {
		JSONObjectBuilder builder = new JSONObjectBuilder(); 
		JSONObject issueEntry = builder.getJSONObject(issueData);
		JSONObject data = builder.updateJSONObjectWithPropertyPath(new JSONObject(), "data", new JSONObject[]{issueEntry});
		JSONObject result = executeRequest(HttpMethod.POST, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceUid)
				.path("/workspaces/")
				.path(workspaceId)
				.path("/defects")
				.entity(data, "application/json"), JSONObject.class);
		String id = SpringExpressionUtil.evaluateExpression(result, "data?.get(0)?.id", String.class);
		if ( id == null ) {
			throw new RuntimeException("Error getting Octane Work Item Id from response: "+result.toString());
		}
		return new SubmittedIssue(id, getIssueDeepLink(sharedSpaceUid, workspaceId, id));
	}
	
	
	private String getIssueDeepLink(String sharedSpaceUid, String workspaceId, String issueId) {
		return getBaseResource()
				.path("ui/entity-navigation")
				.queryParam("p", sharedSpaceUid+"/"+workspaceId)
				.queryParam("entityType", "work_item")
				.queryParam("id", issueId).getURI().toString();
	}


	public interface IOctaneCredentials {
		public JSONObject getCredentials();
	}
	
	public static final class OctaneUserCredentials implements IOctaneCredentials {
		public String user;
		public String password;
		public String getUserName() {
			return user;
		}
		public void setUserName(String userName) {
			this.user = userName;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		
		public JSONObject getCredentials() {
			return new JSONObject(this, new String[]{"user", "password"});
		}
	}
	
	public static final class OctaneClientCredentials implements IOctaneCredentials {
		public String client_id;
		public String client_secret;
		public String getClientId() {
			return client_id;
		}
		public void setClientId(String clientId) {
			this.client_id = clientId;
		}
		public String getClientSecret() {
			return client_secret;
		}
		public void setClientSecret(String clientSecret) {
			this.client_secret = clientSecret;
		}
		
		public JSONObject getCredentials() {
			return new JSONObject(this, new String[]{"client_id", "client_secret"});
		}
	}
	
	/**
	 * This {@link ServiceUnavailableRetryStrategy} implementation will act on Unauthorized responses
	 * by calling the Octane sign_in API and then retry the request. The call to the sign_in API will
	 * result in the Octane authentication cookie (and any other cookies) to be stored in our
	 * {@link CookieStore}.
	 */
	private final class OctaneUnauthorizedRetryStrategy implements ServiceUnavailableRetryStrategy {
		public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
			if ( executionCount < 2 && Arrays.asList(401, 403).contains(response.getStatusLine().getStatusCode()) ) {
				executeRequest(HttpMethod.POST, getBaseResource().path("/authentication/sign_in").entity(credentials.getCredentials(), "application/json"), null);
				return true;
			}
			return false;
		}

		public long getRetryInterval() {
			return 1;
		}
	}
}