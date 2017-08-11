/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.ssc.connection;

import java.util.ArrayList;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;

import org.apache.commons.lang.StringUtils;

import com.fortify.util.json.IJSONMapProcessor;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class provides an authenticated REST connection for SSC.
 * @author Ruud Senden
 *
 */
public class SSCAuthenticatingRestConnection extends SSCBasicRestConnection {
	private final ISSCTokenFactory tokenFactory;
	/** Cache for project version filter sets */
	private final LoadingCache<String, JSONList> avfilterSetsCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONList>() {
				@Override
				public JSONList load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "filterSets");
				}
			});
	/** Cache for project version custom tags */
	private final LoadingCache<String, JSONList> avCustomTagsCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONList>() {
				@Override
				public JSONList load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "customTags");
				}
			});
	/** Cache for project version custom tags */
	private final LoadingCache<String, JSONList> avAttributesCache = CacheBuilder.newBuilder().maximumSize(10)
			.build(new CacheLoader<String, JSONList>() {
				@Override
				public JSONList load(String projectVersionId) {
					return getApplicationVersionEntities(projectVersionId, "attributes");
				}
			});
	/** Memoized supplier for custom tags */
	private final Supplier<JSONList> customTagsSupplier = Suppliers.memoize(new Supplier<JSONList>() {
		public JSONList get() { return getEntities("customTags"); };
	});
	/** Memoized supplier for bug trackers */
	private final Supplier<JSONList> bugTrackersSupplier = Suppliers.memoize(new Supplier<JSONList>() {
		public JSONList get() { return getEntities("bugtrackers"); };
	});
	/** Memoized supplier for attribute definitions */
	private final Supplier<JSONList> attributeDefinitionsSupplier = Suppliers.memoize(new Supplier<JSONList>() {
		public JSONList get() { return getEntities("attributeDefinitions"); };
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
	
	public void processApplicationVersions(IJSONMapProcessor processor) {
		process(getBaseResource().path("api/v1/projectVersions"), 50, processor);
	}
	
	protected void process(WebTarget target, int pageSize, IJSONMapProcessor processor) {
		int start=0;
		int count=50;
		while ( start < count ) {
			WebTarget resource = target.queryParam("start", ""+start).queryParam("limit", ""+pageSize);
			JSONMap data = executeRequest(HttpMethod.GET, resource, JSONMap.class);
			count = data.get("count", Integer.class);
			JSONList list = data.get("data", JSONList.class);
			start += list.size();
			for ( JSONMap obj : list.asValueType(JSONMap.class) ) {
				processor.process(obj);
			}
		}
	}
	
	

	/**
	 * Set multiple custom tag values for the given collection of vulnerabilities
	 * @param applicationVersionId
	 * @param customTagNamesAndValues
	 * @param vulns
	 */
	public void setCustomTagValues(String applicationVersionId, Map<String,String> customTagNamesAndValues, Collection<Object> vulns) {
		// TODO Simplify this code
		JSONMap request = new JSONMap();
		request.put("type", "AUDIT_ISSUE");

		List<JSONMap> issues = new ArrayList<JSONMap>();
		for ( Object vuln : vulns ) {
			JSONMap issue = new JSONMap();
			issue.put("id", SpringExpressionUtil.evaluateExpression(vuln, "id", Long.class));
			issue.put("revision", SpringExpressionUtil.evaluateExpression(vuln, "revision", Long.class));
			issues.add(issue);
		}
		request.putPath("values.issues", issues);
		JSONList customTagAuditValues = new JSONList(customTagNamesAndValues.size());
		for ( Map.Entry<String, String> customTagNameAndValue : customTagNamesAndValues.entrySet() ) {
			JSONMap customTagAudit = new JSONMap();
			customTagAudit.put("customTagGuid", getCustomTagGuid(customTagNameAndValue.getKey()));
			customTagAudit.put("textValue", customTagNameAndValue.getValue());
			customTagAuditValues.add(customTagAudit);
		}
		request.putPath("values.customTagAudit", customTagAuditValues);
		executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("issues/action"),
				Entity.entity(request, "application/json"), JSONMap.class);
	}
	
	/**
	 * Set a custom tag value for the given collection of vulnerabilities
	 * @param applicationVersionId
	 * @param customTagName
	 * @param value
	 * @param vulns
	 */
	public void setCustomTagValue(String applicationVersionId, String customTagName, String value, Collection<Object> vulns) {
		Map<String,String> customTagValues = new HashMap<String, String>(1);
		customTagValues.put(customTagName, value);
		setCustomTagValues(applicationVersionId, customTagValues, vulns);
	}
	
	/**
	 * Check whether SSC bug tracker authentication is required
	 * @param applicationVersionId
	 * @return
	 */
	public boolean isBugTrackerAuthenticationRequired(String applicationVersionId) {
		JSONMap bugFilingRequirements = getInitialBugFilingRequirements(applicationVersionId);
		return SpringExpressionUtil.evaluateExpression(bugFilingRequirements, "requiresAuthentication", Boolean.class);
	}
	
	/**
	 * Authenticate with SSC native bug tracker integration
	 * @param applicationVersionId
	 * @param bugTrackerUserName
	 * @param bugTrackerPassword
	 */
	public void authenticateForBugFiling(String applicationVersionId, String bugTrackerUserName, String bugTrackerPassword) {
		JSONMap request = new JSONMap();
		request.put("type", "login");
		request.put("ids", new JSONList()); // Is this necessary to add?
		request.putPath("values.username", bugTrackerUserName);
		request.putPath("values.password", bugTrackerPassword);
		JSONMap result = executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions")
				.path(applicationVersionId).path("bugfilingrequirements/action"),
				Entity.entity(request, "application/json"), JSONMap.class);
		// TODO Check result
	}
	
	/**
	 * File a bug via an SSC native bug tracker integration
	 * @param applicationVersionId
	 * @param issueDetails
	 * @param issueInstanceIds
	 * @return
	 */
	public JSONMap fileBug(String applicationVersionId, Map<String,Object> issueDetails, List<String> issueInstanceIds) {
		// TODO Clean up this code
		JSONMap bugFilingRequirements = getInitialBugFilingRequirements(applicationVersionId);
		Set<String> processedDependentParams = new HashSet<String>();
		boolean allDependentParamsProcessed = false;
		while ( !allDependentParamsProcessed ) {
			JSONList bugParams = bugFilingRequirements.get("bugParams", JSONList.class);
			JSONList bugParamsWithDependenciesAndChoiceList = bugParams.filter("hasDependentParams && choiceList.size()>0", true);
			LinkedHashMap<String,JSONMap> bugParamsMap = bugParamsWithDependenciesAndChoiceList.toMap("identifier", String.class, JSONMap.class);
			bugParamsMap.keySet().removeAll(processedDependentParams);
			if ( bugParamsMap.isEmpty() ) {
				allDependentParamsProcessed = true;
			} else {
				Iterator<Entry<String, JSONMap>> iterator = bugParamsMap.entrySet().iterator();
				while ( iterator.hasNext() ) {
					Map.Entry<String, JSONMap> entry = iterator.next();
					String key = entry.getKey();
					processedDependentParams.add(key);
					String value = (String)issueDetails.get(key);
					if ( value != null && !value.equals(entry.getValue().get("value")) ) {
						entry.getValue().put("value", value);
						bugFilingRequirements = getBugFilingRequirements(applicationVersionId, bugFilingRequirements, key);
						break;
					}
				}
			}
		}
		
		JSONList bugParams = bugFilingRequirements.get("bugParams", JSONList.class);
		JSONList bugParamsWithoutDependencies = bugParams.filter("hasDependentParams", false);
		for ( JSONMap bugParam : bugParamsWithoutDependencies.asValueType(JSONMap.class) ) {
			String key = bugParam.get("identifier", String.class);
			String value = (String)issueDetails.get(key);
			if ( value != null ) {
				bugParam.put("value", value);
			}
		}
		
		JSONMap request = new JSONMap();
		request.put("type", "FILE_BUG");
		request.put("actionResponse", "false");
		request.putPath("values.bugParams", bugFilingRequirements.get("bugParams", JSONList.class));
		request.putPath("values.issueInstanceIds", issueInstanceIds);
		return executeRequest(HttpMethod.POST, 
				getBaseResource().path("/api/v1/projectVersions")
				.path(applicationVersionId).path("issues/action"),
				Entity.entity(request, "application/json"), JSONMap.class);
	}
	
	/**
	 * Get a JSON object describing the configured native bug tracker integration for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public JSONMap getApplicationVersionBugTracker(String applicationVersionId) {
		JSONMap result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugtracker"), JSONMap.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)?.bugTracker", JSONMap.class);
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
		return getCachedApplicationVersionCustomTags(applicationVersionId).getValues("name", String.class);
	}
	
	/**
	 * Get the list of custom tag GUID's defined for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	public List<String> getApplicationVersionCustomTagGuids(String applicationVersionId) {
		return getCachedApplicationVersionCustomTags(applicationVersionId).getValues("guid", String.class);
	}

	/**
	 * Update the issue search options for the given application version 
	 * @param applicationVersionId
	 * @param issueSearchOptions
	 */
	public void updateApplicationVersionIssueSearchOptions(String applicationVersionId, IssueSearchOptions issueSearchOptions) {
		executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions")
				.path(applicationVersionId).path("issueSearchOptions"),
				Entity.entity(issueSearchOptions.getJSONRequestData(), "application/json"), JSONMap.class);
	}
	
	/**
	 * Get the custom tag GUID for the given custom tag name
	 * @param customTagName
	 * @return
	 */
	public String getCustomTagGuid(String customTagName) {
		return getCachedCustomTags().mapValue("name.toLowerCase()", customTagName.toLowerCase(), "guid", String.class);
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
		return getCachedBugTrackers().toMap("pluginId", String.class, "shortDisplayName", String.class);
	}
	
	/**
	 * Get all application version attribute values for the given application version,
	 * indexed by attribute name.
	 * @param applicationVersionId
	 * @return
	 */
	public Map<String, List<String>> getApplicationVersionAttributeValuesByName(String applicationVersionId) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		JSONList attrs = getCachedApplicationVersionAttributes(applicationVersionId);
		for ( JSONMap attr : attrs.asValueType(JSONMap.class) ) {
			String attrName = getCachedAttributeDefinitions().mapValue("guid", attr.get("guid", String.class), "name", String.class);
			JSONList attrValuesArray = attr.get("values", JSONList.class);
			String attrValue = attr.get("value", String.class);
			List<String> attrValues = StringUtils.isNotBlank(attrValue) 
					? Arrays.asList(attrValue) 
					: attrValuesArray==null ? null : attrValuesArray.getValues("name", String.class);
			result.put(attrName, attrValues);
		}
		return result;
	}
	
	/**
	 * Get a cached JSONList describing all available native bug tracker integrations defined in SSC
	 * @return
	 */
	public JSONList getCachedBugTrackers() {
		return bugTrackersSupplier.get();
	}
	
	/**
	 * Get a cached JSONList describing all available custom tags defined in SSC
	 * @return
	 */
	public JSONList getCachedCustomTags() {
		return customTagsSupplier.get();
	}
	
	/**
	 * Get a cached JSONList describing all available attribute definitions defined in SSC
	 * @return
	 */
	public JSONList getCachedAttributeDefinitions() {
		return attributeDefinitionsSupplier.get();
	}
	
	/**
	 * Get a cached JSONList describing all available filter sets for the given application version
	 * @return
	 */
	public JSONList getCachedApplicationVersionFilterSets(String applicationVersionId) {
		return avfilterSetsCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a cached JSONList describing all available custom tags for the given application version
	 * @return
	 */
	public JSONList getCachedApplicationVersionCustomTags(String applicationVersionId) {
		return avCustomTagsCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a cached JSONList describing all available attributes for the given application version
	 * @return
	 */
	public JSONList getCachedApplicationVersionAttributes(String applicationVersionId) {
		return avAttributesCache.getUnchecked(applicationVersionId);
	}
	
	/**
	 * Get a List<JSONMap> containing all entities for the given entityName
	 * @param entityName
	 * @return
	 */
	private final JSONList getEntities(String entityName) {
		JSONMap data = executeRequest(HttpMethod.GET, getBaseResource().path("/api/v1").path(entityName), JSONMap.class);
		return SpringExpressionUtil.evaluateExpression(data, "data", JSONList.class);
	}
	
	/**
	 * Get a List<JSONMap> containing all entities for the given entityName for the given application version
	 * @param entityName
	 * @return
	 */
	private final JSONList getApplicationVersionEntities(String applicationVersionId, String entityName) {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions/")
    			.path(""+applicationVersionId)
    			.path(entityName), JSONMap.class).get("data", JSONList.class);
	}
	
	/**
	 * Get a JSONMap describing the initial bug filing requirements for the given application version
	 * @param applicationVersionId
	 * @return
	 */
	private JSONMap getInitialBugFilingRequirements(String applicationVersionId) {
		JSONMap result = executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugfilingrequirements"), JSONMap.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)", JSONMap.class);
	}
	
	/**
	 * Get a JSONMap describing the bug filing requirements for the given application version and
	 * given bug parameter data
	 * @param applicationVersionId
	 * @param data
	 * @param changedParamIdentifier
	 * @return
	 */
	private JSONMap getBugFilingRequirements(String applicationVersionId, JSONMap data, String changedParamIdentifier) {
		List<JSONMap> request = new ArrayList<JSONMap>();
		request.add(data);
		JSONMap result = executeRequest(HttpMethod.PUT, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				.path("bugfilingrequirements")
				.queryParam("changedParamIdentifier", changedParamIdentifier),
				Entity.entity(request, "application/json"), JSONMap.class);
		return SpringExpressionUtil.evaluateExpression(result, "data?.get(0)", JSONMap.class);
	}

	public JSONMap getApplicationVersion(String applicationVersionId) {
		return executeRequest(HttpMethod.GET, 
				getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId)
				, JSONMap.class).get("data", JSONMap.class);
	}

	public JSONMap getApplicationVersion(String applicationName, String versionName) {
		JSONList appVersions = executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions")
				.queryParam("q", "project.name:\""+applicationName+"\"+and+name:\""+versionName+"\"")
				.queryParam("fields", "id")
				, JSONMap.class).get("data", JSONList.class);
		if ( appVersions==null || appVersions.size()!=1 ) {
			return null;
		} else {
			return appVersions.asValueType(JSONMap.class).get(0);
		}
	}

}
