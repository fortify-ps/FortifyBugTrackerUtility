package com.fortify.ssc.connection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for SSC.
 */
public class SSCAuthenticatingRestConnection extends SSCBasicRestConnection {
	private final ISSCTokenFactory tokenFactory;
	/** Cache for project version filter sets */
	private final LoadingCache<String, JSONArray> filterSetsCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONArray>() {
				@Override
				public JSONArray load(String projectVersionId) {
					return getFilterSets(projectVersionId);
				}
			});
	/** Memoized supplier for custom tags */
	private final Supplier<JSONArray> customTagsSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getCustomTags(); };
	});

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
	
	public JSONObject fileBug(String applicationVersionId, Map<String,Object> issueDetails, List<String> issueInstanceIds, String bugTrackerUserName, String bugTrackerPassword) {
		// TODO Clean up this code
		JSONObject bugFilingRequirements = getInitialBugFilingRequirements(applicationVersionId);
		if ( SpringExpressionUtil.evaluateExpression(bugFilingRequirements, "requiresAuthentication", Boolean.class) ) {
			bugFilingRequirements = authenticateForBugFiling(applicationVersionId, bugTrackerUserName, bugTrackerPassword);
		}
		Set<String> processedDependentParams = new HashSet<String>();
		boolean allDependentParamsProcessed = false;
		JSONObjectBuilder builder = new JSONObjectBuilder();
		while ( !allDependentParamsProcessed ) {
			JSONArray bugParams = bugFilingRequirements.optJSONArray("bugParams");
			JSONArray bugParamsWithDependenciesAndChoiceList = JSONUtil.filter(bugParams, "hasDependentParams && choiceList.length()>0", true);
			LinkedHashMap<String,JSONObject> bugParamsMap = JSONUtil.toMap(bugParamsWithDependenciesAndChoiceList, "identifier", String.class);
			bugParamsMap.keySet().removeAll(processedDependentParams);
			if ( bugParamsMap.isEmpty() ) {
				allDependentParamsProcessed = true;
			} else {
				Iterator<Entry<String, JSONObject>> iterator = bugParamsMap.entrySet().iterator();
				while ( iterator.hasNext() ) {
					Map.Entry<String, JSONObject> entry = iterator.next();
					String key = entry.getKey();
					processedDependentParams.add(key);
					String value = (String)issueDetails.get(key);
					if ( value != null && !value.equals(entry.getValue().optString("value")) ) {
						builder.updateJSONObjectWithPropertyPath(entry.getValue(), "value", value);
						bugFilingRequirements = getBugFilingRequirements(applicationVersionId, bugFilingRequirements, key);
						break;
					}
				}
			}
		}
		
		JSONArray bugParams = bugFilingRequirements.optJSONArray("bugParams");
		JSONArray bugParamsWithoutDependencies = JSONUtil.filter(bugParams, "hasDependentParams", false);
		for ( int i = 0 ; i < bugParamsWithoutDependencies.length() ; i++ ) {
			JSONObject bugParam = bugParamsWithoutDependencies.optJSONObject(i);
			String key = bugParam.optString("identifier");
			String value = (String)issueDetails.get(key);
			if ( value != null ) {
				builder.updateJSONObjectWithPropertyPath(bugParam, "value", value);
			}
		}
		
		JSONObject request = new JSONObject();
		builder.updateJSONObjectWithPropertyPath(request, "type", "FILE_BUG");
		builder.updateJSONObjectWithPropertyPath(request, "actionResponse", "false");
		builder.updateJSONObjectWithPropertyPath(request, "values.bugParams", bugFilingRequirements.optJSONArray("bugParams"));
		builder.updateJSONObjectWithPropertyPath(request, "values.issueInstanceIds", issueInstanceIds);
		return executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issues/action")
				.entity(request, "application/json"), JSONObject.class);
	}
	
	public JSONObject getBugTrackerConfig(String applicationVersionId) {
		JSONObject result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugtracker"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)?.bugTracker", JSONObject.class);
	}
	
	public String getBugTrackerShortName(String applicationVersionId) {
		return SpringExpressionUtil.evaluateExpression(getBugTrackerConfig(applicationVersionId), "shortDisplayName", String.class);
	}
	
	private JSONObject getInitialBugFilingRequirements(String applicationVersionId) {
		JSONObject result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugfilingrequirements"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)", JSONObject.class);
	}
	
	private JSONObject getBugFilingRequirements(String applicationVersionId, JSONObject data, String changedParamIdentifier) {
		JSONArray request = new JSONArray();
		request.put(data);
		JSONObject result = executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugfilingrequirements")
				.queryParam("changedParamIdentifier", changedParamIdentifier)
				.entity(request, "application/json"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)", JSONObject.class);
	}
	
	private JSONObject authenticateForBugFiling(String applicationVersionId, String bugTrackerUserName, String bugTrackerPassword) {
		JSONObjectBuilder builder = new JSONObjectBuilder();
		JSONObject request = new JSONObject();
		builder.updateJSONObjectWithPropertyPath(request, "type", "login");
		builder.updateJSONObjectWithPropertyPath(request, "ids", new JSONArray()); // Is this necessary to add?
		builder.updateJSONObjectWithPropertyPath(request, "values.username", bugTrackerUserName);
		builder.updateJSONObjectWithPropertyPath(request, "values.password", bugTrackerPassword);
		JSONObject result = executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("bugfilingrequirements/action")
				.entity(request, "application/json"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.values?.requirements", JSONObject.class);
	}

	public void updateIssueSearchOptions(String applicationVersionId, IssueSearchOptions issueSearchOptions) {
		executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issueSearchOptions")
				.entity(issueSearchOptions.getJSONRequestData(), "application/json"), JSONObject.class);
	}
	
	public String getCustomTagGuid(String customTagName) {
		return JSONUtil.mapValue(getCachedCustomTags(), "name.toLowerCase()", customTagName.toLowerCase(), "guid", String.class);
	}
	
	public JSONArray getCachedCustomTags() {
		return customTagsSupplier.get();
	}
	
	public JSONArray getCachedFilterSets(String projectVersionId) {
		return filterSetsCache.getUnchecked(projectVersionId);
	}
	
	private final JSONArray getCustomTags() {
		JSONObject data = executeRequest(HttpMethod.GET, getBaseResource().path("/api/v1/customTags").queryParam("fields", "guid,name"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(data, "data", JSONArray.class);
	}
	
	private final JSONArray getFilterSets(String applicationVersionId) {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions/")
    			.path(""+applicationVersionId)
    			.path("filterSets"), JSONObject.class).optJSONArray("data");
	}
}