package com.fortify.fod.connection;

import javax.ws.rs.core.MultivaluedMap;

import com.fortify.util.rest.ProxyConfiguration;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for FoD.
 */
public class FoDAuthenticatingRestConnection extends FoDBasicRestConnection {
	private final FoDTokenFactory tokenProvider;
	public FoDAuthenticatingRestConnection(String baseUrl, MultivaluedMap<String, String> auth, ProxyConfiguration proxyConfig) {
		super( baseUrl, proxyConfig );
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
}