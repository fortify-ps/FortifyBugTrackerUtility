package com.fortify.ssc.connection;

import javax.ws.rs.client.Invocation.Builder;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;

/**
 * This class provides a basic, non-authenticating REST connection
 * for SSC. It's main characteristics compared to a standard 
 * {@link RestConnection} is that it will add an 
 * <code>Accept: application/json</code> header.
 * 
 * @author Ruud Senden
 *
 */
public class SSCBasicRestConnection extends RestConnection {
	public SSCBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
		super(baseUrl);
		setProxy(proxy);
	}
	
	/**
	 * Update the {@link Builder} to add the Accept and OAuth headers.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.accept("application/json").header("Content-Type", "application/json");
	}
}