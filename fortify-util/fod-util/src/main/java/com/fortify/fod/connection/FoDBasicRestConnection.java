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
 * <code>Accept: application/json</code> header, and disable
 * chunked encoding for data sent to FoD.
 */
public class FoDBasicRestConnection extends RestConnection {
	public FoDBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
		super(baseUrl);
		setProxy(proxy);
	}
	
	/**
	 * Update the {@link Builder} to add the Accept and OAuth headers.
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
	
	/**
	 * This method updates the configuration returned by our superclass to
	 * enable buffering. Not having buffering enabled can result in chunked
	 * encoding, which is not supported by FoD.
	 * We only send relatively small payloads, so buffering these in memory is 
	 * not a problem.   
	 */
	/*
	@Override
	protected ApacheHttpClient4Config getApacheHttpClient4Config() {
		ApacheHttpClient4Config config = super.getApacheHttpClient4Config();
		config.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);
		return config;
	}
	*/
}