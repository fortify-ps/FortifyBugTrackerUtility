package com.fortify.processrunner.archer.connection;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a basic, non-authenticating REST connection
 * for Archer. It's main characteristics compared to a standard 
 * {@link RestConnection} is that it will add an 
 * <code>Accept: application/json</code> header.
 */
public class ArcherBasicRestConnection extends RestConnection {
	public ArcherBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
		super(baseUrl);
		setProxy(proxy);
	}
	
	/**
	 * Update the {@link Builder} to add the Accept header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.accept("application/json");
	}
}