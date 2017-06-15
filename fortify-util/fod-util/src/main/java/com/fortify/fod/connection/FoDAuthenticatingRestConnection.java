package com.fortify.fod.connection;

import java.util.Collection;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;

/**
 * This class provides a token-authenticated REST connection
 * for FoD.
 */
public class FoDAuthenticatingRestConnection extends FoDBasicRestConnection {
	private final FoDTokenFactory tokenProvider;
	public FoDAuthenticatingRestConnection(String baseUrl, Form auth, ProxyConfiguration proxyConfig) {
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
		JSONMap data = new JSONMap();
		data.put("comment", comment);
		data.put("vulnerabilityIds", vulnIds);
		bulkEdit(releaseId, data);
	}
	
	public void addBugLinkToVulnerabilities(String releaseId, String bugLink, Collection<String> vulnIds) {
		String path = String.format("/api/v3/releases/%s/vulnerabilities/bug-link", releaseId);
		JSONMap data = new JSONMap();
		data.put("bugLink", bugLink);
		data.put("vulnerabilityIds", vulnIds);
		postBulk(path, data);
	}
	
	public void bulkEdit(String releaseId, JSONMap bulkEditData) {
		postBulk(String.format("/api/v3/releases/%s/vulnerabilities/bulk-edit", releaseId), bulkEditData);
	}

	private void postBulk(String path, JSONMap data) {
		executeRequest(HttpMethod.POST, getBaseResource().path(path), Entity.entity(data,MediaType.APPLICATION_JSON), JSONMap.class);
	}
}