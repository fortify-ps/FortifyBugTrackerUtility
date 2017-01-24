package com.fortify.processrunner.archer.connection;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.util.rest.ProxyConfiguration;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for Archer.
 */
public class ArcherAuthenticatingRestConnection extends ArcherBasicRestConnection {
	private final ArcherTokenFactory tokenProvider;
	public ArcherAuthenticatingRestConnection(String baseUrl, ArcherAuthData authData, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		tokenProvider = new ArcherTokenFactory(baseUrl, authData, proxyConfig);
	}
	
	/**
	 * Update the {@link Builder} to add the Authorization header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.header("Authorization", "Archer session-id=\""+tokenProvider.getToken()+"\"");
	}

	public SubmittedIssue submitIssue(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}
}