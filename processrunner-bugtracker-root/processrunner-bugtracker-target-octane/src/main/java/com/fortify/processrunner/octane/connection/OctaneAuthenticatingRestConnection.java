package com.fortify.processrunner.octane.connection;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class provides an authenticated REST connection for Octane.
 * 
 */
public class OctaneAuthenticatingRestConnection extends OctaneBasicRestConnection {
	private final IOctaneCredentials credentials;
	private final LoadingCache<OctaneSharedSpaceAndWorkspaceId, LoadingCache<String, JSONList>> entityCache = 
			CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<OctaneSharedSpaceAndWorkspaceId, LoadingCache<String, JSONList>>() {
				@Override
				public LoadingCache<String, JSONList> load(final OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId) {
					return CacheBuilder.newBuilder().maximumSize(10)
							.build(new CacheLoader<String, JSONList>() {
								@Override
								public JSONList load(String entityName) {
									return getEntities(sharedSpaceAndWorkspaceId, entityName);
								}
							});
				}
			});
	
	public OctaneAuthenticatingRestConnection(String baseUrl, IOctaneCredentials credentials, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		this.credentials = credentials;
	}
	
	
	@Override
	protected ServiceUnavailableRetryStrategy getServiceUnavailableRetryStrategy() {
		return new OctaneUnauthorizedRetryStrategy();
	}
	
	public SubmittedIssue submitIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, Map<String, Object> issueData) {
		JSONMap result = submitOrUpdateIssue(sharedSpaceAndWorkspaceId, issueData, HttpMethod.POST);
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
	
	public JSONMap getIssueState(SubmittedIssue submittedIssue) {
		OctaneIssueId fullId = OctaneIssueId.parseFromSubmittedIssue(submittedIssue);
		OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId = fullId.getSharedSpaceAndWorkspaceId();
		String issueId = fullId.getIssueId();
		JSONMap issue = getIssue(sharedSpaceAndWorkspaceId, issueId, "phase");
		JSONMap result = new JSONMap();
		String phaseId = SpringExpressionUtil.evaluateExpression(issue, "phase.id", String.class);
		result.putPath("phase.id", phaseId);
		result.putPath("phase.name", getPhaseName(sharedSpaceAndWorkspaceId, phaseId));
		result.putPath("type", SpringExpressionUtil.evaluateExpression(issue, "type", String.class));
		return result;
	}
	
	public JSONMap getIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String issueId) {
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
	
	private Integer getIdForName(JSONList array, String name) {
		return array.mapValue("name", name, "id", Integer.class);
	}
	
	private String getNameForId(JSONList array, String id) {
		return array.mapValue("id", id, "name", String.class);
	}
	
	private JSONList getEntities(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String entityName) {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path(entityName), JSONMap.class).get("data", JSONList.class);
	}
	
	private JSONMap getIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, String issueId, String... fields) {
		WebTarget request = getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path("/defects")
				.path(issueId);
		if ( fields != null && fields.length > 0 ) {
			request.queryParam("fields", StringUtils.join(fields, ","));
		}
		return executeRequest(HttpMethod.GET, request, JSONMap.class);
	}
	
	private JSONMap submitOrUpdateIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, Map<String, Object> issueData, String httpMethod) {
		JSONMap issueEntry = new JSONMap();
		issueEntry.putPaths(issueData);
		replaceEntityNamesWithIds(sharedSpaceAndWorkspaceId, issueEntry);
		JSONMap data = new JSONMap();
		data.put("data", new JSONMap[]{issueEntry});
		return executeRequest(httpMethod, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path("/defects"),
				Entity.entity(data, "application/json"), JSONMap.class);
	}
	
	private void replaceEntityNamesWithIds(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, JSONMap issueEntry) {
		Set<String> names = issueEntry.keySet();
		for ( String name : names ) {
			Object value = issueEntry.get(name);
			if ( value instanceof JSONMap ) {
				JSONMap jsonValue = (JSONMap)value;
				if ( jsonValue.containsKey("type") && jsonValue.containsKey("name") ) {
					Integer id = getIdForName(sharedSpaceAndWorkspaceId, jsonValue.get("type", String.class)+"s", jsonValue.get("name", String.class));
					jsonValue.put("id", id);
					jsonValue.remove("name");
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


	public interface IOctaneCredentials {}
	
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
				executeRequest(HttpMethod.POST, getBaseResource().path("/authentication/sign_in"), Entity.entity(credentials, "application/json"), null);
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