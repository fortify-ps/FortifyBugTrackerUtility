package com.fortify.fod.connection;

import java.util.Collection;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.rest.ProxyConfiguration;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for FoD.
 */
public class FoDAuthenticatingRestConnection extends FoDBasicRestConnection {
	private final FoDTokenFactory tokenProvider;
	public FoDAuthenticatingRestConnection(String baseUrl, MultivaluedMap<String, String> auth, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		tokenProvider = new FoDTokenFactory(baseUrl, auth, proxyConfig);
	}
	
	/**
	 * Update the {@link Builder} to add the Authorization header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.header("Authorization", "Bearer "+tokenProvider.getToken());
	}
	
	public void addCommentToVulnerabilities(String releaseId, String comment, Collection<String> vulnIds) {
		try {
			JSONObject data = new JSONObject();
			data.put("comment", comment);
			data.put("vulnerabilityIds", new JSONArray(vulnIds));
			bulkEdit(releaseId, data);
		} catch ( JSONException e ) {
			throw new RuntimeException("Cannot create FoD bulk edit request", e);
		}
	}
	
	public void addBugLinkToVulnerabilities(String releaseId, String bugLink, Collection<String> vulnIds) {
		String path = String.format("/api/v3/releases/%s/vulnerabilities/bug-link", releaseId);
		try {
			JSONObject data = new JSONObject();
			data.put("bugLink", bugLink);
			data.put("vulnerabilityIds", vulnIds);
			postBulk(path, data);
		} catch ( JSONException e ) {
			throw new RuntimeException("Cannot create FoD bulk edit request", e);
		}
	}
	
	public void bulkEdit(String releaseId, JSONObject bulkEditData) {
		postBulk(String.format("/api/v3/releases/%s/vulnerabilities/bulk-edit", releaseId), bulkEditData);
	}

	private void postBulk(String path, JSONObject data) {
		executeRequest(HttpMethod.POST, getBaseResource().path(path).entity(data,MediaType.APPLICATION_JSON), JSONObject.class);
	}
}