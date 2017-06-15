package com.fortify.fod.connection;

import javax.ws.rs.client.Invocation.Builder;

import org.apache.http.client.ServiceUnavailableRetryStrategy;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;
import com.fortify.util.rest.TooManyRequestsRetryStrategy;

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
	protected ServiceUnavailableRetryStrategy getServiceUnavailableRetryStrategy() {
		return new TooManyRequestsRetryStrategy("X-Rate-Limit-Reset");
	}
}