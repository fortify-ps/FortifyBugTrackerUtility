package com.fortify.processrunner.octane.connection;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.client.WebResource;

/**
 * This class provides an authenticated REST connection for Octane.
 * 
 */
public class OctaneAuthenticatingRestConnection extends OctaneBasicRestConnection {
	private final IOctaneCredentials credentials;
	private final LoadingCache<OctaneSharedSpaceAndWorkspaceId, LoadingCache<String, JSONArray>> entityCache = 
			CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<OctaneSharedSpaceAndWorkspaceId, LoadingCache<String, JSONArray>>() {
				@Override
				public LoadingCache<String, JSONArray> load(final OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId) {
					return CacheBuilder.newBuilder().maximumSize(10)
							.build(new CacheLoader<String, JSONArray>() {
								@Override
								public JSONArray load(String entityName) {
									return getEntities(sharedSpaceAndWorkspaceId, entityName);
								}
							});
				}
			});
	
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
	
	public SubmittedIssue submitIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, Map<String, Object> issueData) {
		JSONObject result = submitOrUpdateIssue(sharedSpaceAndWorkspaceId, issueData, HttpMethod.POST);
		String id = SpringExpressionUtil.evaluateExpression(result, "data?.get(0)?.id", String.class);
		if ( id == null ) {
			throw new RuntimeException("Error getting Octane Work Item Id from response: "+result.toString());
		}
		OctaneIssueId fullId = new OctaneIssueId(sharedSpaceAndWorkspaceId, id);
		return new SubmittedIssue(fullId.toString(), fullId.getDeepLink(getBaseUrl()));
	}
	
	public void updateIssue(SubmittedIssue submittedIssue, Map<String, Object> issueData) {
		OctaneIssueId issueId = OctaneIssueId.parseFromSubmittedIssue(submittedIssue);
		issueData.put("id", issueId.getIssueId());
		submitOrUpdateIssue(issueId.getSharedSpaceAndWorkspaceId(), issueData, HttpMethod.PUT);
	}
	
	public JSONObject getIssueState(SubmittedIssue submittedIssue) {
		OctaneIssueId fullId = OctaneIssueId.parseFromSubmittedIssue(submittedIssue);
		OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId = fullId.getSharedSpaceAndWorkspaceId();
		String issueId = fullId.getIssueId();
		JSONObject issue = getIssue(sharedSpaceAndWorkspaceId, issueId, "phase");
		JSONObject result = new JSONObject();
		JSONObjectBuilder builder = new JSONObjectBuilder();
		String phaseId = SpringExpressionUtil.evaluateExpression(issue, "phase.id", String.class);
		result = builder.updateJSONObjectWithPropertyPath(result, "phase.id", phaseId);
		result = builder.updateJSONObjectWithPropertyPath(result, "phase.name", getPhaseName(sharedSpaceAndWorkspaceId, phaseId));
		result = builder.updateJSONObjectWithPropertyPath(result, "type", SpringExpressionUtil.evaluateExpression(issue, "type", String.class));
		return result;
	}
	
	public JSONObject getIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String issueId) {
		return getIssue(sharedSpaceAndWorkspaceId, issueId, new String[]{});
	}

	public boolean transition(SubmittedIssue submittedIssue, String transitionName, String comment) {
		Map<String, Object> issueData = new HashMap<String, Object>();
		issueData.put("phase.type", "phase");
		issueData.put("phase.name", transitionName);
		// TODO Add comment
		updateIssue(submittedIssue, issueData);
		// TODO Check new state
		return true;
	}
	
	public Integer getWorkItemId(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String featureName) {
		return getIdForName(sharedSpaceAndWorkspaceId, "work_items", featureName);
	}

	public Integer getFeatureId(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String featureName) {
		return getIdForName(sharedSpaceAndWorkspaceId, "features", featureName);
	}
	
	public Integer getPhaseId(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String phaseName) {
		return getIdForName(sharedSpaceAndWorkspaceId, "phases", phaseName);
	}
	
	public String getWorkItemName(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String featureId) {
		return getNameForId(sharedSpaceAndWorkspaceId, "work_items", featureId);
	}

	public String getFeatureName(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String featureId) {
		return getNameForId(sharedSpaceAndWorkspaceId, "features", featureId);
	}
	
	public String getPhaseName(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String phaseId) {
		return getNameForId(sharedSpaceAndWorkspaceId, "phases", phaseId);
	}
	
	private Integer getIdForName(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String entityName, String name) {
		return getIdForName(entityCache.getUnchecked(sharedSpaceAndWorkspaceId).getUnchecked(entityName), name);
	}
	
	private String getNameForId(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String entityName, String id) {
		return getNameForId(entityCache.getUnchecked(sharedSpaceAndWorkspaceId).getUnchecked(entityName), id);
	}
	
	private Integer getIdForName(JSONArray array, String name) {
		return JSONUtil.mapValue(array, "name", name, "id", Integer.class);
	}
	
	private String getNameForId(JSONArray array, String id) {
		return JSONUtil.mapValue(array, "id", id, "name", String.class);
	}
	
	private JSONArray getEntities(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String entityName) {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path(entityName), JSONObject.class).optJSONArray("data");
	}
	
	private JSONObject getIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String issueId, String... fields) {
		WebResource request = getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path("/defects")
				.path(issueId);
		if ( fields != null && fields.length > 0 ) {
			request.queryParam("fields", StringUtils.join(fields, ","));
		}
		return executeRequest(HttpMethod.GET, request, JSONObject.class);
	}
	
	private JSONObject submitOrUpdateIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, Map<String, Object> issueData, String httpMethod) {
		JSONObjectBuilder builder = new JSONObjectBuilder(); 
		JSONObject issueEntry = builder.getJSONObject(issueData);
		replaceEntityNamesWithIds(sharedSpaceAndWorkspaceId, issueEntry);
		JSONObject data = builder.updateJSONObjectWithPropertyPath(new JSONObject(), "data", new JSONObject[]{issueEntry});
		return executeRequest(httpMethod, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path("/defects")
				.entity(data, "application/json"), JSONObject.class);
	}
	
	private void replaceEntityNamesWithIds(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, JSONObject issueEntry) {
		JSONArray names = issueEntry.names();
		for ( int i = 0 ; i < names.length(); i++ ) {
			Object value = issueEntry.opt(names.optString(i));
			if ( value instanceof JSONObject ) {
				JSONObject jsonValue = (JSONObject)value;
				if ( jsonValue.has("type") && jsonValue.has("name") ) {
					try {
						Integer id = getIdForName(sharedSpaceAndWorkspaceId, jsonValue.getString("type")+"s", jsonValue.getString("name"));
						jsonValue.put("id", id);
						jsonValue.remove("name");
					} catch ( JSONException e ) {
						throw new RuntimeException("Error while replacing entity names with id's", e);
					}
				}
			}
		}
		
	}
	
	private static final class OctaneIssueId {
		private static final MessageFormat FMT_ISSUE_ID = new MessageFormat("{0}/{1}/{2}");
		private static final MessageFormat FMT_DEEP_LINK = new MessageFormat("{0}ui/entity-navigation?p={1}/{2}&entityType=work_item&id={3}");
		private final OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId;
		private final String issueId;
		
		public OctaneIssueId(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String issueId) {
			super();
			this.sharedSpaceAndWorkspaceId = sharedSpaceAndWorkspaceId;
			this.issueId = issueId;
		}
		
		public OctaneSharedSpaceAndWorkspaceId getSharedSpaceAndWorkspaceId() {
			return sharedSpaceAndWorkspaceId;
		}

		public String getIssueId() {
			return issueId;
		}

		@Override
		public String toString() {
			return FMT_ISSUE_ID.format(new Object[]{
				sharedSpaceAndWorkspaceId.getSharedSpaceUid(), sharedSpaceAndWorkspaceId.getWorkspaceId(), issueId});
		}
		
		public String getDeepLink(String baseUrl) {
			return FMT_DEEP_LINK.format(new Object[]{
				baseUrl, sharedSpaceAndWorkspaceId.getSharedSpaceUid(), sharedSpaceAndWorkspaceId.getWorkspaceId(), issueId
			});
		}
		
		public static final OctaneIssueId parseFromIdString(String idString) {
			try {
				Object[] values = FMT_ISSUE_ID.parse(idString);
				return new OctaneIssueId(new OctaneSharedSpaceAndWorkspaceId((String)values[0], (String)values[1]), (String)values[2]);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Error parsing Octane issue id", e);
			}
		}
		
		public static final OctaneIssueId parseFromDeepLink(String deepLink) {
			try {
				Object[] values = FMT_DEEP_LINK.parse(deepLink);
				// String baseUrl = values[0];
				return new OctaneIssueId(new OctaneSharedSpaceAndWorkspaceId((String)values[1], (String)values[2]), (String)values[3]);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Error parsing Octane issue id", e);
			}
		}
		
		public static final OctaneIssueId parseFromSubmittedIssue(SubmittedIssue submittedIssue) {
			String id = submittedIssue.getId();
			return StringUtils.isBlank(id) 
					? parseFromDeepLink(submittedIssue.getDeepLink()) 
					: parseFromIdString(id);
		}
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
	
	public static final class OctaneSharedSpaceAndWorkspaceId {
		private final String sharedSpaceUid;
		private final String workspaceId;
		
		public OctaneSharedSpaceAndWorkspaceId(String sharedSpaceUid, String workspaceId) {
			this.sharedSpaceUid = sharedSpaceUid;
			this.workspaceId = workspaceId;
		}

		public String getSharedSpaceUid() {
			return sharedSpaceUid;
		}

		public String getWorkspaceId() {
			return workspaceId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sharedSpaceUid == null) ? 0 : sharedSpaceUid.hashCode());
			result = prime * result + ((workspaceId == null) ? 0 : workspaceId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OctaneSharedSpaceAndWorkspaceId other = (OctaneSharedSpaceAndWorkspaceId) obj;
			if (sharedSpaceUid == null) {
				if (other.sharedSpaceUid != null)
					return false;
			} else if (!sharedSpaceUid.equals(other.sharedSpaceUid))
				return false;
			if (workspaceId == null) {
				if (other.workspaceId != null)
					return false;
			} else if (!workspaceId.equals(other.workspaceId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return sharedSpaceUid+"/"+workspaceId;
		}
		
		public String asQueryParam() {
			return toString();
		}
		
		
	}
}