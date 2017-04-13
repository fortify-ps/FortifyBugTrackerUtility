package com.fortify.fod.connection;

import org.apache.http.impl.client.HttpClientBuilder;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;
import com.fortify.util.rest.TooManyRequestsRetryStrategy;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a basic, non-authenticating REST connection
 * for FoD. It's main characteristics compared to a standard 
 * {@link RestConnection} is that it will add an 
 * <code>Accept: application/json</code> header, and enable a 
 * 'service unavailable' strategy to retry requests that fail 
 * due to FoD rate limiting.
 */
public class FoDBasicRestConnection extends RestConnection {
	public FoDBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
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
	
	@Override
	protected HttpClientBuilder createApacheHttpClientBuilder() {
		return super.createApacheHttpClientBuilder().setServiceUnavailableRetryStrategy(new TooManyRequestsRetryStrategy("X-Rate-Limit-Reset"));
	}
}