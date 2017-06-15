package com.fortify.processrunner.octane.connection;

import javax.ws.rs.client.Invocation.Builder;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;

/**
 * This class provides a basic, non-authenticating REST connection
 * for Octane. It's main characteristics compared to a standard 
 * {@link RestConnection} is that it will add an 
 * <code>Accept: application/json</code> header.
 */
public class OctaneBasicRestConnection extends RestConnection {
	public OctaneBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
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