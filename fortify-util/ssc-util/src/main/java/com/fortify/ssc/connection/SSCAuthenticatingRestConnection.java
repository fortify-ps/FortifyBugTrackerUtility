package com.fortify.ssc.connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

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
 * This class provides an authenticated REST connection for SSC.
 * @author Ruud Senden
 *
 */
public class SSCAuthenticatingRestConnection extends SSCBasicRestConnection {
	private final ISSCTokenFactory tokenFactory;
	/** Cache for project version filter sets */
	private final LoadingCache<String, JSONArray> avfilterSetsCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONArray>() {
				@Override
				public JSONArray load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "filterSets");
				}
			});
	/** Cache for project version custom tags */
	private final LoadingCache<String, JSONArray> avCustomTagsCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONArray>() {
				@Override
				public JSONArray load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "customTags");
				}
			});
	/** Cache for project version custom tags */
	private final LoadingCache<String, JSONArray> avAttributesCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONArray>() {
				@Override
				public JSONArray load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "attributes");
				}
			});
	/** Memoized supplier for custom tags */
	private final Supplier<JSONArray> customTagsSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getEntities("customTags"); };
	});
	/** Memoized supplier for bug trackers */
	private final Supplier<JSONArray> bugTrackersSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getEntities("bugtrackers"); };
	});
	/** Memoized supplier for attribute definitions */
	private final Supplier<JSONArray> attributeDefinitionsSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getEntities("attributeDefinitions"); };
	});

	/**
	 * Constructor for connecting to SSC using an authentication token
	 * @param baseUrl
	 * @param token
	 * @param proxyConfig
	 */
	public SSCAuthenticatingRestConnection(String baseUrl, String token, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		if ( StringUtils.isBlank(token) ) {
			throw new RuntimeException("SSC authentication token cannot be blank");
		}
		this.tokenFactory = new SSCTokenFactoryTokenCredentials(token);
	}

	/**
	 * Constructor for connecting to SSC using username and password
	 * @param baseUrl
	 * @param userName
	 * @param password
	 * @param proxyConfig
	 */
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

	/**
	 * Set a custom tag value for the given collection of vulnerabilities
	 * @param applicationVersionId
	 * @param customTagName
	 * @param value
	 * @param vulns
	 */
	public void setCustomTagValue(String applicationVersionId, String customTagName, String value, Collection<Object> vulns) {
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
		builder.updateJSONObjectWithPropertyPath(customTagAudit, "textValue", value);
		builder.updateJSONObjectWithPropertyPath(request, "values.customTagAudit", new Object[]{customTagAudit});
		executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issues/action")
				.entity(request, "application/json"), JSONObject.class);
	}
	
	/**
	 * Check whether SSC bug tracker authentication is required
	 * @param applicationVersionId
	 * @return
	 */
	public boolean isBugTrackerAuthenticationRequired(String applicationVersionId) {
		JSONObject bugFilingRequirements = getInitialBugFilingRequirements(applicationVersionId);
		return SpringExpressionUtil.evaluateExpression(bugFilingRequirements, "requiresAuthentication", Boolean.class);
	}
	
	/**
	 * Authenticate with SSC native bug tracker integration
	 * @param applicationVersionId
	 * @param bugTrackerUserName
	 * @param bugTrackerPassword
	 */
	public void authenticateForBugFiling(String applicationVersionId, String bugTrackerUserName, String bugTrackerPassword) {
		JSONObjectBuilder builder = new JSONObjectBuilder();
		JSONObject request = new JSONObject();
		builder.updateJSONObjectWithPropertyPath(request, "type", "login");
		builder.updateJSONObjectWithPropertyPath(request, "ids", new JSONArray()); // Is this necessary to add?
		builder.updateJSONObjectWithPropertyPath(request, "values.username", bugTrackerUserName);
		builder.updateJSONObjectWithPropertyPath(request, "values.password", bugTrackerPassword);
		JSONObject result = executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("bugfilingrequirements/action")
				.entity(request, "application/json"), JSONObject.class);
		// TODO Check result
	}
	
	/**
	 * File a bug via an SSC native bug tracker integration
	 * @param applicationVersionId
	 * @param issueDetails
	 * @param issueInstanceIds
	 * @return
	 */
	public JSONObject fileBug(String applicationVersionId, Map<String,Object> issueDetails, List<String> issueInstanceIds) {
		// TODO Clean up this code
		JSONObject bugFilingRequirements = getInitialBugFilingRequirements(applicationVersionId);
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
	
	/**
	 * Get a JSON object describing the configured native bug tracker integration for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public JSONObject getApplicationVersionBugTracker(String applicationVersionId) {
		JSONObject result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugtracker"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)?.bugTracker", JSONObject.class);
	}
	
	/**
	 * Get the short name for the configured native bug tracker integration for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public String getApplicationVersionBugTrackerShortName(String applicationVersionId) {
		return SpringExpressionUtil.evaluateExpression(getApplicationVersionBugTracker(applicationVersionId), "shortDisplayName", String.class);
	}
	
	/**
	 * Get the list of custom tag names defined for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public List<String> getApplicationVersionCustomTagNames(String applicationVersionId) {
		return JSONUtil.jsonObjectArrayToList(getCachedApplicationVersionCustomTags(applicationVersionId), "name", String.class);
	}
	
	/**
	 * Get the list of custom tag GUID's defined for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public List<String> getApplicationVersionCustomTagGuids(String applicationVersionId) {
		return JSONUtil.jsonObjectArrayToList(getCachedApplicationVersionCustomTags(applicationVersionId), "guid", String.class);
	}

	/**
	 * Update the issue search options for the given application version 
	 * @param applicationVersionId
	 * @param issueSearchOptions
	 */
	public void updateApplicationVersionIssueSearchOptions(String applicationVersionId, IssueSearchOptions issueSearchOptions) {
		executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issueSearchOptions")
				.entity(issueSearchOptions.getJSONRequestData(), "application/json"), JSONObject.class);
	}
	
	/**
	 * Get the custom tag GUID for the given custom tag name
	 * @param customTagName
	 * @return
	 */
	public String getCustomTagGuid(String customTagName) {
		return JSONUtil.mapValue(getCachedCustomTags(), "name.toLowerCase()", customTagName.toLowerCase(), "guid", String.class);
	}
	
	/**
	 * Get the bug tracker plugin id's for the given bug tracker names in unspecified order
	 * @param bugTrackerPluginNames
	 * @return
	 */
	public Set<String> getBugTrackerPluginIdsForNames(final Set<String> bugTrackerPluginNames) {
		Map<String, String> namesById = getBugTrackerShortDisplayNamesByIds();
		namesById.values().removeIf(new Predicate<String>() {
			public boolean test(String name) {
				return !bugTrackerPluginNames.contains(name);
			}
		});
		return namesById.keySet();
	}
	
	/**
	 * Get all short display names for all available native SSC bug tracker integrations,
	 * indexed by bug tracker plugin id.
	 * @return
	 */
	public Map<String, String> getBugTrackerShortDisplayNamesByIds() {
		return JSONUtil.toMap(getCachedBugTrackers(), "pluginId", String.class, "shortDisplayName", String.class);
	}
	
	/**
	 * Get all application version attribute values for the given application version,
	 * indexed by attribute name.
	 * @param applicationVersionId
	 * @return
	 */
	public Map<String, List<String>> getApplicationVersionAttributeValuesByName(String applicationVersionId) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		JSONArray attrs = getCachedApplicationVersionAttributes(applicationVersionId);
		for ( int i = 0 ; i < attrs.length() ; i++ ) {
			JSONObject attr = attrs.optJSONObject(i);
			String attrName = JSONUtil.mapValue(getCachedAttributeDefinitions(), "guid", attr.optString("guid"), "name", String.class);
			JSONArray attrValuesArray = attr.optJSONArray("values");
			String attrValue = attr.optString("value");
			List<String> attrValues = StringUtils.isNotBlank(attrValue) 
					? Arrays.asList(attrValue) 
					: JSONUtil.jsonObjectArrayToList(attrValuesArray, "name", String.class);
			result.put(attrName, attrValues);
		}
		return result;
	}
	
	/**
	 * Get a cached JSONArray describing all available native bug tracker integrations defined in SSC
	 * @return
	 */
	public JSONArray getCachedBugTrackers() {
		return bugTrackersSupplier.get();
	}
	
	/**
	 * Get a cached JSONArray describing all available custom tags defined in SSC
	 * @return
	 */
	public JSONArray getCachedCustomTags() {
		return customTagsSupplier.get();
	}
	
	/**
	 * Get a cached JSONArray describing all available attribute definitions defined in SSC
	 * @return
	 */
	public JSONArray getCachedAttributeDefinitions() {
		return attributeDefinitionsSupplier.get();
	}
	
	/**
	 * Get a cached JSONArray describing all available filter sets for the given application version
	 * @return
	 */
	public JSONArray getCachedApplicationVersionFilterSets(String applicationVersionId) {
		return avfilterSetsCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a cached JSONArray describing all available custom tags for the given application version
	 * @return
	 */
	public JSONArray getCachedApplicationVersionCustomTags(String applicationVersionId) {
		return avCustomTagsCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a cached JSONArray describing all available attributes for the given application version
	 * @return
	 */
	public JSONArray getCachedApplicationVersionAttributes(String applicationVersionId) {
		return avAttributesCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a JSONArray containing all entities for the given entityName
	 * @param entityName
	 * @return
	 */
	private final JSONArray getEntities(String entityName) {
		JSONObject data = executeRequest(HttpMethod.GET, getBaseResource().path("/api/v1").path(entityName), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(data, "data", JSONArray.class);
	}
	
	/**
	 * Get a JSONArray containing all entities for the given entityName for the given application version
	 * @param entityName
	 * @return
	 */
	private final JSONArray getApplicationVersionEntities(String applicationVersionId, String entityName) {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions/")
    			.path(""+applicationVersionId)
    			.path(entityName), JSONObject.class).optJSONArray("data");
	}
	
	/**
	 * Get a JSONObject describing the initial bug filing requirements for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	private JSONObject getInitialBugFilingRequirements(String applicationVersionId) {
		JSONObject result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugfilingrequirements"), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)", JSONObject.class);
	}
	
	/**
	 * Get a JSONObject describing the bug filing requirements for the given application version and
	 * given bug parameter data
	 * @param applicationVersionId
	 * @param data
	 * @param changedParamIdentifier
	 * @return
	 */
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

}