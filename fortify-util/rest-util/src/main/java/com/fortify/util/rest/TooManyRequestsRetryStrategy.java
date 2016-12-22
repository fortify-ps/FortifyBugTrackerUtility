package com.fortify.util.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * This class implements an Apache HttpClient 4.x {@link ServiceUnavailableRetryStrategy}
 * that will retry a request if the server responds with an HTTP 429 (TOO_MANY_REQUESTS)
 * response. This can be enabled on {@link RestConnection} instances by overriding
 * {@link RestConnection#createApacheHttpClientBuilder()} as follows:
 * <code>
 *	protected HttpClientBuilder createApacheHttpClientBuilder() {
 *		return super.createApacheHttpClientBuilder().setServiceUnavailableRetryStrategy(new TooManyRequestsRetryStrategy(retryHeaderName));
 *	}
 * </code>
 * 
 */
public final class TooManyRequestsRetryStrategy implements ServiceUnavailableRetryStrategy {
	private String retryAfterHeaderName = "X-Retry-After";
	private ThreadLocal<Integer> interval = null;
	
	public TooManyRequestsRetryStrategy() {}
	public TooManyRequestsRetryStrategy(String retryAfterHeaderName) {
		this.retryAfterHeaderName = retryAfterHeaderName;
	}

	public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
		if ( executionCount < 2 && response.getStatusLine().getStatusCode()==429 ) {
			interval = new ThreadLocal<Integer>();
			interval.set(Integer.parseInt(response.getFirstHeader(retryAfterHeaderName).getValue()));
			return true;
		}
		return false;
	}

	public long getRetryInterval() {
		long result = interval==null ? -1 : (interval.get()*1000);
		interval = null;
		return result;
	}
}